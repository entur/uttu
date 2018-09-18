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

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith

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
    void createFlexibleStopPlaceWithFlexibleArea() {


        String variables = """    
{
  "flexibleStopPlace": {
    "name": "FlexibleAreaTest",
    "description": "flexible area desc",
    "transportMode": "water",
    "flexibleArea": {
      "polygon": {
        "coordinates": [
          [
            2.1,
            3.3
          ],
          [
            4.1,
            5.2
          ],
          [
            4.9,
            5.9
          ],
          [
            2.1,
            3.3
          ]
        ],
        "type": "Polygon"
      }
    }
  }
}
        """

        executeGraphQL(createStopPlaceQuery, variables)
                .body("data.mutateFlexibleStopPlace.id", startsWith("TST:FlexibleStopPlace"))
                .body("data.mutateFlexibleStopPlace.name", equalTo("FlexibleAreaTest"))
    }

    @Test
    void createFlexibleStopPlaceWithHailAndRideArea() {


        String variables = """    
        {
        "flexibleStopPlace": {
        "name": "HailAndRideTest",

        "description": "hail and ride desc",
        "transportMode": "bus",
        "hailAndRideArea": {"startQuayRef": "NSR:Quay:start","endQuayRef": "NSR:Quay:end"}
    }
        }"""

        executeGraphQL(createStopPlaceQuery, variables)
                .body("data.mutateFlexibleStopPlace.id", startsWith("TST:FlexibleStopPlace"))
                .body("data.mutateFlexibleStopPlace.name", equalTo("HailAndRideTest"))
                .body("data.mutateFlexibleStopPlace.hailAndRideArea.startQuayRef", equalTo("NSR:Quay:start"))
                .body("data.mutateFlexibleStopPlace.hailAndRideArea.endQuayRef", equalTo("NSR:Quay:end"))
    }


}

