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

import java.time.DayOfWeek;
import java.time.LocalDate;
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

  // Use fixed future dates for testing (well beyond the CUTOFF date)
  private static final LocalDate TEST_START_DATE = LocalDate.of(2026, 1, 1); // Wednesday
  private static final LocalDate TEST_END_DATE = LocalDate.of(2026, 1, 31);

  @BeforeEach
  void setUp() {
    objectFactory = new NetexObjectFactory(
      new DateUtils(),
      new ExportTimeZone(),
      new AdditionalCodespacesConfig()
    );
    producer = new DatedServiceJourneyProducer(objectFactory);

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

    // Create operating period: Jan 1-7, 2026
    // Jan 1 = Thursday, Jan 5 = Monday, Jan 7 = Wednesday
    DayTypeAssignment assignment = createPeriodAssignment(
      TEST_START_DATE,
      TEST_START_DATE.plusDays(6)
    );
    assignment.setAvailable(true);

    dayType.getDayTypeAssignments().add(assignment);
    serviceJourney.updateDayTypes(List.of(dayType));

    // Act
    List<DatedServiceJourney> result = producer.produce(serviceJourney, context);

    // Assert - Expected: Should generate only 2 dates (Mon Jan 5, Wed Jan 7)
    assertThat(result)
      .as("Should only generate DatedServiceJourneys for days matching daysOfWeek")
      .hasSize(2);

    Set<LocalDate> operatingDays = context.getOperatingDays();
    assertThat(operatingDays).hasSize(2);

    // Expected: Only Monday and Wednesday should be included
    assertThat(operatingDays).containsExactlyInAnyOrder(
      TEST_START_DATE.plusDays(4), // Monday Jan 5
      TEST_START_DATE.plusDays(6) // Wednesday Jan 7
    );

    // Verify these dates are NOT included
    assertThat(operatingDays).doesNotContain(
      TEST_START_DATE, // Thursday
      TEST_START_DATE.plusDays(1), // Friday
      TEST_START_DATE.plusDays(2), // Saturday
      TEST_START_DATE.plusDays(3), // Sunday
      TEST_START_DATE.plusDays(5) // Tuesday
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

    // Create operating period: Jan 1-14, 2026 (2 weeks, should have 4 weekend days)
    // Jan 3 = Sat, Jan 4 = Sun, Jan 10 = Sat, Jan 11 = Sun
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
      TEST_START_DATE.plusDays(2), // Saturday Jan 3
      TEST_START_DATE.plusDays(3), // Sunday Jan 4
      TEST_START_DATE.plusDays(9), // Saturday Jan 10
      TEST_START_DATE.plusDays(10) // Sunday Jan 11
    );
  }

  @Test
  void produce_shouldHandleExplicitDates_withoutDaysOfWeekFiltering() {
    // Explicit dates should not be filtered by daysOfWeek
    ServiceJourney serviceJourney = createServiceJourney();

    DayType dayType = new DayType();
    dayType.setNetexId("TST:DayType:5");
    dayType.setVersion(1L);

    // Set daysOfWeek to only Monday
    dayType.getDaysOfWeek().add(DayOfWeek.MONDAY);

    // Create an explicit date assignment for a Thursday (Jan 1, 2026)
    DayTypeAssignment explicitDate = new DayTypeAssignment();
    explicitDate.setDate(TEST_START_DATE); // Thursday
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
    // Jan 1-14, 2026 contains Mondays on Jan 5 and Jan 12
    DayTypeAssignment periodAssignment = createPeriodAssignment(
      TEST_START_DATE,
      TEST_START_DATE.plusDays(13)
    );
    periodAssignment.setAvailable(true);
    dayType.getDayTypeAssignments().add(periodAssignment);

    // Add explicit date for a Friday (should be included)
    DayTypeAssignment explicitDate = new DayTypeAssignment();
    explicitDate.setDate(TEST_START_DATE.plusDays(8)); // Friday Jan 9
    explicitDate.setAvailable(true);
    dayType.getDayTypeAssignments().add(explicitDate);

    serviceJourney.updateDayTypes(List.of(dayType));

    // Act
    List<DatedServiceJourney> result = producer.produce(serviceJourney, context);

    // Assert - Expected: 2 Mondays (Jan 5, 12) + 1 explicit Friday (Jan 9) = 3 dates
    assertThat(result).hasSize(3);
    assertThat(context.getOperatingDays()).containsExactlyInAnyOrder(
      TEST_START_DATE.plusDays(4), // Monday Jan 5
      TEST_START_DATE.plusDays(8), // Friday Jan 9 (explicit)
      TEST_START_DATE.plusDays(11) // Monday Jan 12
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
