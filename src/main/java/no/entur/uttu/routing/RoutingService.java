package no.entur.uttu.routing;

import no.entur.uttu.model.VehicleModeEnumeration;

/**
 * For getting the road geometry - resulting in service links and links in sequence in a journey pattern
 */
public interface RoutingService {
  /**
   * Check if the routing service is enabled for a given {@link VehicleModeEnumeration}
   * @param mode
   * @return
   */
  boolean isEnabled(VehicleModeEnumeration mode);

  /**
   * Request route geometry
   * @param request The parameters of the request
   * @return An instance of {@link RouteGeometry}
   */
  RouteGeometry getRouteGeometry(RoutingServiceRequestParams request);
}
