package no.entur.uttu.export.netex;

import java.util.Collections;
import java.util.List;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.Line;
import no.entur.uttu.model.job.ExportLineAssociation;
import org.junit.Assert;
import org.junit.Test;

public class NetexExporterTest {

  @Test
  public void findLinesToExportReturnsAll() {
    NetexExporter exporter = new NetexExporter();

    Line line = new FixedLine();

    Assert.assertEquals(1, exporter.findLinesToExport(null, List.of(line)).size());
  }

  @Test
  public void findLinesToExportFiltersWithLineAssociation() {
    NetexExporter exporter = new NetexExporter();

    Line line1 = new FixedLine();
    Line line2 = new FixedLine();

    ExportLineAssociation la = new ExportLineAssociation();
    la.setLine(line1);

    Assert.assertEquals(
      1,
      exporter
        .findLinesToExport(Collections.singletonList(la), List.of(line1, line2))
        .size()
    );
    Assert.assertEquals(
      line1,
      exporter
        .findLinesToExport(Collections.singletonList(la), List.of(line1, line2))
        .get(0)
    );
  }

  @Test(expected = CodedIllegalArgumentException.class)
  public void findLinesToExportErrorIfEmpty() {
    NetexExporter exporter = new NetexExporter();
    exporter.findLinesToExport(List.of(), List.of());
  }

  @Test(expected = CodedIllegalArgumentException.class)
  public void findLinesToExportErrorIfEmptyAfterFiltering() {
    NetexExporter exporter = new NetexExporter();

    Line line1 = new FixedLine();
    Line line2 = new FixedLine();

    ExportLineAssociation la = new ExportLineAssociation();
    la.setLine(line1);

    exporter.findLinesToExport(List.of(la), List.of(line2));
  }
}
