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

package no.entur.uttu.export.netex.producer.line;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.FlexibleLine;
import org.rutebanken.netex.model.Line_VersionStructure;
import org.rutebanken.netex.model.NoticeAssignment;

import java.util.List;

public interface LineProducerVisitor {
    Line_VersionStructure visitFixedLine(FixedLine fixedLine, List<NoticeAssignment> noticeAssignments, NetexExportContext context);
    Line_VersionStructure visitFlexibleLine(FlexibleLine flexibleLine, List<NoticeAssignment> noticeAssignments, NetexExportContext context);
}
