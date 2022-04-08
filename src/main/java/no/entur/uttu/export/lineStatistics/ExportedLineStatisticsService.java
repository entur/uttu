package no.entur.uttu.export.lineStatistics;

import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.export.netex.producer.line.NetexLineUtilities;
import no.entur.uttu.model.*;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.repository.ExportRepository;
import no.entur.uttu.repository.ExportedLineStatisticsRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ExportedLineStatisticsService {

    private final ExportRepository exportRepository;
    private final ExportedLineStatisticsRepository exportedLineStatisticsRepository;

    public ExportedLineStatisticsService(ExportRepository exportRepository,
                                         ExportedLineStatisticsRepository lineStatisticsRepository) {
        this.exportRepository = exportRepository;
        this.exportedLineStatisticsRepository = lineStatisticsRepository;
    }

    public List<ExportedLineStatistics> getLineStatisticsForProvider(String providerCode) {
        Export export = exportRepository.findFirstByProviderCodeAndDryRunFalseOrderByCreatedDesc(providerCode);
        List<ExportedLineStatistics> exportedLineStatistics = exportedLineStatisticsRepository.findByExportIn(Collections.singletonList(export));

        Map<String, List<ExportedLineStatistics>> lineStatisticsByPublicCode = exportedLineStatistics.stream()
                .collect(Collectors.groupingBy(ExportedLineStatistics::getPublicCode));

        return exportedLineStatistics;
    }

    public List<ExportedLineStatistics> getLineStatisticsForAllProviders() {
        List<Export> latestExportByProviders = exportRepository.getLatestExportByProviders();
        return exportedLineStatisticsRepository.findByExportIn(latestExportByProviders);
    }

    public static ExportedLineStatistics toExportedLineStatistics(Line line) {
        AvailabilityPeriod availabilityPeriod = NetexLineUtilities.calculateAvailabilityPeriodForLine(line);
        ExportedLineStatistics exportedLineStatistics = new ExportedLineStatistics();

        exportedLineStatistics.setLineName(line.getName());
        exportedLineStatistics.setOperatingPeriodFrom(availabilityPeriod.getFrom());
        exportedLineStatistics.setOperatingPeriodTo(availabilityPeriod.getTo());
        exportedLineStatistics.setPublicCode(line.getPublicCode());

        calculateExportedDayTypesStatisticsForLine(line).forEach(exportedLineStatistics::addExportedDayTypesStatistics);

        return exportedLineStatistics;
    }

    private static List<ExportedDayTypeStatistics> calculateExportedDayTypesStatisticsForLine(Line line) {
        return line.getJourneyPatterns().stream()
                .map(JourneyPattern::getServiceJourneys).flatMap(List::stream)
                .map(ServiceJourney::getDayTypes).flatMap(List::stream)
                .map(ExportedLineStatisticsService::getExportedDayTypeStatisticsForDayType)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static ExportedDayTypeStatistics getExportedDayTypeStatisticsForDayType(DayType dayType) {
        return dayType.getDayTypeAssignments().stream()
                .map(NetexLineUtilities::getAvailabilityPeriodFromDayTypeAssignment)
                .filter(Objects::nonNull)
                .reduce(AvailabilityPeriod::union)
                .map(availabilityPeriod -> {
                    ExportedDayTypeStatistics exportedDayTypesStatistics = new ExportedDayTypeStatistics();
                    exportedDayTypesStatistics.setDayTypeNetexId(dayType.getNetexId());
                    exportedDayTypesStatistics.setOperatingPeriodFrom(availabilityPeriod.getFrom());
                    exportedDayTypesStatistics.setOperatingPeriodTo(availabilityPeriod.getTo());
                    return exportedDayTypesStatistics;
                }).orElse(null);
    }

}
