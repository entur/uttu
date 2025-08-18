package no.entur.uttu.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import no.entur.uttu.model.job.ExportStatusEnumeration;
import org.junit.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

public class ExportFixedLineGraphQLIntegrationTest
  extends AbstractGraphQLIntegrationTest {

  private static final String FIXED_LINE_NAME = "Fiktiv linje";

  @Test
  public void testCreateExport() throws Exception {
    // Create fixed line
    var fixedLineResponse = createFixedLine(FIXED_LINE_NAME);
    String lineRef = fixedLineResponse
      .path("mutateFixedLine.id")
      .entity(String.class)
      .get();

    // Create export
    var exportInput = Map.of(
      "name",
      FIXED_LINE_NAME,
      "lineAssociations",
      List.of(Map.of("lineRef", lineRef))
    );

    var exportResponse = graphQlTester
      .documentName("export")
      .variable("export", exportInput)
      .execute();

    String exportId = exportResponse.path("export.id").entity(String.class).get();
    assertThat(exportId).startsWith("TST:Export");
    assertThat(exportResponse.path("export.name").entity(String.class).get()).isEqualTo(
      FIXED_LINE_NAME
    );
    assertThat(
      exportResponse.path("export.exportStatus").entity(String.class).get()
    ).isEqualTo(ExportStatusEnumeration.SUCCESS.value());

    String downloadUrl = exportResponse
      .path("export.downloadUrl")
      .entity(String.class)
      .get();
    assertThat(downloadUrl).startsWith("tst/export/");

    // Verify download URL
    var response = webTarget.path(downloadUrl).request().get();

    assertThat(response.getStatus()).isEqualTo(200);
  }

  private GraphQlTester.Response createFixedLine(String name) {
    String networkId = createNetworkWithName(name)
      .path("mutateNetwork.id")
      .entity(String.class)
      .get();

    String dayTypeRef = createDayType()
      .path("mutateDayType.id")
      .entity(String.class)
      .get();

    var input = InputGenerators.generateFixedLineInput(name, networkId, dayTypeRef);
    return graphQlTester
      .documentName("mutateFixedLine")
      .variable("input", input)
      .execute();
  }

  private GraphQlTester.Response createDayType() {
    var input = InputGenerators.generateDayTypeInput();
    return graphQlTester.documentName("mutateDayType").variable("input", input).execute();
  }

  private GraphQlTester.Response createNetworkWithName(String name) {
    return graphQlTester
      .documentName("mutateNetwork")
      .variable("network", InputGenerators.generateNetworkInput(name, "NOG:Authority:1"))
      .execute();
  }
}
