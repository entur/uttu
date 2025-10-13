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

package no.entur.uttu.export.netex.producer.line;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import no.entur.uttu.config.AdditionalCodespacesConfig;
import no.entur.uttu.config.ExportTimeZone;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Codespace;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DayTypeAssignment;
import no.entur.uttu.model.OperatingPeriod;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.DatedServiceJourney;

class DatedServiceJourneyProducerTest {

  private DatedServiceJourneyProducer producer;
  private NetexObjectFactory objectFactory;
  private NetexExportContext context;
  private Clock clock;

  // Test date is Jan 1, 2024 (a Monday)
  // Cutoff will be Dec 31, 2023
  private static final LocalDate TEST_START_DATE = LocalDate.of(2024, 1, 1);

  @BeforeEach
  void setUp() {
    // Fix the clock to Jan 1, 2024 00:00:00 UTC
    clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));

    objectFactory = new NetexObjectFactory(
      new DateUtils(),
      new ExportTimeZone(),
      new AdditionalCodespacesConfig()
    );
    producer = new DatedServiceJourneyProducer(objectFactory, clock);

    Export export = new Export();
    export.setIncludeDatedServiceJourneys(true);
    Provider provider = new Provider();
    provider.setCode("TEST");
    Codespace codespace = new Codespace();
    codespace.setXmlns("TST");
    provider.setCodespace(codespace);
    export.setProvider(provider);
    context = new NetexExportContext(export);
  }

  @Test
  void produce_shouldOnlyGenerateMatchingDaysOfWeek_whenDaysOfWeekSet() {
    ServiceJourney serviceJourney = createServiceJourney();

    DayType dayType = new DayType();
    dayType.setNetexId("TST:DayType:3");
    dayType.setVersion(1L);

    // Set daysOfWeek to only Monday and Wednesday
    dayType.getDaysOfWeek().add(DayOfWeek.MONDAY);
    dayType.getDaysOfWeek().add(DayOfWeek.WEDNESDAY);

    // Create operating period: Jan 1-7, 2024
    // Jan 1 = Monday, Jan 3 = Wednesday, Jan 8 = Monday (next week)
    DayTypeAssignment assignment = createPeriodAssignment(
      TEST_START_DATE,
      TEST_START_DATE.plusDays(6)
    );
    assignment.setAvailable(true);

    dayType.getDayTypeAssignments().add(assignment);
    serviceJourney.updateDayTypes(List.of(dayType));

    // Act
    List<DatedServiceJourney> result = producer.produce(serviceJourney, context);

    // Assert - Expected: Should generate only 2 dates (Mon Jan 1, Wed Jan 3)
    assertThat(result)
      .as("Should only generate DatedServiceJourneys for days matching daysOfWeek")
      .hasSize(2);

    Set<LocalDate> operatingDays = context.getOperatingDays();
    assertThat(operatingDays).hasSize(2);

    // Expected: Only Monday and Wednesday should be included
    assertThat(operatingDays).containsExactlyInAnyOrder(
      TEST_START_DATE, // Monday Jan 1
      TEST_START_DATE.plusDays(2) // Wednesday Jan 3
    );

    // Verify these dates are NOT included
    assertThat(operatingDays).doesNotContain(
      TEST_START_DATE.plusDays(1), // Tuesday
      TEST_START_DATE.plusDays(3), // Thursday
      TEST_START_DATE.plusDays(4), // Friday
      TEST_START_DATE.plusDays(5), // Saturday
      TEST_START_DATE.plusDays(6) // Sunday
    );
  }

  @Test
  void produce_shouldFilterByDaysOfWeek_withMultipleWeeksCovered() {
    // Test across multiple weeks to ensure pattern is consistent
    ServiceJourney serviceJourney = createServiceJourney();

    DayType dayType = new DayType();
    dayType.setNetexId("TST:DayType:4");
    dayType.setVersion(1L);

    // Only weekends (Saturday and Sunday)
    dayType.getDaysOfWeek().add(DayOfWeek.SATURDAY);
    dayType.getDaysOfWeek().add(DayOfWeek.SUNDAY);

    // Create operating period: Jan 1-14, 2024 (2 weeks)
    // Jan 1 = Mon, Jan 6 = Sat, Jan 7 = Sun, Jan 13 = Sat, Jan 14 = Sun
    DayTypeAssignment assignment = createPeriodAssignment(
      TEST_START_DATE,
      TEST_START_DATE.plusDays(13)
    );
    assignment.setAvailable(true);

    dayType.getDayTypeAssignments().add(assignment);
    serviceJourney.updateDayTypes(List.of(dayType));

    // Act
    List<DatedServiceJourney> result = producer.produce(serviceJourney, context);

    // Assert - Expected: Should generate only 4 dates (2 Saturdays + 2 Sundays)
    assertThat(result)
      .as("Should only generate DatedServiceJourneys for weekend days")
      .hasSize(4);

    Set<LocalDate> operatingDays = context.getOperatingDays();
    assertThat(operatingDays).containsExactlyInAnyOrder(
      TEST_START_DATE.plusDays(5), // Saturday Jan 6
      TEST_START_DATE.plusDays(6), // Sunday Jan 7
      TEST_START_DATE.plusDays(12), // Saturday Jan 13
      TEST_START_DATE.plusDays(13) // Sunday Jan 14
    );
  }

  @Test
  void produce_shouldHandleExplicitDates_withoutDaysOfWeekFiltering() {
    // Explicit dates should not be filtered by daysOfWeek
    ServiceJourney serviceJourney = createServiceJourney();

    DayType dayType = new DayType();
    dayType.setNetexId("TST:DayType:5");
    dayType.setVersion(1L);

    // Set daysOfWeek to only Wednesday (but explicit date is Monday)
    dayType.getDaysOfWeek().add(DayOfWeek.WEDNESDAY);

    // Create an explicit date assignment for Monday Jan 1, 2024
    DayTypeAssignment explicitDate = new DayTypeAssignment();
    explicitDate.setDate(TEST_START_DATE); // Monday
    explicitDate.setAvailable(true);

    dayType.getDayTypeAssignments().add(explicitDate);
    serviceJourney.updateDayTypes(List.of(dayType));

    // Act
    List<DatedServiceJourney> result = producer.produce(serviceJourney, context);

    // Assert - Explicit dates should always be included regardless of daysOfWeek
    assertThat(result).hasSize(1);
    assertThat(context.getOperatingDays()).containsExactly(TEST_START_DATE);
  }

  @Test
  void produce_shouldCombinePeriodAndExplicitDates_withDaysOfWeekFiltering() {
    // Test combination of operating period with daysOfWeek filtering and explicit dates
    ServiceJourney serviceJourney = createServiceJourney();

    DayType dayType = new DayType();
    dayType.setNetexId("TST:DayType:6");
    dayType.setVersion(1L);

    // Set daysOfWeek to only Monday
    dayType.getDaysOfWeek().add(DayOfWeek.MONDAY);

    // Add operating period (should only generate Mondays)
    // Jan 1-14, 2024 contains Mondays on Jan 1 and Jan 8
    DayTypeAssignment periodAssignment = createPeriodAssignment(
      TEST_START_DATE,
      TEST_START_DATE.plusDays(13)
    );
    periodAssignment.setAvailable(true);
    dayType.getDayTypeAssignments().add(periodAssignment);

    // Add explicit date for a Friday (should be included)
    DayTypeAssignment explicitDate = new DayTypeAssignment();
    explicitDate.setDate(TEST_START_DATE.plusDays(4)); // Friday Jan 5
    explicitDate.setAvailable(true);
    dayType.getDayTypeAssignments().add(explicitDate);

    serviceJourney.updateDayTypes(List.of(dayType));

    // Act
    List<DatedServiceJourney> result = producer.produce(serviceJourney, context);

    // Assert - Expected: 2 Mondays (Jan 1, 8) + 1 explicit Friday (Jan 5) = 3 dates
    assertThat(result).hasSize(3);
    assertThat(context.getOperatingDays()).containsExactlyInAnyOrder(
      TEST_START_DATE, // Monday Jan 1
      TEST_START_DATE.plusDays(4), // Friday Jan 5 (explicit)
      TEST_START_DATE.plusDays(7) // Monday Jan 8
    );
  }

  private ServiceJourney createServiceJourney() {
    ServiceJourney sj = new ServiceJourney();
    sj.setNetexId("TST:ServiceJourney:1");
    sj.setVersion(1L);
    return sj;
  }

  private DayTypeAssignment createPeriodAssignment(LocalDate from, LocalDate to) {
    DayTypeAssignment assignment = new DayTypeAssignment();
    OperatingPeriod period = new OperatingPeriod();
    period.setFromDate(from);
    period.setToDate(to);
    assignment.setOperatingPeriod(period);
    return assignment;
  }
}
