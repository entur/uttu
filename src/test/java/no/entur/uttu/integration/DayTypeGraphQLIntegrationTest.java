package no.entur.uttu.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
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
    String dayTypeRef = createDayType()
      .path("mutateDayType.id")
      .entity(String.class)
      .get();
    String networkId = createNetworkWithName()
      .path("mutateNetwork.id")
      .entity(String.class)
      .get();

    createFixedLineWithDayTypeRef("TestSJ", networkId, dayTypeRef);

    graphQlTester
      .documentName("deleteDayType")
      .variable("id", dayTypeRef)
      .execute()
      .errors()
      .satisfy(
        errors ->
          assertThat(errors).anyMatch(
            error ->
              error.getExtensions().get("code").equals("ENTITY_IS_REFERENCED") &&
              ((Map<String, Object>) error.getExtensions().get("metadata")).get(
                  "numberOfReferences"
                ).equals(1)
          )
      );
  }

  private GraphQlTester.Response createDayType() {
    var input = InputGenerators.generateDayTypeInput();

    return graphQlTester.documentName("mutateDayType").variable("input", input).execute();
  }

  private void createFixedLineWithDayTypeRef(
    String name,
    String networkId,
    String dayTypeRef
  ) {
    var input = InputGenerators.generateFixedLineInput(name, networkId, dayTypeRef);

    graphQlTester.documentName("mutateFixedLine").variable("input", input).execute();
  }

  private GraphQlTester.Response createNetworkWithName() {
    return graphQlTester
      .documentName("mutateNetwork")
      .variable(
        "network",
        InputGenerators.generateNetworkInput("TestNetworkForDayTypes", "NOG:Authority:1")
      )
      .execute();
  }
}
