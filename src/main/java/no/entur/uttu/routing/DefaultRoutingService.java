package no.entur.uttu.routing;

import java.math.BigDecimal;
import java.util.ArrayList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation in case routing profile not specified
 */
@Component
@Profile("!routing")
public class DefaultRoutingService implements RoutingService {

  public RouteGeometry getRouteGeometry(
    BigDecimal longitudeFrom,
    BigDecimal latitudeFrom,
    BigDecimal longitudeTo,
    BigDecimal latitudeTo
  ) {
    return new RouteGeometry(new ArrayList<>(), BigDecimal.ZERO);
  }

  public boolean isEnabled() {
    return false;
  }
}
