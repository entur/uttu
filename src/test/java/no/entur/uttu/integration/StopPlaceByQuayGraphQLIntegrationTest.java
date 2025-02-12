package no.entur.uttu.integration;

import org.junit.Test;

public class StopPlaceByQuayGraphQLIntegrationTest
  extends AbstractGraphQLIntegrationTest {

  @Test
  public void testGetStopPlaceByQuayRef() {
    var response = graphQlTester
      .documentName("stopPlaceByQuayRef")
      .variable("id", "NSR:Quay:494")
      .execute();

    response
      .path("stopPlaceByQuayRef.id")
      .entity(String.class)
      .equals("NSR:StopPlace:301");
  }
}
