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

package no.entur.uttu.export.netex.producer.common;

import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.Ref;
import org.rutebanken.netex.model.DayOfWeekEnumeration;
import org.rutebanken.netex.model.DayTypeAssignment;
import org.rutebanken.netex.model.DayTypeRefStructure;
import org.rutebanken.netex.model.OperatingDay;
import org.rutebanken.netex.model.OperatingDayRefStructure;
import org.rutebanken.netex.model.OperatingPeriod;
import org.rutebanken.netex.model.OperatingPeriodRefStructure;
import org.rutebanken.netex.model.PropertiesOfDay_RelStructure;
import org.rutebanken.netex.model.PropertyOfDay;
import org.rutebanken.netex.model.ServiceCalendarFrame;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ServiceCalendarFrameProducer {

  private final NetexObjectFactory objectFactory;

  public ServiceCalendarFrameProducer(NetexObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  private static final Map<DayOfWeek, DayOfWeekEnumeration> dayOfWeekMap =
    new HashMap<>();

  static {
    dayOfWeekMap.put(DayOfWeek.MONDAY, DayOfWeekEnumeration.MONDAY);
    dayOfWeekMap.put(DayOfWeek.TUESDAY, DayOfWeekEnumeration.TUESDAY);
    dayOfWeekMap.put(DayOfWeek.WEDNESDAY, DayOfWeekEnumeration.WEDNESDAY);
    dayOfWeekMap.put(DayOfWeek.THURSDAY, DayOfWeekEnumeration.THURSDAY);
    dayOfWeekMap.put(DayOfWeek.FRIDAY, DayOfWeekEnumeration.FRIDAY);
    dayOfWeekMap.put(DayOfWeek.SATURDAY, DayOfWeekEnumeration.SATURDAY);
    dayOfWeekMap.put(DayOfWeek.SUNDAY, DayOfWeekEnumeration.SUNDAY);
  }

  public ServiceCalendarFrame produce(NetexExportContext context) {
    List<org.rutebanken.netex.model.DayType> netexDayTypes = new ArrayList<>();
    List<DayTypeAssignment> netexDayTypeAssignments = new ArrayList<>();
    List<OperatingPeriod> netexOperatingPeriods = new ArrayList<>();
    List<OperatingDay> netexOperatingDays = new ArrayList<>();

    for (java.time.LocalDate date : context.getOperatingDays()) {
      String operatingDayId = NetexIdProducer.getId(
        OperatingDay.class,
        date.toString(),
        context
      );
      OperatingDay operatingDay = new OperatingDay()
        .withId(operatingDayId)
        .withVersion("0")
        .withCalendarDate(date.atStartOfDay());
      netexOperatingDays.add(operatingDay);
    }

    int dayTypeAssignmentOrder = 1;
    for (DayType localDayType : context.dayTypes) {
      netexDayTypes.add(mapDayType(localDayType));

      List<no.entur.uttu.model.DayTypeAssignment> validDayTypeAssignments = localDayType
        .getDayTypeAssignments()
        .stream()
        .filter(context::isValid)
        .toList();
      for (no.entur.uttu.model.DayTypeAssignment localDayTypeAssignment : validDayTypeAssignments) {
        OperatingPeriod operatingPeriod = null;
        if (context.isValid(localDayTypeAssignment.getOperatingPeriod())) {
          var mappedOperatingPeriod = mapOperatingPeriod(
            localDayTypeAssignment.getOperatingPeriod(),
            context
          );
          netexOperatingPeriods.add(mappedOperatingPeriod.getOperatingPeriod());
          operatingPeriod = mappedOperatingPeriod.getOperatingPeriod();
        }
        netexDayTypeAssignments.add(
          mapDayTypeAssignment(
            localDayTypeAssignment,
            localDayType,
            operatingPeriod,
            dayTypeAssignmentOrder++,
            context
          )
        );
      }
    }

    netexDayTypeAssignments.sort(new DayTypeAssignmentExportComparator());

    return objectFactory.createServiceCalendarFrame(
      context,
      netexDayTypes,
      netexDayTypeAssignments,
      netexOperatingPeriods,
      netexOperatingDays
    );
  }

  private org.rutebanken.netex.model.DayType mapDayType(DayType local) {
    PropertiesOfDay_RelStructure properties = null;
    if (!CollectionUtils.isEmpty(local.getDaysOfWeek())) {
      List<DayOfWeekEnumeration> daysOfWeek = local
        .getDaysOfWeek()
        .stream()
        .map(dayOfWeekMap::get)
        .collect(Collectors.toList());
      properties = new PropertiesOfDay_RelStructure()
        .withPropertyOfDay(List.of(new PropertyOfDay().withDaysOfWeek(daysOfWeek)));
    }
    return objectFactory
      .populateId(new org.rutebanken.netex.model.DayType(), local.getRef())
      .withProperties(properties);
  }

  private MappedOperatingPeriod mapOperatingPeriod(
    no.entur.uttu.model.OperatingPeriod local,
    NetexExportContext context
  ) {
    var id = NetexIdProducer.generateId(OperatingPeriod.class, context);

    String fromDateId = NetexIdProducer.getId(
      OperatingDay.class,
      local.getFromDate().toString(),
      context
    );
    String toDateId = NetexIdProducer.getId(
      OperatingDay.class,
      local.getToDate().toString(),
      context
    );

    var fromDate = new OperatingDay()
      .withId(fromDateId)
      .withVersion("0")
      .withCalendarDate(local.getFromDate().atStartOfDay());
    var toDate = new OperatingDay()
      .withId(toDateId)
      .withVersion("0")
      .withCalendarDate(local.getToDate().atStartOfDay());

    var operatingPeriod = new org.rutebanken.netex.model.OperatingPeriod()
      .withId(id)
      .withVersion(Objects.toString(local.getVersion()))
      .withFromOperatingDayRef(
        objectFactory.populateRefStructure(
          new OperatingDayRefStructure(),
          new Ref(fromDate.getId(), fromDate.getVersion()),
          true
        )
      )
      .withToOperatingDayRef(
        objectFactory.populateRefStructure(
          new OperatingDayRefStructure(),
          new Ref(toDate.getId(), toDate.getVersion()),
          true
        )
      );

    return new MappedOperatingPeriod(operatingPeriod, List.of(fromDate, toDate));
  }

  private DayTypeAssignment mapDayTypeAssignment(
    no.entur.uttu.model.DayTypeAssignment local,
    DayType localDayType,
    OperatingPeriod operatingPeriod,
    int index,
    NetexExportContext context
  ) {
    String id = NetexIdProducer.generateId(DayTypeAssignment.class, context);
    LocalDateTime date = null;
    if (local.getDate() != null) {
      date = local.getDate().atStartOfDay();
    }
    OperatingPeriodRefStructure operatingPeriodRefStructure = null;
    if (operatingPeriod != null) {
      operatingPeriodRefStructure = objectFactory.populateRefStructure(
        new OperatingPeriodRefStructure(),
        new Ref(operatingPeriod.getId(), operatingPeriod.getVersion()),
        true
      );
    }

    JAXBElement<DayTypeRefStructure> dayTypeRefStructure = objectFactory.wrapRefStructure(
      new DayTypeRefStructure(),
      localDayType.getRef(),
      true
    );
    return new DayTypeAssignment()
      .withId(id)
      .withVersion(Objects.toString(local.getVersion()))
      .withOrder(BigInteger.valueOf(index))
      .withDate(date)
      .withOperatingPeriodRef(
        objectFactory.wrapAsJAXBElement(operatingPeriodRefStructure)
      )
      .withDayTypeRef(dayTypeRefStructure)
      .withIsAvailable(local.getAvailable());
  }

  /**
   * Sort DayTypeAssignments for export.
   * <p>
   * Most specific assignments (exclusions) should be sorted after less specific (inclusions) to allow readers to apply assignments sequencially.
   */
  public class DayTypeAssignmentExportComparator
    implements Comparator<DayTypeAssignment> {

    @Override
    public int compare(DayTypeAssignment o1, DayTypeAssignment o2) {
      if (Boolean.FALSE.equals(o1.isIsAvailable())) {
        if (!Boolean.FALSE.equals(o2.isIsAvailable())) {
          return 1;
        }
      } else if (Boolean.FALSE.equals(o2.isIsAvailable())) {
        return -1;
      }

      return o1.getId().compareTo(o2.getId());
    }
  }
}
