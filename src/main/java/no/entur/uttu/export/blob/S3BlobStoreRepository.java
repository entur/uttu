package no.entur.uttu.export.blob;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.rutebanken.helper.storage.BlobAlreadyExistsException;
import org.rutebanken.helper.storage.BlobStoreException;
import org.rutebanken.helper.storage.model.BlobDescriptor;
import org.rutebanken.helper.storage.repository.BlobStoreRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

/**
 * <a href="https://aws.amazon.com/s3/">AWS S3</a> backed implementation of {@link BlobStoreRepository}.
 */
public class S3BlobStoreRepository implements BlobStoreRepository {

  /**
   * All file versions are always hardcoded to be zero since {@link BlobStoreRepository} is modeled after GCS which
   * provides a linear numeric value for object versioning while S3 uses
   * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag">E-Tags</a> which has to be enabled
   * separately. These two are incompatible and since the versioning number is not used in uttu it doesn't matter much
   * if this is hardcoded to be always zero.
   */
  private static final long UNKNOWN_LATEST_VERSION = 0;

  private final S3Client s3Client;

  private String containerName;

  public S3BlobStoreRepository(S3Client s3Client) {
    this.s3Client = Objects.requireNonNull(s3Client);
  }

  @Override
  public InputStream getBlob(String objectName) {
    return s3Client.getObject(
      GetObjectRequest.builder().bucket(containerName).key(objectName).build(),
      ResponseTransformer.toInputStream()
    );
  }

  @Override
  public long uploadBlob(BlobDescriptor blobDescriptor) {
    RequestBody body = null;
    try {
      body = RequestBody.fromBytes(blobDescriptor.inputStream().readAllBytes());
    } catch (IOException e) {
      throw new BlobStoreException("Failed to read all bytes from given InputStream", e);
    }

    s3Client.putObject(
      r -> {
        r.bucket(containerName).key(blobDescriptor.name());
        blobDescriptor.contentType().ifPresent(r::contentType);
        blobDescriptor.metadata().map(this::mimeEncodeValues).ifPresent(r::metadata);
      },
      body
    );
    return UNKNOWN_LATEST_VERSION;
  }

  /**
   * AWS S3 metadata is passed through HTTP headers which requires the values to be in MIME compatible US-ASCII
   * formatting.
   *
   * @param metadata Original metadata.
   * @return Updated metadata with encoded values.
   */
  private Map<String, String> mimeEncodeValues(Map<String, String> metadata) {
    Map<String, String> encodedMetadata = HashMap.newHashMap(metadata.size());
    for (Map.Entry<String, String> entry : metadata.entrySet()) {
      byte[] utf8Bytes = entry.getValue().getBytes(StandardCharsets.UTF_8);
      String base64Encoded = Base64.getEncoder().encodeToString(utf8Bytes);
      encodedMetadata.put(entry.getKey(), "=?UTF-8?B?" + base64Encoded + "?=");
    }
    return encodedMetadata;
  }

  @Override
  public long uploadBlob(String objectName, InputStream inputStream) {
    return uploadBlob(
      new BlobDescriptor(objectName, inputStream, Optional.empty(), Optional.empty())
    );
  }

  @Override
  public long uploadBlob(String objectName, InputStream inputStream, String contentType) {
    return uploadBlob(
      new BlobDescriptor(
        objectName,
        inputStream,
        Optional.of(contentType),
        Optional.empty()
      )
    );
  }

  @Override
  public long uploadNewBlob(String objectName, InputStream inputStream) {
    if (objectExists(containerName, objectName)) {
      throw new BlobAlreadyExistsException(
        "Blob '" + objectName + "' already exists in bucket '" + containerName + "'"
      );
    } else {
      return uploadBlob(
        new BlobDescriptor(objectName, inputStream, Optional.empty(), Optional.empty())
      );
    }
  }

  private boolean objectExists(String containerName, String objectName) {
    try {
      s3Client.headObject(
        headObjectRequest -> headObjectRequest.bucket(containerName).key(objectName)
      );
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }

  @Override
  public void copyBlob(
    String sourceContainerName,
    String sourceObjectName,
    String targetContainerName,
    String targetObjectName
  ) {
    s3Client.copyObject(
      copyObjectRequest ->
        copyObjectRequest
          .sourceBucket(sourceContainerName)
          .sourceKey(sourceObjectName)
          .destinationBucket(targetContainerName)
          .destinationKey(targetObjectName)
    );
  }

  @Override
  public void copyVersionedBlob(
    String sourceContainerName,
    String sourceObjectName,
    Long sourceVersion,
    String targetContainerName,
    String targetObjectName
  ) {
    // NOTE: S3 implementation does not support numeric versioning
    copyBlob(
      sourceContainerName,
      sourceObjectName,
      targetContainerName,
      targetObjectName
    );
  }

  @Override
  public void copyAllBlobs(
    String sourceContainerName,
    String prefix,
    String targetContainerName,
    String targetPrefix
  ) {
    iteratePrefix(sourceContainerName, prefix, s3Objects -> {
      for (S3Object s3Object : s3Objects) {
        String targetKey = targetPrefix + trimPrefix(prefix, s3Object.key());
        copyBlob(sourceContainerName, s3Object.key(), targetContainerName, targetKey);
      }
      return null;
    });
  }

  private String trimPrefix(String prefix, String s) {
    if (s.startsWith(prefix)) {
      return s.substring(prefix.length());
    }
    return s;
  }

  @Override
  public boolean delete(String objectName) {
    s3Client.deleteObject(r -> r.bucket(containerName).key(objectName));

    return !objectExists(containerName, objectName);
  }

  @Override
  public boolean deleteAllFilesInFolder(String folder) {
    for (boolean pageResult : iteratePrefix(containerName, folder, s3Objects -> {
      List<ObjectIdentifier> objectIdentifiers = s3Objects
        .stream()
        .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
        .toList();
      return s3Client
        .deleteObjects(
          deleteObjectsRequest ->
            deleteObjectsRequest.delete(delete -> delete.objects(objectIdentifiers))
        )
        .errors()
        .isEmpty();
    })) {
      if (!pageResult) {
        return false;
      }
    }
    return true;
  }

  private <U> List<U> iteratePrefix(
    String containerName,
    String prefix,
    Function<List<S3Object>, U> mapper
  ) {
    ListObjectsV2Iterable iterable = s3Client.listObjectsV2Paginator(
      req -> req.bucket(containerName).prefix(prefix)
    );

    List<U> pageResults = new ArrayList<>();
    iterable.forEach(a -> pageResults.add(mapper.apply(a.contents())));
    return pageResults;
  }

  /**
   * {@inheritDoc}
   * <p>
   * For this implementation container name maps to AWS S3 bucket name as-is.
   *
   * @param containerName Container name to use as source AWS S3 bucket name.
   */
  @Override
  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }
}
