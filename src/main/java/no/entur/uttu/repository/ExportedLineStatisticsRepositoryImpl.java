package no.entur.uttu.repository;

import no.entur.uttu.model.ExportedLineStatistics;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.job.Export;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class ExportedLineStatisticsRepositoryImpl implements ExportedLineStatisticsRepository {

    private final EntityManager entityManager;
    private final ExportRepository exportRepository;

    public ExportedLineStatisticsRepositoryImpl(EntityManager entityManager, ExportRepository exportRepository) {
        this.entityManager = entityManager;
        this.exportRepository = exportRepository;
    }

    @Override
    public List<ExportedLineStatistics> findExportedLineStatisticsByProvider(String providerCode) {

        // TODO: hva med dryRun False ???
        Export export = exportRepository.findFirstByProviderCodeOrderByCreatedDesc(providerCode);

        List<ExportedLineStatistics> resultList = entityManager
                .createQuery("from ExportedLineStatistics where export = :export", ExportedLineStatistics.class)
                .setParameter("export", export)
                .getResultList();

        return resultList;
    }

    @Override
    public ExportedLineStatistics getExportedLineStatisticsById(Long id) {
        return null;
    }
}
