package no.entur.uttu.integration;

import org.junit.Before;
import org.junit.Test;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

public class ProviderGraphQLIngrationTest extends AbstractGraphQLIntegrationTest {

  @Override
  @Before
  public void setup() {
    userContextServiceStub.setPreferredName("John Doe");
    userContextServiceStub.setAdmin(false);
    userContextServiceStub.setHasAccessToProvider("tst", true);
    userContextServiceStub.setHasAccessToProvider("foo", false);

    WebTestClient.Builder clientBuilder = WebTestClient.bindToServer()
      .baseUrl("http://localhost:" + port + "/services/flexible-lines/providers/graphql");

    graphQlTester = HttpGraphQlTester.builder(clientBuilder)
      .headers(headers -> headers.setBasicAuth("admin", "topsecret"))
      .build();
  }

  @Test
  public void testGetProviders() {
    var response = graphQlTester.documentName("providers").execute();

    response.path("providers").entityList(Object.class).hasSize(1);
    response.path("providers[0].code").entity(String.class).equals("tst");
  }

  @Test
  public void testGetAnotherProvider() {
    userContextServiceStub.setHasAccessToProvider("tst", false);
    userContextServiceStub.setHasAccessToProvider("foo", true);

    var response = graphQlTester.documentName("providers").execute();

    response.path("providers").entityList(Object.class).hasSize(1);
    response.path("providers[0].code").entity(String.class).equals("foo");
  }

  @Test
  public void testGetUserContext() {
    var response = graphQlTester.documentName("userContext").execute();

    response.path("userContext.preferredName").entity(String.class).equals("John Doe");
    response.path("userContext.isAdmin").entity(Boolean.class).equals(false);
    response.path("userContext.providers").entityList(Object.class).hasSize(1);
    response.path("userContext.providers[0].code").entity(String.class).equals("tst");
  }
}
