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

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;

import io.swagger.annotations.Api;
import java.io.InputStream;
import java.nio.file.Paths;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import no.entur.uttu.config.Context;
import no.entur.uttu.export.blob.BlobStoreService;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.repository.ExportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Api
@Path("/{providerCode}/export/")
public class ExportFileDownloadResource {

  @Autowired
  private ExportRepository exportRepository;

  @Autowired
  private BlobStoreService blobStoreService;

  @GET
  @Path("{id}/download")
  @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
  @PreAuthorize(
    "hasRole('" +
    ROLE_ROUTE_DATA_ADMIN +
    "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" +
    ROLE_ROUTE_DATA_EDIT +
    "',#providerCode)"
  )
  public Response downloadExportFile(
    @PathParam("providerCode") String providerCode,
    @PathParam("id") String exportId
  ) {
    Context.setProvider(providerCode);
    try {
      Export export = exportRepository.findByNetexIdAndProviderCode(
        exportId,
        providerCode
      );
      if (export == null) {
        throw new EntityNotFoundException("No export with id= " + exportId + " found");
      }
      if (StringUtils.isEmpty(export.getFileName())) {
        throw new EntityNotFoundException(
          "Export with id= " + exportId + " does not have a reference to export file"
        );
      }

      InputStream content = blobStoreService.downloadBlob(export.getFileName());

      String fileNameOnly = Paths.get(export.getFileName()).getFileName().toString();

      return Response
        .ok(content, MediaType.APPLICATION_OCTET_STREAM)
        .header("content-disposition", "attachment; filename = " + fileNameOnly)
        .build();
    } finally {
      Context.clear();
    }
  }
}
