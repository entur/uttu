package no.entur.uttu.export.netex.producer.line;

import no.entur.uttu.export.model.ExportError;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.export.netex.producer.common.OrganisationProducer;
import no.entur.uttu.model.BookingArrangement;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.model.StopPointInJourneyPattern;
import no.entur.uttu.model.TimetabledPassingTime;
import org.rutebanken.netex.model.BookingAccessEnumeration;
import org.rutebanken.netex.model.BookingMethodEnumeration;
import org.rutebanken.netex.model.DayTypeRefStructure;
import org.rutebanken.netex.model.DayTypeRefs_RelStructure;
import org.rutebanken.netex.model.FlexibleServiceProperties;
import org.rutebanken.netex.model.JourneyPatternRefStructure;
import org.rutebanken.netex.model.OperatorRefStructure;
import org.rutebanken.netex.model.PointInJourneyPatternRefStructure;
import org.rutebanken.netex.model.PurchaseMomentEnumeration;
import org.rutebanken.netex.model.PurchaseWhenEnumeration;
import org.rutebanken.netex.model.StopPointInJourneyPatternRefStructure;
import org.rutebanken.netex.model.TimetabledPassingTimes_RelStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ServiceJourneyProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    @Autowired
    private ContactStructureProducer contactStructureProducer;

    @Autowired
    private OrganisationProducer organisationProducer;

    public org.rutebanken.netex.model.ServiceJourney produce(ServiceJourney local, NetexExportContext context) {

        OperatorRefStructure operatorRefStructure = null;
        if (local.getOperatorRef() != null) {
            operatorRefStructure = organisationProducer.produceOperatorRef(local.getOperatorRef(), false, context);
        }

        DayTypeRefs_RelStructure dayTypeRefs_relStructure = new DayTypeRefs_RelStructure()
                                                                    .withDayTypeRef(local.getDayTypes().stream()
                                                                                            .map(dt -> objectFactory.wrapRefStructure(new DayTypeRefStructure(), dt.getRef(), false)).collect(Collectors.toList()));
        context.dayTypes.addAll(local.getDayTypes());

        List<org.rutebanken.netex.model.TimetabledPassingTime> timetabledPassingTimes = local.getPassingTimes().stream().map(ttpt -> mapTimetabledPassingTime(ttpt, context)).collect(Collectors.toList());


        JAXBElement<JourneyPatternRefStructure> journeyPatternRef = objectFactory.wrapAsJAXBElement(
                objectFactory.populateRefStructure(new JourneyPatternRefStructure(), local.getJourneyPattern().getRef(), true));

        return objectFactory.populate(new org.rutebanken.netex.model.ServiceJourney(), local)
                       .withJourneyPatternRef(journeyPatternRef)
                       .withName(objectFactory.createMultilingualString(local.getName()))
                       .withFlexibleServiceProperties(mapFlexibleServiceProperties(local.getBookingArrangement()))
                       .withPublicCode(local.getPublicCode())
                       .withOperatorRef(operatorRefStructure)
                       .withPassingTimes(new TimetabledPassingTimes_RelStructure().withTimetabledPassingTime(timetabledPassingTimes))
                       .withDayTypes(dayTypeRefs_relStructure);
    }

    private org.rutebanken.netex.model.TimetabledPassingTime mapTimetabledPassingTime(TimetabledPassingTime local, NetexExportContext context) {

        JAXBElement<? extends PointInJourneyPatternRefStructure> pointInJourneyPatternRef = null;
        Optional<StopPointInJourneyPattern> stopPointInJourneyPattern = local.getServiceJourney().getJourneyPattern().getPointsInSequence().stream().filter(sp -> sp.getOrder() == local.getOrder()).findFirst();
        if (stopPointInJourneyPattern.isPresent()) {
            pointInJourneyPatternRef = objectFactory.wrapAsJAXBElement(objectFactory.populateRefStructure(new StopPointInJourneyPatternRefStructure(), stopPointInJourneyPattern.get().getRef(), true));
        } else {
            context.errors.add(new ExportError("No corresponding StopPointInJourneyPattern found for TimetabledPassingTime [id:{}]", local.getNetexId()));
        }

        BigInteger arrivalDayOffset = local.getArrivalDayOffset() != 0 ? BigInteger.valueOf(local.getArrivalDayOffset()) : null;
        BigInteger departureDayOffset = local.getDepartureDayOffset() != 0 ? BigInteger.valueOf(local.getDepartureDayOffset()) : null;
        BigInteger latestArrivalDayOffset = local.getLatestArrivalDayOffset() != 0 ? BigInteger.valueOf(local.getLatestArrivalDayOffset()) : null;
        BigInteger earliestDepartureDayOffset = local.getEarliestDepartureDayOffset() != 0 ? BigInteger.valueOf(local.getEarliestDepartureDayOffset()) : null;

        return objectFactory.populateId(new org.rutebanken.netex.model.TimetabledPassingTime(), local.getRef())
                       .withPointInJourneyPatternRef(pointInJourneyPatternRef)
                       .withArrivalDayOffset(arrivalDayOffset)
                       .withDepartureDayOffset(departureDayOffset)
                       .withLatestArrivalDayOffset(latestArrivalDayOffset)
                       .withEarliestDepartureDayOffset(earliestDepartureDayOffset)
                       .withDepartureTime(local.getDepartureTime())
                       .withArrivalTime(local.getArrivalTime())
                       .withLatestArrivalTime(local.getLatestArrivalTime())
                       .withEarliestDepartureTime(local.getEarliestDepartureTime());
    }


    private FlexibleServiceProperties mapFlexibleServiceProperties(BookingArrangement local) {
        if (local == null) {
            return null;
        }

        // TODO flexibleSerivceType?
        return new FlexibleServiceProperties()
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
