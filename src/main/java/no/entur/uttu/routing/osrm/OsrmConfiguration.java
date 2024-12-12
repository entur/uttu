package no.entur.uttu.routing.osrm;

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
    @Value("${uttu.routing.osrm-api}") String osrmApiEndpoint
  ) {
    return new OsrmService(osrmApiEndpoint);
  }
}
