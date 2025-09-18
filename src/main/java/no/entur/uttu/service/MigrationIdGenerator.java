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

import com.google.common.base.Joiner;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.repository.FixedLineRepository;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.JourneyPatternRepository;
import no.entur.uttu.repository.ServiceJourneyRepository;
import no.entur.uttu.service.LineMigrationService.ConflictResolutionStrategy;
import org.springframework.stereotype.Component;

@Component
public class MigrationIdGenerator {

  private final FixedLineRepository fixedLineRepository;
  private final FlexibleLineRepository flexibleLineRepository;
  private final JourneyPatternRepository journeyPatternRepository;
  private final ServiceJourneyRepository serviceJourneyRepository;

  private final Map<String, String> idMappings = new HashMap<>();

  public MigrationIdGenerator(
    FixedLineRepository fixedLineRepository,
    FlexibleLineRepository flexibleLineRepository,
    JourneyPatternRepository journeyPatternRepository,
    ServiceJourneyRepository serviceJourneyRepository
  ) {
    this.fixedLineRepository = fixedLineRepository;
    this.flexibleLineRepository = flexibleLineRepository;
    this.journeyPatternRepository = journeyPatternRepository;
    this.serviceJourneyRepository = serviceJourneyRepository;
  }

  public String generateNetexId(ProviderEntity entity, Provider targetProvider) {
    String entityType = entity.getNetexName();
    String codespace = targetProvider.getCodespace().getXmlns();
    return Joiner.on(":").join(codespace, entityType, UUID.randomUUID());
  }

  public String generateUniqueNameWithConflictResolution(
    String originalName,
    String entityType,
    Provider targetProvider,
    ConflictResolutionStrategy strategy
  ) {
    if (!hasNameConflict(originalName, entityType, targetProvider)) {
      return originalName;
    }

    switch (strategy) {
      case FAIL:
        throw new IllegalArgumentException(
          String.format(
            "%s with name '%s' already exists in provider %s",
            entityType,
            originalName,
            targetProvider.getCode()
          )
        );
      case RENAME:
        return generateUniqueRenamedName(originalName, entityType, targetProvider);
      case SKIP:
        throw new ConflictSkippedException(
          String.format(
            "%s with name '%s' already exists - skipping due to SKIP strategy",
            entityType,
            originalName
          )
        );
      default:
        throw new IllegalArgumentException("Unknown conflict resolution strategy: " + strategy);
    }
  }

  private boolean hasNameConflict(String name, String entityType, Provider targetProvider) {
    switch (entityType) {
      case "Line":
        return hasLineNameConflict(name, targetProvider);
      case "JourneyPattern":
        return hasJourneyPatternNameConflict(name, targetProvider);
      case "ServiceJourney":
        return hasServiceJourneyNameConflict(name, targetProvider);
      default:
        return false;
    }
  }

  private boolean hasLineNameConflict(String name, Provider targetProvider) {
    var fixedLine = fixedLineRepository.findByProviderAndName(targetProvider, name);
    if (fixedLine != null) {
      return true;
    }
    var flexibleLine = flexibleLineRepository.findByProviderAndName(targetProvider, name);
    return flexibleLine != null;
  }

  private boolean hasJourneyPatternNameConflict(String name, Provider targetProvider) {
    var journeyPattern = journeyPatternRepository.findByProviderAndName(targetProvider, name);
    return journeyPattern != null;
  }

  private boolean hasServiceJourneyNameConflict(String name, Provider targetProvider) {
    var serviceJourney = serviceJourneyRepository.findByProviderAndName(targetProvider, name);
    return serviceJourney != null;
  }

  private String generateUniqueRenamedName(
    String originalName,
    String entityType,
    Provider targetProvider
  ) {
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String baseName = originalName + "_migrated_" + timestamp;
    String candidateName = baseName;
    int counter = 1;

    while (hasNameConflict(candidateName, entityType, targetProvider)) {
      candidateName = baseName + "_" + counter;
      counter++;
    }

    return candidateName;
  }

  public void addIdMapping(String oldId, String newId) {
    idMappings.put(oldId, newId);
  }

  public String getMappedId(String oldId) {
    return idMappings.get(oldId);
  }

  public void clearMappings() {
    idMappings.clear();
  }

  public static class ConflictSkippedException extends RuntimeException {

    public ConflictSkippedException(String message) {
      super(message);
    }
  }
}