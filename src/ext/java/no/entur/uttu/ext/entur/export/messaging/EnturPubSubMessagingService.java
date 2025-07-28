package no.entur.uttu.ext.entur.export.messaging;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import no.entur.uttu.config.Context;
import no.entur.uttu.export.messaging.spi.MessagingService;
import no.entur.uttu.util.ExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("entur-pubsub-messaging-service")
public class EnturPubSubMessagingService implements MessagingService {

  public static final String HEADER_CHOUETTE_REFERENTIAL =
    "RutebankenChouetteReferential";
  public static final String HEADER_USERNAME = "RutebankenUsername";
  public static final String HEADER_CORRELATION_ID = "RutebankenCorrelationId";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final PubSubTemplate pubSubTemplate;

  @Value("${export.notify.enabled:false}")
  private boolean enableNotification;

  @Value("${export.notify.queue.name:FlexibleLinesExportQueue}")
  private String queueName;

  public EnturPubSubMessagingService(PubSubTemplate pubSubTemplate) {
    this.pubSubTemplate = pubSubTemplate;
  }

  /**
   * Notify Marduk that a new Flexible Transport NeTex export has been uploaded.
   * @param codespace the current provider's codespace.
   */
  @Override
  public void notifyExport(final String codespace, String filename) {
    if (enableNotification) {
      Map<String, String> pubSubAttributes = new HashMap<>();
      pubSubAttributes.put(
        HEADER_CHOUETTE_REFERENTIAL,
        ExportUtil.getMigratedReferential(codespace)
      );
      pubSubAttributes.put(
        HEADER_USERNAME,
        Context.getVerifiedUsername() + " (via NPlan)"
      );
      pubSubAttributes.put(HEADER_CORRELATION_ID, UUID.randomUUID().toString());
      pubSubTemplate.publish(queueName, filename, pubSubAttributes);

      logger.debug("Sent export notification for codespace {}.", codespace);
    } else {
      logger.debug("Skipped export notification for codespace {}.", codespace);
    }
  }
}
