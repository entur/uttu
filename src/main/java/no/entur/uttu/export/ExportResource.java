package no.entur.uttu.export;


import io.swagger.annotations.Api;
import no.entur.uttu.config.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;

@Component
@Api
@Path("/{providerCode}")
public class ExportResource {


    @Autowired
    private ExportService exportService;

    @POST
    @Path("/export")
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerCode)")
    public void exportDataSet(@PathParam("providerCode") String providerCode) {

        Context.setProvider(providerCode);
        try {
            exportService.exportDataSet();
        } finally {
            Context.clear();
        }

    }

}
