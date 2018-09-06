package no.entur.uttu.export.netex.producer.common;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.Ref;
import org.rutebanken.netex.model.DayTypeAssignment;
import org.rutebanken.netex.model.DayTypeRefStructure;
import org.rutebanken.netex.model.OperatingPeriod;
import org.rutebanken.netex.model.OperatingPeriodRefStructure;
import org.rutebanken.netex.model.ServiceCalendarFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ServiceCalendarFrameProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    public ServiceCalendarFrame produce(NetexExportContext context) {

        List<org.rutebanken.netex.model.DayType> netexDayTypes = new ArrayList<>();
        List<DayTypeAssignment> netexDayTypeAssignments = new ArrayList<>();
        List<OperatingPeriod> netexOperatingPeriods = new ArrayList<>();


        int dayTypeAssignmentOrder = 1;
        for (DayType localDayType : context.dayTypes) {
            netexDayTypes.add(mapDayType(localDayType));

            // TODO order daytypeassignments. inclusions before excl? chronologically? see chouette

            for (no.entur.uttu.model.DayTypeAssignment localDayTypeAssignment : localDayType.getDayTypeAssignments()) {
                OperatingPeriod operatingPeriod = null;
                if (localDayTypeAssignment.getOperatingPeriod() != null) {
                    operatingPeriod = mapOperatingPeriod(localDayTypeAssignment.getOperatingPeriod(), context);
                    netexOperatingPeriods.add(operatingPeriod);
                }
                netexDayTypeAssignments.add(mapDayTypeAssignment(localDayTypeAssignment, localDayType, operatingPeriod, dayTypeAssignmentOrder++, context));
            }
        }

        return objectFactory.createServiceCalendarFrame(context, netexDayTypes, netexDayTypeAssignments, netexOperatingPeriods);
    }


    private org.rutebanken.netex.model.DayType mapDayType(DayType local) {
        return objectFactory.populateId(new org.rutebanken.netex.model.DayType(), local.getRef());
    }

    private OperatingPeriod mapOperatingPeriod(no.entur.uttu.model.OperatingPeriod local, NetexExportContext context) {

        String id = NetexIdProducer.generateId(OperatingPeriod.class, context);
        return new org.rutebanken.netex.model.OperatingPeriod()
                       .withId(id)
                       .withVersion(Objects.toString(local.getVersion()));
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

        return new DayTypeAssignment()
                       .withId(id)
                       .withVersion(Objects.toString(local.getVersion()))
                       .withOrder(BigInteger.valueOf(index))
                       .withDate(date)
                       .withOperatingPeriodRef(operatingPeriodRefStructure)
                       .withDayTypeRef(objectFactory.createRefStructure(new DayTypeRefStructure(), localDayType.getRef(), true));
    }
}
