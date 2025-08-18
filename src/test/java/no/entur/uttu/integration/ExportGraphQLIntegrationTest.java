package no.entur.uttu.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import no.entur.uttu.model.job.ExportStatusEnumeration;
import org.junit.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

public class ExportGraphQLIntegrationTest extends AbstractGraphQLIntegrationTest {

  private static final String EXPORT_NAME = "ExportTest";

  @Test
  public void testCreateExport() throws Exception {
    // Create flexible line
    var flexibleLineResponse = createFlexibleLine(EXPORT_NAME);
    String lineRef = flexibleLineResponse
      .path("mutateFlexibleLine.id")
      .entity(String.class)
      .get();

    // Create export
    var exportInput = Map.of(
      "name",
      EXPORT_NAME,
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
      EXPORT_NAME
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

    // Delete flexible line
    var deleteResponse = graphQlTester
      .documentName("deleteFlexibleLine")
      .variable("id", lineRef)
      .execute();

    assertThat(
      deleteResponse.path("deleteFlexibleLine.id").entity(String.class).get()
    ).isEqualTo(lineRef);
  }

  private GraphQlTester.Response createFlexibleLine(String name) {
    var networkId = createNetworkWithName(name + "_network")
      .path("mutateNetwork.id")
      .entity(String.class)
      .get();
    var stopPlaceId = createFlexibleStopPlaceWithFlexibleArea(name + "_stop")
      .path("mutateFlexibleStopPlace.id")
      .entity(String.class)
      .get();
    var stopPlaceId2 = createFlexibleStopPlaceWithFlexibleArea(name + "_stop2")
      .path("mutateFlexibleStopPlace.id")
      .entity(String.class)
      .get();
    String dayTypeRef = createDayType()
      .path("mutateDayType.id")
      .entity(String.class)
      .get();

    var input = InputGenerators.generateFlexibleLineInput(
      name,
      "flexibleAreasOnly",
      "NOG:Operator:1",
      networkId,
      stopPlaceId,
      stopPlaceId2,
      dayTypeRef
    );
    return graphQlTester
      .documentName("mutateFlexibleLine")
      .variable("input", input)
      .execute();
  }

  private GraphQlTester.Response createNetworkWithName(String name) {
    return graphQlTester
      .documentName("mutateNetwork")
      .variable("network", InputGenerators.generateNetworkInput(name, "NOG:Authority:1"))
      .execute();
  }

  private GraphQlTester.Response createFlexibleStopPlaceWithFlexibleArea(String name) {
    var input = InputGenerators.generateFlexibleStopPlaceWithFlexibleAreaInput(name);
    return graphQlTester
      .documentName("mutateFlexibleStopPlace")
      .variable("flexibleStopPlace", input)
      .execute();
  }

  private GraphQlTester.Response createDayType() {
    var input = InputGenerators.generateDayTypeInput();
    return graphQlTester.documentName("mutateDayType").variable("input", input).execute();
  }
}
