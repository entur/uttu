package no.entur.uttu.export.blob;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import no.entur.uttu.UttuIntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rutebanken.helper.storage.BlobAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@Testcontainers
@ActiveProfiles({ "s3-blobstore" })
public class S3BlobStoreRepositoryTest extends UttuIntegrationTest {

  private static final String TEST_BUCKET = "test-blobstore-exports";

  private static LocalStackContainer localStack;

  @Autowired
  private S3AsyncClient s3AsyncClient;

  @Autowired
  private S3BlobStoreRepository blobStore;

  @DynamicPropertySource
  static void blobStoreProperties(DynamicPropertyRegistry registry) {
    registry.add(
      "blobstore.s3.endpointOverride",
      () -> localStack.getEndpointOverride(Service.S3)
    );
    registry.add("blobstore.s3.region", () -> localStack.getRegion());
    registry.add("blobstore.s3.accessKeyId", () -> localStack.getAccessKey());
    registry.add("blobstore.s3.secretKey", () -> localStack.getSecretKey());
    registry.add("blobstore.s3.bucket", () -> TEST_BUCKET);
  }

  private void createBucket(String bucketName)
    throws ExecutionException, InterruptedException {
    s3AsyncClient
      .headBucket(request -> request.bucket(bucketName))
      .exceptionally(throwable -> {
        if (throwable.getCause() instanceof NoSuchBucketException) {
          s3AsyncClient
            .createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
            .join();
        }
        return null;
      })
      .get();
  }

  @BeforeClass
  public static void init() {
    localStack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.4.0"))
        .withServices(Service.S3)
        .withEnv("DEFAULT_REGION", Region.EU_NORTH_1.id());
    localStack.start();
  }

  @Before
  public void setUp() throws Exception {
    createBucket(TEST_BUCKET);
  }

  @Test
  public void canRoundtripAFile() throws Exception {
    String original = "Hello, BlobStore!";
    assertBlobExists(TEST_BUCKET, "myblob", false);
    blobStore.uploadBlob("myblob", new ByteArrayInputStream(original.getBytes()));
    assertBlobExists(TEST_BUCKET, "myblob", true);
    Assert.assertEquals(original, new String(blobStore.getBlob("myblob").readAllBytes()));
    Assert.assertTrue(blobStore.delete("myblob"));
  }

  @Test(expected = BlobAlreadyExistsException.class)
  public void cannotOverWriteExistingObject() throws Exception {
    String original = "another bytes the dust";
    assertBlobExists(TEST_BUCKET, "anotherblob", false);
    blobStore.uploadNewBlob("anotherblob", new ByteArrayInputStream(original.getBytes()));
    assertBlobExists(TEST_BUCKET, "anotherblob", true);
    blobStore.uploadNewBlob(
      "anotherblob",
      new ByteArrayInputStream("something silly".getBytes())
    );
  }

  /**
   * Implementation note: Content type can be set for S3, but it does not mean much when downloading. The header
   * does persist in object metadata though.
   */
  @Test
  public void canSetContentTypeForUpload() throws Exception {
    String contentType = "application/json";
    blobStore.uploadBlob(
      "json",
      new ByteArrayInputStream("{\"key\":false}".getBytes()),
      contentType
    );
    assertBlobExists(TEST_BUCKET, "json", true);
    HeadObjectResponse response = s3AsyncClient
      .headObject(request -> request.bucket(TEST_BUCKET).key("json"))
      .join();
    Assert.assertEquals(contentType, response.contentType());
  }

  @Test
  public void canCopyContentBetweenBuckets() throws Exception {
    String targetBucket = "another-bucket";
    String content = "1";
    createBucket(targetBucket);
    blobStore.uploadBlob("smallfile", asStream(content));
    blobStore.copyBlob(TEST_BUCKET, "smallfile", targetBucket, "tinyfile");
    blobStore.setContainerName(targetBucket);
    Assert.assertEquals(
      content,
      new String(blobStore.getBlob("tinyfile").readAllBytes())
    );
    blobStore.setContainerName(TEST_BUCKET);
  }

  /**
   * Implementation note: Version is no-op with S3 as the current interface models it based on GCP's blob storage
   * semantics. This is effectively the same as copying the blob normally.
   * @throws Exception
   */
  @Test
  public void canCopyVersionedContentBetweenBuckets() throws Exception {
    String targetBucket = "yet-another-bucket";
    String content = "a";
    createBucket(targetBucket);
    blobStore.uploadBlob("minusculefile", asStream(content));
    blobStore.copyVersionedBlob(
      TEST_BUCKET,
      "minusculefile",
      -1_000_000L,
      targetBucket,
      "barelyworthmentioningfile"
    );
    blobStore.setContainerName(targetBucket);
    Assert.assertEquals(
      content,
      new String(blobStore.getBlob("barelyworthmentioningfile").readAllBytes())
    );
    blobStore.setContainerName(TEST_BUCKET);
  }

  @Test
  public void canCopyAllBlobsWithSharedPrefix() throws Exception {
    String targetBucket = "one-more-bucket";
    createBucket(targetBucket);
    blobStore.uploadBlob("things/a", asStream("a"));
    blobStore.uploadBlob("things/b", asStream("b"));
    blobStore.uploadBlob("things/c", asStream("c"));
    blobStore.uploadBlob("stuff/d", asStream("d"));
    blobStore.copyAllBlobs(TEST_BUCKET, "things", targetBucket, "bits");
    assertBlobExists(targetBucket, "bits/a", true);
    assertBlobExists(targetBucket, "bits/b", true);
    assertBlobExists(targetBucket, "bits/c", true);
    assertBlobExists(targetBucket, "stuff/d", false);
  }

  private static @NotNull ByteArrayInputStream asStream(String source) {
    return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
  }

  private void assertBlobExists(String bucket, String key, boolean exists)
    throws ExecutionException, InterruptedException {
    HeadObjectResponse response = s3AsyncClient
      .headObject(request -> request.bucket(bucket).key(key))
      .exceptionally(throwable -> null)
      .get();
    if (!exists && response != null) {
      Assert.fail(bucket + " / " + key + " exists");
    }
    if (exists && response == null) {
      Assert.fail(bucket + " / " + key + " does not exist");
    }
  }
}
