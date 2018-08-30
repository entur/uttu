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
