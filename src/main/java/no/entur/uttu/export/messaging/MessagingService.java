package no.entur.uttu.export.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Component
public class MessagingService {

    private static final String CHOUETTE_REFERENTIAL = "RutebankenChouetteReferential";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${export.notify.enabled:false}")
    private boolean enableNotification;

    @Value("${export.notify.queue.name:ChouetteMergeWithFlexibleLinesQueue}")
    private String queueName;

    /**
     * Notify Marduk that a new Flexible Transport NeTex export has been uploaded.
     * @param codespace the current provider's codespace.
     */
    public void notifyExport(final String codespace) {

        if(enableNotification) {
            jmsTemplate.send(queueName, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    Message message = session.createMessage();
                    message.setStringProperty(CHOUETTE_REFERENTIAL,codespace);
                    return message;
                }
            });
        }
        logger.debug("Sent export notification for codespace {}.", codespace);
    }


}
