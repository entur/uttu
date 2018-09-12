package no.entur.uttu.export.netex.producer.common;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.NetexFile;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Ref;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.CompositeFrame;
import org.rutebanken.netex.model.DestinationDisplay;
import org.rutebanken.netex.model.FlexibleStopAssignment;
import org.rutebanken.netex.model.FlexibleStopPlace;
import org.rutebanken.netex.model.FlexibleStopPlaceRefStructure;
import org.rutebanken.netex.model.Network;
import org.rutebanken.netex.model.Notice;
import org.rutebanken.netex.model.Operator;
import org.rutebanken.netex.model.PointProjection;
import org.rutebanken.netex.model.PointRefStructure;
import org.rutebanken.netex.model.Projections_RelStructure;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.ResourceFrame;
import org.rutebanken.netex.model.RoutePoint;
import org.rutebanken.netex.model.ScheduledStopPoint;
import org.rutebanken.netex.model.ScheduledStopPointRefStructure;
import org.rutebanken.netex.model.ServiceCalendarFrame;
import org.rutebanken.netex.model.ServiceFrame;
import org.rutebanken.netex.model.SiteFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NetexCommonFileProducer {

    private static final String COMMON_FILE_NAME = "_common.xml";


    @Autowired
    private NetexObjectFactory objectFactory;

    @Autowired
    private OrganisationProducer organisationProducer;

    @Autowired
    private FlexibleStopPlaceProducer flexibleStopPlaceProducer;

    @Autowired
    private ServiceCalendarFrameProducer serviceCalendarFrameProducer;

    @Autowired
    private NetworkProducer networkProducer;


    public NetexFile toCommonFile(NetexExportContext context) {
        ResourceFrame resourceFrame = createResourceFrame(context);
        SiteFrame siteFrame = createSiteFrame(context);
        ServiceFrame serviceFrame = createServiceFrame(context);
        ServiceCalendarFrame serviceCalendarFrame = serviceCalendarFrameProducer.produce(context);
        CompositeFrame compositeFrame = objectFactory.createCompositeFrame(context, context.getAvailabilityPeriod(), resourceFrame, siteFrame, serviceFrame, serviceCalendarFrame);

        JAXBElement<PublicationDeliveryStructure> publicationDelivery = objectFactory.createPublicationDelivery(context, compositeFrame);

        return new NetexFile(COMMON_FILE_NAME, publicationDelivery);
    }

    private ResourceFrame createResourceFrame(NetexExportContext context) {
        List<Operator> netexOperators = organisationProducer.produceOperators(context);
        List<Authority> netexAuthorities = organisationProducer.produceAuthorities(context);
        return objectFactory.createResourceFrame(context, netexAuthorities, netexOperators);
    }

    private SiteFrame createSiteFrame(NetexExportContext context) {
        List<FlexibleStopPlace> netexFlexibleStopPlaces = flexibleStopPlaceProducer.produce(context);

        return objectFactory.createSiteFrame(context, netexFlexibleStopPlaces);
    }

    private ServiceFrame createServiceFrame(NetexExportContext context) {
        List<Network> networks = networkProducer.produce(context);
        List<RoutePoint> routePoints = context.routePointRefs.stream().map(this::buildRoutePoint).collect(Collectors.toList());
        List<ScheduledStopPoint> scheduledStopPoints = context.flexibleStopPlaces.stream().map(no.entur.uttu.model.FlexibleStopPlace::getRef)
                                                               .map(this::buildScheduledStopPoint).collect(Collectors.toList());
        List<FlexibleStopAssignment> flexibleStopAssignments = context.flexibleStopPlaces.stream().map(no.entur.uttu.model.FlexibleStopPlace::getRef)
                                                                       .map(this::buildFlexibleStopAssignment).collect(Collectors.toList());
        List<Notice> notices = context.notices.stream().map(this::mapNotice).collect(Collectors.toList());
        List<DestinationDisplay> destinationDisplays = context.destinationDisplays.stream().map(this::mapDestinationDisplay).collect(Collectors.toList());


        return objectFactory.createCommonServiceFrame(context, networks, routePoints, scheduledStopPoints, flexibleStopAssignments, notices, destinationDisplays);
    }


    private RoutePoint buildRoutePoint(Ref ref) {
        Ref scheduledStopPointRef = NetexIdProducer.replaceEntityName(ref, ScheduledStopPoint.class.getSimpleName());
        PointRefStructure pointRefStructure = new PointRefStructure().withRef(scheduledStopPointRef.id).withVersion(scheduledStopPointRef.version);
        PointProjection pointProjection = objectFactory.populateId(new PointProjection(), ref).withProjectToPointRef(pointRefStructure);
        Projections_RelStructure projections_relStructure = new Projections_RelStructure().withProjectionRefOrProjection(objectFactory.wrapAsJAXBElement(pointProjection));
        return objectFactory.populateId(new RoutePoint(), ref)
                       .withProjections(projections_relStructure);
    }

    private ScheduledStopPoint buildScheduledStopPoint(Ref ref) {
        return objectFactory.populateId(new ScheduledStopPoint(), ref);
    }

    private FlexibleStopAssignment buildFlexibleStopAssignment(Ref ref) {
        return objectFactory.populateId(new FlexibleStopAssignment(), ref)
                       .withScheduledStopPointRef(objectFactory.wrapRefStructure(new ScheduledStopPointRefStructure(), ref, true))
                       .withFlexibleStopPlaceRef(objectFactory.populateRefStructure(new FlexibleStopPlaceRefStructure(), ref, true));
    }

    public Notice mapNotice(no.entur.uttu.model.Notice local) {
        return objectFactory.populateId(new Notice(), local.getRef()).withText(objectFactory.createMultilingualString(local.getText()));
    }

    public DestinationDisplay mapDestinationDisplay(no.entur.uttu.model.DestinationDisplay local) {
        return objectFactory.populateId(new DestinationDisplay(), local.getRef()).withFrontText(objectFactory.createMultilingualString(local.getFrontText()));
    }
}
