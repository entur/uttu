package no.entur.uttu.integration;

import org.junit.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

public class NetworkGraphQLIntegrationTest extends AbstractGraphQLIntegrationTest {

  @Test
  public void testCreateNetwork() {
    var input = InputGenerators.generateNetworkInput("TestNetwork", "NOG:Authority:1");

    GraphQlTester.Response response = graphQlTester
      .documentName("mutateNetwork")
      .variable("network", input)
      .execute();

    String networkId = response.path("mutateNetwork.id").entity(String.class).get();

    response = graphQlTester.documentName("network").variable("id", networkId).execute();

    response
      .path("network.id")
      .entity(String.class)
      .matches(id -> id.startsWith("TST:Network:"));

    response
      .path("network.name")
      .entity(String.class)
      .matches(name -> name.equals("TestNetwork"));

    response
      .path("network.authorityRef")
      .entity(String.class)
      .matches(authorityRef -> authorityRef.equals("NOG:Authority:1"));
  }
}
