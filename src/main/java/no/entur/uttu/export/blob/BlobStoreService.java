package no.entur.uttu.export.blob;

import java.io.InputStream;

public interface BlobStoreService {
    void uploadBlob(String name, boolean makePublic, InputStream inputStream);
}
