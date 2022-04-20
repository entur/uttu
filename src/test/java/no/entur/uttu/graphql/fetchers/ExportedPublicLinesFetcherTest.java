package no.entur.uttu.graphql.fetchers;

import no.entur.uttu.model.ExportedLineStatistics;
import no.entur.uttu.model.ExportedPublicLine;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.job.Export;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExportedPublicLinesFetcherTest {

    @Test
    public void fetchExportedPublicLineByExportedLineStatistics() {
        Provider provider = new Provider();
        provider.setCode("nsb");
        Export export = new Export();
        export.setProvider(provider);

        ExportedLineStatistics exportedLineStatistics1 = new ExportedLineStatistics();
        exportedLineStatistics1.setExport(export);
        exportedLineStatistics1.setOperatingPeriodFrom(LocalDate.of(2022, 1, 1));
        exportedLineStatistics1.setOperatingPeriodTo(LocalDate.of(2022, 3, 4));
        exportedLineStatistics1.setPublicCode("123");

        List<ExportedPublicLine> exportedPublicLines = ExportedPublicLinesFetcher.exportedLineStatisticsToExportedPublicLine(List.of(exportedLineStatistics1));

        assertEquals(1, exportedPublicLines.size());
        assertEquals(1, exportedPublicLines.get(0).getLines().size());
        assertEquals("nsb", exportedPublicLines.get(0).getProviderCode());
        assertEquals("123", exportedPublicLines.get(0).getPublicCode());
    }

    @Test
    public void fetchExportedPublicLineForMultipleLinesInSingleExportForAProvider() {
        Provider provider = new Provider();
        provider.setCode("nsb");
        Export export = new Export();
        export.setProvider(provider);

        ExportedLineStatistics exportedLineStatistics1 = new ExportedLineStatistics();
        exportedLineStatistics1.setExport(export);
        exportedLineStatistics1.setOperatingPeriodFrom(LocalDate.of(2022, 1, 1));
        exportedLineStatistics1.setOperatingPeriodTo(LocalDate.of(2022, 3, 4));
        exportedLineStatistics1.setPublicCode("123");

        ExportedLineStatistics exportedLineStatistics2 = new ExportedLineStatistics();
        exportedLineStatistics2.setExport(export);
        exportedLineStatistics2.setOperatingPeriodFrom(LocalDate.of(2022, 1, 1));
        exportedLineStatistics2.setOperatingPeriodTo(LocalDate.of(2022, 3, 4));
        exportedLineStatistics2.setPublicCode("345");

        List<ExportedPublicLine> exportedPublicLines =
                ExportedPublicLinesFetcher.exportedLineStatisticsToExportedPublicLine(List.of(exportedLineStatistics1, exportedLineStatistics2));

        assertEquals(2, exportedPublicLines.size());

        assertEquals(1, exportedPublicLines.get(0).getLines().size());
        assertEquals(1, exportedPublicLines.get(1).getLines().size());

        assertEquals("nsb", exportedPublicLines.get(0).getProviderCode());
        assertEquals("nsb", exportedPublicLines.get(1).getProviderCode());

        assertEquals("123", exportedPublicLines.get(0).getPublicCode());
        assertEquals("345", exportedPublicLines.get(1).getPublicCode());
    }

    @Test
    public void fetchExportedPublicLineForLinesWithSamePublicCodeInSingleExportForAProvider() {
        Provider provider = new Provider();
        provider.setCode("nsb");
        Export export = new Export();
        export.setProvider(provider);

        ExportedLineStatistics exportedLineStatistics1 = new ExportedLineStatistics();
        exportedLineStatistics1.setExport(export);
        exportedLineStatistics1.setOperatingPeriodFrom(LocalDate.of(2022, 1, 1));
        exportedLineStatistics1.setOperatingPeriodTo(LocalDate.of(2022, 3, 4));
        exportedLineStatistics1.setPublicCode("123");

        ExportedLineStatistics exportedLineStatistics2 = new ExportedLineStatistics();
        exportedLineStatistics2.setExport(export);
        exportedLineStatistics2.setOperatingPeriodFrom(LocalDate.of(2022, 5, 1));
        exportedLineStatistics2.setOperatingPeriodTo(LocalDate.of(2022, 8, 4));
        exportedLineStatistics2.setPublicCode("123");

        List<ExportedPublicLine> exportedPublicLines =
                ExportedPublicLinesFetcher.exportedLineStatisticsToExportedPublicLine(List.of(exportedLineStatistics1, exportedLineStatistics2));

        assertEquals(1, exportedPublicLines.size());

        assertEquals(2, exportedPublicLines.get(0).getLines().size());

        assertEquals("nsb", exportedPublicLines.get(0).getProviderCode());

        assertEquals("123", exportedPublicLines.get(0).getPublicCode());

        assertEquals(LocalDate.of(2022, 1, 1), exportedPublicLines.get(0).getOperatingPeriodFrom());
        assertEquals(LocalDate.of(2022, 8, 4), exportedPublicLines.get(0).getOperatingPeriodTo());
    }

    @Test
    public void fetchExportedPublicLinesForMultipleProviders() {
        Provider provider1 = new Provider();
        provider1.setCode("nsb");
        Export export1 = new Export();
        export1.setProvider(provider1);

        Provider provider2 = new Provider();
        provider2.setCode("rut");
        Export export2 = new Export();
        export2.setProvider(provider2);

        ExportedLineStatistics exportedLineStatistics1 = new ExportedLineStatistics();
        exportedLineStatistics1.setExport(export1);
        exportedLineStatistics1.setOperatingPeriodFrom(LocalDate.of(2022, 1, 1));
        exportedLineStatistics1.setOperatingPeriodTo(LocalDate.of(2022, 3, 4));
        exportedLineStatistics1.setPublicCode("123");

        ExportedLineStatistics exportedLineStatistics2 = new ExportedLineStatistics();
        exportedLineStatistics2.setExport(export2);
        exportedLineStatistics2.setOperatingPeriodFrom(LocalDate.of(2022, 1, 1));
        exportedLineStatistics2.setOperatingPeriodTo(LocalDate.of(2022, 3, 4));
        exportedLineStatistics2.setPublicCode("123");

        List<ExportedPublicLine> exportedPublicLines =
                ExportedPublicLinesFetcher.exportedLineStatisticsToExportedPublicLine(List.of(exportedLineStatistics1, exportedLineStatistics2));

        assertEquals(2, exportedPublicLines.size());
        assertEquals(1, exportedPublicLines.get(0).getLines().size());
        assertEquals(1, exportedPublicLines.get(1).getLines().size());

        assertEquals("rut", exportedPublicLines.get(0).getProviderCode());
        assertEquals("nsb", exportedPublicLines.get(1).getProviderCode());

        assertEquals("123", exportedPublicLines.get(0).getPublicCode());
        assertEquals("123", exportedPublicLines.get(1).getPublicCode());
    }
}