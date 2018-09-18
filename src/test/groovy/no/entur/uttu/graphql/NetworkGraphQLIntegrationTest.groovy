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

class NetworkGraphQLIntegrationTest extends AbstractFlexibleLinesGraphQLIntegrationTest {


    @Test
    void createNetwork() {
        //Calling GraphQL-api to merge StopPlaces
        String graphQlJsonQuery = """
 mutation mutateNetwork(\$network: NetworkInput!) {
  mutateNetwork(input: \$network) {
    id
    name
    authorityRef
  }
  }
                """

        String variables = """{
            "network": {
                "name": "TestNetwork",
                "authorityRef": "22"
            }
        }"""

        executeGraphQL(graphQlJsonQuery, variables)
                .body("data.mutateNetwork.id", startsWith("TST:Network"))
                .body("data.mutateNetwork.name", equalTo("TestNetwork"))
                .body("data.mutateNetwork.authorityRef", equalTo(22))
    }
}
