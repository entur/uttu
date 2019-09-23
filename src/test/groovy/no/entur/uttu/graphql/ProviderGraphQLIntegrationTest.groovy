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
import no.entur.uttu.config.MockedRoleAssignmentExtractor
import org.junit.Test
import org.rutebanken.helper.organisation.RoleAssignment
import org.springframework.beans.factory.annotation.Autowired

import static org.hamcrest.Matchers.*

class ProviderGraphQLIntegrationTest extends AbstractGraphQLResourceIntegrationTest {
    @Autowired
    MockedRoleAssignmentExtractor mockedRoleAssignmentExtractor;

    String getProvidersQuery = """
   query GetProviders {
     providers {
       name
       code
     }
   } 
"""

    protected String getUrl() {
        return "/services/flexible-lines/providers/graphql"
    }

    protected Properties getCredentials() {
        Properties credentials = new Properties()
        credentials.put("username", "user")
        credentials.put("password", "secret")
        return credentials
    }

    @Test
    void getProvidersTest() {
        mockedRoleAssignmentExtractor.setNextReturnedRoleAssignment(
                RoleAssignment.builder().withRole("editRouteData").withOrganisation("TST").build()
        )
        ValidatableResponse response = executeGraphqQLQueryOnly(getProvidersQuery);
        response
            .body("data.providers", iterableWithSize(1))
    }
}
