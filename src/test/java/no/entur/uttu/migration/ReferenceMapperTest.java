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

package no.entur.uttu.migration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import no.entur.uttu.migration.ReferenceMapper.ReferenceValidationException;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DayTypeAssignment;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Line;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.OperatingPeriod;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.model.StopPointInJourneyPattern;
import no.entur.uttu.repository.DayTypeRepository;
import no.entur.uttu.repository.NetworkRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceMapperTest {

  @Mock
  private NetworkRepository networkRepository;

  @Mock
  private DayTypeRepository dayTypeRepository;

  private ReferenceMapper referenceMapper;
  private Provider targetProvider;

  @Before
  public void setUp() {
    referenceMapper = new ReferenceMapper(networkRepository, dayTypeRepository);

    targetProvider = new Provider();
    targetProvider.setCode("TARGET_PROVIDER");
    referenceMapper.setTargetProvider(targetProvider);
  }

  @Test
  public void testBasicIdMapping() {
    String oldId = "OLD:Line:123";
    String newId = "NEW:Line:456";

    assertFalse(referenceMapper.hasMapping(oldId));
    assertNull(referenceMapper.getMappedId(oldId));

    referenceMapper.addMapping(oldId, newId);

    assertTrue(referenceMapper.hasMapping(oldId));
    assertEquals(newId, referenceMapper.getMappedId(oldId));
  }

  @Test
  public void testClearMappings() {
    referenceMapper.addMapping("old1", "new1");
    referenceMapper.addMapping("old2", "new2");

    assertTrue(referenceMapper.hasMapping("old1"));
    assertTrue(referenceMapper.hasMapping("old2"));

    referenceMapper.clearMappings();

    assertFalse(referenceMapper.hasMapping("old1"));
    assertFalse(referenceMapper.hasMapping("old2"));
  }

  @Test
  public void testUpdateLineReferences() {
    Line line = new FixedLine();
    JourneyPattern jp1 = new JourneyPattern();
    jp1.setNetexId("OLD:JP:1");
    JourneyPattern jp2 = new JourneyPattern();
    jp2.setNetexId("OLD:JP:2");
    line.setJourneyPatterns(Arrays.asList(jp1, jp2));

    referenceMapper.addMapping("OLD:JP:1", "NEW:JP:1");
    referenceMapper.addMapping("OLD:JP:2", "NEW:JP:2");

    referenceMapper.updateLineReferences(line);

    assertEquals("NEW:JP:1", jp1.getNetexId());
    assertEquals("NEW:JP:2", jp2.getNetexId());
  }

  @Test
  public void testUpdateLineReferencesNoMappings() {
    Line line = new FixedLine();
    JourneyPattern jp = new JourneyPattern();
    jp.setNetexId("OLD:JP:1");
    line.setJourneyPatterns(Arrays.asList(jp));

    referenceMapper.updateLineReferences(line);

    assertEquals("OLD:JP:1", jp.getNetexId());
  }

  @Test
  public void testUpdateJourneyPatternReferences() {
    JourneyPattern journeyPattern = new JourneyPattern();
    Line line = new FixedLine();
    line.setNetexId("OLD:Line:1");
    journeyPattern.setLine(line);

    ServiceJourney sj1 = new ServiceJourney();
    sj1.setNetexId("OLD:SJ:1");
    ServiceJourney sj2 = new ServiceJourney();
    sj2.setNetexId("OLD:SJ:2");
    journeyPattern.setServiceJourneys(Arrays.asList(sj1, sj2));

    referenceMapper.addMapping("OLD:Line:1", "NEW:Line:1");
    referenceMapper.addMapping("OLD:SJ:1", "NEW:SJ:1");
    referenceMapper.addMapping("OLD:SJ:2", "NEW:SJ:2");

    referenceMapper.updateJourneyPatternReferences(journeyPattern);

    assertEquals("NEW:Line:1", line.getNetexId());
    assertEquals("NEW:SJ:1", sj1.getNetexId());
    assertEquals("NEW:SJ:2", sj2.getNetexId());
  }

  @Test
  public void testUpdateServiceJourneyReferences() {
    ServiceJourney serviceJourney = new ServiceJourney();
    JourneyPattern journeyPattern = new JourneyPattern();
    journeyPattern.setNetexId("OLD:JP:1");
    serviceJourney.setJourneyPattern(journeyPattern);

    DayType dayType1 = new DayType();
    dayType1.setNetexId("OLD:DT:1");
    dayType1.setName("Weekdays");
    DayType dayType2 = new DayType();
    dayType2.setNetexId("OLD:DT:2");
    dayType2.setName("Weekends");
    serviceJourney.updateDayTypes(Arrays.asList(dayType1, dayType2));

    referenceMapper.addMapping("OLD:JP:1", "NEW:JP:1");
    referenceMapper.addMapping("OLD:DT:1", "NEW:DT:1");
    referenceMapper.addMapping("OLD:DT:2", "NEW:DT:2");

    referenceMapper.updateServiceJourneyReferences(serviceJourney);

    assertEquals("NEW:JP:1", journeyPattern.getNetexId());
    assertEquals(2, serviceJourney.getDayTypes().size());
  }

  @Test
  public void testGetMappedDayTypeWithExistingMapping() {
    DayType originalDayType = new DayType();
    originalDayType.setNetexId("OLD:DT:1");
    originalDayType.setName("Test DayType");
    originalDayType.setDaysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));

    referenceMapper.addMapping("OLD:DT:1", "NEW:DT:1");

    DayType mappedDayType = referenceMapper.getMappedDayType(originalDayType);

    assertNotNull(mappedDayType);
    assertEquals("NEW:DT:1", mappedDayType.getNetexId());
  }

  @Test
  public void testFindExistingMatchingDayType() {
    DayType existingDayType = new DayType();
    existingDayType.setNetexId("EXISTING:DT:1");
    existingDayType.setName("Weekdays");
    existingDayType.setDaysOfWeek(
      Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)
    );

    when(dayTypeRepository.findByProvider(targetProvider)).thenReturn(
      Arrays.asList(existingDayType)
    );

    DayType searchDayType = new DayType();
    searchDayType.setName("Weekdays");
    searchDayType.setDaysOfWeek(
      Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)
    );

    DayType found = referenceMapper.findExistingDayType(searchDayType);

    assertNotNull(found);
    assertEquals("EXISTING:DT:1", found.getNetexId());
  }

  @Test
  public void testFindExistingDayTypeNoMatch() {
    DayType existingDayType = new DayType();
    existingDayType.setName("Weekdays");
    existingDayType.setDaysOfWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));

    when(dayTypeRepository.findByProvider(targetProvider)).thenReturn(
      Arrays.asList(existingDayType)
    );

    DayType searchDayType = new DayType();
    searchDayType.setName("Weekends");
    searchDayType.setDaysOfWeek(Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

    DayType found = referenceMapper.findExistingDayType(searchDayType);

    assertNull(found);
  }

  @Test
  public void testDayTypeEqualityWithAssignments() {
    DayType dayType1 = new DayType();
    dayType1.setName("Special Days");
    dayType1.setDaysOfWeek(Arrays.asList());

    DayTypeAssignment assignment1 = new DayTypeAssignment();
    assignment1.setDate(LocalDate.of(2025, 12, 25));
    assignment1.setAvailable(false);

    dayType1.setDayTypeAssignments(Arrays.asList(assignment1));

    DayType dayType2 = new DayType();
    dayType2.setName("Special Days");
    dayType2.setDaysOfWeek(Arrays.asList());

    DayTypeAssignment assignment2 = new DayTypeAssignment();
    assignment2.setDate(LocalDate.of(2025, 12, 25));
    assignment2.setAvailable(false);

    dayType2.setDayTypeAssignments(Arrays.asList(assignment2));

    when(dayTypeRepository.findByProvider(targetProvider)).thenReturn(
      Arrays.asList(dayType1)
    );

    DayType found = referenceMapper.findExistingDayType(dayType2);

    assertNotNull(found);
  }

  @Test
  public void testDayTypeEqualityWithOperatingPeriod() {
    DayType dayType1 = new DayType();
    dayType1.setName("Summer Schedule");

    OperatingPeriod period1 = new OperatingPeriod();
    period1.setFromDate(LocalDate.of(2025, 6, 1));
    period1.setToDate(LocalDate.of(2025, 8, 31));

    DayTypeAssignment assignment1 = new DayTypeAssignment();
    assignment1.setOperatingPeriod(period1);
    assignment1.setAvailable(true);

    dayType1.setDayTypeAssignments(Arrays.asList(assignment1));

    DayType dayType2 = new DayType();
    dayType2.setName("Summer Schedule");

    OperatingPeriod period2 = new OperatingPeriod();
    period2.setFromDate(LocalDate.of(2025, 6, 1));
    period2.setToDate(LocalDate.of(2025, 8, 31));

    DayTypeAssignment assignment2 = new DayTypeAssignment();
    assignment2.setOperatingPeriod(period2);
    assignment2.setAvailable(true);

    dayType2.setDayTypeAssignments(Arrays.asList(assignment2));

    when(dayTypeRepository.findByProvider(targetProvider)).thenReturn(
      Arrays.asList(dayType1)
    );

    DayType found = referenceMapper.findExistingDayType(dayType2);

    assertNotNull(found);
  }

  @Test
  public void testValidateNetworkReferenceSuccess() throws ReferenceValidationException {
    String networkId = "TARGET:Network:1";
    Network network = new Network();
    network.setProvider(targetProvider);

    when(networkRepository.getOne(networkId)).thenReturn(network);

    referenceMapper.validateNetworkReference(networkId, targetProvider);

    verify(networkRepository).getOne(networkId);
  }

  @Test
  public void testValidateNetworkReferenceNotFound() {
    String networkId = "TARGET:Network:1";

    when(networkRepository.getOne(networkId)).thenReturn(null);

    try {
      referenceMapper.validateNetworkReference(networkId, targetProvider);
      fail("Expected ReferenceValidationException");
    } catch (ReferenceValidationException e) {
      assertTrue(e.getMessage().contains("not found in target provider"));
    }
  }

  @Test
  public void testValidateNetworkReferenceWrongProvider() {
    String networkId = "TARGET:Network:1";
    Network network = new Network();
    Provider otherProvider = new Provider();
    otherProvider.setCode("OTHER");
    network.setProvider(otherProvider);

    when(networkRepository.getOne(networkId)).thenReturn(network);

    try {
      referenceMapper.validateNetworkReference(networkId, targetProvider);
      fail("Expected ReferenceValidationException");
    } catch (ReferenceValidationException e) {
      assertTrue(e.getMessage().contains("not found in target provider"));
    }
  }

  @Test
  public void testValidateNetworkReferenceNullOrEmpty() {
    try {
      referenceMapper.validateNetworkReference(null, targetProvider);
      fail("Expected ReferenceValidationException");
    } catch (ReferenceValidationException e) {
      assertTrue(e.getMessage().contains("cannot be null or empty"));
    }

    try {
      referenceMapper.validateNetworkReference("", targetProvider);
      fail("Expected ReferenceValidationException");
    } catch (ReferenceValidationException e) {
      assertTrue(e.getMessage().contains("cannot be null or empty"));
    }
  }

  @Test
  public void testValidateOperatorReferenceSuccess() throws ReferenceValidationException {
    referenceMapper.validateOperatorReference("NSR:Operator:123");
    referenceMapper.validateOperatorReference("TEST_2:Operator_Type:ID-456");
  }

  @Test
  public void testValidateOperatorReferenceInvalidFormat() {
    try {
      referenceMapper.validateOperatorReference("INVALID");
      fail("Expected ReferenceValidationException");
    } catch (ReferenceValidationException e) {
      assertTrue(e.getMessage().contains("does not match expected NetEx format"));
    }

    try {
      referenceMapper.validateOperatorReference("ONLY:TWO");
      fail("Expected ReferenceValidationException");
    } catch (ReferenceValidationException e) {
      assertTrue(e.getMessage().contains("does not match expected NetEx format"));
    }
  }

  @Test
  public void testValidateQuayReferenceSuccess() throws ReferenceValidationException {
    referenceMapper.validateQuayReference("NSR:Quay:123");
    referenceMapper.validateQuayReference("TEST:StopPlace:456");
  }

  @Test
  public void testValidateQuayReferenceInvalidFormat() {
    try {
      referenceMapper.validateQuayReference("INVALID");
      fail("Expected ReferenceValidationException");
    } catch (ReferenceValidationException e) {
      assertTrue(e.getMessage().contains("does not match expected NetEx format"));
    }
  }

  @Test
  public void testValidateLineReferencesFixedLine() throws ReferenceValidationException {
    FixedLine fixedLine = new FixedLine();

    Network network = new Network();
    network.setNetexId("TARGET:Network:1");
    network.setProvider(targetProvider);
    fixedLine.setNetwork(network);

    fixedLine.setOperatorRef("NSR:Operator:1");

    JourneyPattern jp = new JourneyPattern();
    StopPointInJourneyPattern stopPoint1 = new StopPointInJourneyPattern();
    stopPoint1.setQuayRef("NSR:Quay:1");
    StopPointInJourneyPattern stopPoint2 = new StopPointInJourneyPattern();
    stopPoint2.setQuayRef("NSR:Quay:2");
    jp.setPointsInSequence(Arrays.asList(stopPoint1, stopPoint2));
    fixedLine.setJourneyPatterns(Arrays.asList(jp));

    when(networkRepository.getOne("TARGET:Network:1")).thenReturn(network);

    referenceMapper.validateLineReferences(fixedLine, targetProvider);

    verify(networkRepository).getOne("TARGET:Network:1");
  }

  @Test
  public void testValidateLineReferencesInvalidQuay() {
    FixedLine fixedLine = new FixedLine();

    Network network = new Network();
    network.setNetexId("TARGET:Network:1");
    network.setProvider(targetProvider);
    fixedLine.setNetwork(network);

    JourneyPattern jp = new JourneyPattern();
    StopPointInJourneyPattern stopPoint = new StopPointInJourneyPattern();
    stopPoint.setQuayRef("INVALID_QUAY");
    jp.setPointsInSequence(Arrays.asList(stopPoint));
    fixedLine.setJourneyPatterns(Arrays.asList(jp));

    when(networkRepository.getOne("TARGET:Network:1")).thenReturn(network);

    try {
      referenceMapper.validateLineReferences(fixedLine, targetProvider);
      fail("Expected ReferenceValidationException");
    } catch (ReferenceValidationException e) {
      assertTrue(e.getMessage().contains("does not match expected NetEx format"));
    }
  }

  @Test
  public void testSetTargetProvider() {
    Provider newProvider = new Provider();
    newProvider.setCode("NEW_PROVIDER");

    referenceMapper.setTargetProvider(newProvider);

    // Test that the new provider is used for DayType lookups
    when(dayTypeRepository.findByProvider(newProvider)).thenReturn(Arrays.asList());

    DayType dayType = new DayType();
    dayType.setName("Test");
    DayType found = referenceMapper.findExistingDayType(dayType);

    assertNull(found);
    verify(dayTypeRepository).findByProvider(newProvider);
  }
}
