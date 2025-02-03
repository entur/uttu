package no.entur.uttu.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

public class FixedLineGraphQLIntegrationTest extends AbstractGraphQLIntegrationTest {

  private static final String TEST_FIXED_LINE_NAME = "TestFixedLine";

  @Test
  public void testCreateFixedLine() {
    var response = createFixedLine(TEST_FIXED_LINE_NAME);

    assertThat(response.path("mutateFixedLine.id").entity(String.class).get())
      .startsWith("TST:Line");
    assertThat(response.path("mutateFixedLine.name").entity(String.class).get())
      .isEqualTo(TEST_FIXED_LINE_NAME);
    assertThat(
      response
        .path("mutateFixedLine.journeyPatterns[0].pointsInSequence[0].quayRef")
        .entity(String.class)
        .get()
    )
      .isEqualTo("NSR:Quay:494");
    assertThat(
      response
        .path(
          "mutateFixedLine.journeyPatterns[0].serviceJourneys[0].passingTimes[0].departureTime"
        )
        .entity(String.class)
        .get()
    )
      .isEqualTo("07:00:00");
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
