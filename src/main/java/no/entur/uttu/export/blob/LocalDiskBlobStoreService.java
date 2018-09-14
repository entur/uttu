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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Profile("local-blobstore")
public class LocalDiskBlobStoreService implements BlobStoreService {

    @Value("${blobstore.local.folder:files/blob}")
    private String baseFolder;

    @Override
    public void uploadBlob(String name, boolean makePublic, InputStream inputStream) {
        try {
            Path localPath = Paths.get(name);

            Path folder = Paths.get(baseFolder).resolve(localPath.getParent());
            Files.createDirectories(folder);

            Path fullPath = Paths.get(baseFolder).resolve(localPath);
            Files.deleteIfExists(fullPath);

            Files.copy(inputStream, fullPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
