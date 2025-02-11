package no.entur.uttu.export.netex.producer.common;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.opengis.gml._3.DirectPositionListType;
import net.opengis.gml._3.LineStringType;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Ref;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.routing.RouteGeometry;
import no.entur.uttu.routing.RoutingProfile;
import no.entur.uttu.routing.RoutingService;
import no.entur.uttu.routing.RoutingServiceRequestParams;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.netex.model.LinkSequenceProjection;
import org.rutebanken.netex.model.Projections_RelStructure;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.ScheduledStopPointRefStructure;
import org.rutebanken.netex.model.ServiceLink;
import org.rutebanken.netex.model.StopPlace;
import org.springframework.stereotype.Component;

@Component
public class ServiceLinkProducer {

  private final NetexObjectFactory objectFactory;
  private final StopPlaceRegistry stopPlaceRegistry;
  private final RoutingService routingService;

  public ServiceLinkProducer(
    NetexObjectFactory objectFactory,
    StopPlaceRegistry stopPlaceRegistry,
    RoutingService routingService
  ) {
    this.objectFactory = objectFactory;
    this.stopPlaceRegistry = stopPlaceRegistry;
    this.routingService = routingService;
  }

  public List<ServiceLink> produce(NetexExportContext context) {
    return context.serviceLinks
      .stream()
      .map(serviceLink -> {
        Quay quayFrom = getQuay(serviceLink.quayRefFrom());
        Quay quayTo = getQuay(serviceLink.quayRefTo());

        var params = new RoutingServiceRequestParams(
          quayFrom.getCentroid().getLocation().getLongitude(),
          quayFrom.getCentroid().getLocation().getLatitude(),
          quayTo.getCentroid().getLocation().getLongitude(),
          quayTo.getCentroid().getLocation().getLatitude(),
          mapVehicleModeToRoutingProfile(serviceLink.transportMode())
        );

        RouteGeometry routeGeometry = routingService.getRouteGeometry(params);
        List<Double> posListCoordinates = new ArrayList<>();
        routeGeometry
          .coordinates()
          .forEach(location -> {
            posListCoordinates.add(location.get(0).doubleValue());
            posListCoordinates.add(location.get(1).doubleValue());
          });

        DirectPositionListType posList = new DirectPositionListType()
          .withCount(BigInteger.valueOf(posListCoordinates.size()))
          .withSrsDimension(BigInteger.valueOf(2))
          .withValue(posListCoordinates);

        LineStringType lineString = new LineStringType()
          .withId(getLineStringId(serviceLink.serviceLinkRef()))
          .withPosList(posList);

        LinkSequenceProjection linkSequenceProjection = objectFactory
          .populateId(
            new LinkSequenceProjection(),
            objectFactory.createLinkSequenceProjectionServiceLinkRef(
              serviceLink.serviceLinkRef()
            )
          )
          .withLineString(lineString);

        Projections_RelStructure projections_relStructure = new Projections_RelStructure()
          .withProjectionRefOrProjection(
            objectFactory.wrapAsJAXBElement(linkSequenceProjection)
          );

        Ref scheduledStopPointRefFrom =
          objectFactory.createScheduledStopPointRefFromQuayRef(
            serviceLink.quayRefFrom(),
            context
          );
        Ref scheduledStopPointRefTo =
          objectFactory.createScheduledStopPointRefFromQuayRef(
            serviceLink.quayRefTo(),
            context
          );
        ScheduledStopPointRefStructure scheduledStopPointFrom =
          objectFactory.populateRefStructure(
            new ScheduledStopPointRefStructure(),
            scheduledStopPointRefFrom,
            true
          );
        ScheduledStopPointRefStructure scheduledStopPointTo =
          objectFactory.populateRefStructure(
            new ScheduledStopPointRefStructure(),
            scheduledStopPointRefTo,
            true
          );

        return objectFactory
          .populateId(new ServiceLink(), serviceLink.serviceLinkRef())
          .withDistance(routeGeometry.distance())
          .withFromPointRef(scheduledStopPointFrom)
          .withToPointRef(scheduledStopPointTo)
          .withProjections(projections_relStructure);
      })
      .toList();
  }

  private RoutingProfile mapVehicleModeToRoutingProfile(
    VehicleModeEnumeration vehicleModeEnumeration
  ) {
    return switch (vehicleModeEnumeration) {
      case BUS, TAXI, COACH -> RoutingProfile.BUS;
      case FERRY, WATER -> RoutingProfile.WATER;
      case METRO, TRAM, RAIL -> RoutingProfile.RAIL;
      case AIR, CABLEWAY, FUNICULAR, TROLLEY_BUS, LIFT, OTHER -> null;
    };
  }

  Quay getQuay(String quayRef) {
    Optional<StopPlace> stopPlaceOptional = stopPlaceRegistry.getStopPlaceByQuayRef(
      quayRef
    );
    if (stopPlaceOptional.isEmpty()) {
      return null;
    }
    StopPlace stopPlaceFrom = stopPlaceOptional.get();
    List<Quay> stopPlaceFromQuays = stopPlaceFrom
      .getQuays()
      .getQuayRefOrQuay()
      .stream()
      .map(jaxbElement -> (org.rutebanken.netex.model.Quay) jaxbElement.getValue())
      .toList();
    return stopPlaceFromQuays
      .stream()
      .filter(quay -> quay.getId().equals(quayRef))
      .toList()
      .get(0);
  }

  String getLineStringId(Ref serviceLinkRef) {
    String serviceLinkSuffix = NetexIdProducer.getObjectIdSuffix(serviceLinkRef.id);
    return "LS_" + serviceLinkSuffix;
  }
}
