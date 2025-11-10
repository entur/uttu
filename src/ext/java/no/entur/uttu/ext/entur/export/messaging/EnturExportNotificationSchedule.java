package no.entur.uttu.ext.entur.export.messaging;

import java.util.Arrays;
import no.entur.uttu.config.Context;
import no.entur.uttu.export.messaging.spi.MessagingService;
import no.entur.uttu.repository.ExportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

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
@EnableScheduling
@ConditionalOnProperty(
  value = "entur.export.notification.schedule.enabled",
  havingValue = "true"
)
public class EnturExportNotificationSchedule implements SchedulingConfigurer {

  private final MessagingService messagingService;
  private final ExportRepository exportRepository;

  @Value("${export.blob.folder:inbound/uttu/}")
  private String exportFolder;

  @Value("${entur.export.notification.schedule.codespaces:#{null}}")
  private String[] codespaces;

  public EnturExportNotificationSchedule(
    MessagingService messagingService,
    ExportRepository exportRepository
  ) {
    this.messagingService = messagingService;
    this.exportRepository = exportRepository;
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(1);
    taskScheduler.setThreadNamePrefix("export-notification-scheduler-");
    taskScheduler.initialize();
    taskRegistrar.setTaskScheduler(taskScheduler);
  }

  @Scheduled(cron = "${entur.export.notification.schedule.cron:0 0 2 * * *}")
  public void schedule() {
    try {
      Context.setUserName("export-notification-scheduler");
      exportRepository
        .getLatestExportByProviders()
        .stream()
        .filter(
          export ->
            !export.isDryRun() &&
            export.isSuccess() &&
            (codespaces == null ||
              Arrays.stream(codespaces).anyMatch(
                export.getProvider().getCode()::equalsIgnoreCase
              ))
        )
        .forEach(
          export ->
            messagingService.notifyExport(
              export.getProvider().getCode(),
              export.getFileName().replace(exportFolder, "")
            )
        );
    } finally {
      Context.clear();
    }
  }
}
