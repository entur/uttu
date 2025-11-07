package no.entur.uttu.ext.entur.synthetictestdata;

import no.entur.uttu.export.messaging.spi.MessagingService;
import no.entur.uttu.repository.ExportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@ConditionalOnProperty(
  value = "entur.synthetictestdata.schedule.enabled",
  havingValue = "true"
)
public class SyntheticTestDataExportNotificationSchedule {

  private final MessagingService messagingService;
  private final ExportRepository exportRepository;
  private final String codespace;

  public SyntheticTestDataExportNotificationSchedule(
    MessagingService messagingService,
    ExportRepository exportRepository,
    @Value("${entur.synthetictestdata.schedule.codespace:aas}") String codespace
  ) {
    this.messagingService = messagingService;
    this.exportRepository = exportRepository;
    this.codespace = codespace;
  }

  @Scheduled(cron = "${entur.synthetictestdata.schedule.cron:0 0 2 * * *}")
  public void schedule() {
    exportRepository
      .getLatestExportByProviders()
      .stream()
      .filter(export -> export.getProvider().getCode().equals(codespace))
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
