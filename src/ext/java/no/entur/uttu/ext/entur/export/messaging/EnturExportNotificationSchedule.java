package no.entur.uttu.ext.entur.export.messaging;

import no.entur.uttu.export.messaging.spi.MessagingService;
import no.entur.uttu.repository.ExportRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This class will send an export notification to the messaging service
 * for the latest export of each provider at the configured schedule.
 *
 * <p>
 *   <b>Note: should only be used with a single instance. If you're using
 *   multiple instances, a notification will be sent from each instance!</b>
 * </p>
 *
 * To enable set property
 *    entur.export.notification.schedule.enabled=true
 * To override default schedule use this property
 *    entur.export.notification.schedule.cron=0 30 4 * * *
 */
@Configuration
@ConditionalOnProperty(
  value = "entur.export.notification.schedule.enabled",
  havingValue = "true"
)
public class EnturExportNotificationSchedule {

  private final MessagingService messagingService;
  private final ExportRepository exportRepository;

  public EnturExportNotificationSchedule(
    MessagingService messagingService,
    ExportRepository exportRepository
  ) {
    this.messagingService = messagingService;
    this.exportRepository = exportRepository;
  }

  @Scheduled(cron = "${entur.export.notification.schedule.cron:0 0 2 * * *}")
  public void schedule() {
    exportRepository
      .getLatestExportByProviders()
      .forEach(
        export ->
          messagingService.notifyExport(
            export.getProvider().getCode(),
            export.getFileName()
          )
      );
  }
}
