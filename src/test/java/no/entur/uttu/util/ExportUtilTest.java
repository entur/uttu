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

package no.entur.uttu.util;

import no.entur.uttu.model.Provider;
import no.entur.uttu.model.job.Export;
import org.junit.Assert;
import org.junit.Test;

public class ExportUtilTest {

  @Test
  public void testCreateBackupDataSetFilenameWithExportName() {
    Export export = createExport("t1");
    String fileName = ExportUtil.createBackupDataSetFilename(export);

    Assert.assertTrue(fileName.matches("tst_t1_[0-9]*_null.zip"));
  }

  @Test
  public void testCreateBackupDataSetFilenameWithOutExportName() {
    Export export = createExport(null);
    String fileName = ExportUtil.createBackupDataSetFilename(export);
    Assert.assertTrue(fileName.matches("tst_[0-9]*_null.zip"));
  }

  private Export createExport(String name) {
    Export export = new Export() {};
    export.setName(name);
    Provider provider = new Provider();
    provider.setCode("TST");
    export.setProvider(provider);
    return export;
  }
}
