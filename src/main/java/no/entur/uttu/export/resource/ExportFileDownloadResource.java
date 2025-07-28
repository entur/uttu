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

package no.entur.uttu.export.resource;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.file.Paths;
import no.entur.uttu.config.Context;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.repository.ExportRepository;
import org.rutebanken.helper.storage.repository.BlobStoreRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Path("/{providerCode}/export/")
public class ExportFileDownloadResource {

  private final ExportRepository exportRepository;

  private final BlobStoreRepository blobStoreRepository;

  public ExportFileDownloadResource(
    ExportRepository exportRepository,
    BlobStoreRepository blobStoreRepository
  ) {
    this.exportRepository = exportRepository;
    this.blobStoreRepository = blobStoreRepository;
  }

  @GET
  @Path("{id}/download")
  @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
  @PreAuthorize("@userContextService.hasAccessToProvider(#providerCode)")
  public Response downloadExportFile(
    @PathParam("providerCode") String providerCode,
    @PathParam("id") String exportId
  ) {
    Context.setProvider(providerCode);
    Export export = exportRepository.findByNetexIdAndProviderCode(exportId, providerCode);
    if (export == null) {
      throw new EntityNotFoundException("No export with id= " + exportId + " found");
    }
    if (!StringUtils.hasText(export.getFileName())) {
      throw new EntityNotFoundException(
        "Export with id= " + exportId + " does not have a reference to export file"
      );
    }

    InputStream content = blobStoreRepository.getBlob(export.getFileName());

    String fileNameOnly = Paths.get(export.getFileName()).getFileName().toString();

    return Response
      .ok(content, MediaType.APPLICATION_OCTET_STREAM)
      .header("content-disposition", "attachment; filename = " + fileNameOnly)
      .build();
  }
}
