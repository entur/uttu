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
 *
 */

package no.entur.uttu.export.blob;

import org.rutebanken.helper.storage.repository.BlobStoreRepository;
import org.rutebanken.helper.storage.repository.LocalDiskBlobStoreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "local-disk-blobstore" })
public class LocalDiskBlobStoreRepositoryConfig {

  @Bean
  BlobStoreRepository blobStoreRepository(
    @Value("${blobstore.local.folder:files/blob}") String baseFolder,
    @Value("${blobstore.gcs.container.name}") String containerName
  ) {
    LocalDiskBlobStoreRepository localDiskBlobStoreRepository =
      new LocalDiskBlobStoreRepository(baseFolder);
    localDiskBlobStoreRepository.setContainerName(containerName);
    return localDiskBlobStoreRepository;
  }
}
