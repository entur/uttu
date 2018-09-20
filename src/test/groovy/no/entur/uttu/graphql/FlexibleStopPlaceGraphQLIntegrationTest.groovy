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

    @Test
    void createFlexibleStopPlaceWithFlexibleAreaTest() {
        String flexAreaName = "FlexibleAreaTest"
        createFlexibleStopPlaceWithFlexibleArea(flexAreaName)
                .body("data.mutateFlexibleStopPlace.id", startsWith("TST:FlexibleStopPlace"))
                .body("data.mutateFlexibleStopPlace.name", equalTo(flexAreaName))
                .body("data.mutateFlexibleStopPlace.flexibleArea.polygon.type", equalTo("Polygon"))
                .body("data.mutateFlexibleStopPlace.flexibleArea.polygon.coordinates", hasSize(4))
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


}

