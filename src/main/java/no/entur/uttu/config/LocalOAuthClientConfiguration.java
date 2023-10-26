package no.entur.uttu.config;

import org.entur.oauth2.AuthorizedWebClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("local")
public class LocalOAuthClientConfiguration {

  @Bean
  WebClient webClient(WebClient.Builder webClientBuilder) {
    return webClientBuilder.build();
  }
}
