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
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.export.netex.producer.common.OrganisationProducer;
import no.entur.uttu.model.BookingArrangement;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
import org.rutebanken.netex.model.BookingAccessEnumeration;
import org.rutebanken.netex.model.BookingMethodEnumeration;
import org.rutebanken.netex.model.FlexibleLineRefStructure;
import org.rutebanken.netex.model.FlexibleLineTypeEnumeration;
import org.rutebanken.netex.model.Line_VersionStructure;
import org.rutebanken.netex.model.NoticeAssignment;
import org.rutebanken.netex.model.PurchaseMomentEnumeration;
import org.rutebanken.netex.model.PurchaseWhenEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LineProducer implements LineProducerVisitor {

    @Autowired
    private NetexObjectFactory objectFactory;

    @Autowired
    private ContactStructureProducer contactStructureProducer;

    @Autowired
    private OrganisationProducer organisationProducer;


    public org.rutebanken.netex.model.Line_VersionStructure produce(Line line, List<NoticeAssignment> noticeAssignments, NetexExportContext context) {
        return line.accept(this, noticeAssignments, context);
    }

    @Override
    public Line_VersionStructure visitFixedLine(no.entur.uttu.model.FixedLine fixedLine, List<NoticeAssignment> noticeAssignments, NetexExportContext context) {
        org.rutebanken.netex.model.Line netex = new org.rutebanken.netex.model.Line();
        mapCommon(fixedLine, netex, noticeAssignments, context);
        return NetexIdProducer.copyIdAndVersion(netex, fixedLine);
    }

    @Override
    public Line_VersionStructure visitFlexibleLine(no.entur.uttu.model.FlexibleLine flexibleLine, List<NoticeAssignment> noticeAssignments, NetexExportContext context) {
        org.rutebanken.netex.model.FlexibleLine netex = new org.rutebanken.netex.model.FlexibleLine();

        netex.setFlexibleLineType(objectFactory.mapEnum(flexibleLine.getFlexibleLineType(), FlexibleLineTypeEnumeration.class));
        mapCommon(flexibleLine, netex, noticeAssignments, context);
        mapBookingArrangements(flexibleLine.getBookingArrangement(), netex);

        return NetexIdProducer.copyIdAndVersion(netex, flexibleLine);
    }

    protected void mapCommon(no.entur.uttu.model.Line local, org.rutebanken.netex.model.Line_VersionStructure netex, List<NoticeAssignment> noticeAssignments, NetexExportContext context) {
        netex.setName(objectFactory.createMultilingualString(local.getName()));
        netex.setPrivateCode(objectFactory.createPrivateCodeStructure(local.getPrivateCode()));

        netex.setTransportMode(objectFactory.mapEnum(local.getTransportMode(), AllVehicleModesOfTransportEnumeration.class));
        netex.setTransportSubmode(objectFactory.mapTransportSubmodeStructure(local.getTransportSubmode()));

        netex.setPublicCode(local.getPublicCode());
        netex.setDescription(objectFactory.createMultilingualString(local.getDescription()));

        if (local.getOperatorRef() != null) {
            netex.setOperatorRef(organisationProducer.produceOperatorRef(local.getOperatorRef(), false, context));
            context.operatorRefs.add(local.getOperatorRef());
        }

        netex.setRepresentedByGroupRef(objectFactory.createGroupOfLinesRefStructure(local.getNetwork().getNetexId()));
        context.networks.add(local.getNetwork());

        noticeAssignments.addAll(objectFactory.createNoticeAssignments(local, FlexibleLineRefStructure.class, local.getNotices(), context));
        context.notices.addAll(local.getNotices());
    }

    protected void mapBookingArrangements(BookingArrangement local, org.rutebanken.netex.model.FlexibleLine netex) {
        if (local != null) {
            netex.withBookingAccess(objectFactory.mapEnum(local.getBookingAccess(), BookingAccessEnumeration.class))
                    .withBookingMethods(objectFactory.mapEnums(local.getBookingMethods(), BookingMethodEnumeration.class))
                    .withBookWhen(objectFactory.mapEnum(local.getBookWhen(), PurchaseWhenEnumeration.class))
                    .withBuyWhen(objectFactory.mapEnums(local.getBuyWhen(), PurchaseMomentEnumeration.class))
                    .withLatestBookingTime(local.getLatestBookingTime())
                    .withMinimumBookingPeriod(local.getMinimumBookingPeriod())
                    .withBookingNote(objectFactory.createMultilingualString(local.getBookingNote()))
                    .withBookingContact(contactStructureProducer.mapContactStructure(local.getBookingContact()));
        }
    }


}