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

class NetworkGraphQLIntegrationTest extends AbstractFlexibleLinesGraphQLIntegrationTest {
    String testNetworkName="TestNetwork"

    @Test
    void createNetworkTest() {
        ValidatableResponse rsp = createNetwork(testNetworkName)

        assertNetworkResponse(rsp,"mutateNetwork")
        String id = getNetworkId(rsp)

        String queryForNetwork = """ { network(id:"$id") { id, name, authorityRef } } """

        assertNetworkResponse(executeGraphqQLQueryOnly(queryForNetwork), "network")
    }


    void assertNetworkResponse(ValidatableResponse rsp, String path) {
        rsp.body("data. "+path+".id", startsWith("TST:Network"))
                .body("data. "+path+".name", equalTo(testNetworkName))
                .body("data. "+path+".authorityRef", equalTo("NOG:Organisation:1"))
    }
}
