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

import java.util.List;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.export.netex.producer.common.OrganisationProducer;
import no.entur.uttu.model.BookingArrangement;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.Line;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
import org.rutebanken.netex.model.BookingAccessEnumeration;
import org.rutebanken.netex.model.BookingMethodEnumeration;
import org.rutebanken.netex.model.FlexibleLineRefStructure;
import org.rutebanken.netex.model.FlexibleLineTypeEnumeration;
import org.rutebanken.netex.model.LineRefStructure;
import org.rutebanken.netex.model.Line_VersionStructure;
import org.rutebanken.netex.model.NoticeAssignment;
import org.rutebanken.netex.model.PurchaseMomentEnumeration;
import org.rutebanken.netex.model.PurchaseWhenEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LineProducer {

  private final NetexObjectFactory objectFactory;
  private final ContactStructureProducer contactStructureProducer;
  private final OrganisationProducer organisationProducer;

  @Autowired
  public LineProducer(
    NetexObjectFactory objectFactory,
    ContactStructureProducer contactStructureProducer,
    OrganisationProducer organisationProducer
  ) {
    this.objectFactory = objectFactory;
    this.contactStructureProducer = contactStructureProducer;
    this.organisationProducer = organisationProducer;
  }

  public org.rutebanken.netex.model.Line_VersionStructure produce(
    Line line,
    List<NoticeAssignment> noticeAssignments,
    NetexExportContext context
  ) {
    LineVisitor lineVisitor = new LineVisitor(noticeAssignments, context);
    line.accept(lineVisitor);
    return lineVisitor.getLine();
  }

  protected void mapCommon(
    no.entur.uttu.model.Line local,
    org.rutebanken.netex.model.Line_VersionStructure netex,
    NetexExportContext context
  ) {
    netex.setName(objectFactory.createMultilingualString(local.getName()));
    netex.setPrivateCode(
      objectFactory.createPrivateCodeStructure(local.getPrivateCode())
    );

    netex.setTransportMode(
      objectFactory.mapEnum(
        local.getTransportMode(),
        AllVehicleModesOfTransportEnumeration.class
      )
    );
    netex.setTransportSubmode(
      objectFactory.mapTransportSubmodeStructure(local.getTransportSubmode())
    );

    netex.setPublicCode(local.getPublicCode());
    netex.setDescription(objectFactory.createMultilingualString(local.getDescription()));

    if (local.getOperatorRef() != null) {
      netex.setOperatorRef(
        organisationProducer.produceOperatorRef(local.getOperatorRef(), false, context)
      );
      context.operatorRefs.add(local.getOperatorRef());
    }

    netex.setRepresentedByGroupRef(
      objectFactory.createGroupOfLinesRefStructure(local.getNetwork().getNetexId())
    );
    context.networks.add(local.getNetwork());
    context.notices.addAll(local.getNotices());
  }

  protected void mapBookingArrangements(
    BookingArrangement local,
    org.rutebanken.netex.model.FlexibleLine netex
  ) {
    if (local != null) {
      netex
        .withBookingAccess(
          objectFactory.mapEnum(local.getBookingAccess(), BookingAccessEnumeration.class)
        )
        .withBookingMethods(
          objectFactory.mapEnums(
            local.getBookingMethods(),
            BookingMethodEnumeration.class
          )
        )
        .withBookWhen(
          objectFactory.mapEnum(local.getBookWhen(), PurchaseWhenEnumeration.class)
        )
        .withBuyWhen(
          objectFactory.mapEnums(local.getBuyWhen(), PurchaseMomentEnumeration.class)
        )
        .withLatestBookingTime(local.getLatestBookingTime())
        .withMinimumBookingPeriod(local.getMinimumBookingPeriod())
        .withBookingNote(objectFactory.createMultilingualString(local.getBookingNote()))
        .withBookingContact(
          contactStructureProducer.mapContactStructure(local.getBookingContact())
        );
    }
  }

  private class LineVisitor implements no.entur.uttu.model.LineVisitor {

    private List<NoticeAssignment> noticeAssignments;
    private NetexExportContext context;
    private Line_VersionStructure line;

    public LineVisitor(
      List<NoticeAssignment> noticeAssignments,
      NetexExportContext context
    ) {
      this.noticeAssignments = noticeAssignments;
      this.context = context;
    }

    public Line_VersionStructure getLine() {
      return line;
    }

    @Override
    public void visitFixedLine(FixedLine fixedLine) {
      org.rutebanken.netex.model.Line netexLine = new org.rutebanken.netex.model.Line();
      noticeAssignments.addAll(
        objectFactory.createNoticeAssignments(
          fixedLine,
          LineRefStructure.class,
          fixedLine.getNotices(),
          context
        )
      );
      mapCommon(fixedLine, netexLine, context);
      line = NetexIdProducer.copyIdAndVersion(netexLine, fixedLine);
    }

    @Override
    public void visitFlexibleLine(FlexibleLine flexibleLine) {
      org.rutebanken.netex.model.FlexibleLine netexLine =
        new org.rutebanken.netex.model.FlexibleLine();
      netexLine.setFlexibleLineType(
        objectFactory.mapEnum(
          flexibleLine.getFlexibleLineType(),
          FlexibleLineTypeEnumeration.class
        )
      );
      noticeAssignments.addAll(
        objectFactory.createNoticeAssignments(
          flexibleLine,
          FlexibleLineRefStructure.class,
          flexibleLine.getNotices(),
          context
        )
      );
      mapCommon(flexibleLine, netexLine, context);
      mapBookingArrangements(flexibleLine.getBookingArrangement(), netexLine);
      line = NetexIdProducer.copyIdAndVersion(netexLine, flexibleLine);
    }
  }
}
