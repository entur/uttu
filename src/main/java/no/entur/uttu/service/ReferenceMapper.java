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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Line;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.model.StopPointInJourneyPattern;
import no.entur.uttu.repository.DayTypeRepository;
import no.entur.uttu.repository.NetworkRepository;
import org.springframework.stereotype.Component;

@Component
public class ReferenceMapper {

  private final NetworkRepository networkRepository;
  private final DayTypeRepository dayTypeRepository;

  private final Map<String, String> idMappings = new HashMap<>();
  private final Map<String, DayType> sharedDayTypes = new HashMap<>();

  public ReferenceMapper(
    NetworkRepository networkRepository,
    DayTypeRepository dayTypeRepository
  ) {
    this.networkRepository = networkRepository;
    this.dayTypeRepository = dayTypeRepository;
  }

  public void addMapping(String oldId, String newId) {
    idMappings.put(oldId, newId);
  }

  public String getMappedId(String oldId) {
    return idMappings.get(oldId);
  }

  public boolean hasMapping(String oldId) {
    return idMappings.containsKey(oldId);
  }

  public void clearMappings() {
    idMappings.clear();
    sharedDayTypes.clear();
  }

  public void updateLineReferences(Line clonedLine) {
    if (clonedLine.getJourneyPatterns() != null) {
      clonedLine.getJourneyPatterns().forEach(journeyPattern -> {
        if (hasMapping(journeyPattern.getNetexId())) {
          journeyPattern.setNetexId(getMappedId(journeyPattern.getNetexId()));
        }
      });
    }
  }

  public void updateJourneyPatternReferences(JourneyPattern clonedJourneyPattern) {
    String lineId = clonedJourneyPattern.getLine().getNetexId();
    if (hasMapping(lineId)) {
      clonedJourneyPattern.getLine().setNetexId(getMappedId(lineId));
    }

    if (clonedJourneyPattern.getServiceJourneys() != null) {
      clonedJourneyPattern.getServiceJourneys().forEach(serviceJourney -> {
        if (hasMapping(serviceJourney.getNetexId())) {
          serviceJourney.setNetexId(getMappedId(serviceJourney.getNetexId()));
        }
      });
    }
  }

  public void updateServiceJourneyReferences(ServiceJourney clonedServiceJourney) {
    String journeyPatternId = clonedServiceJourney.getJourneyPattern().getNetexId();
    if (hasMapping(journeyPatternId)) {
      clonedServiceJourney.getJourneyPattern().setNetexId(getMappedId(journeyPatternId));
    }

    if (clonedServiceJourney.getDayTypes() != null) {
      Set<DayType> updatedDayTypes = new java.util.HashSet<>();
      for (DayType dayType : clonedServiceJourney.getDayTypes()) {
        DayType mappedDayType = getMappedDayType(dayType);
        updatedDayTypes.add(mappedDayType);
      }
      clonedServiceJourney.getDayTypes().clear();
      clonedServiceJourney.getDayTypes().addAll(updatedDayTypes);
    }
  }

  public DayType getMappedDayType(DayType originalDayType) {
    String dayTypeKey = generateDayTypeKey(originalDayType);

    if (sharedDayTypes.containsKey(dayTypeKey)) {
      return sharedDayTypes.get(dayTypeKey);
    }

    String mappedId = getMappedId(originalDayType.getNetexId());
    if (mappedId != null) {
      DayType mappedDayType = createDayTypeClone(originalDayType);
      mappedDayType.setNetexId(mappedId);
      sharedDayTypes.put(dayTypeKey, mappedDayType);
      return mappedDayType;
    }

    return originalDayType;
  }

  private String generateDayTypeKey(DayType dayType) {
    return dayType.getName() + "_" + dayType.getDaysOfWeek() + "_" + dayType.getDayTypeAssignments();
  }

  private DayType createDayTypeClone(DayType original) {
    DayType clone = new DayType();
    clone.setName(original.getName());
    clone.setDaysOfWeek(new java.util.ArrayList<>(original.getDaysOfWeek()));
    clone.setDayTypeAssignments(new java.util.ArrayList<>(original.getDayTypeAssignments()));
    return clone;
  }

  public void validateNetworkReference(String networkId, Provider targetProvider)
      throws ReferenceValidationException {
    if (networkId == null || networkId.trim().isEmpty()) {
      throw new ReferenceValidationException("Network reference cannot be null or empty");
    }

    try {
      Network network = networkRepository.getOne(networkId);
      if (network == null || !network.getProvider().equals(targetProvider)) {
        throw new ReferenceValidationException(
          String.format("Network with ID '%s' not found in target provider '%s'",
                       networkId, targetProvider.getCode())
        );
      }
    } catch (Exception e) {
      throw new ReferenceValidationException(
        String.format("Failed to validate Network reference '%s': %s",
                     networkId, e.getMessage())
      );
    }
  }

  public void validateOperatorReference(String operatorRef)
      throws ReferenceValidationException {
    if (operatorRef == null || operatorRef.trim().isEmpty()) {
      throw new ReferenceValidationException("OperatorRef cannot be null or empty");
    }

    if (!operatorRef.matches("^[A-Za-z0-9_-]+:[A-Za-z0-9_-]+:[A-Za-z0-9_-]+$")) {
      throw new ReferenceValidationException(
        String.format("OperatorRef '%s' does not match expected NetEx format 'codespace:type:id'", operatorRef)
      );
    }
  }

  public void validateQuayReference(String quayRef)
      throws ReferenceValidationException {
    if (quayRef == null || quayRef.trim().isEmpty()) {
      throw new ReferenceValidationException("QuayRef cannot be null or empty");
    }

    if (!quayRef.matches("^[A-Za-z0-9_-]+:[A-Za-z0-9_-]+:[A-Za-z0-9_-]+$")) {
      throw new ReferenceValidationException(
        String.format("QuayRef '%s' does not match expected NetEx format 'codespace:type:id'", quayRef)
      );
    }
  }

  public void validateLineReferences(Line line, Provider targetProvider)
      throws ReferenceValidationException {
    if (line.getNetwork() != null) {
      validateNetworkReference(line.getNetwork().getNetexId(), targetProvider);
    }

    if (line.getOperatorRef() != null) {
      validateOperatorReference(line.getOperatorRef());
    }

    if (line instanceof FixedLine) {
      validateFixedLineReferences((FixedLine) line);
    }
  }

  private void validateFixedLineReferences(FixedLine fixedLine)
      throws ReferenceValidationException {
    for (JourneyPattern journeyPattern : fixedLine.getJourneyPatterns()) {
      for (StopPointInJourneyPattern stopPoint : journeyPattern.getPointsInSequence()) {
        if (stopPoint.getQuayRef() != null) {
          validateQuayReference(stopPoint.getQuayRef());
        }
      }
    }
  }

  public static class ReferenceValidationException extends Exception {
    public ReferenceValidationException(String message) {
      super(message);
    }
  }
}