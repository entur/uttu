package no.entur.uttu.ext.entur.synthetictestdata;

import no.entur.uttu.export.messaging.spi.MessagingService;
import no.entur.uttu.repository.ExportRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@ConditionalOnProperty(
  value = "entur.synthetictestdata.schedule.enabled",
  havingValue = "true"
)
public class SyntheticTestDataExportNotificationSchedule {

  private static final String SCHEDULE = "0 2 0 * * *";
  private static final String SYNTHETIC_TEST_DATA_CODESPACE = "aas";

  private final MessagingService messagingService;
  private final ExportRepository exportRepository;

  public SyntheticTestDataExportNotificationSchedule(
    MessagingService messagingService,
    ExportRepository exportRepository
  ) {
    this.messagingService = messagingService;
    this.exportRepository = exportRepository;
  }

  @Scheduled(cron = SCHEDULE)
  public void schedule() {
    exportRepository
      .getLatestExportByProviders()
      .stream()
      .filter(
        export -> export.getProvider().getCode().equals(SYNTHETIC_TEST_DATA_CODESPACE)
      )
      .findFirst()
      .ifPresent(
        export ->
          messagingService.notifyExport(
            export.getProvider().getCode(),
            export.getFileName()
          )
      );
  }
}
