package no.entur.uttu.routing.osrm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("routing")
public class OsrmConfiguration {

  @Bean
  OsrmService osrmService(@Value("${uttu.routing.osrm-api}") String osrmApi) {
    return new OsrmService(osrmApi);
  }
}
