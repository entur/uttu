package no.entur.uttu.export.netex.producer.line;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DayTypeAssignment;
import no.entur.uttu.model.OperatingPeriod;
import no.entur.uttu.model.ServiceJourney;
import org.rutebanken.netex.model.DatedServiceJourney;
import org.rutebanken.netex.model.JourneyRefStructure;
import org.rutebanken.netex.model.OperatingDayRefStructure;
import org.rutebanken.netex.model.ServiceJourneyRefStructure;
import org.springframework.stereotype.Component;

@Component
public class DatedServiceJourneyProducer {

  private final NetexObjectFactory objectFactory;
  private final Clock clock;

  public DatedServiceJourneyProducer(NetexObjectFactory objectFactory, Clock clock) {
    this.objectFactory = objectFactory;
    this.clock = clock;
  }

  private LocalDate getCutoff() {
    return LocalDate.now(clock).minusDays(1);
  }

  public List<DatedServiceJourney> produce(
    ServiceJourney local,
    NetexExportContext context
  ) {
    Set<LocalDate> operatingDates = collectOperatingDates(local, context);

    if (operatingDates.isEmpty()) {
      return List.of();
    }

    return createDatedServiceJourneys(local, operatingDates, context);
  }

  private Set<LocalDate> collectOperatingDates(
    ServiceJourney serviceJourney,
    NetexExportContext context
  ) {
    return serviceJourney
      .getDayTypes()
      .stream()
      .filter(context::isValid)
      .flatMap(dayType -> expandDayTypeToLocalDates(dayType).stream())
      .filter(date -> !date.isBefore(getCutoff()))
      .collect(Collectors.toSet());
  }

  private Set<LocalDate> expandDayTypeToLocalDates(DayType dayType) {
    return dayType
      .getDayTypeAssignments()
      .stream()
      .filter(dta -> Boolean.TRUE.equals(dta.getAvailable()))
      .flatMap(dta -> expandDayTypeAssignmentToLocalDates(dta, dayType))
      .collect(Collectors.toSet());
  }

  private Stream<LocalDate> expandDayTypeAssignmentToLocalDates(
    DayTypeAssignment assignment,
    DayType dayType
  ) {
    // Explicit dates are always included, regardless of daysOfWeek
    if (assignment.getDate() != null) {
      return Stream.of(assignment.getDate());
    }

    // Handle operating period ranges with optional daysOfWeek filtering
    if (assignment.getOperatingPeriod() != null) {
      return expandOperatingPeriodToLocalDates(assignment.getOperatingPeriod(), dayType);
    }

    return Stream.empty();
  }

  private Stream<LocalDate> expandOperatingPeriodToLocalDates(
    OperatingPeriod period,
    DayType dayType
  ) {
    LocalDate from = period.getFromDate();
    LocalDate to = period.getToDate();

    if (from == null || to == null) {
      return Stream.empty();
    }

    Stream<LocalDate> allDatesInRange = from.datesUntil(to.plusDays(1));

    // Filter by daysOfWeek if specified
    if (dayType.getDaysOfWeek() != null && !dayType.getDaysOfWeek().isEmpty()) {
      return allDatesInRange.filter(
        date -> dayType.getDaysOfWeek().contains(date.getDayOfWeek())
      );
    }

    return allDatesInRange;
  }

  private List<DatedServiceJourney> createDatedServiceJourneys(
    ServiceJourney serviceJourney,
    Set<LocalDate> operatingDates,
    NetexExportContext context
  ) {
    String sjSuffix = NetexIdProducer.getObjectIdSuffix(serviceJourney.getNetexId());

    return operatingDates
      .stream()
      .map(date -> {
        context.addOperatingDay(date);
        return createDatedServiceJourney(serviceJourney, date, sjSuffix, context);
      })
      .toList();
  }

  private DatedServiceJourney createDatedServiceJourney(
    ServiceJourney serviceJourney,
    LocalDate date,
    String sjSuffix,
    NetexExportContext context
  ) {
    String dsjId = NetexIdProducer.getId(
      DatedServiceJourney.class,
      sjSuffix + "-" + date,
      context
    );

    String operatingDayId = NetexIdProducer.getId(
      org.rutebanken.netex.model.OperatingDay.class,
      date.toString(),
      context
    );

    JourneyRefStructure sjRef = new ServiceJourneyRefStructure()
      .withRef(serviceJourney.getNetexId())
      .withVersion(serviceJourney.getNetexVersion());

    OperatingDayRefStructure odRef = new OperatingDayRefStructure()
      .withRef(operatingDayId);

    return new DatedServiceJourney()
      .withId(dsjId)
      .withVersion("0")
      .withJourneyRef(Collections.singletonList(objectFactory.wrapAsJAXBElement(sjRef)))
      .withOperatingDayRef(odRef);
  }
}
