package no.entur.uttu.export.netex.producer.line;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.ServiceJourney;
import org.rutebanken.netex.model.DatedServiceJourney;
import org.rutebanken.netex.model.JourneyRefStructure;
import org.rutebanken.netex.model.OperatingDayRefStructure;
import org.rutebanken.netex.model.ServiceJourneyRefStructure;
import org.springframework.stereotype.Component;

@Component
public class DatedServiceJourneyProducer {

  private final NetexObjectFactory objectFactory;

  public DatedServiceJourneyProducer(NetexObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  public List<DatedServiceJourney> produce(
    ServiceJourney local,
    NetexExportContext context
  ) {
    Set<LocalDate> dates = local
      .getDayTypes()
      .stream()
      .filter(context::isValid)
      .map(DayType::getDayTypeAssignments)
      .flatMap(List::stream)
      .filter(dta -> Boolean.TRUE.equals(dta.getAvailable()))
      .flatMap(dta -> {
        // Handle explicit dates
        if (dta.getDate() != null) {
          return Stream.of(dta.getDate());
        }
        // Handle operating period ranges
        if (dta.getOperatingPeriod() != null) {
          LocalDate from = dta.getOperatingPeriod().getFromDate();
          LocalDate to = dta.getOperatingPeriod().getToDate();
          if (from != null && to != null) {
            return from.datesUntil(to.plusDays(1));
          }
        }
        return Stream.empty();
      })
      .collect(Collectors.toSet());

    if (dates.isEmpty()) {
      return List.of();
    }

    String sjSuffix = NetexIdProducer.getObjectIdSuffix(local.getNetexId());

    List<DatedServiceJourney> result = new ArrayList<>();
    for (LocalDate date : dates) {
      context.addOperatingDay(date);

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
        .withRef(local.getNetexId())
        .withVersion(local.getNetexVersion());

      OperatingDayRefStructure odRef = new OperatingDayRefStructure()
        .withRef(operatingDayId);

      DatedServiceJourney dsj = new DatedServiceJourney()
        .withId(dsjId)
        .withVersion("0")
        .withJourneyRef(Collections.singletonList(objectFactory.wrapAsJAXBElement(sjRef)))
        .withOperatingDayRef(odRef);

      result.add(dsj);
    }

    return result;
  }
}
