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

import java.net.URI;
import org.rutebanken.helper.storage.repository.BlobStoreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3CrtAsyncClientBuilder;

@Configuration
@Profile("s3-blobstore")
public class S3BlobStoreRepositoryConfig {

  @Value("${blobstore.s3.region}")
  private String region;

  @Value("${blobstore.s3.endpointOverride:#{null}}")
  private String endpointOverride;

  @Bean
  BlobStoreRepository blobStoreRepository(
    @Value("${blobstore.s3.bucket}") String containerName,
    S3AsyncClient s3AsyncClient
  ) {
    S3BlobStoreRepository s3BlobStoreRepository = new S3BlobStoreRepository(
      s3AsyncClient
    );
    s3BlobStoreRepository.setContainerName(containerName);
    return s3BlobStoreRepository;
  }

  @Profile("local | test")
  @Bean
  public AwsCredentialsProvider localCredentials(
    @Value("blobstore.s3.accessKeyId") String accessKeyId,
    @Value("blobstore.s3.secretKey") String secretKey
  ) {
    return StaticCredentialsProvider.create(
      AwsBasicCredentials.create(accessKeyId, secretKey)
    );
  }

  @Profile("!local & !test")
  @Bean
  public AwsCredentialsProvider cloudCredentials() {
    return DefaultCredentialsProvider.create();
  }

  @Bean
  public S3AsyncClient s3AsyncClient(AwsCredentialsProvider credentialsProvider) {
    S3CrtAsyncClientBuilder b = S3AsyncClient
      .crtBuilder()
      .credentialsProvider(credentialsProvider)
      .region(Region.of(region));
    if (endpointOverride != null) {
      b = b.endpointOverride(URI.create(endpointOverride));
    }
    return b.build();
  }
}
