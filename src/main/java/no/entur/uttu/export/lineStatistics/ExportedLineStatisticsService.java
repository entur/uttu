package no.entur.uttu.export.lineStatistics;

import no.entur.uttu.model.ExportedLineStatistics;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.repository.ExportRepository;
import no.entur.uttu.repository.ExportedLineStatisticsRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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
        return exportedLineStatisticsRepository.findByExportIn(Collections.singletonList(export));
    }

    public List<ExportedLineStatistics> getLineStatisticsForAllProviders() {
        List<Export> latestExportByProviders = exportRepository.getLatestExportByProviders();
        return exportedLineStatisticsRepository.findByExportIn(latestExportByProviders);
    }
}
