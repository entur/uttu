package no.entur.uttu.routing.osrm;

import java.util.HashMap;
import java.util.Map;
import no.entur.uttu.routing.RoutingService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for the OSRM (Open Source Routing Machine) routing service.
 *
 * <p>This configuration is activated via the spring profile "osrm-routing-service".</p>
 *
 * <p>The routing profiles can be configured via properties to map a set of transport
 * modes to specific OSRM endpoints. The configuration follows this pattern:</p>
 *
 * <pre>
 *   uttu.routing.osrm-api.profiles.[label].modes=[list of modes]
 *   uttu.routing.osrm-api.profiles.[label].endpoint=[url to service]
 * </pre>
 *
 * <p>For example, to configure routing for "bus" mode to be handled by http://localhost:5000,
 * use the following configuration:</p>
 *
 * <pre>
 *   uttu.routing.osrm-api.profiles.bus.modes=bus
 *   uttu.routing.osrm-api.profiles.bus.endpoint=http://localhost:5000
 * </pre>
 *
 * <p>Note: The "label" in the configuration is arbitrary and used only for identification purposes.</p>
 *
 * @see OsrmService
 */
@Configuration
@ConfigurationProperties(prefix = "uttu.routing.osrm-api")
@ConfigurationPropertiesScan
@Profile("osrm-routing-service")
public class OsrmConfiguration {

  private Map<String, OsrmProfile> profiles = new HashMap<>();

  public Map<String, OsrmProfile> getProfiles() {
    return profiles;
  }

  @Bean
  RoutingService routingService() {
    return new OsrmService(profiles.values().stream().toList());
  }
}
