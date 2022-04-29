package no.entur.uttu.organisation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrganisationRestResource {
    private final OrganisationRegistry organisationRegistry;

    public OrganisationRestResource(@Autowired OrganisationRegistry organisationRegistry) {
        this.organisationRegistry = organisationRegistry;
    }

    @GetMapping(value="/organisations", produces="application/json")
    @PreAuthorize("isAuthenticated()")
    public List<Organisation> getOrganisations() {
        return organisationRegistry.getOrganisations();
    }

    @GetMapping(value="/organisations/{id}", produces="application/json")
    @PreAuthorize("isAuthenticated()")
    public Organisation getOrganisation(@PathVariable String id) { return organisationRegistry.getOrganisation(id); }
}
