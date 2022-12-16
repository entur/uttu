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

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.export.netex.producer.common.OrganisationProducer;
import no.entur.uttu.model.BookingArrangement;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.model.StopPointInJourneyPattern;
import no.entur.uttu.model.TimetabledPassingTime;
import no.entur.uttu.model.job.SeverityEnumeration;
import org.rutebanken.netex.model.BookingAccessEnumeration;
import org.rutebanken.netex.model.BookingMethodEnumeration;
import org.rutebanken.netex.model.DayTypeRefStructure;
import org.rutebanken.netex.model.DayTypeRefs_RelStructure;
import org.rutebanken.netex.model.FlexibleServiceProperties;
import org.rutebanken.netex.model.JourneyPatternRefStructure;
import org.rutebanken.netex.model.NoticeAssignment;
import org.rutebanken.netex.model.OperatorRefStructure;
import org.rutebanken.netex.model.PointInJourneyPatternRefStructure;
import org.rutebanken.netex.model.PurchaseMomentEnumeration;
import org.rutebanken.netex.model.PurchaseWhenEnumeration;
import org.rutebanken.netex.model.ServiceJourneyRefStructure;
import org.rutebanken.netex.model.StopPointInJourneyPatternRefStructure;
import org.rutebanken.netex.model.TimetabledPassingTimeRefStructure;
import org.rutebanken.netex.model.TimetabledPassingTimes_RelStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.entur.uttu.export.netex.producer.NetexObjectFactory.VERSION_ONE;

@Component
public class ServiceJourneyProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    @Autowired
    private ContactStructureProducer contactStructureProducer;

    @Autowired
    private OrganisationProducer organisationProducer;

    public org.rutebanken.netex.model.ServiceJourney produce(ServiceJourney local, List<NoticeAssignment> noticeAssignments, NetexExportContext context) {

        OperatorRefStructure operatorRefStructure = null;
        if (local.getOperatorRef() != null) {
            operatorRefStructure = organisationProducer.produceOperatorRef(local.getOperatorRef(), false, context);
            context.operatorRefs.add(local.getOperatorRef());
        }

        List<DayType> validDayTypes = local.getDayTypes().stream().filter(context::isValid).collect(Collectors.toList());

        DayTypeRefs_RelStructure dayTypeRefs_relStructure = new DayTypeRefs_RelStructure()
                                                                    .withDayTypeRef(validDayTypes.stream()
                                                                                            .map(dt -> objectFactory.wrapRefStructure(
                                                                                                    new DayTypeRefStructure(), dt.getRef(), false))
                                                                                            .collect(Collectors.toList()));
        context.dayTypes.addAll(validDayTypes);

        List<org.rutebanken.netex.model.TimetabledPassingTime> timetabledPassingTimes = local.getPassingTimes()
                                                                                                .stream().map(ttpt -> mapTimetabledPassingTime(ttpt, noticeAssignments, context))
                                                                                                .collect(Collectors.toList());


        JAXBElement<JourneyPatternRefStructure> journeyPatternRef = objectFactory.wrapAsJAXBElement(
                objectFactory.populateRefStructure(new JourneyPatternRefStructure(), local.getJourneyPattern().getRef(), true));


        noticeAssignments.addAll(objectFactory.createNoticeAssignments(local, ServiceJourneyRefStructure.class, local.getNotices(), context));
        context.notices.addAll(local.getNotices());

        return objectFactory.populate(new org.rutebanken.netex.model.ServiceJourney(), local)
                       .withJourneyPatternRef(journeyPatternRef)
                       .withName(objectFactory.createMultilingualString(local.getName()))
                       .withFlexibleServiceProperties(mapFlexibleServiceProperties(local.getBookingArrangement(), context))
                       .withPublicCode(local.getPublicCode())
                       .withOperatorRef(operatorRefStructure)
                       .withPassingTimes(new TimetabledPassingTimes_RelStructure().withTimetabledPassingTime(timetabledPassingTimes))
                       .withDayTypes(dayTypeRefs_relStructure);
    }

    private org.rutebanken.netex.model.TimetabledPassingTime mapTimetabledPassingTime(TimetabledPassingTime local, List<NoticeAssignment> noticeAssignments, NetexExportContext context) {

        JAXBElement<? extends PointInJourneyPatternRefStructure> pointInJourneyPatternRef = null;
        Optional<StopPointInJourneyPattern> stopPointInJourneyPattern = local.getServiceJourney().getJourneyPattern().getPointsInSequence().stream().filter(sp -> sp.getOrder() == local.getOrder()).findFirst();
        if (stopPointInJourneyPattern.isPresent()) {
            pointInJourneyPatternRef = objectFactory.wrapAsJAXBElement(objectFactory.populateRefStructure(new StopPointInJourneyPatternRefStructure(), stopPointInJourneyPattern.get().getRef(), true));
        } else {
            context.addExportMessage(SeverityEnumeration.ERROR, "No corresponding StopPointInJourneyPattern found for TimetabledPassingTime [id:{}]", local.getNetexId());
        }

        BigInteger arrivalDayOffset = local.getArrivalDayOffset() != 0 ? BigInteger.valueOf(local.getArrivalDayOffset()) : null;
        BigInteger departureDayOffset = local.getDepartureDayOffset() != 0 ? BigInteger.valueOf(local.getDepartureDayOffset()) : null;
        BigInteger latestArrivalDayOffset = local.getLatestArrivalDayOffset() != 0 ? BigInteger.valueOf(local.getLatestArrivalDayOffset()) : null;
        BigInteger earliestDepartureDayOffset = local.getEarliestDepartureDayOffset() != 0 ? BigInteger.valueOf(local.getEarliestDepartureDayOffset()) : null;

        noticeAssignments.addAll(objectFactory.createNoticeAssignments(local, TimetabledPassingTimeRefStructure.class, local.getNotices(), context));
        context.notices.addAll(local.getNotices());

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


    private FlexibleServiceProperties mapFlexibleServiceProperties(BookingArrangement local, NetexExportContext context) {
        if (local == null) {
            return null;
        }

        // TODO flexibleSerivceType?
        return new FlexibleServiceProperties()
                .withId(NetexIdProducer.generateId(FlexibleServiceProperties.class, context))
                .withVersion(VERSION_ONE)
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
