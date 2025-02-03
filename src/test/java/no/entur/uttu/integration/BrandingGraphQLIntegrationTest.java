package no.entur.uttu.integration;

import org.junit.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

public class BrandingGraphQLIntegrationTest extends AbstractGraphQLIntegrationTest {

  @Test
  public void testCreateDeleteBranding() {
    var input = InputGenerators.generateBrandingInput("TestBrand");

    GraphQlTester.Response response = graphQlTester
      .documentName("mutateBranding")
      .variable("branding", input)
      .execute();

    String brandingId = response.path("mutateBranding.id").entity(String.class).get();

    response =
      graphQlTester.documentName("branding").variable("id", brandingId).execute();

    response
      .path("branding.id")
      .entity(String.class)
      .matches(id -> id.startsWith("TST:Branding:"));

    response
      .path("branding.name")
      .entity(String.class)
      .matches(name -> name.equals("TestBrand"));


    response =
            graphQlTester.documentName("deleteBranding").variable("id", brandingId).execute();

    response
            .path("deleteBranding.id")
            .entity(String.class)
            .matches(id -> id.equals(brandingId));
  }
}
