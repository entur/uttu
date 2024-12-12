package no.entur.uttu.routing;

import java.math.BigDecimal;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation in case routing profile not specified
 */
@Component
@ConditionalOnMissingBean(
  value = RoutingService.class,
  ignored = DefaultRoutingService.class
)
public class DefaultRoutingService implements RoutingService {

  private static final Logger logger = LoggerFactory.getLogger(
    DefaultRoutingService.class
  );

  public DefaultRoutingService() {
    logger.info("DefaultRoutingService got initialised");
  }

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
