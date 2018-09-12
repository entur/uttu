package no.entur.uttu.organisation;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganisationRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String organisationRegistryUrl;

    public OrganisationRegistry(@Value("${organisation.registry.url:https://tjenester.entur.org/organisations/v1/organisations/}") String organisationRegistryUrl) {
        this.organisationRegistryUrl = organisationRegistryUrl;
    }

    private RestTemplate restTemplate = new RestTemplate();

    public Organisation getOrganisation(Long organisationId) {
        try {
            ResponseEntity<Organisation> rateResponse =
                    restTemplate.getForEntity(organisationRegistryUrl + organisationId,
                            Organisation.class);
            return rateResponse.getBody();
        } catch (HttpClientErrorException ex) {
            logger.warn("Exception while trying to fetch operator: " + organisationId + " : " + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Return provided operatorRef if valid, else throw exception.
     */
    public Long getVerifiedOperatorRef(Long operatorRef) {
        if (operatorRef == null) {
            return null;
        }
        Organisation organisation = getOrganisation(operatorRef);
        Preconditions.checkArgument(organisation != null, "Organisation with ref %s not found in organisation registry", operatorRef);
        Preconditions.checkArgument(organisation.getOperatorNetexId() != null, "Organisation with ref %s is not a valid operator", operatorRef);
        return operatorRef;
    }

    /**
     * Return provided authorityRef if valid, else throw exception.
     */
    public Long getVerifiedAuthorityRef(Long authorityRef) {
        if (authorityRef == null) {
            return null;
        }
        Organisation organisation = getOrganisation(authorityRef);
        Preconditions.checkArgument(organisation != null, "Organisation with ref %s not found in organisation registry", authorityRef);
        Preconditions.checkArgument(organisation.getAuthorityNetexId() != null, "Organisation with ref %s is not a valid authority", authorityRef);
        return authorityRef;
    }

}
