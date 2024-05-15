package no.entur.uttu.export.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(
        value = MessagingService.class, ignored = NoopMessagingService.class
)
public class NoopMessagingService implements MessagingService {
    @Override
    public void notifyExport(String codespace, String filename) {
        // noop
    }
}
