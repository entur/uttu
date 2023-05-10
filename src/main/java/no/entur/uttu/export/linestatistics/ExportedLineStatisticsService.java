package no.entur.uttu.export.linestatistics;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.export.netex.producer.line.NetexLineUtilities;
import no.entur.uttu.model.*;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.repository.ExportRepository;
import no.entur.uttu.repository.ExportedLineStatisticsRepository;
import org.springframework.stereotype.Component;

@Component
public class ExportedLineStatisticsService {

  private final ExportRepository exportRepository;
  private final ExportedLineStatisticsRepository exportedLineStatisticsRepository;

  public ExportedLineStatisticsService(
    ExportRepository exportRepository,
    ExportedLineStatisticsRepository lineStatisticsRepository
  ) {
    this.exportRepository = exportRepository;
    this.exportedLineStatisticsRepository = lineStatisticsRepository;
  }

  public List<ExportedLineStatistics> getLineStatisticsForProvider(String providerCode) {
    Export export =
      exportRepository.findFirstByProviderCodeAndDryRunFalseOrderByCreatedDesc(
        providerCode
      );
    return exportedLineStatisticsRepository.findByExportIn(
      Collections.singletonList(export)
    );
  }

  public List<ExportedLineStatistics> getLineStatisticsForAllProviders() {
    List<Export> latestExportByProviders = exportRepository.getLatestExportByProviders();
    return exportedLineStatisticsRepository.findByExportIn(latestExportByProviders);
  }

  public static ExportedLineStatistics toExportedLineStatistics(Line line) {
    AvailabilityPeriod availabilityPeriod =
      NetexLineUtilities.calculateAvailabilityPeriodForLine(line);
    ExportedLineStatistics exportedLineStatistics = new ExportedLineStatistics();

    exportedLineStatistics.setLineName(line.getName());
    exportedLineStatistics.setOperatingPeriodFrom(availabilityPeriod.getFrom());
    exportedLineStatistics.setOperatingPeriodTo(availabilityPeriod.getTo());
    exportedLineStatistics.setPublicCode(line.getPublicCode());

    calculateExportedDayTypesStatisticsForLine(line)
      .forEach(exportedLineStatistics::addExportedDayTypesStatistics);

    return exportedLineStatistics;
  }

  protected static List<ExportedDayTypeStatistics> calculateExportedDayTypesStatisticsForLine(
    Line line
  ) {
    return line
      .getJourneyPatterns()
      .stream()
      .map(JourneyPattern::getServiceJourneys)
      .flatMap(List::stream)
      .map(ExportedLineStatisticsService::getExportedDayTypeStatisticsForServiceJourney)
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  private static List<ExportedDayTypeStatistics> getExportedDayTypeStatisticsForServiceJourney(
    ServiceJourney serviceJourney
  ) {
    String serviceJourneyName = serviceJourney.getName();
    return serviceJourney
      .getDayTypes()
      .stream()
      .map(ExportedLineStatisticsService::getExportedDayTypeStatisticsForDayType)
      .filter(Objects::nonNull)
      .peek(exportedDayTypeStatistics ->
        exportedDayTypeStatistics.setServiceJourneyName(serviceJourneyName)
      )
      .collect(Collectors.toList());
  }

  protected static ExportedDayTypeStatistics getExportedDayTypeStatisticsForDayType(
    DayType dayType
  ) {
    return dayType
      .getDayTypeAssignments()
      .stream()
      .map(NetexLineUtilities::getAvailabilityPeriodFromDayTypeAssignment)
      .filter(Objects::nonNull)
      .reduce(AvailabilityPeriod::union)
      .map(availabilityPeriod -> {
        ExportedDayTypeStatistics exportedDayTypesStatistics =
          new ExportedDayTypeStatistics();
        exportedDayTypesStatistics.setDayTypeNetexId(dayType.getNetexId());
        exportedDayTypesStatistics.setOperatingPeriodFrom(availabilityPeriod.getFrom());
        exportedDayTypesStatistics.setOperatingPeriodTo(availabilityPeriod.getTo());
        return exportedDayTypesStatistics;
      })
      .orElse(null);
  }
}
