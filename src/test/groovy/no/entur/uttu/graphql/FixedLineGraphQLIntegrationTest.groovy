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

class FixedLineGraphQLIntegrationTest extends AbstractFixedLinesGraphQLIntegrationTest {

    String testFixedLineName = "TestFixedLine"

    @Test
    void createFixedLineTest() {
        createFixedLine(testFixedLineName)
                .body("data.mutateFixedLine.id", startsWith("TST:Line"))
                .body("data.mutateFixedLine.name", equalTo(testFixedLineName))
                .body("data.mutateFixedLine.journeyPatterns[0].pointsInSequence[0].quayRef", equalTo("NSR:Quay:69"))
                .body("data.mutateFixedLine.journeyPatterns[0].serviceJourneys[0].passingTimes[0].departureTime", equalTo("07:00:00"))


    }
}