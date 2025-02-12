package no.entur.uttu.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

public class BrandingGraphQLIntegrationTest extends AbstractGraphQLIntegrationTest {

  @Test
  public void testCreateUpdateDeleteBranding() {
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

    input = InputGenerators.generateBrandingInputForEdit(brandingId, "AnotherTestBrand");

    graphQlTester.documentName("mutateBranding").variable("branding", input).execute();

    graphQlTester
      .documentName("branding")
      .variable("id", brandingId)
      .execute()
      .path("branding.name")
      .entity(String.class)
      .matches(name -> name.equals("AnotherTestBrand"));

    graphQlTester
      .documentName("deleteBranding")
      .variable("id", brandingId)
      .execute()
      .path("deleteBranding.id")
      .entity(String.class)
      .matches(id -> id.equals(brandingId));
  }

  @Test
  public void testUnableToDeleteBrandingUsedByLine() {
    String brandingId = createBrandingWithName("TestBrand")
      .path("mutateBranding.id")
      .entity(String.class)
      .get();

    createFixedLine("TestLine", brandingId);

    graphQlTester
      .documentName("deleteBranding")
      .variable("id", brandingId)
      .execute()
      .errors()
      .satisfy(errors ->
        assertThat(errors)
          .anyMatch(error ->
            error.getExtensions().get("code").equals("ENTITY_IS_REFERENCED") &&
            ((Map<String, Object>) error.getExtensions().get("metadata")).get(
                "numberOfReferences"
              )
              .equals(1)
          )
      );
  }

  private GraphQlTester.Response createFixedLine(String name, String brandingId) {
    String networkId = createNetworkWithName(name)
      .path("mutateNetwork.id")
      .entity(String.class)
      .get();

    String dayTypeRef = createDayType()
      .path("mutateDayType.id")
      .entity(String.class)
      .get();

    var input = InputGenerators.generateFixedLineInput(
      name,
      networkId,
      dayTypeRef,
      brandingId
    );
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

  private GraphQlTester.Response createBrandingWithName(String name) {
    return graphQlTester
      .documentName("mutateBranding")
      .variable("branding", InputGenerators.generateBrandingInput(name))
      .execute();
  }
}
