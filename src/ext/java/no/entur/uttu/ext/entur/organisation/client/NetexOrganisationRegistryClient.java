package no.entur.uttu.ext.entur.organisation.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("entur-netex-organisation-registry-client")
public class NetexOrganisationRegistryClient {

  @Bean("orgRegisterClient")
  WebClient webClient(WebClient.Builder webClientBuilder) {
    return webClientBuilder.defaultHeader("Et-Client-Name", "entur-nplan").build();
  }
}
