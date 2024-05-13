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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import no.entur.uttu.export.linestatistics.ExportedLineStatisticsService;
import no.entur.uttu.export.messaging.MessagingService;
import no.entur.uttu.export.netex.DataSetProducer;
import no.entur.uttu.export.netex.NetexExporter;
import no.entur.uttu.model.Line;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.model.job.ExportMessage;
import no.entur.uttu.model.job.SeverityEnumeration;
import no.entur.uttu.util.ExportUtil;
import org.apache.commons.io.IOUtils;
import org.rutebanken.helper.storage.repository.BlobStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExportService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final NetexExporter exporter;

  private final BlobStoreRepository blobStoreRepository;

  private final MessagingService messagingService;

  @Value("${export.working.folder:tmp}")
  private String workingFolder;

  @Value("${export.blob.folder:inbound/uttu/}")
  private String exportFolder;

  @Value("${export.blob.filenameSuffix:-flexible-lines}")
  private String exportedFilenameSuffix;

  public ExportService(
    NetexExporter exporter,
    BlobStoreRepository blobStoreRepository,
    MessagingService messagingService
  ) {
    this.exporter = exporter;
    this.blobStoreRepository = blobStoreRepository;
    this.messagingService = messagingService;
  }

  public void exportDataSet(Export export) {
    export.checkPersistable();

    logger.info("Starting {}", export);

    try (DataSetProducer dataSetProducer = new DataSetProducer(workingFolder)) {
      boolean validateAgainstSchema = true;
      List<Line> exportedLines = exporter.exportDataSet(
        export,
        dataSetProducer,
        validateAgainstSchema
      );

      InputStream dataSetStream = dataSetProducer.buildDataSet();
      byte[] bytes = IOUtils.toByteArray(dataSetStream);
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

      if (!export.isDryRun() && !exportHasErrors(export)) {
        String exportedDataSetFilename = ExportUtil.createExportedDataSetFilename(
          export.getProvider(),
          exportedFilenameSuffix
        );
        String blobName = exportFolder + exportedDataSetFilename;
        blobStoreRepository.uploadBlob(blobName, bis);
        bis.reset();
        // notify Marduk that a new export is available
        messagingService.notifyExport(
          export.getProvider().getCode().toLowerCase(),
          exportedDataSetFilename
        );
        exportedLines
          .stream()
          .map(ExportedLineStatisticsService::toExportedLineStatistics)
          .forEach(export::addExportedLineStatistics);
      }
      export.setFileName(exportFolder + ExportUtil.createBackupDataSetFilename(export));
      blobStoreRepository.uploadBlob(export.getFileName(), bis);
    } catch (CodedIllegalArgumentException iae) {
      ExportMessage msg = new ExportMessage(SeverityEnumeration.ERROR, iae.getCode());
      export.addMessage(msg);
      logger.info(
        "{} Export failed with exception: {}",
        export.identity(),
        iae.getMessage(),
        iae
      );
    } catch (IllegalArgumentException iae) {
      ExportMessage msg = new ExportMessage(SeverityEnumeration.ERROR, iae.getMessage());
      export.addMessage(msg);
      logger.info(
        "{} Export failed with exception: {}",
        export.identity(),
        iae.getMessage(),
        iae
      );
    } catch (Exception e) {
      ExportMessage msg = new ExportMessage(
        SeverityEnumeration.ERROR,
        "Export failed with exception {0} : {1}",
        e.getClass().getSimpleName(),
        e.getMessage()
      );
      export.addMessage(msg);
      logger.warn(msg.getMessage(), e);
    }

    export.markAsFinished();
    logger.info("Completed {}", export);
  }

  private boolean exportHasErrors(Export export) {
    return export
      .getMessages()
      .stream()
      .anyMatch(message -> message.getSeverity().equals(SeverityEnumeration.ERROR));
  }
}
