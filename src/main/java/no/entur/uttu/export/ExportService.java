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

package no.entur.uttu.export;

import no.entur.uttu.config.Context;
import no.entur.uttu.export.blob.BlobStoreService;
import no.entur.uttu.export.model.ExportException;
import no.entur.uttu.export.netex.DataSetProducer;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.NetexExporter;
import no.entur.uttu.util.FileNameUtil;
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
            NetexExportContext context = exporter.exportDataSet(providerCode, dataSetProducer, validateAgainstSchema);

            InputStream dataSetStream = dataSetProducer.buildDataSet();

// TODO
            String blobName = "outbound/netex/" + FileNameUtil.createDataSetFilename(context.provider);
            blobStoreService.uploadBlob(blobName, true, dataSetStream);


            // TODO remove me, tmp doing validation after export in order to see results
            exporter.exportDataSet(providerCode, dataSetProducer, true);


        } catch (IOException ioe) {
            throw new ExportException("Export failed with exception: " + ioe.getMessage(), ioe);
        }
    }
}
