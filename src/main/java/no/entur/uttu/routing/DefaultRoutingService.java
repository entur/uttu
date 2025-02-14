package no.entur.uttu.routing;

import no.entur.uttu.model.VehicleModeEnumeration;
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
    logger.info("DefaultRoutingService initialised");
  }

  @Override
  public boolean isEnabled(VehicleModeEnumeration mode) {
    return false;
  }

  @Override
  public RouteGeometry getRouteGeometry(RoutingServiceRequestParams request) {
    return null;
  }
}
