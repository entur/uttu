package no.entur.uttu.export.netex.producer.line;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.export.netex.producer.common.OrganisationProducer;
import no.entur.uttu.model.BookingArrangement;
import no.entur.uttu.model.FlexibleLine;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
import org.rutebanken.netex.model.BookingAccessEnumeration;
import org.rutebanken.netex.model.BookingMethodEnumeration;
import org.rutebanken.netex.model.FlexibleLineRefStructure;
import org.rutebanken.netex.model.FlexibleLineTypeEnumeration;
import org.rutebanken.netex.model.NoticeAssignment;
import org.rutebanken.netex.model.PurchaseMomentEnumeration;
import org.rutebanken.netex.model.PurchaseWhenEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlexibleLineProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    @Autowired
    private ContactStructureProducer contactStructureProducer;

    @Autowired
    private OrganisationProducer organisationProducer;

    public org.rutebanken.netex.model.FlexibleLine produce(FlexibleLine local, List<NoticeAssignment> noticeAssignments, NetexExportContext context) {
        org.rutebanken.netex.model.FlexibleLine netex = new org.rutebanken.netex.model.FlexibleLine();

        netex.setName(objectFactory.createMultilingualString(local.getName()));
        netex.setPrivateCode(objectFactory.createPrivateCodeStructure(local.getPrivateCode()));

        netex.setFlexibleLineType(objectFactory.mapEnum(local.getFlexibleLineType(), FlexibleLineTypeEnumeration.class));
        netex.setTransportMode(objectFactory.mapEnum(local.getTransportMode(), AllVehicleModesOfTransportEnumeration.class));
        netex.setTransportSubmode(objectFactory.mapTransportSubmodeStructure(local.getTransportSubmode()));

        netex.setPublicCode(local.getPublicCode());
        netex.setDescription(objectFactory.createMultilingualString(local.getDescription()));

        mapBookingArrangements(local.getBookingArrangement(), netex);


        if (local.getOperatorRef() != null) {
            netex.setOperatorRef(organisationProducer.produceOperatorRef(local.getOperatorRef(), false, context));
            context.operatorRefs.add(local.getOperatorRef());
        }

        netex.setRepresentedByGroupRef(objectFactory.createGroupOfLinesRefStructure(local.getNetwork().getNetexId()));
        context.networks.add(local.getNetwork());

        noticeAssignments.addAll(objectFactory.createNoticeAssignments(local, FlexibleLineRefStructure.class, local.getNotices(), context));
        context.notices.addAll(local.getNotices());

        return NetexIdProducer.copyIdAndVersion(netex, local);
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