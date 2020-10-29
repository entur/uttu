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
import no.entur.uttu.model.job.ExportStatusEnumeration
import org.junit.Test
import org.xmlunit.validation.ValidationResult

import java.time.LocalDate

import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

class ExportGraphQLIntegrationTest extends AbstractFlexibleLinesGraphQLIntegrationTest {


    @Test
    void createExport() {
        String name = "ExportTest"
        ValidatableResponse flexibleLineResponse = createFlexibleLine(name)

        String createExportQuery = """
 mutation export(\$export: ExportInput!) {
  export(input: \$export) {
    id
    name
    exportStatus
    downloadUrl
    messages {
        message
        severity
    }
  }
  }
         """

        LocalDate today = LocalDate.now()

        LocalDate fromDate = today.minusDays(10)
        LocalDate toDate = today.plusDays(100)

        String lineRef = flexibleLineResponse.extract().body().path("data.mutateFlexibleLine.id")
        String variables = """    
{
  "export": {
    "name": "$name",
    "fromDate": "$fromDate" ,
    "toDate":"$toDate",
    "lineAssociations":[{ "lineRef": "$lineRef" }]
  }
}
        """

        ValidatableResponse rsp = executeGraphQL(createExportQuery, variables)
                .body("data.export.id", startsWith("TST:Export"))
                .body("data.export.name", equalTo(name))
                .body("data.export.exportStatus", equalTo(ExportStatusEnumeration.SUCCESS.value()))
                .body("data.export.downloadUrl", startsWith("tst/export/"))

        String downloadUrl = rsp.extract().body().path("data.export.downloadUrl")
        authenticatedRequestSpecification()
                .port(port)
                .when()
                .get("/services/flexible-lines/" + downloadUrl)
                .then()
                .log().body()
                .statusCode(200)
                .body(not(isEmptyOrNullString()))


        String deleteLineMutation = """
 mutation DeleteLine(\$id: ID!) {
  deleteFlexibleLine(id: \$id) {
    id
  }
  }
         """

        String deleteLineVariables = """
{
  "id": "$lineRef"
}
"""


        ValidatableResponse deleteLineRsp = executeGraphQL(deleteLineMutation, deleteLineVariables)
                .body("data.deleteFlexibleLine.id", equalTo(lineRef))
    }
}
