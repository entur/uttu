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

package no.entur.uttu.ext.entur.organisation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.organisation.spi.OrganisationRegistry;
import no.entur.uttu.util.Preconditions;
import org.rutebanken.netex.model.ContactStructure;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.netex.model.MultilingualString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Component
@Profile("entur-legacy-organisation-registry")
public class EnturLegacyOrganisationRegistry implements OrganisationRegistry {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int HTTP_TIMEOUT = 10000;

  private String organisationRegistryUrl;
  private WebClient orgRegisterClient;
  private final int maxRetryAttempts;

  private final LoadingCache<String, List<Organisation>> organisationsCache = CacheBuilder
    .newBuilder()
    .expireAfterWrite(6, TimeUnit.HOURS)
    .build(
      new CacheLoader<>() {
        @Override
        public List<Organisation> load(String unused) throws Exception {
          return lookupOrganisations();
        }
      }
    );

  private final LoadingCache<String, Organisation> organisationCache = CacheBuilder
    .newBuilder()
    .expireAfterWrite(6, TimeUnit.HOURS)
    .build(
      new CacheLoader<>() {
        @Override
        public Organisation load(String id) throws Exception {
          return lookupOrganisation(id);
        }
      }
    );

  public EnturLegacyOrganisationRegistry(
    @Value(
      "${organisation.registry.url:https://tjenester.entur.org/organisations/v1/organisations}"
    ) String organisationRegistryUrl,
    @Value("${organisation.registry.retry.max:3}") int maxRetryAttempts,
    @Autowired WebClient orgRegisterClient
  ) {
    this.organisationRegistryUrl = organisationRegistryUrl;
    this.orgRegisterClient =
      orgRegisterClient
        .mutate()
        .clientConnector(
          new ReactorClientHttpConnector(
            HttpClient
              .create()
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, HTTP_TIMEOUT)
              .doOnConnected(connection -> {
                connection.addHandlerLast(
                  new ReadTimeoutHandler(HTTP_TIMEOUT, TimeUnit.MILLISECONDS)
                );
                connection.addHandlerLast(
                  new WriteTimeoutHandler(HTTP_TIMEOUT, TimeUnit.MILLISECONDS)
                );
              })
          )
        )
        .build();

    this.maxRetryAttempts = maxRetryAttempts;
  }

  @Override
  public Optional<org.rutebanken.netex.model.Organisation> getOrganisation(
    String organisationId
  ) {
    try {
      Organisation organisation = organisationCache.get(organisationId);
      var mappedOrganisation = mapToNetexOrganisation(organisation);
      return Optional.of(mappedOrganisation);
    } catch (HttpClientErrorException | ExecutionException ex) {
      logger.warn(
        "Exception while trying to fetch organisation: " +
        organisationId +
        " : " +
        ex.getMessage(),
        ex
      );
      return Optional.empty();
    }
  }

  @Override
  public List<org.rutebanken.netex.model.Organisation> getOrganisations() {
    try {
      List<Organisation> organisations = organisationsCache.get("");
      return organisations
        .stream()
        .map(this::mapToNetexOrganisation)
        .collect(Collectors.toList());
    } catch (HttpClientErrorException | ExecutionException ex) {
      logger.warn("Exception while trying to fetch all organisations");
      return Collections.emptyList();
    }
  }

  protected static final Predicate<Throwable> is5xx = throwable ->
    throwable instanceof WebClientResponseException &&
    ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();

  /**
   * Throw exception if ref is not a valid operator
   */
  @Override
  public void validateOperatorRef(String operatorRef) {
    Organisation organisation = lookupOrganisation(operatorRef);
    Preconditions.checkArgument(
      organisation != null,
      "Organisation with ref %s not found in organisation registry",
      operatorRef
    );
    Preconditions.checkArgument(
      organisation.getOperatorNetexId() != null,
      CodedError.fromErrorCode(ErrorCodeEnumeration.ORGANISATION_NOT_VALID_OPERATOR),
      "Organisation with ref %s is not a valid operator",
      operatorRef
    );
  }

  /**
   * Throw exception if ref is not a valid authority
   */
  @Override
  public void validateAuthorityRef(String authorityRef) {
    Organisation organisation = lookupOrganisation(authorityRef);
    Preconditions.checkArgument(
      organisation != null,
      "Organisation with ref %s not found in organisation registry",
      authorityRef
    );
    Preconditions.checkArgument(
      organisation.getAuthorityNetexId() != null,
      "Organisation with ref %s is not a valid authority",
      authorityRef
    );
  }

  protected Organisation lookupOrganisation(String id) {
    return orgRegisterClient
      .get()
      .uri(organisationRegistryUrl + "/" + id)
      .header("Et-Client-Name", "entur-nplan")
      .retrieve()
      .bodyToMono(Organisation.class)
      .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(1)).filter(is5xx))
      .block(Duration.ofMillis(HTTP_TIMEOUT));
  }

  protected List<Organisation> lookupOrganisations() {
    return orgRegisterClient
      .get()
      .uri(organisationRegistryUrl)
      .header("Et-Client-Name", "entur-nplan")
      .retrieve()
      .bodyToMono(new ParameterizedTypeReference<List<Organisation>>() {})
      .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(1)).filter(is5xx))
      .block(Duration.ofMillis(HTTP_TIMEOUT));
  }

  private org.rutebanken.netex.model.Organisation mapToNetexOrganisation(
    Organisation organisation
  ) {
    org.rutebanken.netex.model.Organisation mapped =
      new org.rutebanken.netex.model.Organisation()
        .withId(organisation.id)
        .withVersion(organisation.version)
        .withName(new MultilingualString().withValue(organisation.name))
        .withLegalName(new MultilingualString().withValue(organisation.legalName))
        .withCompanyNumber(organisation.getCompanyNumber())
        .withContactDetails(
          organisation.contact != null
            ? new ContactStructure()
              .withEmail(organisation.contact.email)
              .withPhone(organisation.contact.phone)
              .withUrl(organisation.contact.url)
            : null
        );

    List<String> legacyIdList = new ArrayList<>();

    if (organisation.getAuthorityNetexId() != null) {
      legacyIdList.add(organisation.getAuthorityNetexId());
    }

    if (organisation.getOperatorNetexId() != null) {
      legacyIdList.add(organisation.getOperatorNetexId());
    }

    if (!legacyIdList.isEmpty()) {
      mapped.withKeyList(
        new KeyListStructure()
          .withKeyValue(
            new KeyValueStructure()
              .withKey("LegacyId")
              .withValue(String.join(",", legacyIdList))
          )
      );
    }

    return mapped;
  }
}
