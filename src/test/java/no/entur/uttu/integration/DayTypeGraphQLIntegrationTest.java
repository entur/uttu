package no.entur.uttu.integration;

import java.util.List;
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
    String networkId = createNetworkWithName()
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
