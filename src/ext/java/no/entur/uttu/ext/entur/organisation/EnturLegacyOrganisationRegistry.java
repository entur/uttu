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

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.organisation.spi.OrganisationRegistry;
import no.entur.uttu.util.Preconditions;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.ContactStructure;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Component
@Profile("entur-legacy-organisation-registry")
public class EnturLegacyOrganisationRegistry implements OrganisationRegistry {

  private static final int HTTP_TIMEOUT = 10000;

  private String organisationRegistryUrl;
  private WebClient orgRegisterClient;
  private final int maxRetryAttempts;

  private final Map<String, Organisation> organisationCache = new HashMap<>();

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

  @PostConstruct
  public void init() {
    organisationCache.putAll(
      lookupOrganisations().stream().collect(Collectors.toMap(v -> v.id, v -> v))
    );
  }

  protected static final Predicate<Throwable> is5xx = throwable ->
    throwable instanceof WebClientResponseException &&
    ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();

  @Override
  public List<Authority> getAuthorities() {
    return organisationCache
      .values()
      .stream()
      .filter(org -> org.getAuthorityNetexId() != null)
      .map(this::mapToAuthority)
      .toList();
  }

  @Override
  public Optional<Authority> getAuthority(String id) {
    Organisation organisation = organisationCache.get(id);
    if (organisation.getAuthorityNetexId() != null) {
      var mappedOrganisation = mapToAuthority(organisation);
      return Optional.of(mappedOrganisation);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public List<Operator> getOperators() {
    return organisationCache
      .values()
      .stream()
      .filter(org -> org.getOperatorNetexId() != null)
      .map(this::mapToOperator)
      .toList();
  }

  @Override
  public Optional<Operator> getOperator(String id) {
    Organisation organisation = organisationCache.get(id);
    if (organisation.getOperatorNetexId() != null) {
      var mappedOrganisation = mapToOperator(organisation);
      return Optional.of(mappedOrganisation);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Throw exception if ref is not a valid operator
   */
  @Override
  public void validateOperatorRef(String operatorRef) {
    Organisation organisation = organisationCache.get(operatorRef);

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
    Organisation organisation = organisationCache.get(authorityRef);
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

  private Authority mapToAuthority(Organisation organisation) {
    Authority authority = new Authority();
    return mapCommon(authority, organisation);
  }

  private Operator mapToOperator(Organisation organisation) {
    Operator operator = new Operator();
    return mapCommon(operator, organisation);
  }

  private <
    T extends org.rutebanken.netex.model.Organisation_VersionStructure
  > T mapCommon(T mapped, Organisation organisation) {
    mapped
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
