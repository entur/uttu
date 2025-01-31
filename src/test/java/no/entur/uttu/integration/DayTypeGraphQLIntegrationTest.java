package no.entur.uttu.integration;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

public class DayTypeGraphQLIntegrationTest extends AbstractGraphQLIntegrationTest {

  @Test
  public void testDayTypeLifeCycle() {
    var response = createDayType();
    String dayTypeRef = response.path("mutateDayType.id").entity(String.class).get();

    graphQlTester
      .documentName("dayTypesByIds")
      .variable("ids", List.of(dayTypeRef))
      .execute()
      .errors()
      .equals(null);

    graphQlTester
      .documentName("deleteDayType")
      .variable("id", dayTypeRef)
      .execute()
      .errors()
      .equals(null);
  }

  @Test
  public void testUnableToDeleteDayTypeUsedByServiceJourney() {
    var response = createDayType();
    String dayTypeRef = response.path("mutateDayType.id").entity(String.class).get();
    String networkId = createNetworkWithName("TestNetworkForDayTypes")
      .path("mutateNetwork.id")
      .entity(String.class)
      .get();

    createFixedLineWithDayTypeRef("TestSJ", networkId, dayTypeRef);

    response =
      graphQlTester.documentName("deleteDayType").variable("id", dayTypeRef).execute();

    // TODO is there a better way to assert on errors?
    response
      .errors()
      .expect(error -> {
        var extentions = error.getExtensions();
        return extentions.get("code").equals("ENTITY_IS_REFERENCED");
      });
    /*executeGraphQL(deleteDayTypeMutation, "{ \"id\": \"" + dayTypeRef + "\" }", 200)
                .body("errors[0].extensions.code", equalTo("ENTITY_IS_REFERENCED"))
                .body("errors[0].extensions.metadata.numberOfReferences", equalTo(1))*/
  }

  private GraphQlTester.Response createDayType() {
    var input = generateDayTypeInput();

    return graphQlTester.documentName("mutateDayType").variable("input", input).execute();
  }

  private static @NotNull Map<String, Object> generateDayTypeInput() {
    return Map.of(
      "daysOfWeek",
      List.of("monday", "tuesday", "wednesday", "thursday", "friday"),
      "dayTypeAssignments",
      Map.of(
        "operatingPeriod",
        Map.of("fromDate", "2020-04-01", "toDate", "2020-05-01"),
        "isAvailable",
        true
      )
    );
  }

  private void createFixedLineWithDayTypeRef(
    String name,
    String networkId,
    String dayTypeRef
  ) {
    var input = generateFixedLineInput(name, networkId, dayTypeRef);

    graphQlTester.documentName("mutateFixedLine").variable("input", input).execute();
  }

  private static @NotNull Map<String, Object> generateFixedLineInput(
    String name,
    String networkId,
    String dayTypeRef
  ) {
    return Map.of(
      "name",
      name,
      "publicCode",
      "TestFixedLine",
      "transportMode",
      "bus",
      "transportSubmode",
      "localBus",
      "networkRef",
      networkId,
      "operatorRef",
      "NOG:Operator:1",
      "journeyPatterns",
      List.of(
        Map.of(
          "pointsInSequence",
          List.of(
            Map.of(
              "quayRef",
              "NSR:Quay:494",
              "destinationDisplay",
              Map.of("frontText", "Første stopp")
            ),
            Map.of("quayRef", "NSR:Quay:563")
          ),
          "serviceJourneys",
          List.of(
            Map.of(
              "name",
              "Hverdager3-" + System.currentTimeMillis(),
              "dayTypesRefs",
              List.of(dayTypeRef),
              "passingTimes",
              List.of(
                Map.of("departureTime", "07:00:00"),
                Map.of("arrivalTime", "07:15:00")
              )
            )
          )
        )
      )
    );
  }

  private GraphQlTester.Response createNetworkWithName(String name) {
    return graphQlTester
      .documentName("mutateNetwork")
      .variable("network", Map.of("name", name, "authorityRef", "NOG:Authority:1"))
      .execute();
  }
}
