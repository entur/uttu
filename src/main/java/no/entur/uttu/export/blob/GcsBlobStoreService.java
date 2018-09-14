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

package no.entur.uttu.export.blob;

import com.google.cloud.storage.Storage;
import org.rutebanken.helper.gcp.BlobStoreHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Service
@Profile("gcs-blobstore")
public class GcsBlobStoreService implements BlobStoreService {


    @Value("${blobstore.gcs.credential.path}")
    private String credentialPath;


    @Value("${blobstore.gcs.project.id}")
    private String projectId;

    @Value("${blobstore.gcs.container.name}")
    private String containerName;

    private Storage storage;

    @PostConstruct
    private void init() {
        storage = BlobStoreHelper.getStorage(credentialPath, projectId);
    }

    public void uploadBlob(String name, boolean makePublic, InputStream inputStream) {
        BlobStoreHelper.uploadBlobWithRetry(storage, containerName, name, inputStream, makePublic);
    }


}

