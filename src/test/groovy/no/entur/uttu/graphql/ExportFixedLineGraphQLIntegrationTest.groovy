package no.entur.uttu.graphql

import io.restassured.response.ValidatableResponse
import no.entur.uttu.model.job.ExportStatusEnumeration
import org.junit.Test

import java.time.LocalDate

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.isEmptyOrNullString
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.startsWith

class ExportFixedLineGraphQLIntegrationTest extends AbstractFixedLinesGraphQLIntegrationTest {

    @Test
    void createExport() {
        String name = "Fiktiv linje";
        ValidatableResponse fixedLineResponse = createFixedLine(name);

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

        String lineRef = fixedLineResponse.extract().body().path("data.mutateFixedLine.id")
        String variables = """    
{
  "export": {
    "name": "$name",
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
    }
}
