package no.entur.uttu.export;


import io.swagger.annotations.Api;
import no.entur.uttu.config.Context;
import no.entur.uttu.export.blob.BlobStoreService;
import no.entur.uttu.export.model.ExportException;
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
@Path("/lines/{providerCode}")
public class ExportResource {

    // TODO use grapQL for this as well?

    @Autowired
    private NetexExporter exporter;

    @Autowired
    private BlobStoreService blobStoreService;
    @Value("${export.working.folder:tmp}")
    private String workingFolder;

    @POST
    @Path("/export")
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerCode)")
    public void exportDataSet(@PathParam("providerCode") String providerCode) {

        Context.setProvider(providerCode);
        try (DataSetProducer dataSetProducer = new DataSetProducer(workingFolder)) {


            boolean validateAgainstSchema = false; // TODO
            exporter.exportDataSet(providerCode, dataSetProducer, validateAgainstSchema);

            InputStream dataSetStream = dataSetProducer.buildDataSet();

// TODO
            String blobName = "outbound/netex/rb_" + providerCode + "_flexible_lines.zip";
            blobStoreService.uploadBlob(blobName, true, dataSetStream);
        } catch (IOException ioe) {
            throw new ExportException("Export failed with exception: " + ioe.getMessage(), ioe);
        } finally {
            Context.clear();
        }

    }

    @POST
    @Path("/validate")
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerCode)")
    public void validateDataSet(@PathParam("providerCode") String providerCode) {
        Context.setProvider(providerCode);

        try (DataSetProducer dataSetProducer = new DataSetProducer(workingFolder)) {
            exporter.exportDataSet(providerCode, dataSetProducer, true);
            dataSetProducer.buildDataSet();
        } catch (IOException ioe) {
            throw new ExportException("Export failed with exception: " + ioe.getMessage(), ioe);
        } finally {
            Context.clear();
        }

    }
}
