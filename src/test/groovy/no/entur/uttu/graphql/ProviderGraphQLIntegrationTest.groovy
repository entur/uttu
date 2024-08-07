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

import no.entur.uttu.stubs.UserContextServiceStub
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.concurrent.NotThreadSafe

import static org.hamcrest.Matchers.*

@NotThreadSafe
class ProviderGraphQLIntegrationTest extends AbstractGraphQLResourceIntegrationTest {

    @Autowired
    UserContextServiceStub userContextServiceStub;

    String getProvidersQuery = """
   query GetProviders {
     providers {
       name
       code
     }
   } 
"""

    String getUserContextQuery = """
   query GetUserContext {
    userContext {
        preferredName
        isAdmin
        providers {
            name
            code
        }
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
        executeGraphqQLQueryOnly(getProvidersQuery)
                .body("data.providers", iterableWithSize(1))
                .body("data.providers[0].code", equalTo("tst"))
    }

    @Test
    void getMoreProvidersTest() {
        userContextServiceStub.setHasAccessToProvider("tst", false)
        userContextServiceStub.setHasAccessToProvider("foo", true)

        executeGraphqQLQueryOnly(getProvidersQuery)
                .body("data.providers", iterableWithSize(1))
                .body("data.providers[0].code", equalTo("foo"))
    }

    @Test
    void getUserContextTest() {
        executeGraphqQLQueryOnly(getUserContextQuery)
            .body("data.userContext.preferredName", equalTo("John Doe"))
            .body("data.userContext.isAdmin", equalTo(false))
            .body("data.userContext.providers", iterableWithSize(1))
            .body("data.userContext.providers[0].code", equalTo("tst"))
    }
}
