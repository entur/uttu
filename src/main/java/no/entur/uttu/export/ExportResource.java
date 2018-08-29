package no.entur.uttu.export;


import io.swagger.annotations.Api;
import no.entur.uttu.config.Context;
import no.entur.uttu.export.blob.BlobStoreService;
import no.entur.uttu.export.netex.DataSetProducer;
import no.entur.uttu.export.netex.NetexExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;
import java.io.InputStream;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;

@Component
@Api
@Path("/export")
public class ExportResource {

    @Autowired
    private NetexExporter exporter;

    @Autowired
    private BlobStoreService blobStoreService;
    @Value("${export.working.folder:tmp}")
    private String workingFolder;

    @POST
    @Path("{providerId}")
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerId)")
    public void exportDataSet(@PathParam("providerId") Long providerId) {
        Context.setProvider(providerId);

        try (DataSetProducer dataSetProducer = new DataSetProducer(workingFolder)) {

            exporter.exportDataSet(providerId, dataSetProducer);

            InputStream dataSetStream = dataSetProducer.buildDataSet();

// TODO
            String blobName = "outbound/netex/rb_" + providerId + "_flexible_lines.zip";
            blobStoreService.uploadBlob(blobName, true, dataSetStream);
        } catch (IOException ioe) {
            throw new ExportException("Export failed with exception: " + ioe.getMessage(), ioe);
        }

    }
}
