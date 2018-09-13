package no.entur.uttu.export;

import no.entur.uttu.config.Context;
import no.entur.uttu.export.blob.BlobStoreService;
import no.entur.uttu.export.model.ExportException;
import no.entur.uttu.export.netex.DataSetProducer;
import no.entur.uttu.export.netex.NetexExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class ExportService {

    @Autowired
    private NetexExporter exporter;

    @Autowired
    private BlobStoreService blobStoreService;
    @Value("${export.working.folder:tmp}")
    private String workingFolder;

    public void exportDataSet() {
        try (DataSetProducer dataSetProducer = new DataSetProducer(workingFolder)) {

            String providerCode = Context.getVerifiedProviderCode();
            boolean validateAgainstSchema = false; // TODO
            exporter.exportDataSet(providerCode, dataSetProducer, validateAgainstSchema);

            InputStream dataSetStream = dataSetProducer.buildDataSet();

// TODO
            String blobName = "outbound/netex/rb_" + providerCode.toLowerCase() + "_flexible_lines.zip";
            blobStoreService.uploadBlob(blobName, true, dataSetStream);


            // TODO remove me, tmp doing validation after export in order to see results
            exporter.exportDataSet(providerCode, dataSetProducer, true);


        } catch (IOException ioe) {
            throw new ExportException("Export failed with exception: " + ioe.getMessage(), ioe);
        }
    }
}
