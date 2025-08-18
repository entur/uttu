package no.entur.uttu.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

public class FlexibleLineGraphQLIntegrationTest extends AbstractGraphQLIntegrationTest {

  private static final String TEST_FLEXIBLE_LINE_NAME = "TestFlexibleLine";
  private static final String TEST_FLEXIBLE_LINE_INVALID_OPERATOR =
    "TestFlexibleLineWithInvalidOperator";

  @Test
  public void testCreateFlexibleLine() {
    var networkId = createNetworkWithName("Network_1")
      .path("mutateNetwork.id")
      .entity(String.class)
      .get();
    var stopPlaceId = createFlexibleStopPlaceWithFlexibleArea("TestArea")
      .path("mutateFlexibleStopPlace.id")
      .entity(String.class)
      .get();
    var stopPlaceId2 = createFlexibleStopPlaceWithHailAndRideArea("TestArea2")
      .path("mutateFlexibleStopPlace.id")
      .entity(String.class)
      .get();
    var response = createFlexibleLine(
      TEST_FLEXIBLE_LINE_NAME,
      "flexibleAreasOnly",
      "NOG:Operator:1",
      networkId,
      stopPlaceId,
      stopPlaceId2
    );

    assertThat(
      response.path("mutateFlexibleLine.id").entity(String.class).get()
    ).startsWith("TST:FlexibleLine");
    assertThat(
      response.path("mutateFlexibleLine.name").entity(String.class).get()
    ).isEqualTo(TEST_FLEXIBLE_LINE_NAME);
    assertThat(
      response
        .path(
          "mutateFlexibleLine.journeyPatterns[0].serviceJourneys[0].passingTimes[0].departureTime"
        )
        .entity(String.class)
        .get()
    ).isEqualTo("16:00:00");
  }

  @Test
  public void testCreateFlexibleLineWithInvalidOperator() {
    var networkId = createNetworkWithName("Network_2")
      .path("mutateNetwork.id")
      .entity(String.class)
      .get();
    var response = createFlexibleLine(
      TEST_FLEXIBLE_LINE_INVALID_OPERATOR,
      "flexibleAreasOnly",
      "NOG:Operator:2",
      networkId,
      null,
      null
    );

    response
      .errors()
      .satisfy(
        errors ->
          assertThat(errors).anyMatch(
            error ->
              error
                .getExtensions()
                .get("code")
                .equals("ORGANISATION_NOT_IN_ORGANISATION_REGISTRY")
          )
      );
  }

  @Test
  public void testCreateFlexibleLineWithExistingName() {
    String name = "foobar";
    String operatorRef = "NOG:Operator:1";

    // Create network
    String networkId = createNetworkWithName(name)
      .path("mutateNetwork.id")
      .entity(String.class)
      .get();

    // Create flexible stop places
    var flexAreaStopPlace = createFlexibleStopPlaceWithFlexibleArea(name + "FlexArea");
    String flexAreaStopPlaceId = flexAreaStopPlace
      .path("mutateFlexibleStopPlace.id")
      .entity(String.class)
      .get();

    var hailAndRideStopPlace = createFlexibleStopPlaceWithHailAndRideArea(
      name + "HailAndRide"
    );
    String hailAndRideStopPlaceId = hailAndRideStopPlace
      .path("mutateFlexibleStopPlace.id")
      .entity(String.class)
      .get();

    // Create first flexible line
    createFlexibleLine(
      name,
      "flexibleAreasOnly",
      operatorRef,
      networkId,
      flexAreaStopPlaceId,
      hailAndRideStopPlaceId
    );

    // Try to create second flexible line with same name
    var response = createFlexibleLine(
      name,
      "flexibleAreasOnly",
      operatorRef,
      networkId,
      flexAreaStopPlaceId,
      hailAndRideStopPlaceId
    );

    response
      .errors()
      .satisfy(
        errors ->
          assertThat(errors).anyMatch(
            error -> error.getExtensions().get("code").equals("CONSTRAINT_VIOLATION")
          )
      );
  }

  private GraphQlTester.Response createFlexibleLine(
    String name,
    String flexibleLineType,
    String operatorRef,
    String networkId,
    String flexAreaStopPlaceId,
    String hailAndRideStopPlaceId
  ) {
    var dayTypeRef = createDayType().path("mutateDayType.id").entity(String.class).get();
    var input = InputGenerators.generateFlexibleLineInput(
      name,
      flexibleLineType,
      operatorRef,
      networkId,
      flexAreaStopPlaceId,
      hailAndRideStopPlaceId,
      dayTypeRef
    );
    return graphQlTester
      .documentName("mutateFlexibleLine")
      .variable("input", input)
      .execute();
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

  private GraphQlTester.Response createNetworkWithName(String name) {
    return graphQlTester
      .documentName("mutateNetwork")
      .variable("network", InputGenerators.generateNetworkInput(name, "NOG:Authority:1"))
      .execute();
  }

  private GraphQlTester.Response createDayType() {
    var input = InputGenerators.generateDayTypeInput();
    return graphQlTester.documentName("mutateDayType").variable("input", input).execute();
  }
}
