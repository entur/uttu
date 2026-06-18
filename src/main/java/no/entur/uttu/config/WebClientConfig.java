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
 */

package no.entur.uttu.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provides a {@link WebClient.Builder} bean. Spring Boot stopped auto-configuring one in 4.0
 * (WebClient support was de-emphasised in favour of RestClient), so we expose it explicitly
 * for the components that still inject it (e.g. the organisation register client).
 */
@Configuration
public class WebClientConfig {

  @Bean
  @ConditionalOnMissingBean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}
