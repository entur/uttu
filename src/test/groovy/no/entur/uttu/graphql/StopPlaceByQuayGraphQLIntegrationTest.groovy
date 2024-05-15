package no.entur.uttu.graphql

import io.restassured.response.ValidatableResponse
import org.junit.Test

import static org.hamcrest.Matchers.equalTo

class StopPlaceByQuayGraphQLIntegrationTest extends AbstractFlexibleLinesGraphQLIntegrationTest{

    @Test
    void getStopPlaceByQuayRefTest() {
        String quayId = "NSR:Quay:494";
        String expectedStopPlaceId = "NSR:StopPlace:301"
        String query = """ { stopPlaceByQuayRef(id:"$quayId") { id, name { lang value }, quays { id publicCode }}}"""
        assertResponse(executeGraphqQLQueryOnly(query), "stopPlaceByQuayRef", expectedStopPlaceId)
    }

    void assertResponse(ValidatableResponse rsp, String path, String expectedId) {
        rsp.body("data. "+path+".id", equalTo(expectedId))
    }
}

