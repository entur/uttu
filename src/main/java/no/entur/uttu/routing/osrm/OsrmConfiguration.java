package no.entur.uttu.routing.osrm;

import java.util.HashMap;
import java.util.Map;
import no.entur.uttu.routing.RoutingProfile;
import no.entur.uttu.routing.RoutingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("osrm-routing-service")
public class OsrmConfiguration {

  @Bean
  RoutingService routingService(
    @Value("${uttu.routing.osrm-api}") String osrmApiEndpoint,
    @Value("${uttu.routing.osrm-api-bus}") String osrmApiBusEndpoint,
    @Value("${uttu.routing.osrm-api-rail}") String osrmApiRailEndpoint,
    @Value("${uttu.routing.osrm-api-water}") String osrmApiWaterEndpoint
  ) {
    if (osrmApiEndpoint != null) {
      return createStandaloneBusRoutingService(osrmApiEndpoint);
    } else {
      return createRoutingService(osrmApiBusEndpoint, osrmApiRailEndpoint, osrmApiWaterEndpoint);
    }
  }

  private RoutingService createRoutingService(String osrmApiBusEndpoint, String osrmApiRailEndpoint, String osrmApiWaterEndpoint) {
    var endpointMap = new HashMap<RoutingProfile, String>();

    if (osrmApiBusEndpoint != null) {
      endpointMap.put(RoutingProfile.BUS, osrmApiBusEndpoint);
    }
    if (osrmApiRailEndpoint != null) {
      endpointMap.put(RoutingProfile.RAIL, osrmApiRailEndpoint);
    }
    if (osrmApiWaterEndpoint != null) {
      endpointMap.put(RoutingProfile.WATER, osrmApiWaterEndpoint);
    }
    return new OsrmService(endpointMap);
  }

  private RoutingService createStandaloneBusRoutingService(String osrmApiEndpoint) {
    return new OsrmService(Map.of(RoutingProfile.BUS, osrmApiEndpoint));
  }
}
