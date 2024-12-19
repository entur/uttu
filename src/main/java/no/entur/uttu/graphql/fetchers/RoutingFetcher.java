package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Optional;
import no.entur.uttu.graphql.model.ServiceLink;
import no.entur.uttu.routing.RouteGeometry;
import no.entur.uttu.routing.RoutingService;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.StopPlace;
import org.springframework.stereotype.Service;

@Service("routingFetcher")
public class RoutingFetcher implements DataFetcher<ServiceLink> {

  private final RoutingService routingService;
  private final StopPlaceRegistry stopPlaceRegistry;

  public RoutingFetcher(
    RoutingService routingService,
    StopPlaceRegistry stopPlaceRegistry
  ) {
    this.routingService = routingService;
    this.stopPlaceRegistry = stopPlaceRegistry;
  }

  @Override
  public ServiceLink get(DataFetchingEnvironment environment) {
    String quayRefFrom = environment.getArgument("quayRefFrom");
    String quayRefTo = environment.getArgument("quayRefTo");
    Quay quayFrom = getQuay(quayRefFrom);
    Quay quayTo = getQuay(quayRefTo);

    if (quayFrom == null || quayTo == null) {
      return new ServiceLink(quayRefFrom + "_" + quayRefTo, null, null, null);
    }

    RouteGeometry routeGeometry = routingService.getRouteGeometry(
      quayFrom.getCentroid().getLocation().getLongitude(),
      quayFrom.getCentroid().getLocation().getLatitude(),
      quayTo.getCentroid().getLocation().getLongitude(),
      quayTo.getCentroid().getLocation().getLatitude()
    );

    return new ServiceLink(
      quayRefFrom + "_" + quayRefTo,
      quayRefFrom,
      quayRefTo,
      routeGeometry
    );
  }

  Quay getQuay(String quayRef) {
    if (quayRef == null) {
      return null;
    }
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
}
