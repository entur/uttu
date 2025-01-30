package no.entur.uttu.integration;

import no.entur.uttu.UttuIntegrationTest;
import no.entur.uttu.stubs.UserContextServiceStub;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles({ "in-memory-blobstore" })
public abstract class AbstractGraphQLIntegrationTest extends UttuIntegrationTest {

  @Autowired
  protected UserContextServiceStub userContextServiceStub;

  protected HttpGraphQlTester graphQlTester;

  @Before
  public void setup() {
    userContextServiceStub.setPreferredName("John Doe");
    userContextServiceStub.setAdmin(false);
    userContextServiceStub.setHasAccessToProvider("tst", true);
    userContextServiceStub.setHasAccessToProvider("foo", false);

    WebTestClient.Builder clientBuilder = WebTestClient
      .bindToServer()
      .baseUrl("http://localhost:" + port + "/services/flexible-lines/tst/graphql");

    graphQlTester =
      HttpGraphQlTester
        .builder(clientBuilder)
        .headers(headers -> headers.setBasicAuth("admin", "topsecret"))
        .build();
  }
}
