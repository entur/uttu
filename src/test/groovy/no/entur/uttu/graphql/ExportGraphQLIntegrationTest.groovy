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

import no.entur.uttu.model.job.ExportStatusEnumeration
import org.junit.Test

import java.time.LocalDate

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith

class ExportGraphQLIntegrationTest extends AbstractFlexibleLinesGraphQLIntegrationTest {


    @Test
    void createExport() {
        String name="ExportTest"
        createFlexibleLine(name)

        String createExportQuery = """
 mutation export(\$export: ExportInput!) {
  export(input: \$export) {
    id
    name
    exportStatus
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

        String variables = """    
{
  "export": {
    "name": "$name",
    "fromDate": "$fromDate" ,
    "toDate":"$toDate"
  }
}
        """

        executeGraphQL(createExportQuery, variables)
                .body("data.export.id", startsWith("TST:Export"))
                .body("data.export.name", equalTo(name))
                .body("data.export.exportStatus", equalTo(ExportStatusEnumeration.SUCCESS.value()))
    }
}
