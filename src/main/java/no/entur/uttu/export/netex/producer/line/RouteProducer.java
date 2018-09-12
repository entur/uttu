package no.entur.uttu.export.netex.producer.line;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.StopPointInJourneyPattern;
import org.rutebanken.netex.model.DirectionTypeEnumeration;
import org.rutebanken.netex.model.PointOnRoute;
import org.rutebanken.netex.model.PointsOnRoute_RelStructure;
import org.rutebanken.netex.model.Route;
import org.rutebanken.netex.model.RoutePointRefStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RouteProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    public List<Route> produce(FlexibleLine line, NetexExportContext context) {
        return line.getJourneyPatterns().stream().map(jp -> mapRoute(jp,context)).collect(Collectors.toList());
    }

    /**
     * Create Route from JourneyPattern.
     *
     * User first and last StopPointInJourneyPattern as RoutePoints
     */
    private Route mapRoute(JourneyPattern journeyPattern, NetexExportContext context) {
        StopPointInJourneyPattern firstStopPointInJP = journeyPattern.getPointsInSequence().get(0);
        StopPointInJourneyPattern lastStopPointInJP = journeyPattern.getPointsInSequence().get(journeyPattern.getPointsInSequence().size() - 1);

        // TODO these might be refs to NSR stops in the future. how do we avoid collision for ScheduledStopPoints? prefix NSR scheduledStopPoints?
        context.routePointRefs.add(firstStopPointInJP.getFlexibleStopPlace().getRef());
        context.routePointRefs.add(lastStopPointInJP.getFlexibleStopPlace().getRef());

        PointsOnRoute_RelStructure pointsOnRoute_relStructure = new PointsOnRoute_RelStructure().withPointOnRoute().withPointOnRoute(
                mapPointOnRoute(firstStopPointInJP, 1),
                mapPointOnRoute(lastStopPointInJP, 2)
        );

        return objectFactory.populateId(new Route(), journeyPattern.getRef())
                       .withName(objectFactory.createMultilingualString(journeyPattern.getName()))
                       .withDirectionType(objectFactory.mapEnum(journeyPattern.getDirectionType(), DirectionTypeEnumeration.class))
                       .withPointsInSequence(pointsOnRoute_relStructure);
    }

    private PointOnRoute mapPointOnRoute(StopPointInJourneyPattern stopPointInJourneyPattern, int order) {
        FlexibleStopPlace flexibleStopPlace = stopPointInJourneyPattern.getFlexibleStopPlace();
        return objectFactory.populateId(new PointOnRoute(), flexibleStopPlace.getRef())
                       .withOrder(BigInteger.valueOf(order))
                       .withPointRef(objectFactory.wrapRefStructure(new RoutePointRefStructure(), flexibleStopPlace.getRef(), false));
    }
}
