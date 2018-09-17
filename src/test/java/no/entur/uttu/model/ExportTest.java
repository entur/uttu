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

package no.entur.uttu.model;

import no.entur.uttu.model.job.Export;
import no.entur.uttu.model.job.ExportMessage;
import no.entur.uttu.model.job.SeverityEnumeration;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class ExportTest {


    @Test
    public void exportMessagesAreSortedBySeverity() {

        ExportMessage errorMsg = new ExportMessage(SeverityEnumeration.ERROR, "errorMsg");
        ExportMessage warnMsg1 = new ExportMessage(SeverityEnumeration.WARN, "warnMsg1");
        ExportMessage warnMsg2 = new ExportMessage(SeverityEnumeration.WARN, "warnMsg2");
        ExportMessage infoMsg = new ExportMessage(SeverityEnumeration.INFO, "infoMsg");

        Export export = new Export();

        export.addMessage(warnMsg1);
        export.addMessage(infoMsg);
        export.addMessage(errorMsg);
        export.addMessage(warnMsg2);

        Assert.assertEquals(4, export.getMessages().size(), 4);

        Iterator<ExportMessage> messages = export.getMessages().iterator();

        Assert.assertEquals(errorMsg, messages.next());
        Assert.assertEquals(warnMsg1, messages.next());
        Assert.assertEquals(warnMsg2, messages.next());
        Assert.assertEquals(infoMsg, messages.next());

    }
}
