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

package no.entur.uttu.export.netex.producer.common;

import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.NetexFile;
import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Ref;
import no.entur.uttu.util.ExportUtil;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.Branding;
import org.rutebanken.netex.model.CompositeFrame;
import org.rutebanken.netex.model.DestinationDisplay;
import org.rutebanken.netex.model.FlexibleStopAssignment;
import org.rutebanken.netex.model.FlexibleStopPlace;
import org.rutebanken.netex.model.FlexibleStopPlaceRefStructure;
import org.rutebanken.netex.model.Network;
import org.rutebanken.netex.model.Notice;
import org.rutebanken.netex.model.Operator;
import org.rutebanken.netex.model.PassengerStopAssignment;
import org.rutebanken.netex.model.PointProjection;
import org.rutebanken.netex.model.PointRefStructure;
import org.rutebanken.netex.model.Projections_RelStructure;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.QuayRefStructure;
import org.rutebanken.netex.model.ResourceFrame;
import org.rutebanken.netex.model.RoutePoint;
import org.rutebanken.netex.model.ScheduledStopPoint;
import org.rutebanken.netex.model.ScheduledStopPointRefStructure;
import org.rutebanken.netex.model.ServiceCalendarFrame;
import org.rutebanken.netex.model.ServiceFrame;
import org.rutebanken.netex.model.ServiceLink;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopAssignment_VersionStructure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NetexCommonFileProducer {

  private final NetexObjectFactory objectFactory;
  private final OrganisationProducer organisationProducer;
  private final FlexibleStopPlaceProducer flexibleStopPlaceProducer;
  private final ServiceCalendarFrameProducer serviceCalendarFrameProducer;
  private final NetworkProducer networkProducer;
  private final ServiceLinkProducer serviceLinkProducer;
  private final BrandingProducer brandingProducer;

  @Value("${export.blob.commonFileFilenameSuffix:_flexible_shared_data}")
  private String commonFileFilenameSuffix;

  public NetexCommonFileProducer(
    NetexObjectFactory objectFactory,
    OrganisationProducer organisationProducer,
    FlexibleStopPlaceProducer flexibleStopPlaceProducer,
    ServiceCalendarFrameProducer serviceCalendarFrameProducer,
    NetworkProducer networkProducer,
    ServiceLinkProducer serviceLinkProducer,
    BrandingProducer brandingProducer
  ) {
    this.objectFactory = objectFactory;
    this.organisationProducer = organisationProducer;
    this.flexibleStopPlaceProducer = flexibleStopPlaceProducer;
    this.serviceCalendarFrameProducer = serviceCalendarFrameProducer;
    this.networkProducer = networkProducer;
    this.serviceLinkProducer = serviceLinkProducer;
    this.brandingProducer = brandingProducer;
  }

  public NetexFile toCommonFile(NetexExportContext context) {
    ResourceFrame resourceFrame = createResourceFrame(context);
    SiteFrame siteFrame = createSiteFrame(context);
    ServiceFrame serviceFrame = createServiceFrame(context);
    ServiceCalendarFrame serviceCalendarFrame = serviceCalendarFrameProducer.produce(
      context
    );
    CompositeFrame compositeFrame = objectFactory.createCompositeFrame(
      context,
      context.getAvailabilityPeriod(),
      resourceFrame,
      siteFrame,
      serviceFrame,
      serviceCalendarFrame
    );

    JAXBElement<PublicationDeliveryStructure> publicationDelivery =
      objectFactory.createPublicationDelivery(context, compositeFrame);

    String fileName = ExportUtil.createCommonFileFilename(
      context.provider,
      commonFileFilenameSuffix
    );

    return new NetexFile(fileName, publicationDelivery);
  }

  private ResourceFrame createResourceFrame(NetexExportContext context) {
    List<Operator> netexOperators = organisationProducer.produceOperators(context);
    List<Authority> netexAuthorities = organisationProducer.produceAuthorities(context);
    List<Branding> brandings = brandingProducer.produce(context);
    return objectFactory.createResourceFrame(
      context,
      netexAuthorities,
      netexOperators,
      brandings
    );
  }

  private SiteFrame createSiteFrame(NetexExportContext context) {
    List<FlexibleStopPlace> netexFlexibleStopPlaces = flexibleStopPlaceProducer.produce(
      context
    );

    return objectFactory.createSiteFrame(context, netexFlexibleStopPlaces);
  }

  private ServiceFrame createServiceFrame(NetexExportContext context) {
    List<Network> networks = networkProducer.produce(context);
    List<RoutePoint> routePoints = context.routePointRefs
      .stream()
      .map(this::buildRoutePoint)
      .collect(Collectors.toList());
    List<ScheduledStopPoint> scheduledStopPoints = context.scheduledStopPointRefs
      .stream()
      .map(this::buildScheduledStopPoint)
      .collect(Collectors.toList());

    List<StopAssignment_VersionStructure> stopAssignments = context.flexibleStopPlaces
      .stream()
      .map(no.entur.uttu.model.FlexibleStopPlace::getRef)
      .map(this::buildFlexibleStopAssignment)
      .collect(Collectors.toList());

    AtomicInteger passengerStopAssignmentOrder = new AtomicInteger(1);

    stopAssignments.addAll(
      context.quayRefs
        .stream()
        .map(quayRef ->
          mapPassengerStopAssignment(
            quayRef,
            passengerStopAssignmentOrder.getAndIncrement(),
            context
          )
        )
        .collect(Collectors.toList())
    );

    List<Notice> notices = context.notices
      .stream()
      .map(this::mapNotice)
      .collect(Collectors.toList());
    List<DestinationDisplay> destinationDisplays = context.destinationDisplays
      .stream()
      .map(this::mapDestinationDisplay)
      .collect(Collectors.toList());

    List<ServiceLink> serviceLinks = serviceLinkProducer.produce(context);

    return objectFactory.createCommonServiceFrame(
      context,
      networks,
      routePoints,
      scheduledStopPoints,
      stopAssignments,
      notices,
      destinationDisplays,
      serviceLinks
    );
  }

  private RoutePoint buildRoutePoint(Ref ref) {
    Ref scheduledStopPointRef = NetexIdProducer.replaceEntityName(
      ref,
      ScheduledStopPoint.class.getSimpleName()
    );
    PointRefStructure pointRefStructure = new PointRefStructure()
      .withRef(scheduledStopPointRef.id)
      .withVersion(scheduledStopPointRef.version);
    PointProjection pointProjection = objectFactory
      .populateId(new PointProjection(), ref)
      .withProjectToPointRef(pointRefStructure);
    Projections_RelStructure projections_relStructure = new Projections_RelStructure()
      .withProjectionRefOrProjection(objectFactory.wrapAsJAXBElement(pointProjection));
    return objectFactory
      .populateId(new RoutePoint(), ref)
      .withProjections(projections_relStructure);
  }

  private ScheduledStopPoint buildScheduledStopPoint(Ref ref) {
    return objectFactory.populateId(new ScheduledStopPoint(), ref);
  }

  private FlexibleStopAssignment buildFlexibleStopAssignment(Ref ref) {
    return objectFactory
      .populateId(new FlexibleStopAssignment(), ref)
      .withScheduledStopPointRef(
        objectFactory.wrapRefStructure(new ScheduledStopPointRefStructure(), ref, true)
      )
      .withFlexibleStopPlaceRef(
        objectFactory.populateRefStructure(new FlexibleStopPlaceRefStructure(), ref, true)
      );
  }

  public Notice mapNotice(no.entur.uttu.model.Notice local) {
    return objectFactory
      .populateId(new Notice(), local.getRef())
      .withText(objectFactory.createMultilingualString(local.getText()));
  }

  public DestinationDisplay mapDestinationDisplay(
    no.entur.uttu.model.DestinationDisplay local
  ) {
    return objectFactory
      .populateId(new DestinationDisplay(), local.getRef())
      .withFrontText(objectFactory.createMultilingualString(local.getFrontText()));
  }

  public PassengerStopAssignment mapPassengerStopAssignment(
    String quayRef,
    int order,
    NetexExportContext context
  ) {
    Ref scheduledStopPointRef = objectFactory.createScheduledStopPointRefFromQuayRef(
      quayRef,
      context
    );
    return objectFactory
      .populateId(new PassengerStopAssignment(), scheduledStopPointRef)
      .withOrder(BigInteger.valueOf(order))
      .withScheduledStopPointRef(
        objectFactory.wrapRefStructure(
          new ScheduledStopPointRefStructure(),
          scheduledStopPointRef,
          true
        )
      )
      .withQuayRef(
        objectFactory.wrapAsJAXBElement(new QuayRefStructure().withRef(quayRef))
      );
  }
}
