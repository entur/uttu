package no.entur.uttu.export.netex.producer.line;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.BookingArrangement;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Ref;
import no.entur.uttu.model.StopPointInJourneyPattern;
import org.rutebanken.netex.model.BookingAccessEnumeration;
import org.rutebanken.netex.model.BookingArrangementsStructure;
import org.rutebanken.netex.model.BookingMethodEnumeration;
import org.rutebanken.netex.model.DestinationDisplayRefStructure;
import org.rutebanken.netex.model.JourneyPatternRefStructure;
import org.rutebanken.netex.model.NoticeAssignment;
import org.rutebanken.netex.model.PointInLinkSequence_VersionedChildStructure;
import org.rutebanken.netex.model.PointsInJourneyPattern_RelStructure;
import org.rutebanken.netex.model.PurchaseMomentEnumeration;
import org.rutebanken.netex.model.PurchaseWhenEnumeration;
import org.rutebanken.netex.model.RouteRefStructure;
import org.rutebanken.netex.model.ScheduledStopPoint;
import org.rutebanken.netex.model.ScheduledStopPointRefStructure;
import org.rutebanken.netex.model.StopPointInJourneyPatternRefStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JourneyPatternProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    @Autowired
    private ContactStructureProducer contactStructureProducer;

    public org.rutebanken.netex.model.JourneyPattern produce(JourneyPattern local, List<NoticeAssignment> noticeAssignments, NetexExportContext context) {
        List<PointInLinkSequence_VersionedChildStructure> netexStopPoints = local.getPointsInSequence().stream().map(spinjp -> mapStopPointInJourneyPattern(spinjp, noticeAssignments, context)).collect(Collectors.toList());
        PointsInJourneyPattern_RelStructure pointsInJourneyPattern_relStructure = new PointsInJourneyPattern_RelStructure().withPointInJourneyPatternOrStopPointInJourneyPatternOrTimingPointInJourneyPattern(netexStopPoints);

        RouteRefStructure routeRef = objectFactory.populateRefStructure(new RouteRefStructure(), local.getRef(), true);

        noticeAssignments.addAll(objectFactory.createNoticeAssignments(local, JourneyPatternRefStructure.class, local.getNotices(), context));
        context.notices.addAll(local.getNotices());

        return objectFactory.populate(new org.rutebanken.netex.model.JourneyPattern(), local)
                       .withRouteRef(routeRef)
                       .withName(objectFactory.createMultilingualString(local.getName()))
                       .withPointsInSequence(pointsInJourneyPattern_relStructure);
    }


    private org.rutebanken.netex.model.StopPointInJourneyPattern mapStopPointInJourneyPattern(StopPointInJourneyPattern local,
                                                                                                     List<NoticeAssignment> noticeAssignments,
                                                                                                     NetexExportContext context) {
        DestinationDisplayRefStructure destinationDisplayRefStructure = null;
        if (local.getDestinationDisplay() != null) {
            context.destinationDisplays.add(local.getDestinationDisplay());
            destinationDisplayRefStructure = objectFactory.populateRefStructure(new DestinationDisplayRefStructure(), local.getDestinationDisplay().getRef(), false);
        }

        // Create ref to scheduledStopPoint referring to either a flexible stop place or a NSR QuayRef
        Ref stopRef;
        if (local.getFlexibleStopPlace() != null) {
            context.flexibleStopPlaces.add(local.getFlexibleStopPlace());
            stopRef = local.getFlexibleStopPlace().getRef();
        } else {
            context.quayRefs.add(local.getQuayRef());
            stopRef = objectFactory.createScheduledStopPointRefFromQuayRef(local.getQuayRef(), context);
        }

        Ref scheduledStopPointRef = NetexIdProducer.replaceEntityName(stopRef, ScheduledStopPoint.class.getSimpleName());
        context.scheduledStopPointRefs.add(scheduledStopPointRef);
        JAXBElement<ScheduledStopPointRefStructure> scheduledStopPointRefStructure = objectFactory.wrapAsJAXBElement(new ScheduledStopPointRefStructure().withRef(scheduledStopPointRef.id));

        noticeAssignments.addAll(objectFactory.createNoticeAssignments(local, StopPointInJourneyPatternRefStructure.class, local.getNotices(), context));
        context.notices.addAll(local.getNotices());

        return objectFactory.populateId(new org.rutebanken.netex.model.StopPointInJourneyPattern(), local.getRef())
                       .withBookingArrangements(mapBookingArrangement(local.getBookingArrangement()))
                       .withForAlighting(local.getForAlighting())
                       .withForBoarding(local.getForBoarding())
                       .withOrder(BigInteger.valueOf(local.getOrder()))
                       .withDestinationDisplayRef(destinationDisplayRefStructure)
                       .withScheduledStopPointRef(scheduledStopPointRefStructure);
    }

    private BookingArrangementsStructure mapBookingArrangement(BookingArrangement local) {
        if (local == null) {
            return null;
        }
        return new BookingArrangementsStructure()
                       .withBookingAccess(objectFactory.mapEnum(local.getBookingAccess(), BookingAccessEnumeration.class))
                       .withBookingMethods(objectFactory.mapEnums(local.getBookingMethods(), BookingMethodEnumeration.class))
                       .withBookWhen(objectFactory.mapEnum(local.getBookWhen(), PurchaseWhenEnumeration.class))
                       .withBuyWhen(objectFactory.mapEnums(local.getBuyWhen(), PurchaseMomentEnumeration.class))
                       .withLatestBookingTime(local.getLatestBookingTime())
                       .withMinimumBookingPeriod(local.getMinimumBookingPeriod())
                       .withBookingNote(objectFactory.createMultilingualString(local.getBookingNote()))
                       .withBookingContact(contactStructureProducer.mapContactStructure(local.getBookingContact()));

    }
}
