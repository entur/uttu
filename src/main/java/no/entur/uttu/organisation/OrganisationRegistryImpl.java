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

import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OrganisationRegistryImpl implements OrganisationRegistry {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String organisationRegistryUrl;
    private WebClient orgRegisterClient;

    public OrganisationRegistryImpl(
            @Value("${organisation.registry.url:https://tjenester.entur.org/organisations/v1/organisations/}") String organisationRegistryUrl,
            @Autowired WebClient orgRegisterClient
    ) {
        this.organisationRegistryUrl = organisationRegistryUrl;
        this.orgRegisterClient = orgRegisterClient;
    }

    public Organisation getOrganisation(String organisationId) {
        try {
            return orgRegisterClient.get()
                    .uri(organisationRegistryUrl + organisationId)
                    .retrieve()
                    .bodyToMono(Organisation.class)
                    .block();
        } catch (HttpClientErrorException ex) {
            logger.warn("Exception while trying to fetch organisation: " + organisationId + " : " + ex.getMessage(), ex);
            return null;
        }
    }

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
