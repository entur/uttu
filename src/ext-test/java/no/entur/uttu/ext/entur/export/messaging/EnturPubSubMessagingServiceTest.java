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

package no.entur.uttu.ext.entur.export.messaging;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.pubsub.v1.PubsubMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import no.entur.uttu.UttuIntegrationTest;
import no.entur.uttu.export.messaging.spi.MessagingService;
import org.entur.pubsub.base.EnturGooglePubSubAdmin;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ActiveProfiles({ "test", "entur-pubsub-messaging-service" })
public class EnturPubSubMessagingServiceTest extends UttuIntegrationTest {

  public static final String TEST_CODESPACE = "rut";

  public static final String TEST_EXPORT_FILE_NAME = "netex.zip";
  private static PubSubEmulatorContainer pubsubEmulator;

  @Autowired
  private MessagingService messagingService;

  @Autowired
  private PubSubTemplate pubSubTemplate;

  @Autowired
  PubSubSubscriberTemplate subscriberTemplate;

  @Autowired
  private EnturGooglePubSubAdmin enturGooglePubSubAdmin;

  @Value("${export.notify.queue.name:FlexibleLinesExportQueue}")
  private String queueName;

  @DynamicPropertySource
  static void emulatorProperties(DynamicPropertyRegistry registry) {
    registry.add(
      "spring.cloud.gcp.pubsub.emulator-host",
      pubsubEmulator::getEmulatorEndpoint
    );
    registry.add("spring.cloud.gcp.project-id", () -> "test");
    registry.add("spring.cloud.gcp.pubsub.project-id", () -> "test");
    registry.add("export.notify.enabled", () -> true);
    registry.add("export.notify.queue.name", () -> "FlexibleLinesExportQueue");
  }

  @BeforeClass
  public static void init() {
    pubsubEmulator =
      new PubSubEmulatorContainer(
        DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:emulators")
      );
    pubsubEmulator.start();
  }

  @AfterClass
  public static void tearDown() {
    pubsubEmulator.stop();
  }

  @Before
  public void setup() {
    enturGooglePubSubAdmin.createSubscriptionIfMissing(queueName);
  }

  @After
  public void teardown() {
    enturGooglePubSubAdmin.deleteAllSubscriptions();
  }

  // By default, autoconfiguration will initialize application default credentials.
  // For testing purposes, don't use any credentials. Bootstrap w/ NoCredentialsProvider.
  @TestConfiguration
  static class PubSubEmulatorConfiguration {

    @Bean
    CredentialsProvider googleCredentials() {
      return NoCredentialsProvider.create();
    }
  }

  @Test
  public void testNotifyExport() {
    messagingService.notifyExport(TEST_CODESPACE, TEST_EXPORT_FILE_NAME);

    List<PubsubMessage> messages = pubSubTemplate.pullAndAck(queueName, 1, false);
    Assert.assertEquals(messages.size(), 1);
    PubsubMessage pubsubMessage = messages.get(0);
    String codespace = pubsubMessage
      .getAttributesMap()
      .get(EnturPubSubMessagingService.HEADER_CHOUETTE_REFERENTIAL);
    Assert.assertEquals("rb_" + TEST_CODESPACE, codespace);
    Assert.assertEquals(
      TEST_EXPORT_FILE_NAME,
      pubsubMessage.getData().toString(StandardCharsets.UTF_8)
    );
  }
}
