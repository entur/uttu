package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.model.ExportedLineStatistics;
import no.entur.uttu.model.ExportedPublicLine;

public class ExportedPublicLinesFetcher implements DataFetcher<List<ExportedPublicLine>> {

  @Override
  public List<ExportedPublicLine> get(DataFetchingEnvironment environment)
    throws Exception {
    List<ExportedLineStatistics> exportedLineStatistics = environment.getSource();
    return exportedLineStatisticsToExportedPublicLine(exportedLineStatistics);
  }

  protected static List<ExportedPublicLine> exportedLineStatisticsToExportedPublicLine(
    List<ExportedLineStatistics> exportedLineStatistics
  ) {
    return exportedLineStatistics
      .stream()
      .collect(
        Collectors.groupingBy(
          lineStatistics -> lineStatistics.getExport().getProvider().getCode(),
          Collectors.groupingBy(ExportedLineStatistics::getPublicCode)
        )
      )
      .entrySet()
      .stream()
      .flatMap(lineStatisticsByProviderEntry ->
        lineStatisticsByProviderEntry
          .getValue()
          .entrySet()
          .stream()
          .map(lineStatisticsByPublicCodeEntry -> {
            AvailabilityPeriod availabilityPeriodForPublicLine =
              lineStatisticsByPublicCodeEntry
                .getValue()
                .stream()
                .map(exportedLine ->
                  new AvailabilityPeriod(
                    exportedLine.getOperatingPeriodFrom(),
                    exportedLine.getOperatingPeriodTo()
                  )
                )
                .reduce(AvailabilityPeriod::union)
                .orElse(null);

            ExportedPublicLine exportedPublicLine = new ExportedPublicLine();
            exportedPublicLine.setOperatingPeriodFrom(
              Objects.requireNonNull(availabilityPeriodForPublicLine).getFrom()
            );
            exportedPublicLine.setOperatingPeriodTo(
              availabilityPeriodForPublicLine.getTo()
            );
            exportedPublicLine.setPublicCode(lineStatisticsByPublicCodeEntry.getKey());
            exportedPublicLine.setLines(lineStatisticsByPublicCodeEntry.getValue());
            exportedPublicLine.setProviderCode(lineStatisticsByProviderEntry.getKey());
            return exportedPublicLine;
          })
      )
      .collect(Collectors.toList());
  }
}
