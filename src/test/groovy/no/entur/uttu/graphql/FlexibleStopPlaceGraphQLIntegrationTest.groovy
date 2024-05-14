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
import no.entur.uttu.repository.StopPointInJourneyPatternRepository
import no.entur.uttu.stubs.StopPointInJourneyPatternRepositoryStub
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

import javax.annotation.concurrent.NotThreadSafe

import static org.hamcrest.Matchers.*

class FlexibleStopPlaceGraphQLIntegrationTest extends AbstractFlexibleLinesGraphQLIntegrationTest {

    @Autowired
    StopPointInJourneyPatternRepositoryStub stopPointInJourneyPatternRepository;

    String deleteFlexibleStopPlaceMutation = """
mutation deleteFlexibleStopPlace(\$id: ID!) {
  deleteFlexibleStopPlace(id: \$id) {
    id
  }
}
"""

    String flexAreaName = "FlexibleAreaTest"
    String flexAreasName = "FlexibleAreasTest"

    @Test
    void createFlexibleStopPlaceWithFlexibleAreaTest() {

        ValidatableResponse response = createFlexibleStopPlaceWithFlexibleArea(flexAreaName)
        assertFlexibleAreaResponse(response, "mutateFlexibleStopPlace")

        String id = extractId(response, "mutateFlexibleStopPlace")
        String queryForFlexibleArea = """ {flexibleStopPlace (id:"$id") {id name keyValues { key values } flexibleArea { polygon {type coordinates}}}}"""

        assertFlexibleAreaResponse(executeGraphqQLQueryOnly(queryForFlexibleArea), "flexibleStopPlace");
    }

    @Test
    void createFlexibleStopPlaceWithFlexibleAreasTest() {

        ValidatableResponse response = createFlexibleStopPlaceWithFlexibleAreas(flexAreasName)
        assertFlexibleAreasResponse(response, "mutateFlexibleStopPlace")

        String id = extractId(response, "mutateFlexibleStopPlace")
        String queryForFlexibleAreas = """ {flexibleStopPlace (id:"$id") {id name flexibleAreas { keyValues { key values } polygon {type coordinates}}}}"""

        assertFlexibleAreasResponse(executeGraphqQLQueryOnly(queryForFlexibleAreas), "flexibleStopPlace");
    }

    @Test
    void createFlexibleStopPlaceWithHailAndRideAreaTest() {
        String hailAndRideTest = "HailAndRideTest"
        createFlexibleStopPlaceWithHailAndRideArea(hailAndRideTest)
                .body("data.mutateFlexibleStopPlace.id", startsWith("TST:FlexibleStopPlace"))
                .body("data.mutateFlexibleStopPlace.name", equalTo(hailAndRideTest))
                .body("data.mutateFlexibleStopPlace.hailAndRideArea.startQuayRef", equalTo("NSR:Quay:565"))
                .body("data.mutateFlexibleStopPlace.hailAndRideArea.endQuayRef", equalTo("NSR:Quay:494"))
    }

    @Test
    void deleteFlexibleStopPlace() {
        stopPointInJourneyPatternRepository.setNextCountByFlexibleStopPlace(1)
        executeGraphQL(deleteFlexibleStopPlaceMutation, "{ \"id\": \"TST:FlexibleStopPlace:1\" }", 200)
                .body("errors[0].extensions.code", equalTo("ENTITY_IS_REFERENCED"))
                .body("errors[0].extensions.metadata.numberOfReferences", equalTo(1))
        stopPointInJourneyPatternRepository.setNextCountByFlexibleStopPlace(0)
        executeGraphQL(deleteFlexibleStopPlaceMutation, "{ \"id\": \"TST:FlexibleStopPlace:1\" }", 200)
            .body("errors", nullValue())
    }

    void assertFlexibleAreaResponse(ValidatableResponse rsp, String path) {
        rsp.body("data." + path + ".id", startsWith("TST:FlexibleStopPlace"))
                .body("data." + path + ".name", equalTo(flexAreaName))
                .body("data." + path + ".keyValues[0].key", equalTo("foo"))
                .body("data." + path + ".flexibleArea.polygon.type", equalTo("Polygon"))
                .body("data." + path + ".flexibleArea.polygon.coordinates", hasSize(4))

    }

    void assertFlexibleAreasResponse(ValidatableResponse rsp, String path) {
        rsp.body("data." + path + ".id", startsWith("TST:FlexibleStopPlace"))
                .body("data." + path + ".name", equalTo(flexAreasName))
                .body("data." + path + ".flexibleAreas[0].keyValues[0].key", equalTo("foo"))
                .body("data." + path + ".flexibleAreas[0].polygon.type", equalTo("Polygon"))
                .body("data." + path + ".flexibleAreas[0].polygon.coordinates", hasSize(4))

    }

}

