package no.entur.uttu.organisation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrganisationRegistry {

    private String organisationRegistryUrl;

    public OrganisationRegistry(@Value("${organisation.registry.url:https://tjenester.entur.org/organisations/v1/organisations/}") String organisationRegistryUrl) {
        this.organisationRegistryUrl = organisationRegistryUrl;
    }

    private RestTemplate restTemplate = new RestTemplate();

    public Organisation getOrganisation(String organisationId) {
        ResponseEntity<Organisation> rateResponse =
                restTemplate.getForEntity(organisationRegistryUrl + organisationId,
                        Organisation.class);
        return rateResponse.getBody();
    }

}
