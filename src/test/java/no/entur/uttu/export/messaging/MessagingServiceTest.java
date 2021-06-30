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

package no.entur.uttu.export.messaging;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.PubsubMessage;
import no.entur.uttu.UttuIntegrationTest;
import org.entur.pubsub.base.EnturGooglePubSubAdmin;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class MessagingServiceTest extends UttuIntegrationTest {

    public static final String TEST_CODESPACE = "rut";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private PubSubTemplate pubSubTemplate;

    @Autowired
    private EnturGooglePubSubAdmin enturGooglePubSubAdmin;


    @Value("${export.notify.queue.name:ChouetteMergeWithFlexibleLinesQueue}")
    private String queueName;

    @Test
    public void testNotifyExport() {

        enturGooglePubSubAdmin.createSubscriptionIfMissing(queueName);

        messagingService.notifyExport(TEST_CODESPACE);

        List<PubsubMessage> messages = pubSubTemplate.pullAndAck(queueName, 1, false);
        Assert.assertEquals(messages.size(), 1);
        String codespace = messages.get(0).getAttributesMap().get(PubSubMessagingService.CHOUETTE_REFERENTIAL);
        Assert.assertEquals(codespace, "rb_" + TEST_CODESPACE);
    }

}
