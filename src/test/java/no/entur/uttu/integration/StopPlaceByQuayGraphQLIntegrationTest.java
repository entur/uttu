package no.entur.uttu.integration;

import org.junit.Test;

public class StopPlaceByQuayGraphQLIntegrationTest
  extends AbstractGraphQLIntegrationTest {

  @Test
  public void testGetStopPlaceByQuayRef() {
    var response = graphQlTester
      .document(
        """
                 query GetStopPlaceByQuayRef($id: ID!){ stopPlaceByQuayRef(id:$id) { id, name { lang value }, quays { id publicCode }}}
                 """
      )
      .variable("id", "NSR:Quay:494")
      .execute();

    response
      .path("stopPlaceByQuayRef.id")
      .entity(String.class)
      .equals("NSR:StopPlace:301");
  }
}
