package no.entur.uttu.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import no.entur.uttu.stubs.StopPointInJourneyPatternRepositoryStub;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

public class FlexibleStopPlaceGraphQLIntegrationTest
  extends AbstractGraphQLIntegrationTest {

  @Autowired
  private StopPointInJourneyPatternRepositoryStub stopPointInJourneyPatternRepository;

  private static final String FLEX_AREA_NAME = "FlexibleAreaTest";
  private static final String FLEX_AREAS_NAME = "FlexibleAreasTest";
  private static final String HAIL_AND_RIDE_NAME = "HailAndRideTest";

  @Test
  public void testCreateFlexibleStopPlaceWithFlexibleArea() {
    var response = createFlexibleStopPlaceWithFlexibleArea(FLEX_AREA_NAME);
    assertFlexibleAreaResponse(response, "mutateFlexibleStopPlace");

    String id = response.path("mutateFlexibleStopPlace.id").entity(String.class).get();
    var queryResponse = graphQlTester
      .documentName("flexibleStopPlace")
      .variable("id", id)
      .execute();

    assertFlexibleAreaResponse(queryResponse, "flexibleStopPlace");
  }

  @Test
  public void testCreateFlexibleStopPlaceWithFlexibleAreas() {
    var response = createFlexibleStopPlaceWithFlexibleAreas(FLEX_AREAS_NAME);
    assertFlexibleAreasResponse(response, "mutateFlexibleStopPlace");

    String id = response.path("mutateFlexibleStopPlace.id").entity(String.class).get();
    var queryResponse = graphQlTester
      .documentName("flexibleStopPlace")
      .variable("id", id)
      .execute();

    assertFlexibleAreasResponse(queryResponse, "flexibleStopPlace");
  }

  @Test
  public void testCreateFlexibleStopPlaceWithHailAndRideArea() {
    var response = createFlexibleStopPlaceWithHailAndRideArea(HAIL_AND_RIDE_NAME);

    assertThat(
      response.path("mutateFlexibleStopPlace.id").entity(String.class).get()
    ).startsWith("TST:FlexibleStopPlace");
    assertThat(
      response.path("mutateFlexibleStopPlace.name").entity(String.class).get()
    ).isEqualTo(HAIL_AND_RIDE_NAME);
    assertThat(
      response
        .path("mutateFlexibleStopPlace.hailAndRideArea.startQuayRef")
        .entity(String.class)
        .get()
    ).isEqualTo("NSR:Quay:565");
    assertThat(
      response
        .path("mutateFlexibleStopPlace.hailAndRideArea.endQuayRef")
        .entity(String.class)
        .get()
    ).isEqualTo("NSR:Quay:494");
  }

  @Test
  public void testDeleteFlexibleStopPlace() {
    stopPointInJourneyPatternRepository.setNextCountByFlexibleStopPlace(1);

    var response = graphQlTester
      .documentName("deleteFlexibleStopPlace")
      .variable("id", "TST:FlexibleStopPlace:1")
      .execute();

    response
      .errors()
      .satisfy(errors -> {
        assertThat(errors).anyMatch(error -> {
          Map<String, Object> extensions = error.getExtensions();
          return (
            extensions.get("code").equals("ENTITY_IS_REFERENCED") &&
            ((Map<String, Object>) extensions.get("metadata")).get(
                "numberOfReferences"
              ).equals(1)
          );
        });
      });

    stopPointInJourneyPatternRepository.setNextCountByFlexibleStopPlace(0);

    response = graphQlTester
      .documentName("deleteFlexibleStopPlace")
      .variable("id", "TST:FlexibleStopPlace:1")
      .execute();

    response.errors().verify();
  }

  private void assertFlexibleAreaResponse(GraphQlTester.Response response, String path) {
    assertThat(response.path(path + ".id").entity(String.class).get()).startsWith(
      "TST:FlexibleStopPlace"
    );
    assertThat(response.path(path + ".name").entity(String.class).get()).isEqualTo(
      FLEX_AREA_NAME
    );
    assertThat(
      response.path(path + ".keyValues[0].key").entity(String.class).get()
    ).isEqualTo("foo");
    assertThat(
      response.path(path + ".flexibleArea.polygon.type").entity(String.class).get()
    ).isEqualTo("Polygon");
    assertThat(
      response.path(path + ".flexibleArea.polygon.coordinates").entity(List.class).get()
    ).hasSize(4);
  }

  private void assertFlexibleAreasResponse(GraphQlTester.Response response, String path) {
    assertThat(response.path(path + ".id").entity(String.class).get()).startsWith(
      "TST:FlexibleStopPlace"
    );
    assertThat(response.path(path + ".name").entity(String.class).get()).isEqualTo(
      FLEX_AREAS_NAME
    );
    assertThat(
      response
        .path(path + ".flexibleAreas[0].keyValues[0].key")
        .entity(String.class)
        .get()
    ).isEqualTo("foo");
    assertThat(
      response.path(path + ".flexibleAreas[0].polygon.type").entity(String.class).get()
    ).isEqualTo("Polygon");
    assertThat(
      response
        .path(path + ".flexibleAreas[0].polygon.coordinates")
        .entity(List.class)
        .get()
    ).hasSize(4);
  }

  private GraphQlTester.Response createFlexibleStopPlaceWithFlexibleArea(String name) {
    var input = InputGenerators.generateFlexibleStopPlaceWithFlexibleAreaInput(name);
    return graphQlTester
      .documentName("mutateFlexibleStopPlace")
      .variable("flexibleStopPlace", input)
      .execute();
  }

  private GraphQlTester.Response createFlexibleStopPlaceWithHailAndRideArea(String name) {
    var input = InputGenerators.generateFlexibleStopPlaceWithHailAndRideAreaInput(name);
    return graphQlTester
      .documentName("mutateFlexibleStopPlace")
      .variable("flexibleStopPlace", input)
      .execute();
  }

  private GraphQlTester.Response createFlexibleStopPlaceWithFlexibleAreas(String name) {
    var input = InputGenerators.generateFlexibleStopPlaceWithFlexibleAreasInput(name);
    return graphQlTester
      .documentName("mutateFlexibleStopPlace")
      .variable("flexibleStopPlace", input)
      .execute();
  }
}
