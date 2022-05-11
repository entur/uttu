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

package no.entur.uttu.organisation;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import javax.jdo.annotations.Cacheable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Component
public class OrganisationRegistryImpl implements OrganisationRegistry {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int HTTP_TIMEOUT = 10000;

    private String organisationRegistryUrl;
    private WebClient orgRegisterClient;
    private final int maxRetryAttempts;


    public OrganisationRegistryImpl(
            @Value("${organisation.registry.url:https://tjenester.entur.org/organisations/v1/organisations/}") String organisationRegistryUrl,
            @Value("${organisation.registry.retry.max:3}") int maxRetryAttempts,
            @Autowired WebClient orgRegisterClient
    ) {
        this.organisationRegistryUrl = organisationRegistryUrl;
        this.orgRegisterClient = orgRegisterClient.mutate().clientConnector(new ReactorClientHttpConnector(HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, HTTP_TIMEOUT).doOnConnected(connection -> {
            connection.addHandlerLast(new ReadTimeoutHandler(HTTP_TIMEOUT, TimeUnit.MILLISECONDS));
            connection.addHandlerLast(new WriteTimeoutHandler(HTTP_TIMEOUT, TimeUnit.MILLISECONDS));
        }))).build();

        this.maxRetryAttempts = maxRetryAttempts;
    }

    @Override
    @Cacheable("organisation")
    public Organisation getOrganisation(String organisationId) {
        try {
            return orgRegisterClient.get()
                    .uri(organisationRegistryUrl + organisationId)
                    .header("Et-Client-Name", "entur-nplan")
                    .retrieve()
                    .bodyToMono(Organisation.class)
                    .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(1)).filter(is5xx))
                    .block(Duration.ofMillis(HTTP_TIMEOUT));
        } catch (HttpClientErrorException ex) {
            logger.warn("Exception while trying to fetch organisation: " + organisationId + " : " + ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    @Cacheable("organisations")
    public List<Organisation> getOrganisations() {
        try {
            return orgRegisterClient.get()
                    .uri(organisationRegistryUrl)
                    .header("Et-Client-Name", "entur-nplan")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Organisation>>() {})
                    .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(1)).filter(is5xx))
                    .block(Duration.ofMillis(HTTP_TIMEOUT));
        } catch (HttpClientErrorException ex) {
            logger.warn("Exception while trying to fetch all organisations");
            return Collections.emptyList();
        }
    }

    protected static final Predicate<Throwable> is5xx =
            throwable -> throwable instanceof WebClientResponseException && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();


    /**
     * Return provided operatorRef if valid, else throw exception.
     */
    public String getVerifiedOperatorRef(String operatorRef) {
        if (StringUtils.isEmpty(operatorRef)) {
            return null;
        }
        Organisation organisation = getOrganisation(operatorRef);
        Preconditions.checkArgument(organisation != null, "Organisation with ref %s not found in organisation registry", operatorRef);
        Preconditions.checkArgument(organisation.getOperatorNetexId() != null, CodedError.fromErrorCode(ErrorCodeEnumeration.ORGANISATION_NOT_VALID_OPERATOR),"Organisation with ref %s is not a valid operator", operatorRef);
        return operatorRef;
    }

    /**
     * Return provided authorityRef if valid, else throw exception.
     */
    public String getVerifiedAuthorityRef(String authorityRef) {
        if (StringUtils.isEmpty(authorityRef)) {
            return null;
        }
        Organisation organisation = getOrganisation(authorityRef);
        Preconditions.checkArgument(organisation != null, "Organisation with ref %s not found in organisation registry", authorityRef);
        Preconditions.checkArgument(organisation.getAuthorityNetexId() != null, "Organisation with ref %s is not a valid authority", authorityRef);
        return authorityRef;
    }

}
