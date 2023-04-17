package no.entur.uttu.graphql

import io.restassured.response.ValidatableResponse
import org.junit.Test

import static org.hamcrest.Matchers.equalTo

class StopPlaceByQuayGraphQLIntegrationTest extends AbstractFlexibleLinesGraphQLIntegrationTest{

    @Test
    void getStopPlaceByQuayRefTest() {
        String id = "NSR:StopPlace:337"
        String query = """ { stopPlaceByQuayRef(id:"$id") { id, name { lang value }, quays { id publicCode }}}"""
        assertResponse(executeGraphqQLQueryOnly(query), "stopPlaceByQuayRef")
    }

    void assertResponse(ValidatableResponse rsp, String path) {
        rsp.body("data. "+path+".id", equalTo("NSR:StopPlace:337"))
    }
}

