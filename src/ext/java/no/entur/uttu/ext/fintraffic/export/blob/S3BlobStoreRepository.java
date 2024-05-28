package no.entur.uttu.ext.fintraffic.export.blob;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.rutebanken.helper.storage.BlobAlreadyExistsException;
import org.rutebanken.helper.storage.repository.BlobStoreRepository;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.BlockingInputStreamAsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Publisher;

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

  private final S3AsyncClient s3AsyncClient;

  private String containerName;

  public S3BlobStoreRepository(S3AsyncClient s3AsyncClient) {
    this.s3AsyncClient = Objects.requireNonNull(s3AsyncClient);
  }

  @Override
  public InputStream getBlob(String objectName) {
    return s3AsyncClient
      .getObject(
        GetObjectRequest.builder().bucket(containerName).key(objectName).build(),
        AsyncResponseTransformer.toBlockingInputStream()
      )
      .join();
  }

  @Override
  public long uploadBlob(String objectName, InputStream inputStream) {
    return uploadBlob(objectName, inputStream, null);
  }

  @Override
  public long uploadBlob(String objectName, InputStream inputStream, String contentType) {
    BlockingInputStreamAsyncRequestBody body = AsyncRequestBody.forBlockingInputStream(
      null
    ); // 'null' indicates a stream will be provided later.

    CompletableFuture<PutObjectResponse> responseFuture = s3AsyncClient.putObject(
      r -> {
        r.bucket(containerName).key(objectName);
        if (contentType != null) {
          r.contentType(contentType);
        }
      },
      body
    );

    // Provide the stream of data to be uploaded.
    long v = body.writeInputStream(inputStream);

    PutObjectResponse r = responseFuture.join(); // Wait for the response.
    return UNKNOWN_LATEST_VERSION;
  }

  @Override
  public long uploadNewBlob(String objectName, InputStream inputStream) {
    if (objectExists(containerName, objectName)) {
      throw new BlobAlreadyExistsException(
        "Blob '" + objectName + "' already exists in bucket '" + containerName + "'"
      );
    } else {
      return uploadBlob(objectName, inputStream);
    }
  }

  private boolean objectExists(String containerName, String objectName) {
    return s3AsyncClient
      .headObject(headObjectRequest ->
        headObjectRequest.bucket(containerName).key(objectName)
      )
      .exceptionally(throwable -> null)
      .thenApply(Objects::nonNull)
      .join();
  }

  @Override
  public void copyBlob(
    String sourceContainerName,
    String sourceObjectName,
    String targetContainerName,
    String targetObjectName
  ) {
    s3AsyncClient
      .copyObject(copyObjectRequest ->
        copyObjectRequest
          .sourceBucket(sourceContainerName)
          .sourceKey(sourceObjectName)
          .destinationBucket(targetContainerName)
          .destinationKey(targetObjectName)
      )
      .join();
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
    iteratePrefix(
      sourceContainerName,
      prefix,
      s3Objects -> {
        for (S3Object s3Object : s3Objects) {
          String targetKey = targetPrefix + trimPrefix(prefix, s3Object.key());
          copyBlob(sourceContainerName, s3Object.key(), targetContainerName, targetKey);
        }
        return null;
      }
    );
  }

  private String trimPrefix(String prefix, String s) {
    if (s.startsWith(prefix)) {
      return s.substring(prefix.length());
    }
    return s;
  }

  @Override
  public boolean delete(String objectName) {
    s3AsyncClient.deleteObject(r -> r.bucket(containerName).key(objectName)).join();

    return !objectExists(containerName, objectName);
  }

  @Override
  public boolean deleteAllFilesInFolder(String folder) {
    for (boolean pageResult : iteratePrefix(
      containerName,
      folder,
      s3Objects -> {
        List<ObjectIdentifier> objectIdentifiers = s3Objects
          .stream()
          .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
          .toList();
        return s3AsyncClient
          .deleteObjects(deleteObjectsRequest ->
            deleteObjectsRequest.delete(delete -> delete.objects(objectIdentifiers))
          )
          .join()
          .errors()
          .isEmpty();
      }
    )) {
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
    ListObjectsV2Publisher publisher = s3AsyncClient.listObjectsV2Paginator(req ->
      req.bucket(containerName).prefix(prefix)
    );

    List<U> pageResults = new ArrayList<>();
    CompletableFuture<Void> future = publisher.subscribe(res ->
      pageResults.add(mapper.apply(res.contents()))
    );
    try {
      future.get();
    } catch (InterruptedException | ExecutionException ignored) {
      // ignored on purpose
    }

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
