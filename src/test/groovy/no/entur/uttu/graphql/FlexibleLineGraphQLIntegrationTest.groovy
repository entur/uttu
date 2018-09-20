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

class FlexibleLineGraphQLIntegrationTest extends AbstractFlexibleLinesGraphQLIntegrationTest {

    String testFlexibleLineName = "TestFlexibleLine"

    @Test
    void createFlexibleLineTest() {

        createFlexibleLine(testFlexibleLineName)
                .body("data.mutateFlexibleLine.id", startsWith("TST:FlexibleLine"))
                .body("data.mutateFlexibleLine.name", equalTo(testFlexibleLineName))
                .body("data.mutateFlexibleLine.journeyPatterns[0].serviceJourneys[0].passingTimes[0].departureTime", equalTo("16:00:00"))

    }
}
