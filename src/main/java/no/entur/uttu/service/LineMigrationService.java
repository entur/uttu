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

import java.util.ArrayList;
import java.util.List;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Line;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.repository.FixedLineRepository;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.NetworkRepository;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.security.spi.UserContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LineMigrationService {

  private final ProviderRepository providerRepository;
  private final FixedLineRepository fixedLineRepository;
  private final FlexibleLineRepository flexibleLineRepository;
  private final NetworkRepository networkRepository;
  private final UserContextService userContextService;
  private final EntityCloner entityCloner;
  private final ReferenceMapper referenceMapper;
  private final MigrationIdGenerator idGenerator;

  public LineMigrationService(
    ProviderRepository providerRepository,
    FixedLineRepository fixedLineRepository,
    FlexibleLineRepository flexibleLineRepository,
    NetworkRepository networkRepository,
    UserContextService userContextService,
    EntityCloner entityCloner,
    ReferenceMapper referenceMapper,
    MigrationIdGenerator idGenerator
  ) {
    this.providerRepository = providerRepository;
    this.fixedLineRepository = fixedLineRepository;
    this.flexibleLineRepository = flexibleLineRepository;
    this.networkRepository = networkRepository;
    this.userContextService = userContextService;
    this.entityCloner = entityCloner;
    this.referenceMapper = referenceMapper;
    this.idGenerator = idGenerator;
  }

  public LineMigrationResult migrateLine(LineMigrationInput input) {
    long startTime = System.currentTimeMillis();
    List<LineMigrationWarning> warnings = new ArrayList<>();

    try {
      validateMigration(input);

      Line sourceLine = loadSourceLine(input.getSourceLineId());
      List<Object> sourceEntities = loadSourceEntities(sourceLine);

      Provider targetProvider = providerRepository.getOne(input.getTargetProviderId());
      Network targetNetwork = networkRepository.getOne(input.getTargetNetworkId());

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

      // Validate external references before cloning
      try {
        referenceMapper.validateLineReferences(sourceLine, targetProvider);
      } catch (ReferenceMapper.ReferenceValidationException e) {
        warnings.add(
          createWarning("REFERENCE_VALIDATION", e.getMessage(), sourceLine.getNetexId())
        );
      }

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
        // Save the cloned entities
        if (clonedLine instanceof FixedLine) {
          clonedLine = fixedLineRepository.save((FixedLine) clonedLine);
        } else {
          clonedLine = flexibleLineRepository.save((FlexibleLine) clonedLine);
        }
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

    // Validate network reference using ReferenceMapper
    Provider targetProvider = providerRepository.getOne(input.getTargetProviderId());
    try {
      referenceMapper.validateNetworkReference(
        input.getTargetNetworkId(),
        targetProvider
      );
    } catch (ReferenceMapper.ReferenceValidationException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private void validateProviderAccess(LineMigrationInput input) {
    // Load source line to get source provider
    Line sourceLine = loadSourceLine(input.getSourceLineId());
    if (sourceLine == null) {
      throw new IllegalArgumentException(
        "Source line not found: " + input.getSourceLineId()
      );
    }

    String sourceProviderCode = sourceLine.getProvider().getCode();
    String targetProviderCode = input.getTargetProviderId();

    // Validate access to source provider (read)
    if (!userContextService.hasAccessToProvider(sourceProviderCode)) {
      throw new SecurityException(
        "User does not have access to source provider: " + sourceProviderCode
      );
    }

    // Validate access to target provider (write)
    if (!userContextService.hasAccessToProvider(targetProviderCode)) {
      throw new SecurityException(
        "User does not have access to target provider: " + targetProviderCode
      );
    }

    // Prevent migration within the same provider
    if (sourceProviderCode.equals(targetProviderCode)) {
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
    var network = networkRepository.getOne(targetNetworkId);
    if (network == null) {
      throw new IllegalArgumentException("Target network not found: " + targetNetworkId);
    }

    if (!network.getProvider().getCode().equals(targetProviderId)) {
      throw new IllegalArgumentException(
        "Target network " +
        targetNetworkId +
        " does not belong to provider " +
        targetProviderId
      );
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
    Line line = fixedLineRepository.getOne(sourceLineId);
    if (line == null) {
      line = flexibleLineRepository.getOne(sourceLineId);
    }
    return line;
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
          }
        }
      }
    }

    return entities;
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
