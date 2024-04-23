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

package no.entur.uttu.ext.entur.security.permissionstore;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Predicate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

/**
 * Client for accessing the Permission Store API.
 */
public class DefaultPermissionStoreClient implements PermissionStoreClient {

  private static final long MAX_RETRY_ATTEMPTS = 3;

  private final WebClient webClient;

  public DefaultPermissionStoreClient(WebClient permissionStoreWebClient) {
    this.webClient = permissionStoreWebClient;
  }

  @Override
  public Collection<PermissionStorePermission> getPermissions(
    String subject,
    String authority,
    int application
  ) {
    return webClient
      .get()
      .uri(uriBuilder ->
        uriBuilder
          .path("/applications/{applicationId}/permissions")
          .queryParam("subject", subject)
          .queryParam("authority", authority)
          .build(application)
      )
      .retrieve()
      .bodyToFlux(PermissionStorePermission.class)
      .collectList()
      .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(1)).filter(is5xx))
      .block();
  }

  @Override
  public int getApplicationId(PermissionStoreApplication permissionStoreApplication) {
    return webClient
      .post()
      .uri("/applications")
      .body(BodyInserters.fromValue(permissionStoreApplication))
      .retrieve()
      .bodyToMono(Integer.class)
      .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(1)).filter(is5xx))
      .block();
  }

  private static final Predicate<Throwable> is5xx = throwable ->
    throwable instanceof WebClientResponseException webClientResponseException &&
    webClientResponseException.getStatusCode().is5xxServerError();
}
