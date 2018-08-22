package no.entur.uttu.export.blob;

import com.google.cloud.storage.Storage;
import org.rutebanken.helper.gcp.BlobStoreHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class BlobStoreService {

    @Autowired
    private Storage storage;

    @Value("${blobstore.gcs.container.name}")
    private String containerName;


    public void uploadBlob(String name, boolean makePublic, InputStream inputStream) {
        BlobStoreHelper.uploadBlobWithRetry(storage, containerName, name, inputStream, makePublic);
    }


}

