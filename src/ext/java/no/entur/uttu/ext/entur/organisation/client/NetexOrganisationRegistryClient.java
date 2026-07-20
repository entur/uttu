package no.entur.uttu.ext.entur.organisation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("entur-netex-organisation-registry-client")
public class NetexOrganisationRegistryClient {

  @Value("${netex.organisations.max-in-memory-size:500KB}")
  DataSize maxInMemorySize;

  @Bean("orgRegisterClient")
  WebClient webClient(WebClient.Builder webClientBuilder) {
    return webClientBuilder
      .defaultHeader("Et-Client-Name", "entur-nplan")
      .exchangeStrategies(
        ExchangeStrategies.builder()
          .codecs(
            codecs ->
              codecs.defaultCodecs().maxInMemorySize((int) maxInMemorySize.toBytes())
          )
          .build()
      )
      .build();
  }
}
