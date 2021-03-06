package no.entur.uttu.export.messaging;

import no.entur.uttu.util.ExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PubSubMessagingService implements  MessagingService{

    public static final String CHOUETTE_REFERENTIAL = "RutebankenChouetteReferential";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PubSubTemplate pubSubTemplate;

    @Value("${export.notify.enabled:false}")
    private boolean enableNotification;

    @Value("${export.notify.queue.name:ChouetteMergeWithFlexibleLinesQueue}")
    private String queueName;

    /**
     * Notify Marduk that a new Flexible Transport NeTex export has been uploaded.
     * @param codespace the current provider's codespace.
     */
    @Override
    public void notifyExport(final String codespace) {

        if(enableNotification) {

            Map<String, String> pubSubAttributes = new HashMap<>();
            pubSubAttributes.put(CHOUETTE_REFERENTIAL, ExportUtil.getMigratedReferential(codespace));
            pubSubTemplate.publish(queueName, "", pubSubAttributes);

            logger.debug("Sent export notification for codespace {}.", codespace);
        } else {
            logger.debug("Skipped export notification for codespace {}.", codespace);
        }

    }

}
