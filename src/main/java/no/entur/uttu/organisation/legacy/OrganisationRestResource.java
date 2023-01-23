package no.entur.uttu.organisation.legacy;

import io.swagger.annotations.Api;
import no.entur.uttu.organisation.OrganisationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Api
@Path("/organisations")
public class OrganisationRestResource {
    private final EnturLegacyOrganisationRegistry organisationRegistry;

    public OrganisationRestResource(@Autowired EnturLegacyOrganisationRegistry organisationRegistry) {
        this.organisationRegistry = organisationRegistry;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PreAuthorize("isAuthenticated()")
    public List<Organisation> getOrganisations() {
        return organisationRegistry.lookupOrganisations();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @PreAuthorize("isAuthenticated()")
    public Organisation getOrganisation(@PathVariable String id) { return organisationRegistry.lookupOrganisation(id); }
}
