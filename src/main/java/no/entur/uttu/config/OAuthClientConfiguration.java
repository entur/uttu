package no.entur.uttu.config;

import org.entur.oauth2.AuthorizedWebClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("!test")
public class OAuthClientConfiguration {
    @Bean
    WebClient webClient(WebClient.Builder webClientBuilder, OAuth2ClientProperties properties, @Value("${orgregister.oauth2.client.audience}") String audience) {
        return new AuthorizedWebClientBuilder(webClientBuilder)
                .withOAuth2ClientProperties(properties)
                .withAudience(audience)
                .withClientRegistrationId("orgregister")
                .build();
    }
}
