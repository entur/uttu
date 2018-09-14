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

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.Ref;
import org.rutebanken.netex.model.DayOfWeekEnumeration;
import org.rutebanken.netex.model.DayTypeAssignment;
import org.rutebanken.netex.model.DayTypeRefStructure;
import org.rutebanken.netex.model.OperatingPeriod;
import org.rutebanken.netex.model.OperatingPeriodRefStructure;
import org.rutebanken.netex.model.PropertiesOfDay_RelStructure;
import org.rutebanken.netex.model.PropertyOfDay;
import org.rutebanken.netex.model.ServiceCalendarFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBElement;
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

@Component
public class ServiceCalendarFrameProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    private static final Map<DayOfWeek, DayOfWeekEnumeration> dayOfWeekMap = new HashMap<>();

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


        int dayTypeAssignmentOrder = 1;
        for (DayType localDayType : context.dayTypes) {
            netexDayTypes.add(mapDayType(localDayType));

            List<no.entur.uttu.model.DayTypeAssignment> validDayTypeAssignments = localDayType.getDayTypeAssignments()
                                                                                          .stream().filter(context::isValid).collect(Collectors.toList());
            for (no.entur.uttu.model.DayTypeAssignment localDayTypeAssignment : validDayTypeAssignments) {
                OperatingPeriod operatingPeriod = null;
                if (context.isValid(localDayTypeAssignment.getOperatingPeriod())) {
                    operatingPeriod = mapOperatingPeriod(localDayTypeAssignment.getOperatingPeriod(), context);
                    netexOperatingPeriods.add(operatingPeriod);
                }
                netexDayTypeAssignments.add(mapDayTypeAssignment(localDayTypeAssignment, localDayType, operatingPeriod, dayTypeAssignmentOrder++, context));
            }
        }

        Collections.sort(netexDayTypeAssignments, new DayTypeAssignmentExportComparator());

        return objectFactory.createServiceCalendarFrame(context, netexDayTypes, netexDayTypeAssignments, netexOperatingPeriods);
    }


    private org.rutebanken.netex.model.DayType mapDayType(DayType local) {

        PropertiesOfDay_RelStructure properties = null;
        if (!CollectionUtils.isEmpty(local.getDaysOfWeek())) {

            List<DayOfWeekEnumeration> daysOfWeek = local.getDaysOfWeek().stream().map(dayOfWeekMap::get).collect(Collectors.toList());
            properties = new PropertiesOfDay_RelStructure().withPropertyOfDay(Arrays.asList(new PropertyOfDay().withDaysOfWeek(daysOfWeek)));
        }
        return objectFactory.populateId(new org.rutebanken.netex.model.DayType(), local.getRef()).withProperties(properties);
    }

    private OperatingPeriod mapOperatingPeriod(no.entur.uttu.model.OperatingPeriod local, NetexExportContext context) {

        String id = NetexIdProducer.generateId(OperatingPeriod.class, context);
        return new org.rutebanken.netex.model.OperatingPeriod()
                       .withId(id)
                       .withVersion(Objects.toString(local.getVersion()))
                       .withFromDate(local.getFromDate().atStartOfDay())
                       .withToDate(local.getToDate().atStartOfDay());
    }

    private DayTypeAssignment mapDayTypeAssignment(no.entur.uttu.model.DayTypeAssignment local, DayType localDayType,
                                                          OperatingPeriod operatingPeriod, int index, NetexExportContext context) {
        String id = NetexIdProducer.generateId(DayTypeAssignment.class, context);
        LocalDateTime date = null;
        if (local.getDate() != null) {
            date = local.getDate().atStartOfDay();
        }
        OperatingPeriodRefStructure operatingPeriodRefStructure = null;
        if (operatingPeriod != null) {
            operatingPeriodRefStructure = objectFactory.populateRefStructure(new OperatingPeriodRefStructure(), new Ref(operatingPeriod.getId(), operatingPeriod.getVersion()), true);
        }

        JAXBElement<DayTypeRefStructure> dayTypeRefStructure = objectFactory.wrapRefStructure(new DayTypeRefStructure(), localDayType.getRef(), true);
        return new DayTypeAssignment()
                       .withId(id)
                       .withVersion(Objects.toString(local.getVersion()))
                       .withOrder(BigInteger.valueOf(index))
                       .withDate(date)
                       .withOperatingPeriodRef(operatingPeriodRefStructure)
                       .withDayTypeRef(dayTypeRefStructure)
                       .withIsAvailable(local.getAvailable());
    }


    /**
     * Sort DayTypeAssignments for export.
     * <p>
     * Most specific assignments (exclusions) should be sorted after less specific (inclusions) to allow readers to apply assignments sequencially.
     */
    public class DayTypeAssignmentExportComparator implements Comparator<DayTypeAssignment> {

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
