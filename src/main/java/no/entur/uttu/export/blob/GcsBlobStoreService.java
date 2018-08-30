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

