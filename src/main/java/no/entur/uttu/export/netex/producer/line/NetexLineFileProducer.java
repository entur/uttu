package no.entur.uttu.export.netex.producer.line;

import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.NetexFile;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DayTypeAssignment;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.ServiceJourney;
import org.rutebanken.netex.model.CompositeFrame;
import org.rutebanken.netex.model.NoticeAssignment;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.Route;
import org.rutebanken.netex.model.ServiceFrame;
import org.rutebanken.netex.model.TimetableFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NetexLineFileProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    @Autowired
    private LineProducer lineProducer;

    @Autowired
    private RouteProducer routeProducer;

    @Autowired
    private JourneyPatternProducer journeyPatternProducer;

    @Autowired
    private ServiceJourneyProducer serviceJourneyProducer;

    public NetexFile toNetexFile(FlexibleLine line, NetexExportContext context) {

        String fileName = createFileName(line);

        ServiceFrame serviceFrame = createServiceFrame(line, context);
        TimetableFrame timetableFrame = createTimetableFrame(line, context);

        AvailabilityPeriod availabilityPeriod = calculateAvailabilityPeriod(line);
        context.updateAvailabilityPeriod(availabilityPeriod);

        CompositeFrame compositeFrame = objectFactory.createCompositeFrame(context, availabilityPeriod, serviceFrame, timetableFrame);
        JAXBElement<PublicationDeliveryStructure> publicationDelivery = objectFactory.createPublicationDelivery(context, compositeFrame);

        return new NetexFile(fileName, publicationDelivery);
    }

    protected String createFileName(FlexibleLine line) {
        return line.getPublicCode() + "_" + line.getName() + ".xml";
    }

    private AvailabilityPeriod calculateAvailabilityPeriod(FlexibleLine line) {
        AvailabilityPeriod period = null;
        List<DayTypeAssignment> allDayTypeAssignmentsForLine = line.getJourneyPatterns().stream()
                                                                       .map(JourneyPattern::getServiceJourneys).flatMap(List::stream)
                                                                       .map(ServiceJourney::getDayTypes).flatMap(List::stream)
                                                                       .map(DayType::getDayTypeAssignments).flatMap(List::stream)
                                                                       .collect(Collectors.toList());
        for (DayTypeAssignment dayTypeAssignment : allDayTypeAssignmentsForLine) {
            period = union(period, dayTypeAssignment);
        }

        return period;
    }

    private AvailabilityPeriod union(AvailabilityPeriod period, DayTypeAssignment dayTypeAssignment) {
        AvailabilityPeriod dayTypeAssignmentPeriod;
        if (dayTypeAssignment.getOperatingPeriod() != null) {
            dayTypeAssignmentPeriod = new AvailabilityPeriod(dayTypeAssignment.getOperatingPeriod().getFromDate(), dayTypeAssignment.getOperatingPeriod().getToDate());
        } else if (dayTypeAssignment.getDate() != null) {
            dayTypeAssignmentPeriod = new AvailabilityPeriod(dayTypeAssignment.getDate(), dayTypeAssignment.getDate());
        } else {
            return period;
        }

        if (period == null) {
            return dayTypeAssignmentPeriod;
        }
        return period.union(dayTypeAssignmentPeriod);
    }


    private ServiceFrame createServiceFrame(FlexibleLine line, NetexExportContext context) {

        org.rutebanken.netex.model.FlexibleLine netexLine = lineProducer.produce(line, context);

        List<Route> netexRoutes = routeProducer.produce(line, context);
        List<org.rutebanken.netex.model.JourneyPattern> netexJourneyPatterns = line.getJourneyPatterns().stream()
                                                                                       .map(jp -> journeyPatternProducer.produce(jp, context)).collect(Collectors.toList());
        List<NoticeAssignment> noticeAssignments = new ArrayList<>();
        return objectFactory.createLineServiceFrame(context, netexLine, netexRoutes, netexJourneyPatterns, noticeAssignments);
    }

    private TimetableFrame createTimetableFrame(FlexibleLine line, NetexExportContext context) {
        List<org.rutebanken.netex.model.ServiceJourney> netexServiceJourneys = line.getJourneyPatterns().stream().map(JourneyPattern::getServiceJourneys).flatMap(List::stream)
                                                                                       .map(sj -> serviceJourneyProducer.produce(sj, context)).collect(Collectors.toList());
        List<NoticeAssignment> noticeAssignments = new ArrayList<>();
        return objectFactory.createTimetableFrame(context, netexServiceJourneys, noticeAssignments);
    }
}
