package no.entur.uttu.graphql

import io.restassured.response.ValidatableResponse
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.nullValue

class DayTypeGraphQLIntegrationTest extends AbstractFixedLinesGraphQLIntegrationTest {

    String deleteDayTypeMutation = """
mutation DeleteDayType(\$id: ID!) {
  deleteDayType(id: \$id) {
    id
  }
}
"""
    String getDayTypesByIds = """
  query GetDayTypesByIds(\$ids: [ID!]!) {
    dayTypesByIds(ids: \$ids) {
      id
      numberOfServiceJourneys
    }
  }
"""

    @Test
    void testDayTypeLifeCycle() {
        ValidatableResponse dayTypeResponse = createDayType()
        String dayTypeRef = dayTypeResponse.extract().body().path("data.mutateDayType.id")
        executeGraphQL(getDayTypesByIds, "{ \"ids\": [\"" + dayTypeRef + "\"]}", 200)
                .body("errors", nullValue())
        executeGraphQL(deleteDayTypeMutation, "{ \"id\": \"" + dayTypeRef + "\" }", 200)
                .body("errors", nullValue())
    }

    @Test
    void testUnableToDeleteDayTypeUsedByServiceJourney() {
        ValidatableResponse dayTypeResponse = createDayType()
        String dayTypeRef = dayTypeResponse.extract().body().path("data.mutateDayType.id")

        createFixedLineWithDayTypeRef("TestSJ", dayTypeRef)

        executeGraphQL(deleteDayTypeMutation, "{ \"id\": \"" + dayTypeRef + "\" }", 200)
                .body("errors[0].extensions.code", equalTo("ENTITY_IS_REFERENCED"))
                .body("errors[0].extensions.metadata.numberOfReferences", equalTo(1))
    }
}
