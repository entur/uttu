package no.entur.uttu.routing;

import java.math.BigDecimal;

/**
 * For getting the road geometry - resulting in service links and links in sequence in a journey pattern
 */
public interface RoutingService {
  RouteGeometry getRouteGeometry(
    BigDecimal longitudeFrom,
    BigDecimal latitudeFrom,
    BigDecimal longitudeTo,
    BigDecimal latitudeTo
  );
  boolean isEnabled();
}
