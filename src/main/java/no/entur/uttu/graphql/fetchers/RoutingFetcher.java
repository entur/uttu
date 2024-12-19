package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.model.ServiceLink;
import no.entur.uttu.routing.RouteGeometry;
import no.entur.uttu.routing.RoutingService;
import org.springframework.stereotype.Service;

@Service("routingFetcher")
public class RoutingFetcher implements DataFetcher<ServiceLink> {

  RoutingService routingService;

  public RoutingFetcher(RoutingService routingService) {
    this.routingService = routingService;
  }

  @Override
  public ServiceLink get(DataFetchingEnvironment environment) {
    RouteGeometry routeGeometry = routingService.getRouteGeometry(
      environment.getArgument("longitudeFrom"),
      environment.getArgument("latitudeFrom"),
      environment.getArgument("longitudeTo"),
      environment.getArgument("latitudeTo")
    );

    String quayRefFrom = environment.getArgument("quayRefFrom");
    String quayRefTo = environment.getArgument("quayRefTo");
    return new ServiceLink(
      quayRefFrom + "_" + quayRefTo,
      quayRefFrom,
      quayRefTo,
      routeGeometry
    );
  }
}
