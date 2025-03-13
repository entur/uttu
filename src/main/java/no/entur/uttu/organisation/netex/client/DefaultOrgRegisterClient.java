package no.entur.uttu.organisation.netex.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnMissingBean(name = "orgRegisterClient")
public class DefaultOrgRegisterClient {

  @Bean("orgRegisterClient")
  WebClient orgRegisterClient(WebClient.Builder webClientBuilder) {
    return webClientBuilder.build();
  }
}
