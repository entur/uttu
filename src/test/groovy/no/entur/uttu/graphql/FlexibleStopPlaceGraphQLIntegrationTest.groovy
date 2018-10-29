/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.graphql

import io.restassured.response.ValidatableResponse
import org.junit.Test

import static org.hamcrest.Matchers.*

class FlexibleStopPlaceGraphQLIntegrationTest extends AbstractFlexibleLinesGraphQLIntegrationTest {
    String createStopPlaceQuery = """
 mutation mutateFlexibleStopPlace(\$flexibleStopPlace: FlexibleStopPlaceInput!) {
  mutateFlexibleStopPlace(input: \$flexibleStopPlace) {
    id
    name
    flexibleArea {
      polygon {
        type
        coordinates
      }
    }
    hailAndRideArea {
      startQuayRef
      endQuayRef 
    }
  }
  }
         """
    String flexAreaName = "FlexibleAreaTest"

    @Test
    void createFlexibleStopPlaceWithFlexibleAreaTest() {

        ValidatableResponse response = createFlexibleStopPlaceWithFlexibleArea(flexAreaName)
        assertFlexibleAreaResponse(response, "mutateFlexibleStopPlace")

        String id = extractId(response, "mutateFlexibleStopPlace")
        String queryForFlexibleArea = """ {flexibleStopPlace (id:"$id") {id name flexibleArea { polygon {type coordinates}}}}"""

        assertFlexibleAreaResponse(executeGraphqQLQueryOnly(queryForFlexibleArea), "flexibleStopPlace");
    }

    @Test
    void createFlexibleStopPlaceWithHailAndRideAreaTest() {
        String hailAndRideTest = "HailAndRideTest"
        createFlexibleStopPlaceWithHailAndRideArea(hailAndRideTest)
                .body("data.mutateFlexibleStopPlace.id", startsWith("TST:FlexibleStopPlace"))
                .body("data.mutateFlexibleStopPlace.name", equalTo(hailAndRideTest))
                .body("data.mutateFlexibleStopPlace.hailAndRideArea.startQuayRef", equalTo("NSR:Quay:start"))
                .body("data.mutateFlexibleStopPlace.hailAndRideArea.endQuayRef", equalTo("NSR:Quay:end"))
    }


    void assertFlexibleAreaResponse(ValidatableResponse rsp, String path) {
        rsp.body("data." + path + ".id", startsWith("TST:FlexibleStopPlace"))
                .body("data." + path + ".name", equalTo(flexAreaName))
                .body("data." + path + ".flexibleArea.polygon.type", equalTo("Polygon"))
                .body("data." + path + ".flexibleArea.polygon.coordinates", hasSize(4))

    }

}

