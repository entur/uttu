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

package no.entur.uttu.export.netex.producer;

import static no.entur.uttu.export.netex.producer.NetexIdProducer.getEntityName;
import static no.entur.uttu.export.netex.producer.NetexIdProducer.getObjectIdSuffix;

import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import no.entur.uttu.config.ExportTimeZone;
import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.model.Ref;
import no.entur.uttu.model.VehicleSubmodeEnumeration;
import no.entur.uttu.util.DateUtils;
import org.rutebanken.netex.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component(value = "netexObjectFactory")
public class NetexObjectFactory {

  public static final String VERSION_ONE = "1";
  public static final String DEFAULT_LANGUAGE = "no";
  public static final String NSR_XMLNS = "NSR";
  public static final String NSR_XMLNSURL = "http://www.rutebanken.org/ns/nsr";

  @Value("${netex.export.version:1.15:NO-NeTEx-networktimetable:1.5}")
  private String netexVersion;

  private final ObjectFactory objectFactory = new ObjectFactory();
  private final DateUtils dateUtils;
  private final ExportTimeZone exportTimeZone;

  public NetexObjectFactory(DateUtils dateUtils, ExportTimeZone exportTimeZone) {
    this.dateUtils = dateUtils;
    this.exportTimeZone = exportTimeZone;
  }

  public <E> JAXBElement<E> wrapAsJAXBElement(E entity) {
    if (entity == null) {
      return null;
    }
    return new JAXBElement(
      new QName("http://www.netex.org.uk/netex", getEntityName(entity)),
      entity.getClass(),
      null,
      entity
    );
  }

  public <
    N extends LinkSequence_VersionStructure,
    L extends no.entur.uttu.model.GroupOfEntities_VersionStructure
  > N populate(N netex, L local) {
    return (N) populateId(netex, local.getRef())
      .withName(createMultilingualString(local.getName()))
      .withPrivateCode(createPrivateCodeStructure(local.getPrivateCode()))
      .withShortName(createMultilingualString(local.getShortName()))
      .withDescription(createMultilingualString(local.getDescription()));
  }

  public <N extends EntityInVersionStructure> N populateId(N netex, Ref ref) {
    netex.setId(NetexIdProducer.getId(netex, ref));
    netex.setVersion(ref.version);
    return netex;
  }

  public <N extends VersionOfObjectRefStructure> N populateRefStructure(
    N netex,
    Ref ref,
    boolean withVersion
  ) {
    netex.setRef(NetexIdProducer.getReference(netex, ref));
    if (withVersion) {
      netex.setVersion(ref.version);
    }
    return netex;
  }

  public <N extends VersionOfObjectRefStructure> JAXBElement<N> wrapRefStructure(
    N netex,
    Ref ref,
    boolean withVersion
  ) {
    populateRefStructure(netex, ref, withVersion);
    return wrapAsJAXBElement(netex);
  }

  public JAXBElement<PublicationDeliveryStructure> createPublicationDelivery(
    NetexExportContext exportContext,
    CompositeFrame compositeFrame
  ) {
    PublicationDeliveryStructure.DataObjects dataObjects =
      objectFactory.createPublicationDeliveryStructureDataObjects();
    dataObjects
      .getCompositeFrameOrCommonFrame()
      .add(objectFactory.createCompositeFrame(compositeFrame));

    String participantRef = toNMTOKENString(exportContext.provider.getName());
    PublicationDeliveryStructure publicationDeliveryStructure = objectFactory
      .createPublicationDeliveryStructure()
      .withVersion(netexVersion)
      .withPublicationTimestamp(
        dateUtils.toExportLocalDateTime(exportContext.publicationTimestamp)
      )
      .withParticipantRef(participantRef)
      .withDataObjects(dataObjects);
    return objectFactory.createPublicationDelivery(publicationDeliveryStructure);
  }

  /**
   * Make sure String value is a valid NMTOKEN value by replacing any white space chars with underscore
   *
   */
  private String toNMTOKENString(String org) {
    if (org == null) {
      return "unknown";
    }
    return org.replace(' ', '_').replace("(", "_").replace(")", "_");
  }

  public <F extends Common_VersionFrameStructure> CompositeFrame createCompositeFrame(
    NetexExportContext context,
    AvailabilityPeriod availabilityPeriod,
    F... frames
  ) {
    ValidityConditions_RelStructure validityConditionsStruct = objectFactory
      .createValidityConditions_RelStructure()
      .withValidityConditionRefOrValidBetweenOrValidityCondition_(
        createAvailabilityCondition(availabilityPeriod, context)
      );

    no.entur.uttu.model.Codespace localProviderCodespace =
      context.provider.getCodespace();
    Codespace providerCodespace = createCodespace(
      localProviderCodespace.getXmlns(),
      localProviderCodespace.getXmlnsUrl()
    );
    Codespace nsrCodespace = createCodespace(NSR_XMLNS, NSR_XMLNSURL);

    Codespaces_RelStructure codespaces = objectFactory
      .createCodespaces_RelStructure()
      .withCodespaceRefOrCodespace(providerCodespace)
      .withCodespaceRefOrCodespace(nsrCodespace);

    LocaleStructure localeStructure = objectFactory
      .createLocaleStructure()
      .withTimeZone(exportTimeZone.getDefaultTimeZoneId().getId())
      .withDefaultLanguage(DEFAULT_LANGUAGE);

    VersionFrameDefaultsStructure versionFrameDefaultsStructure = objectFactory
      .createVersionFrameDefaultsStructure()
      .withDefaultLocale(localeStructure);

    Frames_RelStructure frames_relStructure = null;
    if (frames != null) {
      frames_relStructure =
        new Frames_RelStructure()
          .withCommonFrame(
            Arrays
              .stream(frames)
              .map(this::wrapAsJAXBElement)
              .collect(Collectors.toList())
          );
    }
    String compositeFrameId = NetexIdProducer.generateId(CompositeFrame.class, context);

    CompositeFrame compositeFrame = objectFactory
      .createCompositeFrame()
      .withVersion(VERSION_ONE)
      .withCreated(dateUtils.toExportLocalDateTime(context.publicationTimestamp))
      .withId(compositeFrameId)
      .withValidityConditions(validityConditionsStruct)
      .withFrames(frames_relStructure)
      .withCodespaces(codespaces)
      .withFrameDefaults(versionFrameDefaultsStructure);

    return compositeFrame;
  }

  public ResourceFrame createResourceFrame(
    NetexExportContext context,
    Collection<Authority> authorities,
    Collection<Operator> operators,
    Collection<Branding> brandings
  ) {
    String resourceFrameId = NetexIdProducer.generateId(ResourceFrame.class, context);
    OrganisationsInFrame_RelStructure organisationsStruct = objectFactory
      .createOrganisationsInFrame_RelStructure()
      .withOrganisation_(
        Stream
          .concat(
            authorities.stream().map(Organisation_VersionStructure.class::cast),
            operators.stream().map(Organisation_VersionStructure.class::cast)
          )
          .distinct()
          .collect(toDistinctOrganisation())
          .values()
          .stream()
          .map(this::wrapAsJAXBElement)
          .collect(Collectors.toList())
      );

    var resourceFrame = objectFactory
      .createResourceFrame()
      .withOrganisations(organisationsStruct)
      .withVersion(VERSION_ONE)
      .withId(resourceFrameId);

    if (!brandings.isEmpty()) {
      TypesOfValueInFrame_RelStructure typesOfValueStruct = objectFactory
        .createTypesOfValueInFrame_RelStructure()
        .withValueSetOrTypeOfValue(
          brandings
            .stream()
            .map(Branding_VersionStructure.class::cast)
            .distinct()
            .map(this::wrapAsJAXBElement)
            .collect(Collectors.toList())
        );

      resourceFrame = resourceFrame.withTypesOfValue(typesOfValueStruct);
    }

    return resourceFrame;
  }

  private static Collector<Organisation_VersionStructure, ?, Map<String, Organisation_VersionStructure>> toDistinctOrganisation() {
    return Collectors.toMap(
      Organisation_VersionStructure::getId,
      Function.identity(),
      (o1, o2) -> o1
    );
  }

  public SiteFrame createSiteFrame(
    NetexExportContext context,
    Collection<FlexibleStopPlace> flexibleStopPlaces
  ) {
    if (CollectionUtils.isEmpty(flexibleStopPlaces)) {
      return null;
    }
    String frameId = NetexIdProducer.generateId(SiteFrame.class, context);
    return objectFactory
      .createSiteFrame()
      .withFlexibleStopPlaces(
        new FlexibleStopPlacesInFrame_RelStructure()
          .withFlexibleStopPlace(flexibleStopPlaces)
      )
      .withVersion(VERSION_ONE)
      .withId(frameId);
  }

  public ServiceFrame createCommonServiceFrame(
    NetexExportContext context,
    Collection<Network> networks,
    Collection<RoutePoint> routePoints,
    Collection<ScheduledStopPoint> scheduledStopPoints,
    Collection<? extends StopAssignment_VersionStructure> stopAssignmentElements,
    Collection<Notice> notices,
    Collection<DestinationDisplay> destinationDisplays,
    List<ServiceLink> serviceLinks
  ) {
    RoutePointsInFrame_RelStructure routePointStruct = objectFactory
      .createRoutePointsInFrame_RelStructure()
      .withRoutePoint(routePoints);

    ScheduledStopPointsInFrame_RelStructure scheduledStopPointsStruct = objectFactory
      .createScheduledStopPointsInFrame_RelStructure()
      .withScheduledStopPoint(scheduledStopPoints);

    StopAssignmentsInFrame_RelStructure stopAssignmentsStruct = objectFactory
      .createStopAssignmentsInFrame_RelStructure()
      .withStopAssignment(
        stopAssignmentElements
          .stream()
          .map(this::wrapAsJAXBElement)
          .collect(Collectors.toList())
      );

    DestinationDisplaysInFrame_RelStructure destinationDisplaysInFrame_relStructure =
      null;
    if (!CollectionUtils.isEmpty(destinationDisplays)) {
      destinationDisplaysInFrame_relStructure =
        objectFactory
          .createDestinationDisplaysInFrame_RelStructure()
          .withDestinationDisplay(destinationDisplays);
    }

    NoticesInFrame_RelStructure noticesInFrame_relStructure = null;
    if (!CollectionUtils.isEmpty(notices)) {
      noticesInFrame_relStructure =
        objectFactory.createNoticesInFrame_RelStructure().withNotice(notices);
    }

    NetworksInFrame_RelStructure additionalNetworks = null;
    Network network = null;
    if (!CollectionUtils.isEmpty(networks)) {
      Iterator<Network> networkIterator = networks.iterator();
      network = networkIterator.next();

      if (networkIterator.hasNext()) {
        additionalNetworks = new NetworksInFrame_RelStructure();
        while (networkIterator.hasNext()) {
          additionalNetworks.getNetwork().add(networkIterator.next());
        }
      }
    }

    ServiceLinksInFrame_RelStructure serviceLinksInFrame_relStructure = objectFactory
      .createServiceLinksInFrame_RelStructure()
      .withServiceLink(serviceLinks);

    return createServiceFrame(context)
      .withRoutePoints(routePointStruct)
      .withScheduledStopPoints(scheduledStopPointsStruct)
      .withStopAssignments(stopAssignmentsStruct)
      .withNetwork(network)
      .withAdditionalNetworks(additionalNetworks)
      .withNotices(noticesInFrame_relStructure)
      .withDestinationDisplays(destinationDisplaysInFrame_relStructure)
      .withServiceLinks(serviceLinks.isEmpty() ? null : serviceLinksInFrame_relStructure);
  }

  public <N extends Line_VersionStructure> ServiceFrame createLineServiceFrame(
    NetexExportContext context,
    N line,
    List<Route> routes,
    Collection<JourneyPattern> journeyPatterns,
    Collection<NoticeAssignment> noticeAssignments
  ) {
    RoutesInFrame_RelStructure routesInFrame =
      objectFactory.createRoutesInFrame_RelStructure();
    for (Route route : routes) {
      JAXBElement<Route> routeElement = objectFactory.createRoute(route);
      routesInFrame.getRoute_().add(routeElement);
    }

    LinesInFrame_RelStructure linesInFrame =
      objectFactory.createLinesInFrame_RelStructure();
    linesInFrame.getLine_().add(wrapAsJAXBElement(line));

    JourneyPatternsInFrame_RelStructure journeyPatternsInFrame =
      objectFactory.createJourneyPatternsInFrame_RelStructure();
    for (JourneyPattern journeyPattern : journeyPatterns) {
      JAXBElement<JourneyPattern> journeyPatternElement =
        objectFactory.createJourneyPattern(journeyPattern);
      journeyPatternsInFrame
        .getJourneyPattern_OrJourneyPatternView()
        .add(journeyPatternElement);
    }

    orderAssignments(noticeAssignments);

    return createServiceFrame(context)
      .withRoutes(routesInFrame)
      .withLines(linesInFrame)
      .withJourneyPatterns(journeyPatternsInFrame)
      .withNoticeAssignments(wrapNoticeAssignments(noticeAssignments));
  }

  private void orderAssignments(
    Collection<? extends Assignment_VersionStructure_> assignments
  ) {
    AtomicInteger cnt = new AtomicInteger(1);
    assignments
      .stream()
      .forEach(a -> a.setOrder(BigInteger.valueOf(cnt.incrementAndGet())));
  }

  private ServiceFrame createServiceFrame(NetexExportContext context) {
    String serviceFrameId = NetexIdProducer.generateId(ServiceFrame.class, context);

    return objectFactory
      .createServiceFrame()
      .withVersion(VERSION_ONE)
      .withId(serviceFrameId);
  }

  public TimetableFrame createTimetableFrame(
    NetexExportContext context,
    Collection<ServiceJourney> serviceJourneys,
    Collection<NoticeAssignment> noticeAssignments
  ) {
    JourneysInFrame_RelStructure journeysInFrameRelStructure =
      objectFactory.createJourneysInFrame_RelStructure();
    journeysInFrameRelStructure
      .getVehicleJourneyOrDatedVehicleJourneyOrNormalDatedVehicleJourney()
      .addAll(serviceJourneys);

    orderAssignments(noticeAssignments);

    String timetableFrameId = NetexIdProducer.generateId(TimetableFrame.class, context);
    return objectFactory
      .createTimetableFrame()
      .withVersion(VERSION_ONE)
      .withId(timetableFrameId)
      .withNoticeAssignments(wrapNoticeAssignments(noticeAssignments))
      .withVehicleJourneys(journeysInFrameRelStructure);
  }

  public <T extends ProviderEntity> List<NoticeAssignment> createNoticeAssignments(
    T entity,
    Collection<no.entur.uttu.model.Notice> notices,
    NetexExportContext context
  ) {
    return notices
      .stream()
      .map(notice -> createNoticeAssignment(entity, notice, context))
      .collect(Collectors.toList());
  }

  public <T extends ProviderEntity> NoticeAssignment createNoticeAssignment(
    T entity,
    no.entur.uttu.model.Notice notice,
    NetexExportContext context
  ) {
    String netexId = NetexIdProducer.generateId(NoticeAssignment.class, context);

    VersionOfObjectRefStructure noticedObjectRef = new VersionOfObjectRefStructure()
      .withRef(entity.getNetexId())
      .withVersion(entity.getNetexVersion());

    return objectFactory
      .createNoticeAssignment()
      .withId(netexId)
      .withVersion(VERSION_ONE)
      .withNoticeRef(
        populateRefStructure(new NoticeRefStructure(), notice.getRef(), false)
      )
      .withNoticedObjectRef(noticedObjectRef);
  }

  private NoticeAssignmentsInFrame_RelStructure wrapNoticeAssignments(
    Collection<NoticeAssignment> noticeAssignments
  ) {
    if (CollectionUtils.isEmpty(noticeAssignments)) {
      return null;
    }
    return new NoticeAssignmentsInFrame_RelStructure()
      .withNoticeAssignment_(
        noticeAssignments
          .stream()
          .map(this::wrapAsJAXBElement)
          .collect(Collectors.toList())
      );
  }

  public ServiceCalendarFrame createServiceCalendarFrame(
    NetexExportContext context,
    Collection<DayType> dayTypes,
    Collection<DayTypeAssignment> dayTypeAssignments,
    Collection<OperatingPeriod> operatingPeriods,
    Collection<OperatingDay> operatingDays
  ) {
    String frameId = NetexIdProducer.generateId(ServiceCalendarFrame.class, context);

    DayTypesInFrame_RelStructure dayTypesStruct = null;
    if (dayTypes != null) {
      dayTypesStruct =
        new DayTypesInFrame_RelStructure()
          .withDayType_(
            dayTypes.stream().map(this::wrapAsJAXBElement).collect(Collectors.toList())
          );
    }

    DayTypeAssignmentsInFrame_RelStructure dayTypeAssignmentsInFrameRelStructure = null;
    if (dayTypeAssignments != null) {
      dayTypeAssignmentsInFrameRelStructure =
        new DayTypeAssignmentsInFrame_RelStructure()
          .withDayTypeAssignment(dayTypeAssignments);
      dayTypeAssignmentsInFrameRelStructure
        .getDayTypeAssignment()
        .sort(Comparator.comparing(DayTypeAssignment::getOrder));
    }

    OperatingPeriodsInFrame_RelStructure operatingPeriodsInFrameRelStructure = null;
    if (!CollectionUtils.isEmpty(operatingPeriods)) {
      operatingPeriodsInFrameRelStructure = new OperatingPeriodsInFrame_RelStructure();
      operatingPeriodsInFrameRelStructure
        .getOperatingPeriodOrUicOperatingPeriod()
        .addAll(operatingPeriods);
    }

    OperatingDaysInFrame_RelStructure operatingDaysInFrameRelStructure = null;
    if (!CollectionUtils.isEmpty(operatingDays)) {
      operatingDaysInFrameRelStructure = new OperatingDaysInFrame_RelStructure();
      operatingDaysInFrameRelStructure.getOperatingDay().addAll(operatingDays);
      operatingDaysInFrameRelStructure
        .getOperatingDay()
        .sort(Comparator.comparing(OperatingDay_VersionStructure::getCalendarDate));
    }

    return objectFactory
      .createServiceCalendarFrame()
      .withVersion(VERSION_ONE)
      .withId(frameId)
      .withDayTypes(dayTypesStruct)
      .withDayTypeAssignments(dayTypeAssignmentsInFrameRelStructure)
      .withOperatingPeriods(operatingPeriodsInFrameRelStructure)
      .withOperatingDays(operatingDaysInFrameRelStructure);
  }

  public JAXBElement<AvailabilityCondition> createAvailabilityCondition(
    AvailabilityPeriod availabilityPeriod,
    NetexExportContext context
  ) {
    String availabilityConditionId = NetexIdProducer.generateId(
      AvailabilityCondition.class,
      context
    );

    availabilityConditionId =
      NetexIdProducer.updateIdSuffix(
        availabilityConditionId,
        getObjectIdSuffix(availabilityConditionId) + "_UTTU"
      );

    AvailabilityCondition availabilityCondition = objectFactory
      .createAvailabilityCondition()
      .withVersion(VERSION_ONE)
      .withId(availabilityConditionId)
      .withFromDate(availabilityPeriod.getFrom().atStartOfDay())
      .withToDate(availabilityPeriod.getTo().atStartOfDay());

    return objectFactory.createAvailabilityCondition(availabilityCondition);
  }

  public <N extends Enum<N>, L extends Enum> List<N> mapEnums(
    Collection<L> local,
    Class<N> netexEnumClass
  ) {
    if (local == null) {
      return null;
    }
    return local
      .stream()
      .map(localEnum -> mapEnum(localEnum, netexEnumClass))
      .collect(Collectors.toList());
  }

  public <N extends Enum<N>, L extends Enum> N mapEnum(L local, Class<N> netexEnumClass) {
    if (local == null) {
      return null;
    }
    return Enum.valueOf(netexEnumClass, local.name());
  }

  public TransportSubmodeStructure mapTransportSubmodeStructure(
    VehicleSubmodeEnumeration submode
  ) {
    if (submode == null) {
      return null;
    }

    TransportSubmodeStructure submodeStructure = new TransportSubmodeStructure();
    switch (submode.getVehicleMode()) {
      case TROLLEY_BUS:
      case BUS:
        submodeStructure.withBusSubmode(mapEnum(submode, BusSubmodeEnumeration.class));
        break;
      case COACH:
        submodeStructure.withCoachSubmode(
          mapEnum(submode, CoachSubmodeEnumeration.class)
        );
        break;
      case TRAM:
        submodeStructure.withTramSubmode(mapEnum(submode, TramSubmodeEnumeration.class));
        break;
      case WATER:
        submodeStructure.withWaterSubmode(
          mapEnum(submode, WaterSubmodeEnumeration.class)
        );
        break;
      case RAIL:
        submodeStructure.withRailSubmode(mapEnum(submode, RailSubmodeEnumeration.class));
        break;
      case AIR:
        submodeStructure.withAirSubmode(mapEnum(submode, AirSubmodeEnumeration.class));
        break;
      case FUNICULAR:
        submodeStructure.withFunicularSubmode(
          mapEnum(submode, FunicularSubmodeEnumeration.class)
        );
        break;
      case METRO:
        submodeStructure.withMetroSubmode(
          mapEnum(submode, MetroSubmodeEnumeration.class)
        );
        break;
      case CABLEWAY:
        submodeStructure.withTelecabinSubmode(
          mapEnum(submode, TelecabinSubmodeEnumeration.class)
        );
        break;
      case TAXI:
        submodeStructure.withTaxiSubmode(mapEnum(submode, TaxiSubmodeEnumeration.class));
    }

    return submodeStructure;
  }

  public Codespace createCodespace(String xmlns, String xmlnsUrl) {
    return objectFactory
      .createCodespace()
      .withId(xmlns.toLowerCase())
      .withXmlns(xmlns)
      .withXmlnsUrl(xmlnsUrl);
  }

  public MultilingualString createMultilingualString(String value) {
    if (value == null) {
      return null;
    }
    return objectFactory.createMultilingualString().withValue(value);
  }

  public PrivateCodeStructure createPrivateCodeStructure(String value) {
    if (value == null) {
      return null;
    }
    return objectFactory.createPrivateCodeStructure().withValue(value);
  }

  public GroupOfLinesRefStructure createGroupOfLinesRefStructure(String groupOfLinesId) {
    return objectFactory.createGroupOfLinesRefStructure().withRef(groupOfLinesId);
  }

  public Ref createScheduledStopPointRefFromQuayRef(
    String quayRef,
    NetexExportContext context
  ) {
    Ref ref = new Ref(NetexIdProducer.updateIdPrefix(quayRef, context), VERSION_ONE);
    Ref newRef = NetexIdProducer.replaceEntityName(
      ref,
      ScheduledStopPoint.class.getSimpleName()
    );
    return new Ref(newRef.id + "_UTTU", newRef.version);
  }

  public Ref createLinkSequenceProjectionServiceLinkRef(Ref serviceLinkRef) {
    Ref newRef = NetexIdProducer.replaceEntityName(
      serviceLinkRef,
      LinkSequenceProjection.class.getSimpleName()
    );
    return new Ref(newRef.id, newRef.version);
  }

  public KeyListStructure mapKeyValues(Map<String, no.entur.uttu.model.Value> keyValues) {
    if (keyValues.isEmpty()) {
      return null;
    }

    return new KeyListStructure()
      .withKeyValue(
        keyValues
          .entrySet()
          .stream()
          .flatMap(entry ->
            entry
              .getValue()
              .getItems()
              .stream()
              .map(value ->
                new KeyValueStructure().withKey(entry.getKey()).withValue(value)
              )
          )
          .collect(Collectors.toList())
      );
  }
}
