/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 */

package no.entur.uttu.ext.entur.config;

import no.entur.uttu.ext.entur.security.permissionstore.DefaultPermissionStoreClient;
import no.entur.uttu.ext.entur.security.permissionstore.PermissionStoreClient;
import org.entur.oauth2.AuthorizedWebClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configure Spring Beans for OAuth2 resource server and OAuth2 client security.
 */
@Configuration
public class PermissionStoreConfig {

  @Bean
  @Profile("!test")
  PermissionStoreClient permissionStoreClient(
    @Qualifier("permissionStoreWebClient") WebClient permissionStoreWebClient
  ) {
    return new DefaultPermissionStoreClient(permissionStoreWebClient);
  }

  @Bean("permissionStoreWebClient")
  @Profile("!test")
  WebClient permissionStoreWebClient(
    WebClient.Builder webClientBuilder,
    OAuth2ClientProperties properties,
    @Value("${uttu.permissionstore.oauth2.client.audience}") String audience,
    ClientHttpConnector clientHttpConnector,
    @Value("${uttu.permissionstore.url}") String organisationRegistryUrl
  ) {
    return new AuthorizedWebClientBuilder(webClientBuilder)
      .withOAuth2ClientProperties(properties)
      .withAudience(audience)
      .withClientRegistrationId("permissionstore")
      .build()
      .mutate()
      .clientConnector(clientHttpConnector)
      .defaultHeader("Et-Client-Name", "entur-uttu")
      .baseUrl(organisationRegistryUrl)
      .build();
  }
}
