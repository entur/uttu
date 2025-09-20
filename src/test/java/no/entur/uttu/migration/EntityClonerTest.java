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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import no.entur.uttu.migration.LineMigrationService.ConflictResolutionStrategy;
import no.entur.uttu.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntityClonerTest {

  @Mock
  private MigrationIdGenerator idGenerator;

  @Mock
  private ReferenceMapper referenceMapper;

  private EntityCloner entityCloner;
  private Provider sourceProvider;
  private Provider targetProvider;
  private Network sourceNetwork;
  private Network targetNetwork;
  private Codespace sourceCodespace;
  private Codespace targetCodespace;

  @Before
  public void setUp() {
    entityCloner = new EntityCloner(idGenerator, referenceMapper);

    sourceCodespace = new Codespace();
    sourceCodespace.setXmlns("SOURCE");

    targetCodespace = new Codespace();
    targetCodespace.setXmlns("TARGET");

    sourceProvider = new Provider();
    sourceProvider.setCode("SOURCE_PROVIDER");
    sourceProvider.setCodespace(sourceCodespace);

    targetProvider = new Provider();
    targetProvider.setCode("TARGET_PROVIDER");
    targetProvider.setCodespace(targetCodespace);

    sourceNetwork = new Network();
    sourceNetwork.setNetexId("SOURCE:Network:1");
    sourceNetwork.setName("Source Network");
    sourceNetwork.setProvider(sourceProvider);

    targetNetwork = new Network();
    targetNetwork.setNetexId("TARGET:Network:1");
    targetNetwork.setName("Target Network");
    targetNetwork.setProvider(targetProvider);
  }

  @Test
  public void testCloneFixedLine() {
    FixedLine sourceLine = createSampleFixedLine();

    when(idGenerator.generateNetexId(any(Line.class), eq(targetProvider))).thenReturn(
      "TARGET:Line:123"
    );
    when(
      idGenerator.generateUniqueNameWithConflictResolution(
        eq("Test Fixed Line"),
        eq("Line"),
        eq(targetProvider),
        any()
      )
    ).thenReturn("Test Fixed Line");

    FixedLine clonedLine = entityCloner.cloneLine(
      sourceLine,
      targetProvider,
      targetNetwork
    );

    assertNotNull(clonedLine);
    assertEquals("TARGET:Line:123", clonedLine.getNetexId());
    assertEquals("Test Fixed Line", clonedLine.getName());
    assertEquals(targetProvider, clonedLine.getProvider());
    assertEquals(targetNetwork, clonedLine.getNetwork());
    assertEquals("FL1", clonedLine.getPublicCode());
    assertEquals(VehicleModeEnumeration.BUS, clonedLine.getTransportMode());
    assertEquals("NSR:Operator:1", clonedLine.getOperatorRef());

    verify(idGenerator).addIdMapping("SOURCE:Line:1", "TARGET:Line:123");
    verify(referenceMapper).addMapping("SOURCE:Line:1", "TARGET:Line:123");
  }

  @Test
  public void testCloneFlexibleLine() {
    FlexibleLine sourceLine = createSampleFlexibleLine();

    when(idGenerator.generateNetexId(any(Line.class), eq(targetProvider))).thenReturn(
      "TARGET:Line:456"
    );
    when(
      idGenerator.generateUniqueNameWithConflictResolution(
        eq("Test Flexible Line"),
        eq("Line"),
        eq(targetProvider),
        any()
      )
    ).thenReturn("Test Flexible Line");

    FlexibleLine clonedLine = entityCloner.cloneLine(
      sourceLine,
      targetProvider,
      targetNetwork
    );

    assertNotNull(clonedLine);
    assertEquals("TARGET:Line:456", clonedLine.getNetexId());
    assertEquals("Test Flexible Line", clonedLine.getName());
    assertEquals(targetProvider, clonedLine.getProvider());
    assertEquals(targetNetwork, clonedLine.getNetwork());
    assertEquals(
      FlexibleLineTypeEnumeration.FLEXIBLE_AREAS_ONLY,
      clonedLine.getFlexibleLineType()
    );
    assertNotNull(clonedLine.getBookingArrangement());
    assertEquals(
      "booking@example.com",
      clonedLine.getBookingArrangement().getBookingContact().getEmail()
    );

    verify(idGenerator).addIdMapping("SOURCE:Line:2", "TARGET:Line:456");
    verify(referenceMapper).addMapping("SOURCE:Line:2", "TARGET:Line:456");
  }

  @Test
  public void testCloneJourneyPattern() {
    FixedLine targetLine = new FixedLine();
    targetLine.setProvider(targetProvider);

    JourneyPattern sourcePattern = createSampleJourneyPattern();

    when(
      idGenerator.generateNetexId(any(JourneyPattern.class), eq(targetProvider))
    ).thenReturn("TARGET:JP:123");
    when(
      idGenerator.generateUniqueNameWithConflictResolution(
        eq("Pattern 1"),
        eq("JourneyPattern"),
        eq(targetProvider),
        any()
      )
    ).thenReturn("Pattern 1");

    JourneyPattern clonedPattern = entityCloner.cloneJourneyPattern(
      sourcePattern,
      targetLine
    );

    assertNotNull(clonedPattern);
    assertEquals("TARGET:JP:123", clonedPattern.getNetexId());
    assertEquals("Pattern 1", clonedPattern.getName());
    assertEquals(targetLine, clonedPattern.getLine());
    assertEquals(DirectionTypeEnumeration.INBOUND, clonedPattern.getDirectionType());

    verify(idGenerator).addIdMapping("SOURCE:JP:1", "TARGET:JP:123");
    verify(referenceMapper).addMapping("SOURCE:JP:1", "TARGET:JP:123");
  }

  @Test
  public void testCloneServiceJourney() {
    JourneyPattern targetPattern = new JourneyPattern();
    targetPattern.setProvider(targetProvider);

    ServiceJourney sourceJourney = createSampleServiceJourney();

    when(
      idGenerator.generateNetexId(any(ServiceJourney.class), eq(targetProvider))
    ).thenReturn("TARGET:SJ:123");
    when(
      idGenerator.generateUniqueNameWithConflictResolution(
        eq("Journey 1"),
        eq("ServiceJourney"),
        eq(targetProvider),
        any()
      )
    ).thenReturn("Journey 1");

    ServiceJourney clonedJourney = entityCloner.cloneServiceJourney(
      sourceJourney,
      targetPattern
    );

    assertNotNull(clonedJourney);
    assertEquals("TARGET:SJ:123", clonedJourney.getNetexId());
    assertEquals("Journey 1", clonedJourney.getName());
    assertEquals(targetPattern, clonedJourney.getJourneyPattern());
    assertEquals("SJ001", clonedJourney.getPublicCode());
    assertEquals("NSR:Operator:1", clonedJourney.getOperatorRef());

    verify(idGenerator).addIdMapping("SOURCE:SJ:1", "TARGET:SJ:123");
    verify(referenceMapper).addMapping("SOURCE:SJ:1", "TARGET:SJ:123");
  }

  @Test
  public void testCloneDayType() {
    DayType sourceDayType = createSampleDayType();

    when(idGenerator.generateNetexId(any(DayType.class), eq(targetProvider))).thenReturn(
      "TARGET:DT:123"
    );

    DayType clonedDayType = entityCloner.cloneDayType(sourceDayType, targetProvider);

    assertNotNull(clonedDayType);
    assertEquals("TARGET:DT:123", clonedDayType.getNetexId());
    assertEquals("Weekdays", clonedDayType.getName());
    assertEquals(5, clonedDayType.getDaysOfWeek().size());
    assertTrue(clonedDayType.getDaysOfWeek().contains(DayOfWeek.MONDAY));
    assertTrue(clonedDayType.getDaysOfWeek().contains(DayOfWeek.FRIDAY));

    verify(idGenerator).addIdMapping("SOURCE:DT:1", "TARGET:DT:123");
    verify(referenceMapper).addMapping("SOURCE:DT:1", "TARGET:DT:123");
  }

  @Test
  public void testCloneDayTypeWithAssignments() {
    DayType sourceDayType = createSampleDayType();

    DayTypeAssignment assignment = new DayTypeAssignment();
    assignment.setDate(LocalDate.of(2025, 12, 25));
    assignment.setAvailable(false);
    assignment.setCreated(Instant.now());
    assignment.setChanged(Instant.now());
    sourceDayType.setDayTypeAssignments(Arrays.asList(assignment));

    when(idGenerator.generateNetexId(any(DayType.class), eq(targetProvider))).thenReturn(
      "TARGET:DT:456"
    );

    DayType clonedDayType = entityCloner.cloneDayType(sourceDayType, targetProvider);

    assertNotNull(clonedDayType);
    assertNotNull(clonedDayType.getDayTypeAssignments());
    assertEquals(1, clonedDayType.getDayTypeAssignments().size());

    DayTypeAssignment clonedAssignment = clonedDayType.getDayTypeAssignments().get(0);
    assertEquals(LocalDate.of(2025, 12, 25), clonedAssignment.getDate());
    assertEquals(Boolean.FALSE, clonedAssignment.getAvailable());
  }

  @Test
  public void testCloneNotice() {
    Notice sourceNotice = new Notice();
    sourceNotice.setNetexId("SOURCE:Notice:1");
    sourceNotice.setText("Test notice text");
    sourceNotice.setProvider(sourceProvider);
    sourceNotice.setCreated(java.time.Instant.now());
    sourceNotice.setChanged(java.time.Instant.now());

    Notice clonedNotice = entityCloner.cloneNotice(sourceNotice, targetProvider);

    assertNotNull(clonedNotice);
    assertNull(clonedNotice.getNetexId());
    assertEquals("Test notice text", clonedNotice.getText());
    assertEquals(targetProvider, clonedNotice.getProvider());
  }

  @Test
  public void testCloneBookingArrangement() {
    BookingArrangement sourceBooking = createSampleBookingArrangement();

    BookingArrangement clonedBooking = entityCloner.cloneBookingArrangement(
      sourceBooking,
      targetProvider
    );

    assertNotNull(clonedBooking);
    assertEquals("booking@example.com", clonedBooking.getBookingContact().getEmail());
    assertEquals(BookingAccessEnumeration.PUBLIC, clonedBooking.getBookingAccess());
    assertTrue(
      clonedBooking.getBookingMethods().contains(BookingMethodEnumeration.CALL_OFFICE)
    );
    assertEquals(PurchaseWhenEnumeration.UNTIL_PREVIOUS_DAY, clonedBooking.getBookWhen());
    assertEquals(
      PurchaseMomentEnumeration.ON_RESERVATION,
      clonedBooking.getBuyWhen().get(0)
    );
    assertEquals(LocalTime.of(16, 0), clonedBooking.getLatestBookingTime());
  }

  @Test
  public void testCloneDestinationDisplay() {
    DestinationDisplay sourceDisplay = new DestinationDisplay();
    sourceDisplay.setNetexId("SOURCE:DD:1");
    sourceDisplay.setFrontText("City Center");
    sourceDisplay.setProvider(sourceProvider);
    sourceDisplay.setCreated(java.time.Instant.now());
    sourceDisplay.setChanged(java.time.Instant.now());

    DestinationDisplay clonedDisplay = entityCloner.cloneDestinationDisplay(
      sourceDisplay,
      targetProvider
    );

    assertNotNull(clonedDisplay);
    assertEquals("City Center", clonedDisplay.getFrontText());
    assertEquals(targetProvider, clonedDisplay.getProvider());
  }

  @Test
  public void testCloneWithConflictResolutionRename() {
    entityCloner.setConflictResolution(ConflictResolutionStrategy.RENAME);

    FixedLine sourceLine = createSampleFixedLine();

    when(idGenerator.generateNetexId(any(Line.class), eq(targetProvider))).thenReturn(
      "TARGET:Line:789"
    );
    when(
      idGenerator.generateUniqueNameWithConflictResolution(
        eq("Test Fixed Line"),
        eq("Line"),
        eq(targetProvider),
        eq(ConflictResolutionStrategy.RENAME)
      )
    ).thenReturn("Test Fixed Line_migrated_123456");

    FixedLine clonedLine = entityCloner.cloneLine(
      sourceLine,
      targetProvider,
      targetNetwork
    );

    assertEquals("Test Fixed Line_migrated_123456", clonedLine.getName());
  }

  @Test
  public void testCloneServiceJourneyWithoutDayTypes() {
    entityCloner.setIncludeDayTypes(false);

    JourneyPattern targetPattern = new JourneyPattern();
    targetPattern.setProvider(targetProvider);

    ServiceJourney sourceJourney = createSampleServiceJourney();
    DayType dayType = createSampleDayType();
    sourceJourney.updateDayTypes(Arrays.asList(dayType));

    when(
      idGenerator.generateNetexId(any(ServiceJourney.class), eq(targetProvider))
    ).thenReturn("TARGET:SJ:123");
    when(
      idGenerator.generateUniqueNameWithConflictResolution(
        anyString(),
        anyString(),
        any(),
        any()
      )
    ).thenReturn("Journey 1");

    ServiceJourney clonedJourney = entityCloner.cloneServiceJourney(
      sourceJourney,
      targetPattern
    );

    assertNotNull(clonedJourney);
    assertTrue(clonedJourney.getDayTypes().isEmpty());
  }

  @Test
  public void testCloneServiceJourneyWithExistingDayType() {
    entityCloner.setIncludeDayTypes(true);

    JourneyPattern targetPattern = new JourneyPattern();
    targetPattern.setProvider(targetProvider);

    ServiceJourney sourceJourney = createSampleServiceJourney();
    DayType sourceDayType = createSampleDayType();
    sourceJourney.updateDayTypes(Arrays.asList(sourceDayType));

    DayType existingDayType = new DayType();
    existingDayType.setNetexId("TARGET:DT:EXISTING");
    existingDayType.setName("Weekdays");

    when(referenceMapper.findExistingDayType(sourceDayType)).thenReturn(existingDayType);
    when(
      idGenerator.generateNetexId(any(ServiceJourney.class), eq(targetProvider))
    ).thenReturn("TARGET:SJ:123");
    when(
      idGenerator.generateUniqueNameWithConflictResolution(
        anyString(),
        anyString(),
        any(),
        any()
      )
    ).thenReturn("Journey 1");

    ServiceJourney clonedJourney = entityCloner.cloneServiceJourney(
      sourceJourney,
      targetPattern
    );

    assertNotNull(clonedJourney);
    assertEquals(1, clonedJourney.getDayTypes().size());
    assertTrue(clonedJourney.getDayTypes().contains(existingDayType));
  }

  @Test
  public void testClearMappings() {
    entityCloner.clearMappings();

    verify(idGenerator).clearMappings();
    verify(referenceMapper).clearMappings();
  }

  @Test
  public void testCloneLineWithJourneyPatternsAndServiceJourneys() {
    FixedLine sourceLine = createSampleFixedLine();
    JourneyPattern sourcePattern = createSampleJourneyPattern();
    ServiceJourney sourceJourney = createSampleServiceJourney();
    sourcePattern.setServiceJourneys(Arrays.asList(sourceJourney));
    sourceLine.setJourneyPatterns(Arrays.asList(sourcePattern));

    when(idGenerator.generateNetexId(any(), eq(targetProvider))).thenReturn(
      "TARGET:Line:1",
      "TARGET:JP:1",
      "TARGET:SJ:1"
    );
    when(
      idGenerator.generateUniqueNameWithConflictResolution(
        anyString(),
        anyString(),
        eq(targetProvider),
        any()
      )
    ).thenAnswer(invocation -> invocation.getArgument(0));

    FixedLine clonedLine = entityCloner.cloneLine(
      sourceLine,
      targetProvider,
      targetNetwork
    );

    assertNotNull(clonedLine);
    assertNotNull(clonedLine.getJourneyPatterns());
    assertEquals(1, clonedLine.getJourneyPatterns().size());

    JourneyPattern clonedPattern = clonedLine.getJourneyPatterns().get(0);
    assertNotNull(clonedPattern.getServiceJourneys());
    assertEquals(1, clonedPattern.getServiceJourneys().size());
  }

  private FixedLine createSampleFixedLine() {
    FixedLine line = new FixedLine();
    line.setNetexId("SOURCE:Line:1");
    line.setName("Test Fixed Line");
    line.setProvider(sourceProvider);
    line.setNetwork(sourceNetwork);
    line.setPublicCode("FL1");
    line.setTransportMode(VehicleModeEnumeration.BUS);
    line.setOperatorRef("NSR:Operator:1");
    line.setCreated(java.time.Instant.now());
    line.setChanged(java.time.Instant.now());
    return line;
  }

  private FlexibleLine createSampleFlexibleLine() {
    FlexibleLine line = new FlexibleLine();
    line.setNetexId("SOURCE:Line:2");
    line.setName("Test Flexible Line");
    line.setProvider(sourceProvider);
    line.setNetwork(sourceNetwork);
    line.setFlexibleLineType(FlexibleLineTypeEnumeration.FLEXIBLE_AREAS_ONLY);
    line.setBookingArrangement(createSampleBookingArrangement());
    line.setCreated(java.time.Instant.now());
    line.setChanged(java.time.Instant.now());
    return line;
  }

  private JourneyPattern createSampleJourneyPattern() {
    JourneyPattern pattern = new JourneyPattern();
    pattern.setNetexId("SOURCE:JP:1");
    pattern.setName("Pattern 1");
    pattern.setProvider(sourceProvider);
    pattern.setDirectionType(DirectionTypeEnumeration.INBOUND);
    pattern.setCreated(java.time.Instant.now());
    pattern.setChanged(java.time.Instant.now());
    return pattern;
  }

  private ServiceJourney createSampleServiceJourney() {
    ServiceJourney journey = new ServiceJourney();
    journey.setNetexId("SOURCE:SJ:1");
    journey.setName("Journey 1");
    journey.setProvider(sourceProvider);
    journey.setPublicCode("SJ001");
    journey.setOperatorRef("NSR:Operator:1");
    journey.setCreated(java.time.Instant.now());
    journey.setChanged(java.time.Instant.now());
    return journey;
  }

  private DayType createSampleDayType() {
    DayType dayType = new DayType();
    dayType.setNetexId("SOURCE:DT:1");
    dayType.setName("Weekdays");
    dayType.setProvider(sourceProvider);
    dayType.setDaysOfWeek(
      Arrays.asList(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
      )
    );
    dayType.setCreated(java.time.Instant.now());
    dayType.setChanged(java.time.Instant.now());
    return dayType;
  }

  private BookingArrangement createSampleBookingArrangement() {
    BookingArrangement booking = new BookingArrangement();
    Contact contact = new Contact();
    contact.setEmail("booking@example.com");
    contact.setCreated(java.time.Instant.now());
    contact.setChanged(java.time.Instant.now());
    booking.setBookingContact(contact);
    booking.setBookingAccess(BookingAccessEnumeration.PUBLIC);
    booking.setBookingMethods(
      Arrays.asList(BookingMethodEnumeration.CALL_OFFICE, BookingMethodEnumeration.ONLINE)
    );
    booking.setBookWhen(PurchaseWhenEnumeration.UNTIL_PREVIOUS_DAY);
    booking.setBuyWhen(Arrays.asList(PurchaseMomentEnumeration.ON_RESERVATION));
    booking.setLatestBookingTime(LocalTime.of(16, 0));
    booking.setCreated(java.time.Instant.now());
    booking.setChanged(java.time.Instant.now());
    return booking;
  }
}
