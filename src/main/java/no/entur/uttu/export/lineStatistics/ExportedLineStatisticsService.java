package no.entur.uttu.export.lineStatistics;

import no.entur.uttu.model.Line;
import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.ExportedLineStatisticsRepository;
import org.springframework.stereotype.Component;

@Component
public class ExportedLineStatisticsService {

    private ExportedLineStatisticsRepository lineStatisticsRepository;

    public void updateLineStatistics(Provider provider, Line line) {
    }

}
