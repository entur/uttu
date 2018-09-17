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

import no.entur.uttu.export.blob.BlobStoreService;
import no.entur.uttu.export.model.ExportException;
import no.entur.uttu.export.netex.DataSetProducer;
import no.entur.uttu.export.netex.NetexExporter;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.util.FileNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

@Component
public class ExportService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NetexExporter exporter;

    @Autowired
    private BlobStoreService blobStoreService;
    @Value("${export.working.folder:tmp}")
    private String workingFolder;

    @Value("${export.days.historic.default:2}")
    private int historicDaysDefault;

    @Value("${export.days.future.default:185}")
    private int futureDaysDefault;

    public void exportDataSet(Export export) {
        setExportDefaults(export);

        logger.info("Starting {}", export);

        try (DataSetProducer dataSetProducer = new DataSetProducer(workingFolder)) {

            boolean validateAgainstSchema = true;
            exporter.exportDataSet(export, dataSetProducer, validateAgainstSchema);

            InputStream dataSetStream = dataSetProducer.buildDataSet();


            String blobName = "outbound/netex/" + FileNameUtil.createDataSetFilename(export.getProvider());
            blobStoreService.uploadBlob(blobName, true, dataSetStream);

            export.markAsFinished();
        } catch (IOException ioe) {
            throw new ExportException("Export failed with exception: " + ioe.getMessage(), ioe);
        }

        logger.info("Completed {}", export);
    }

    private void setExportDefaults(Export export) {
        LocalDate today = LocalDate.now();
        if (export.getFromDate() == null) {
            export.setFromDate(today.minusDays(historicDaysDefault));
        }
        if (export.getToDate() == null) {
            export.setToDate(today.plusDays(futureDaysDefault));
        }
        export.checkPersistable();
    }
}
