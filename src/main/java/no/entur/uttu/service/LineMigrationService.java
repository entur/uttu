/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import no.entur.uttu.config.Context;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DayTypeAssignment;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Line;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.Notice;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.repository.DayTypeRepository;
import no.entur.uttu.repository.FixedLineRepository;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.NetworkRepository;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.security.spi.UserContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LineMigrationService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final ProviderRepository providerRepository;
  private final FixedLineRepository fixedLineRepository;
  private final FlexibleLineRepository flexibleLineRepository;
  private final NetworkRepository networkRepository;
  private final DayTypeRepository dayTypeRepository;
  private final UserContextService userContextService;
  private final EntityCloner entityCloner;
  private final ReferenceMapper referenceMapper;
  private final MigrationIdGenerator idGenerator;
  private final EntityManager entityManager;

  public LineMigrationService(
    ProviderRepository providerRepository,
    FixedLineRepository fixedLineRepository,
    FlexibleLineRepository flexibleLineRepository,
    NetworkRepository networkRepository,
    DayTypeRepository dayTypeRepository,
    UserContextService userContextService,
    EntityCloner entityCloner,
    ReferenceMapper referenceMapper,
    MigrationIdGenerator idGenerator,
    EntityManager entityManager
  ) {
    this.providerRepository = providerRepository;
    this.fixedLineRepository = fixedLineRepository;
    this.flexibleLineRepository = flexibleLineRepository;
    this.networkRepository = networkRepository;
    this.dayTypeRepository = dayTypeRepository;
    this.userContextService = userContextService;
    this.entityCloner = entityCloner;
    this.referenceMapper = referenceMapper;
    this.idGenerator = idGenerator;
    this.entityManager = entityManager;
  }

  public LineMigrationResult migrateLine(LineMigrationInput input) {
    long startTime = System.currentTimeMillis();
    List<LineMigrationWarning> warnings = new ArrayList<>();

    logger.info(
      "Starting line migration - sourceLineId: {}, targetProviderId: {}, targetNetworkId: {}",
      input.getSourceLineId(),
      input.getTargetProviderId(),
      input.getTargetNetworkId()
    );

    try {
      validateMigration(input);

      Line sourceLine = loadSourceLine(input.getSourceLineId());

      // Initialize all lazy collections before detaching
      initializeLazyCollections(sourceLine);

      List<Object> sourceEntities = loadSourceEntities(sourceLine);

      // Clear the entire persistence context to avoid provider mismatch issues
      // This ensures no entities from the source provider remain tracked
      entityManager.clear();

      Provider targetProvider = providerRepository.getOne(input.getTargetProviderId());

      // Load target network with proper provider context
      Network targetNetwork;
      String currentProvider = Context.getProvider();
      try {
        Context.setProvider(input.getTargetProviderId());
        targetNetwork = networkRepository.getOne(input.getTargetNetworkId());
      } finally {
        // Restore original provider context without clearing username
        if (currentProvider != null) {
          Context.setProvider(currentProvider);
        }
        // Don't use Context.clear() as it would also clear the username
      }

      // Clear mappings and set conflict resolution strategy
      entityCloner.clearMappings();
      referenceMapper.clearMappings();
      idGenerator.clearMappings();

      // Set target provider for DayType deduplication
      referenceMapper.setTargetProvider(targetProvider);

      ConflictResolutionStrategy strategy = input.getOptions() != null
        ? input.getOptions().getConflictResolution()
        : ConflictResolutionStrategy.FAIL;
      entityCloner.setConflictResolution(strategy);

      Boolean includeDayTypes = input.getOptions() != null
        ? input.getOptions().getIncludeDayTypes()
        : true;
      entityCloner.setIncludeDayTypes(includeDayTypes != null ? includeDayTypes : true);

      // Note: We don't validate the source line's network reference here because
      // it will be replaced with the target network during cloning.
      // Only validate other external references like operators and stop places.
      // TODO: Implement selective validation that skips network reference

      // Clone the line with all its entities
      Line clonedLine = entityCloner.cloneLine(sourceLine, targetProvider, targetNetwork);

      // Update references after cloning
      if (clonedLine.getJourneyPatterns() != null) {
        for (JourneyPattern jp : clonedLine.getJourneyPatterns()) {
          referenceMapper.updateJourneyPatternReferences(jp);
          if (jp.getServiceJourneys() != null) {
            for (ServiceJourney sj : jp.getServiceJourneys()) {
              referenceMapper.updateServiceJourneyReferences(sj);
            }
          }
        }
      }

      // Perform dry run check
      Boolean dryRun = input.getOptions() != null
        ? input.getOptions().getDryRun()
        : false;
      if (!dryRun) {
        // Save the cloned entities with target provider context
        // Don't capture the current provider - we want to ensure target provider is set
        String currentUsername = Context.getVerifiedUsername(); // Preserve username
        try {
          Context.setProvider(input.getTargetProviderId());
          Context.setUserName(currentUsername); // Restore username after setting provider

          // First, save all DayTypes and replace references with persisted instances
          saveDayTypesAndUpdateReferences(clonedLine);

          // Note: Don't flush here as it may affect entities from other providers
          // The save operation will handle the persistence properly

          if (clonedLine instanceof FixedLine) {
            clonedLine = fixedLineRepository.save((FixedLine) clonedLine);
            logger.info(
              "Successfully migrated FixedLine {} to provider {} with new ID: {}",
              input.getSourceLineId(),
              input.getTargetProviderId(),
              clonedLine.getNetexId()
            );
          } else {
            clonedLine = flexibleLineRepository.save((FlexibleLine) clonedLine);
            logger.info(
              "Successfully migrated FlexibleLine {} to provider {} with new ID: {}",
              input.getSourceLineId(),
              input.getTargetProviderId(),
              clonedLine.getNetexId()
            );
          }
        } finally {
          // Keep the target provider context, don't restore
          // The context should remain as the target provider
        }
      } else {
        logger.info(
          "Dry run completed successfully for line migration - sourceLineId: {}, targetProviderId: {}",
          input.getSourceLineId(),
          input.getTargetProviderId()
        );
      }

      LineMigrationResult result = new LineMigrationResult();
      result.setSuccess(true);
      result.setMigratedLineId(clonedLine.getNetexId());
      result.setWarnings(warnings);

      LineMigrationSummary summary = new LineMigrationSummary();
      summary.setEntitiesMigrated(sourceEntities.size());
      summary.setWarningsCount(warnings.size());
      summary.setExecutionTimeMs(System.currentTimeMillis() - startTime);
      result.setSummary(summary);

      return result;
    } catch (MigrationIdGenerator.ConflictSkippedException e) {
      // Handle SKIP strategy
      logger.warn(
        "Line migration skipped due to conflicts - sourceLineId: {}, targetProviderId: {}, reason: {}",
        input.getSourceLineId(),
        input.getTargetProviderId(),
        e.getMessage()
      );
      LineMigrationResult result = new LineMigrationResult();
      result.setSuccess(false);
      result.setErrorMessage(e.getMessage());
      result.setWarnings(warnings);

      LineMigrationSummary summary = new LineMigrationSummary();
      summary.setEntitiesMigrated(0);
      summary.setWarningsCount(warnings.size());
      summary.setExecutionTimeMs(System.currentTimeMillis() - startTime);
      result.setSummary(summary);

      return result;
    }
  }

  public void validateMigration(LineMigrationInput input) {
    validateProviderAccess(input);
    validateTargetNetwork(input.getTargetProviderId(), input.getTargetNetworkId());
    validateSourceLine(input.getSourceLineId());

    // Validate network reference using ReferenceMapper with provider context
    Provider targetProvider = providerRepository.getOne(input.getTargetProviderId());
    String currentProvider = Context.getProvider();
    try {
      Context.setProvider(input.getTargetProviderId());
      referenceMapper.validateNetworkReference(
        input.getTargetNetworkId(),
        targetProvider
      );
    } catch (ReferenceMapper.ReferenceValidationException e) {
      throw new IllegalArgumentException(e.getMessage());
    } finally {
      // Restore original provider context without clearing username
      if (currentProvider != null) {
        Context.setProvider(currentProvider);
      }
      // Don't use Context.clear() as it would also clear the username
    }
  }

  private void validateProviderAccess(LineMigrationInput input) {
    // Load source line to get source provider
    Line sourceLine = loadSourceLine(input.getSourceLineId());
    if (sourceLine == null) {
      logger.error("Source line not found during migration: {}", input.getSourceLineId());
      throw new IllegalArgumentException(
        "Source line not found: " + input.getSourceLineId()
      );
    }

    String sourceProviderCode = sourceLine.getProvider().getCode();
    String targetProviderCode = input.getTargetProviderId();

    // Validate access to source provider (read)
    if (!userContextService.hasAccessToProvider(sourceProviderCode)) {
      logger.error(
        "User attempted to access source provider without permission: {}",
        sourceProviderCode
      );
      throw new SecurityException(
        "User does not have access to source provider: " + sourceProviderCode
      );
    }

    // Validate access to target provider (write)
    if (!userContextService.hasAccessToProvider(targetProviderCode)) {
      logger.error(
        "User attempted to access target provider without permission: {}",
        targetProviderCode
      );
      throw new SecurityException(
        "User does not have access to target provider: " + targetProviderCode
      );
    }

    // Prevent migration within the same provider
    if (sourceProviderCode.equals(targetProviderCode)) {
      logger.error(
        "Attempted migration within same provider: {} -> {}",
        sourceProviderCode,
        targetProviderCode
      );
      throw new IllegalArgumentException("Cannot migrate line within the same provider");
    }
  }

  private void validateTargetNetwork(String targetProviderId, String targetNetworkId) {
    Provider targetProvider = providerRepository.getOne(targetProviderId);
    if (targetProvider == null) {
      throw new IllegalArgumentException(
        "Target provider not found: " + targetProviderId
      );
    }

    // Verify network exists and belongs to target provider
    // Need to set provider context for network repository
    String currentProvider = Context.getProvider();
    try {
      Context.setProvider(targetProviderId);
      var network = networkRepository.getOne(targetNetworkId);
      if (network == null) {
        throw new IllegalArgumentException(
          "Target network not found: " + targetNetworkId
        );
      }

      if (!network.getProvider().getCode().equals(targetProviderId)) {
        throw new IllegalArgumentException(
          "Target network " +
          targetNetworkId +
          " does not belong to provider " +
          targetProviderId
        );
      }
    } finally {
      // Restore original provider context without clearing username
      if (currentProvider != null) {
        Context.setProvider(currentProvider);
      }
      // Don't use Context.clear() as it would also clear the username
    }
  }

  private void validateSourceLine(String sourceLineId) {
    Line sourceLine = loadSourceLine(sourceLineId);
    if (sourceLine == null) {
      throw new IllegalArgumentException("Source line not found: " + sourceLineId);
    }

    // Validate line has required entities for migration
    if (
      sourceLine.getJourneyPatterns() == null || sourceLine.getJourneyPatterns().isEmpty()
    ) {
      throw new IllegalArgumentException(
        "Source line " + sourceLineId + " has no journey patterns to migrate"
      );
    }
  }

  private Line loadSourceLine(String sourceLineId) {
    // Find the line across all providers using global EntityManager queries
    Line line = findLineGlobally(sourceLineId);
    if (line != null) {
      String sourceProviderCode = line.getProvider().getCode();

      // Now load the line with proper provider context to ensure full lazy loading
      String currentProvider = Context.getProvider();
      try {
        Context.setProvider(sourceProviderCode);

        // Reload with provider context for full entity graph
        if (line instanceof FixedLine) {
          line = fixedLineRepository.getOne(sourceLineId);
        } else {
          line = flexibleLineRepository.getOne(sourceLineId);
        }
      } finally {
        // Restore original provider context without clearing username
        if (currentProvider != null) {
          Context.setProvider(currentProvider);
        }
        // Don't use Context.clear() as it would also clear the username
      }
    }
    return line;
  }

  private Line findLineGlobally(String sourceLineId) {
    try {
      // Try to find as FixedLine first
      Line line = entityManager
        .createQuery(
          "SELECT l FROM FixedLine l WHERE l.netexId = :netexId",
          FixedLine.class
        )
        .setParameter("netexId", sourceLineId)
        .getSingleResult();
      return line;
    } catch (NoResultException e) {
      // Try to find as FlexibleLine
      try {
        return entityManager
          .createQuery(
            "SELECT l FROM FlexibleLine l WHERE l.netexId = :netexId",
            FlexibleLine.class
          )
          .setParameter("netexId", sourceLineId)
          .getSingleResult();
      } catch (NoResultException ex) {
        return null;
      }
    }
  }

  private List<Object> loadSourceEntities(Line sourceLine) {
    List<Object> entities = new ArrayList<>();

    entities.add(sourceLine);

    if (sourceLine.getJourneyPatterns() != null) {
      entities.addAll(sourceLine.getJourneyPatterns());

      for (JourneyPattern pattern : sourceLine.getJourneyPatterns()) {
        if (pattern.getPointsInSequence() != null) {
          entities.addAll(pattern.getPointsInSequence());
        }
        if (pattern.getServiceJourneys() != null) {
          entities.addAll(pattern.getServiceJourneys());

          for (ServiceJourney journey : pattern.getServiceJourneys()) {
            if (journey.getPassingTimes() != null) {
              entities.addAll(journey.getPassingTimes());
            }
            // Include DayTypes to be detached
            if (journey.getDayTypes() != null) {
              entities.addAll(journey.getDayTypes());
              // Also include DayTypeAssignments
              for (DayType dayType : journey.getDayTypes()) {
                if (dayType.getDayTypeAssignments() != null) {
                  entities.addAll(dayType.getDayTypeAssignments());
                }
              }
            }
          }
        }
      }
    }

    // Include Notices if any
    if (sourceLine.getNotices() != null) {
      entities.addAll(sourceLine.getNotices());
    }

    return entities;
  }

  private void initializeLazyCollections(Line line) {
    // Force initialization of all lazy collections
    if (line.getNotices() != null) {
      line.getNotices().size(); // Force initialization
    }

    if (line.getJourneyPatterns() != null) {
      line.getJourneyPatterns().size(); // Force initialization
      for (JourneyPattern pattern : line.getJourneyPatterns()) {
        if (pattern.getNotices() != null) {
          pattern.getNotices().size();
        }
        if (pattern.getPointsInSequence() != null) {
          pattern.getPointsInSequence().size();
          // Initialize notices for each stop point
          for (var stopPoint : pattern.getPointsInSequence()) {
            if (stopPoint.getNotices() != null) {
              stopPoint.getNotices().size();
            }
            // Initialize flexible stop place if present
            if (stopPoint.getFlexibleStopPlace() != null) {
              // Access a property to force initialization
              stopPoint.getFlexibleStopPlace().getName();
            }
          }
        }
        if (pattern.getServiceJourneys() != null) {
          pattern.getServiceJourneys().size();
          for (ServiceJourney journey : pattern.getServiceJourneys()) {
            if (journey.getPassingTimes() != null) {
              journey.getPassingTimes().size();
              // Initialize notices for each passing time
              for (var passingTime : journey.getPassingTimes()) {
                if (passingTime.getNotices() != null) {
                  passingTime.getNotices().size();
                }
              }
            }
            if (journey.getDayTypes() != null) {
              journey.getDayTypes().size();
              for (DayType dayType : journey.getDayTypes()) {
                if (dayType.getDayTypeAssignments() != null) {
                  dayType.getDayTypeAssignments().size();
                }
                if (dayType.getDaysOfWeek() != null) {
                  dayType.getDaysOfWeek().size();
                }
              }
            }
            if (journey.getNotices() != null) {
              journey.getNotices().size();
            }
          }
        }
      }
    }

    // Initialize Branding if it's a FixedLine
    if (line instanceof FixedLine) {
      FixedLine fixedLine = (FixedLine) line;
      if (fixedLine.getBranding() != null) {
        // Access branding to force initialization
        fixedLine.getBranding().getName();
      }
    }

    // Initialize FlexibleLine specific fields
    if (line instanceof FlexibleLine) {
      FlexibleLine flexibleLine = (FlexibleLine) line;
      if (flexibleLine.getBookingArrangement() != null) {
        // Access booking arrangement to force initialization
        flexibleLine.getBookingArrangement().getBookingContact();
      }
    }
  }

  private void saveDayTypesAndUpdateReferences(Line line) {
    // Map to track saved DayTypes by their NetexId
    java.util.Map<String, DayType> savedDayTypes = new java.util.HashMap<>();

    if (line.getJourneyPatterns() != null) {
      for (JourneyPattern pattern : line.getJourneyPatterns()) {
        if (pattern.getServiceJourneys() != null) {
          for (ServiceJourney journey : pattern.getServiceJourneys()) {
            if (journey.getDayTypes() != null && !journey.getDayTypes().isEmpty()) {
              Set<DayType> updatedDayTypes = new HashSet<>();

              for (DayType dayType : journey.getDayTypes()) {
                DayType persistedDayType;

                // Check if we've already saved this DayType
                if (savedDayTypes.containsKey(dayType.getNetexId())) {
                  persistedDayType = savedDayTypes.get(dayType.getNetexId());
                } else {
                  // Save the DayType if it's not already persisted
                  if (dayType.getPk() == null) {
                    persistedDayType = dayTypeRepository.save(dayType);
                    savedDayTypes.put(persistedDayType.getNetexId(), persistedDayType);
                  } else {
                    persistedDayType = dayType;
                    savedDayTypes.put(dayType.getNetexId(), dayType);
                  }
                }

                updatedDayTypes.add(persistedDayType);
              }

              // Replace the DayTypes with the persisted instances
              journey.updateDayTypes(new ArrayList<>(updatedDayTypes));
            }
          }
        }
      }
    }
  }

  private LineMigrationWarning createWarning(
    String type,
    String message,
    String entityId
  ) {
    LineMigrationWarning warning = new LineMigrationWarning();
    warning.setType(type);
    warning.setMessage(message);
    warning.setEntityId(entityId);
    return warning;
  }

  // Input and Result classes to be defined based on GraphQL types
  public static class LineMigrationInput {

    private String sourceLineId;
    private String targetProviderId;
    private String targetNetworkId;
    private LineMigrationOptions options;

    public String getSourceLineId() {
      return sourceLineId;
    }

    public void setSourceLineId(String sourceLineId) {
      this.sourceLineId = sourceLineId;
    }

    public String getTargetProviderId() {
      return targetProviderId;
    }

    public void setTargetProviderId(String targetProviderId) {
      this.targetProviderId = targetProviderId;
    }

    public String getTargetNetworkId() {
      return targetNetworkId;
    }

    public void setTargetNetworkId(String targetNetworkId) {
      this.targetNetworkId = targetNetworkId;
    }

    public LineMigrationOptions getOptions() {
      return options;
    }

    public void setOptions(LineMigrationOptions options) {
      this.options = options;
    }
  }

  public static class LineMigrationOptions {

    private ConflictResolutionStrategy conflictResolution =
      ConflictResolutionStrategy.FAIL;
    private Boolean includeDayTypes = true;
    private Boolean dryRun = false;

    public ConflictResolutionStrategy getConflictResolution() {
      return conflictResolution;
    }

    public void setConflictResolution(ConflictResolutionStrategy conflictResolution) {
      this.conflictResolution = conflictResolution;
    }

    public Boolean getIncludeDayTypes() {
      return includeDayTypes;
    }

    public void setIncludeDayTypes(Boolean includeDayTypes) {
      this.includeDayTypes = includeDayTypes;
    }

    public Boolean getDryRun() {
      return dryRun;
    }

    public void setDryRun(Boolean dryRun) {
      this.dryRun = dryRun;
    }
  }

  public enum ConflictResolutionStrategy {
    FAIL,
    RENAME,
    SKIP,
  }

  public static class LineMigrationResult {

    private boolean success;
    private String migratedLineId;
    private LineMigrationSummary summary;
    private List<LineMigrationWarning> warnings;
    private String errorMessage;

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public String getMigratedLineId() {
      return migratedLineId;
    }

    public void setMigratedLineId(String migratedLineId) {
      this.migratedLineId = migratedLineId;
    }

    public LineMigrationSummary getSummary() {
      return summary;
    }

    public void setSummary(LineMigrationSummary summary) {
      this.summary = summary;
    }

    public List<LineMigrationWarning> getWarnings() {
      return warnings;
    }

    public void setWarnings(List<LineMigrationWarning> warnings) {
      this.warnings = warnings;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

  public static class LineMigrationSummary {

    private int entitiesMigrated;
    private int warningsCount;
    private long executionTimeMs;

    public int getEntitiesMigrated() {
      return entitiesMigrated;
    }

    public void setEntitiesMigrated(int entitiesMigrated) {
      this.entitiesMigrated = entitiesMigrated;
    }

    public int getWarningsCount() {
      return warningsCount;
    }

    public void setWarningsCount(int warningsCount) {
      this.warningsCount = warningsCount;
    }

    public long getExecutionTimeMs() {
      return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
      this.executionTimeMs = executionTimeMs;
    }
  }

  public static class LineMigrationWarning {

    private String type;
    private String message;
    private String entityId;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public String getEntityId() {
      return entityId;
    }

    public void setEntityId(String entityId) {
      this.entityId = entityId;
    }
  }
}
