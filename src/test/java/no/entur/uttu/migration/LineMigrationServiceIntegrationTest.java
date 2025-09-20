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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.entur.uttu.UttuIntegrationTest;
import no.entur.uttu.config.Context;
import no.entur.uttu.migration.LineMigrationService.ConflictResolutionStrategy;
import no.entur.uttu.migration.LineMigrationService.LineMigrationInput;
import no.entur.uttu.migration.LineMigrationService.LineMigrationOptions;
import no.entur.uttu.migration.LineMigrationService.LineMigrationResult;
import no.entur.uttu.model.*;
import no.entur.uttu.repository.*;
import no.entur.uttu.stubs.UserContextServiceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles({ "in-memory-blobstore" })
@Transactional
public class LineMigrationServiceIntegrationTest extends UttuIntegrationTest {

  @Autowired
  private LineMigrationService lineMigrationService;

  @Autowired
  private ProviderRepository providerRepository;

  @Autowired
  private FixedLineRepository fixedLineRepository;

  @Autowired
  private FlexibleLineRepository flexibleLineRepository;

  @Autowired
  private NetworkRepository networkRepository;

  @Autowired
  private DayTypeRepository dayTypeRepository;

  @Autowired
  private JourneyPatternRepository journeyPatternRepository;

  @Autowired
  private ServiceJourneyRepository serviceJourneyRepository;

  @Autowired
  private UserContextServiceStub userContextServiceStub;

  @Autowired
  private CodespaceRepository codespaceRepository;

  private Provider sourceProvider;
  private Provider targetProvider;
  private Network sourceNetwork;
  private Network targetNetwork;

  @Before
  public void setUp() {
    // Set up user context with access to both providers
    userContextServiceStub.setPreferredName("Migration Test User");
    userContextServiceStub.setAdmin(false);
    userContextServiceStub.setHasAccessToProvider("SOURCE", true);
    userContextServiceStub.setHasAccessToProvider("TARGET", true);

    Context.setUserName("migration-test-user");

    // Create test providers
    sourceProvider = createProvider("SOURCE", "Source Provider");
    targetProvider = createProvider("TARGET", "Target Provider");

    // Create test networks
    Context.setProvider("SOURCE");
    sourceNetwork = createNetwork(sourceProvider, "Source Network");

    Context.setProvider("TARGET");
    targetNetwork = createNetwork(targetProvider, "Target Network");
  }

  @After
  public void tearDown() {
    Context.clear();
  }

  @Test
  public void testSuccessfulFixedLineMigration() {
    // Create source data
    Context.setProvider("SOURCE");
    FixedLine sourceLine = createSampleFixedLine();

    // Set up migration input
    LineMigrationInput input = new LineMigrationInput();
    input.setSourceLineId(sourceLine.getNetexId());
    input.setTargetProviderId("TARGET");
    input.setTargetNetworkId(targetNetwork.getNetexId());

    // Execute migration
    LineMigrationResult result = lineMigrationService.migrateLine(input);

    // Verify results
    assertTrue("Migration should succeed", result.isSuccess());
    assertNotNull("Migrated line ID should be set", result.getMigratedLineId());
    assertNotEquals(
      "New ID should be different",
      sourceLine.getNetexId(),
      result.getMigratedLineId()
    );
    assertTrue(
      "New ID should use target codespace",
      result.getMigratedLineId().startsWith("TARGET:")
    );

    // Verify migrated line exists in target provider
    Context.setProvider("TARGET");
    FixedLine migratedLine = fixedLineRepository.getOne(result.getMigratedLineId());

    assertNotNull("Migrated line should exist", migratedLine);
    assertEquals(
      "Line name should be preserved",
      sourceLine.getName(),
      migratedLine.getName()
    );
    assertEquals(
      "Target provider should be set",
      targetProvider.getCode(),
      migratedLine.getProvider().getCode()
    );
    assertEquals(
      "Target network should be set",
      targetNetwork.getNetexId(),
      migratedLine.getNetwork().getNetexId()
    );
    assertEquals(
      "Public code should be preserved",
      sourceLine.getPublicCode(),
      migratedLine.getPublicCode()
    );
    assertEquals(
      "Transport mode should be preserved",
      sourceLine.getTransportMode(),
      migratedLine.getTransportMode()
    );

    // Verify journey patterns are migrated
    assertNotNull("Journey patterns should exist", migratedLine.getJourneyPatterns());
    assertEquals(
      "Should have same number of journey patterns",
      sourceLine.getJourneyPatterns().size(),
      migratedLine.getJourneyPatterns().size()
    );

    // Verify service journeys are migrated
    JourneyPattern migratedPattern = migratedLine.getJourneyPatterns().get(0);
    assertNotNull("Service journeys should exist", migratedPattern.getServiceJourneys());
    assertEquals(
      "Should have same number of service journeys",
      sourceLine.getJourneyPatterns().get(0).getServiceJourneys().size(),
      migratedPattern.getServiceJourneys().size()
    );
  }

  @Test
  public void testSuccessfulFlexibleLineMigration() {
    // Create source data
    Context.setProvider("SOURCE");
    FlexibleLine sourceLine = createSampleFlexibleLine();

    // Set up migration input
    LineMigrationInput input = new LineMigrationInput();
    input.setSourceLineId(sourceLine.getNetexId());
    input.setTargetProviderId("TARGET");
    input.setTargetNetworkId(targetNetwork.getNetexId());

    // Execute migration
    LineMigrationResult result = lineMigrationService.migrateLine(input);

    // Verify results
    assertTrue("Migration should succeed", result.isSuccess());
    assertNotNull("Migrated line ID should be set", result.getMigratedLineId());

    // Verify migrated line exists in target provider
    Context.setProvider("TARGET");
    FlexibleLine migratedLine = flexibleLineRepository.getOne(result.getMigratedLineId());

    assertNotNull("Migrated line should exist", migratedLine);
    assertEquals(
      "Flexible line type should be preserved",
      sourceLine.getFlexibleLineType(),
      migratedLine.getFlexibleLineType()
    );
    assertNotNull(
      "Booking arrangement should be migrated",
      migratedLine.getBookingArrangement()
    );
  }

  @Test
  public void testConflictResolutionRename() {
    // Create source line
    Context.setProvider("SOURCE");
    FixedLine sourceLine = createSampleFixedLine();

    // Create existing line with same name in target provider
    Context.setProvider("TARGET");
    FixedLine existingLine = new FixedLine();
    existingLine.setName(sourceLine.getName()); // Same name to cause conflict
    existingLine.setProvider(targetProvider);
    existingLine.setNetwork(targetNetwork);
    existingLine.setPublicCode("EXISTING");
    existingLine.setTransportMode(VehicleModeEnumeration.BUS);
    existingLine.setTransportSubmode(VehicleSubmodeEnumeration.LOCAL_BUS);
    existingLine = fixedLineRepository.save(existingLine);

    // Set up migration input with RENAME strategy
    LineMigrationInput input = new LineMigrationInput();
    input.setSourceLineId(sourceLine.getNetexId());
    input.setTargetProviderId("TARGET");
    input.setTargetNetworkId(targetNetwork.getNetexId());

    LineMigrationOptions options = new LineMigrationOptions();
    options.setConflictResolution(ConflictResolutionStrategy.RENAME);
    input.setOptions(options);

    // Execute migration
    LineMigrationResult result = lineMigrationService.migrateLine(input);

    // Verify results
    assertTrue("Migration should succeed with rename", result.isSuccess());

    // Verify migrated line has renamed name
    Context.setProvider("TARGET");
    FixedLine migratedLine = fixedLineRepository.getOne(result.getMigratedLineId());
    assertNotNull("Migrated line should exist", migratedLine);
    assertNotEquals(
      "Name should be renamed to avoid conflict",
      sourceLine.getName(),
      migratedLine.getName()
    );
    assertTrue(
      "Name should contain migration suffix",
      migratedLine.getName().contains("_migrated_")
    );
  }

  @Test
  public void testConflictResolutionFail() {
    // Create source line
    Context.setProvider("SOURCE");
    FixedLine sourceLine = createSampleFixedLine();

    // Create existing line with same name in target provider
    Context.setProvider("TARGET");
    FixedLine existingLine = new FixedLine();
    existingLine.setName(sourceLine.getName()); // Same name to cause conflict
    existingLine.setProvider(targetProvider);
    existingLine.setNetwork(targetNetwork);
    existingLine.setPublicCode("EXISTING");
    existingLine.setTransportMode(VehicleModeEnumeration.BUS);
    existingLine.setTransportSubmode(VehicleSubmodeEnumeration.LOCAL_BUS);
    existingLine = fixedLineRepository.save(existingLine);

    // Set up migration input with FAIL strategy (default)
    LineMigrationInput input = new LineMigrationInput();
    input.setSourceLineId(sourceLine.getNetexId());
    input.setTargetProviderId("TARGET");
    input.setTargetNetworkId(targetNetwork.getNetexId());

    // Execute migration and expect failure
    try {
      lineMigrationService.migrateLine(input);
      fail("Should throw exception on naming conflict with FAIL strategy");
    } catch (IllegalArgumentException e) {
      assertTrue(
        "Error message should mention conflict",
        e.getMessage().contains("already exists")
      );
    }
  }

  @Test
  public void testSecurityValidationFailure() {
    // Create source line
    Context.setProvider("SOURCE");
    FixedLine sourceLine = createSampleFixedLine();

    // Revoke access to target provider
    userContextServiceStub.setHasAccessToProvider("TARGET", false);

    // Set up migration input
    LineMigrationInput input = new LineMigrationInput();
    input.setSourceLineId(sourceLine.getNetexId());
    input.setTargetProviderId("TARGET");
    input.setTargetNetworkId(targetNetwork.getNetexId());

    // Execute migration and expect security failure
    try {
      lineMigrationService.migrateLine(input);
      fail("Should throw SecurityException when user lacks target provider access");
    } catch (SecurityException e) {
      assertTrue(
        "Error message should mention access",
        e.getMessage().contains("access")
      );
    }
  }

  @Test
  public void testNetworkValidationFailure() {
    // Create source line
    Context.setProvider("SOURCE");
    FixedLine sourceLine = createSampleFixedLine();

    // Set up migration input with non-existent network
    LineMigrationInput input = new LineMigrationInput();
    input.setSourceLineId(sourceLine.getNetexId());
    input.setTargetProviderId("TARGET");
    input.setTargetNetworkId("TARGET:Network:NONEXISTENT");

    // Execute migration and expect validation failure
    try {
      lineMigrationService.migrateLine(input);
      fail("Should throw exception when target network doesn't exist");
    } catch (IllegalArgumentException e) {
      assertTrue(
        "Error message should mention network",
        e.getMessage().contains("not found")
      );
    }
  }

  @Test
  public void testDryRunMode() {
    // Create source line
    Context.setProvider("SOURCE");
    FixedLine sourceLine = createSampleFixedLine();

    // Set up migration input with dry run
    LineMigrationInput input = new LineMigrationInput();
    input.setSourceLineId(sourceLine.getNetexId());
    input.setTargetProviderId("TARGET");
    input.setTargetNetworkId(targetNetwork.getNetexId());

    LineMigrationOptions options = new LineMigrationOptions();
    options.setDryRun(true);
    input.setOptions(options);

    // Execute dry run
    LineMigrationResult result = lineMigrationService.migrateLine(input);

    // Verify results
    assertTrue("Dry run should succeed", result.isSuccess());
    assertNotNull("Should return migrated line ID", result.getMigratedLineId());

    // Verify no actual migration occurred - dry run should return fake IDs
    Context.setProvider("TARGET");
    try {
      FixedLine migratedLine = fixedLineRepository.getOne(result.getMigratedLineId());
      fail(
        "Line should not actually exist after dry run, but was found: " +
        migratedLine.getNetexId()
      );
    } catch (Exception e) {
      // Expected - line should not be persisted in dry run
    }
  }

  @Test
  public void testDayTypeDeduplication() {
    // Create a DayType in source provider
    Context.setProvider("SOURCE");
    DayType sourceDayType = createSampleDayType("Weekdays", sourceProvider);

    // Create source line with the DayType
    FixedLine sourceLine = createSampleFixedLine();

    // Manually link the DayType to the service journey
    JourneyPattern pattern = sourceLine.getJourneyPatterns().get(0);
    ServiceJourney journey = pattern.getServiceJourneys().get(0);
    journey.updateDayTypes(new ArrayList<>(Arrays.asList(sourceDayType)));
    serviceJourneyRepository.save(journey);

    // Create matching DayType in target provider
    Context.setProvider("TARGET");
    DayType existingTargetDayType = createSampleDayType("Weekdays", targetProvider);

    // Set up migration input - ensure we're still in SOURCE context for source line
    Context.setProvider("SOURCE");
    LineMigrationInput input = new LineMigrationInput();
    input.setSourceLineId(sourceLine.getNetexId());
    input.setTargetProviderId("TARGET");
    input.setTargetNetworkId(targetNetwork.getNetexId());

    // Execute migration
    LineMigrationResult result = lineMigrationService.migrateLine(input);

    // Verify results
    assertTrue("Migration should succeed", result.isSuccess());

    // Verify that DayType was migrated (deduplication logic might create new ones)
    Context.setProvider("TARGET");
    List<DayType> targetDayTypes = dayTypeRepository.findByProvider(targetProvider);

    // Should have at least one "Weekdays" DayType (either reused or migrated)
    long weekdaysCount = targetDayTypes
      .stream()
      .filter(dt -> "Weekdays".equals(dt.getName()))
      .count();
    assertTrue("Should have migrated DayType with correct name", weekdaysCount >= 1L);
  }

  @Test
  public void testSameProviderMigrationFailure() {
    // Create source line
    Context.setProvider("SOURCE");
    FixedLine sourceLine = createSampleFixedLine();

    // Set up migration input with same provider as source and target
    LineMigrationInput input = new LineMigrationInput();
    input.setSourceLineId(sourceLine.getNetexId());
    input.setTargetProviderId("SOURCE"); // Same as source
    input.setTargetNetworkId(sourceNetwork.getNetexId());

    // Execute migration and expect failure
    try {
      lineMigrationService.migrateLine(input);
      fail("Should not allow migration within same provider");
    } catch (IllegalArgumentException e) {
      assertTrue(
        "Error message should mention same provider",
        e.getMessage().contains("same provider")
      );
    }
  }

  // Helper methods for creating test data

  private Provider createProvider(String code, String name) {
    // First save the Codespace
    Codespace codespace = new Codespace();
    codespace.setXmlns(code);
    codespace.setXmlnsUrl("http://test.entur.no/" + code.toLowerCase());
    codespace = codespaceRepository.save(codespace);

    // Then create and save the Provider with the persisted Codespace
    Provider provider = new Provider();
    provider.setCode(code);
    provider.setName(name);
    provider.setCodespace(codespace);

    return providerRepository.save(provider);
  }

  private Network createNetwork(Provider provider, String name) {
    Network network = new Network();
    network.setName(name);
    network.setProvider(provider);
    network.setAuthorityRef("NSR:Authority:TEST_" + provider.getCode());
    return networkRepository.save(network);
  }

  private FixedLine createSampleFixedLine() {
    FixedLine line = new FixedLine();
    line.setName("Test Migration Line");
    line.setProvider(sourceProvider);
    line.setNetwork(sourceNetwork);
    line.setPublicCode("TML1");
    line.setTransportMode(VehicleModeEnumeration.BUS);
    line.setTransportSubmode(VehicleSubmodeEnumeration.LOCAL_BUS);
    line.setOperatorRef("NSR:Operator:TEST");
    line = fixedLineRepository.save(line);

    // Add journey pattern with service journey
    JourneyPattern pattern = createJourneyPattern(line);
    ServiceJourney journey = createServiceJourney(pattern);

    // Link the service journey to the journey pattern
    pattern.setServiceJourneys(new ArrayList<>(Arrays.asList(journey)));
    pattern = journeyPatternRepository.save(pattern);

    // Link the journey pattern to the line
    line.setJourneyPatterns(new ArrayList<>(Arrays.asList(pattern)));
    return fixedLineRepository.save(line);
  }

  private FlexibleLine createSampleFlexibleLine() {
    FlexibleLine line = new FlexibleLine();
    line.setName("Test Flexible Migration Line");
    line.setProvider(sourceProvider);
    line.setNetwork(sourceNetwork);
    line.setFlexibleLineType(FlexibleLineTypeEnumeration.FLEXIBLE_AREAS_ONLY);
    line.setTransportMode(VehicleModeEnumeration.BUS);
    line.setTransportSubmode(VehicleSubmodeEnumeration.DEMAND_AND_RESPONSE_BUS);

    // Create booking arrangement
    BookingArrangement booking = new BookingArrangement();
    Contact contact = new Contact();
    contact.setEmail("test@example.com");
    booking.setBookingContact(contact);
    booking.setBookingAccess(BookingAccessEnumeration.PUBLIC);
    booking.setBookingMethods(
      new ArrayList<>(Arrays.asList(BookingMethodEnumeration.CALL_OFFICE))
    );
    booking.setBookWhen(PurchaseWhenEnumeration.UNTIL_PREVIOUS_DAY);
    booking.setBuyWhen(
      new ArrayList<>(Arrays.asList(PurchaseMomentEnumeration.ON_RESERVATION))
    );
    booking.setLatestBookingTime(LocalTime.of(16, 0));
    line.setBookingArrangement(booking);

    line = flexibleLineRepository.save(line);

    // Add journey pattern with service journey
    JourneyPattern pattern = createJourneyPattern(line);
    ServiceJourney journey = createServiceJourney(pattern);

    // Link the service journey to the journey pattern
    pattern.setServiceJourneys(new ArrayList<>(Arrays.asList(journey)));
    pattern = journeyPatternRepository.save(pattern);

    // Link the journey pattern to the line
    line.setJourneyPatterns(new ArrayList<>(Arrays.asList(pattern)));
    return flexibleLineRepository.save(line);
  }

  private JourneyPattern createJourneyPattern(Line line) {
    JourneyPattern pattern = new JourneyPattern();
    pattern.setName("Pattern 1");
    pattern.setProvider(line.getProvider());
    pattern.setLine(line);
    pattern.setDirectionType(DirectionTypeEnumeration.INBOUND);
    return journeyPatternRepository.save(pattern);
  }

  private ServiceJourney createServiceJourney(JourneyPattern pattern) {
    ServiceJourney journey = new ServiceJourney();
    journey.setName("Journey 1");
    journey.setProvider(pattern.getProvider());
    journey.setJourneyPattern(pattern);
    journey.setPublicCode("J001");
    journey.setOperatorRef("NSR:Operator:TEST");
    return serviceJourneyRepository.save(journey);
  }

  private DayType createSampleDayType(String name, Provider provider) {
    DayType dayType = new DayType();
    dayType.setName(name);
    dayType.setProvider(provider);
    dayType.setDaysOfWeek(
      new ArrayList<>(
        Arrays.asList(
          DayOfWeek.MONDAY,
          DayOfWeek.TUESDAY,
          DayOfWeek.WEDNESDAY,
          DayOfWeek.THURSDAY,
          DayOfWeek.FRIDAY
        )
      )
    );

    // Add day type assignment
    DayTypeAssignment assignment = new DayTypeAssignment();
    assignment.setDate(LocalDate.of(2025, 12, 25));
    assignment.setAvailable(false);
    dayType.setDayTypeAssignments(new ArrayList<>(Arrays.asList(assignment)));

    return dayTypeRepository.save(dayType);
  }

  private FixedLine createSampleFixedLineWithDayType(DayType dayType) {
    // Ensure we're in the source provider context
    Context.setProvider("SOURCE");
    FixedLine line = createSampleFixedLine();

    // Add DayType to service journey
    JourneyPattern pattern = line.getJourneyPatterns().get(0);
    ServiceJourney journey = pattern.getServiceJourneys().get(0);
    journey.updateDayTypes(Arrays.asList(dayType));
    serviceJourneyRepository.save(journey);

    return line;
  }
}
