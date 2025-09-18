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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DayTypeAssignment;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Line;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.OperatingPeriod;
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
  private Provider currentTargetProvider;

  public ReferenceMapper(
    NetworkRepository networkRepository,
    DayTypeRepository dayTypeRepository
  ) {
    this.networkRepository = networkRepository;
    this.dayTypeRepository = dayTypeRepository;
  }

  public void setTargetProvider(Provider provider) {
    this.currentTargetProvider = provider;
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
      clonedLine
        .getJourneyPatterns()
        .forEach(journeyPattern -> {
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
      clonedJourneyPattern
        .getServiceJourneys()
        .forEach(serviceJourney -> {
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

    // Check if we've already mapped this DayType
    if (sharedDayTypes.containsKey(dayTypeKey)) {
      return sharedDayTypes.get(dayTypeKey);
    }

    // Check if an equivalent DayType already exists in the database
    DayType existingDayType = findExistingMatchingDayType(originalDayType);
    if (existingDayType != null) {
      sharedDayTypes.put(dayTypeKey, existingDayType);
      return existingDayType;
    }

    // Create new DayType if no match found
    String mappedId = getMappedId(originalDayType.getNetexId());
    if (mappedId != null) {
      DayType mappedDayType = createDayTypeClone(originalDayType);
      mappedDayType.setNetexId(mappedId);
      sharedDayTypes.put(dayTypeKey, mappedDayType);
      return mappedDayType;
    }

    return originalDayType;
  }

  public DayType findExistingDayType(DayType dayType) {
    return findExistingMatchingDayType(dayType);
  }

  private DayType findExistingMatchingDayType(DayType dayType) {
    if (currentTargetProvider == null) {
      return null;
    }

    // Find all DayTypes for the target provider
    List<DayType> existingDayTypes = dayTypeRepository.findByProvider(
      currentTargetProvider
    );

    // Check if any existing DayType matches the content of the source DayType
    for (DayType existing : existingDayTypes) {
      if (isDayTypeContentEqual(existing, dayType)) {
        return existing;
      }
    }

    return null;
  }

  private boolean isDayTypeContentEqual(DayType dayType1, DayType dayType2) {
    // Compare the content of two DayTypes (not the IDs)
    if (!Objects.equals(dayType1.getName(), dayType2.getName())) {
      return false;
    }

    if (!Objects.equals(dayType1.getDaysOfWeek(), dayType2.getDaysOfWeek())) {
      return false;
    }

    // Compare DayTypeAssignments by their content
    if (
      dayType1.getDayTypeAssignments() == null && dayType2.getDayTypeAssignments() == null
    ) {
      return true;
    }

    if (
      dayType1.getDayTypeAssignments() == null || dayType2.getDayTypeAssignments() == null
    ) {
      return false;
    }

    List<DayTypeAssignment> assignments1 = dayType1.getDayTypeAssignments();
    List<DayTypeAssignment> assignments2 = dayType2.getDayTypeAssignments();

    if (assignments1.size() != assignments2.size()) {
      return false;
    }

    // Sort assignments for comparison
    List<DayTypeAssignment> sorted1 = new ArrayList<>(assignments1);
    List<DayTypeAssignment> sorted2 = new ArrayList<>(assignments2);

    sorted1.sort(this::compareDayTypeAssignments);
    sorted2.sort(this::compareDayTypeAssignments);

    // Compare each assignment
    for (int i = 0; i < sorted1.size(); i++) {
      if (!isDayTypeAssignmentEqual(sorted1.get(i), sorted2.get(i))) {
        return false;
      }
    }

    return true;
  }

  private boolean isDayTypeAssignmentEqual(
    DayTypeAssignment assignment1,
    DayTypeAssignment assignment2
  ) {
    // Compare available flag
    if (!Objects.equals(assignment1.getAvailable(), assignment2.getAvailable())) {
      return false;
    }

    // Compare date (single date assignment)
    if (assignment1.getDate() != null && assignment2.getDate() != null) {
      return Objects.equals(assignment1.getDate(), assignment2.getDate());
    }

    // Compare operating period (date range assignment)
    if (
      assignment1.getOperatingPeriod() != null && assignment2.getOperatingPeriod() != null
    ) {
      OperatingPeriod period1 = assignment1.getOperatingPeriod();
      OperatingPeriod period2 = assignment2.getOperatingPeriod();
      return (
        Objects.equals(period1.getFromDate(), period2.getFromDate()) &&
        Objects.equals(period1.getToDate(), period2.getToDate())
      );
    }

    // One has date and other has operating period, or both are null
    return (
      assignment1.getDate() == null &&
      assignment2.getDate() == null &&
      assignment1.getOperatingPeriod() == null &&
      assignment2.getOperatingPeriod() == null
    );
  }

  private int compareDayTypeAssignments(DayTypeAssignment a1, DayTypeAssignment a2) {
    // Sort by date or operating period start date
    LocalDate date1 = a1.getDate();
    LocalDate date2 = a2.getDate();

    if (date1 == null && a1.getOperatingPeriod() != null) {
      date1 = a1.getOperatingPeriod().getFromDate();
    }
    if (date2 == null && a2.getOperatingPeriod() != null) {
      date2 = a2.getOperatingPeriod().getFromDate();
    }

    if (date1 == null && date2 == null) {
      return 0;
    }
    if (date1 == null) {
      return -1;
    }
    if (date2 == null) {
      return 1;
    }
    return date1.compareTo(date2);
  }

  private String generateDayTypeKey(DayType dayType) {
    return (
      dayType.getName() +
      "_" +
      dayType.getDaysOfWeek() +
      "_" +
      dayType.getDayTypeAssignments()
    );
  }

  private DayType createDayTypeClone(DayType original) {
    DayType clone = new DayType();
    clone.setName(original.getName());
    clone.setDaysOfWeek(new java.util.ArrayList<>(original.getDaysOfWeek()));
    clone.setDayTypeAssignments(
      new java.util.ArrayList<>(original.getDayTypeAssignments())
    );
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
          String.format(
            "Network with ID '%s' not found in target provider '%s'",
            networkId,
            targetProvider.getCode()
          )
        );
      }
    } catch (Exception e) {
      throw new ReferenceValidationException(
        String.format(
          "Failed to validate Network reference '%s': %s",
          networkId,
          e.getMessage()
        )
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
        String.format(
          "OperatorRef '%s' does not match expected NetEx format 'codespace:type:id'",
          operatorRef
        )
      );
    }
  }

  public void validateQuayReference(String quayRef) throws ReferenceValidationException {
    if (quayRef == null || quayRef.trim().isEmpty()) {
      throw new ReferenceValidationException("QuayRef cannot be null or empty");
    }

    if (!quayRef.matches("^[A-Za-z0-9_-]+:[A-Za-z0-9_-]+:[A-Za-z0-9_-]+$")) {
      throw new ReferenceValidationException(
        String.format(
          "QuayRef '%s' does not match expected NetEx format 'codespace:type:id'",
          quayRef
        )
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
