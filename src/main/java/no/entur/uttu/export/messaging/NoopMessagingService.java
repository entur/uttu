package no.entur.uttu.export.messaging;

import no.entur.uttu.export.messaging.spi.MessagingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * The default implementation is a noop operation
 */
@Component
@ConditionalOnMissingBean(
  value = MessagingService.class,
  ignored = NoopMessagingService.class
)
public class NoopMessagingService implements MessagingService {

  @Override
  public void notifyExport(String codespace, String filename) {
    // intentionally empty
  }
}
