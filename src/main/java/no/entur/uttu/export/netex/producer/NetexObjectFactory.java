package no.entur.uttu.export.netex.producer;


import no.entur.uttu.config.ExportTimeZone;
import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.model.Ref;
import no.entur.uttu.util.DateUtils;
import org.rutebanken.netex.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.entur.uttu.export.netex.producer.NetexIdProducer.getEntityName;

@Component(value = "netexObjectFactory")
public class NetexObjectFactory {

    private static final String VERSION_ONE = "1";
    private static final String DEFAULT_LANGUAGE = "no";

    @Value("${netex.export.version:1.08:NO-NeTEx-networktimetable:1.3}")
    private String netexVersion;


    private ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private ExportTimeZone exportTimeZone;

    private static final Map<DayOfWeek, DayOfWeekEnumeration> dayOfWeekMap = new HashMap<>();

    static {
        dayOfWeekMap.put(DayOfWeek.MONDAY, DayOfWeekEnumeration.MONDAY);
        dayOfWeekMap.put(DayOfWeek.TUESDAY, DayOfWeekEnumeration.TUESDAY);
        dayOfWeekMap.put(DayOfWeek.WEDNESDAY, DayOfWeekEnumeration.WEDNESDAY);
        dayOfWeekMap.put(DayOfWeek.THURSDAY, DayOfWeekEnumeration.THURSDAY);
        dayOfWeekMap.put(DayOfWeek.FRIDAY, DayOfWeekEnumeration.FRIDAY);
        dayOfWeekMap.put(DayOfWeek.SATURDAY, DayOfWeekEnumeration.SATURDAY);
        dayOfWeekMap.put(DayOfWeek.SUNDAY, DayOfWeekEnumeration.SUNDAY);
    }

    public <E> JAXBElement<E> wrapAsJAXBElement(E entity) {
        if (entity == null) {
            return null;
        }
        return new JAXBElement(new QName("http://www.netex.org.uk/netex", getEntityName(entity)), entity.getClass(), null, entity);
    }

    public <N extends LinkSequence_VersionStructure, L extends no.entur.uttu.model.GroupOfEntities_VersionStructure> N populate(N netex, L local) {
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

    public <N extends VersionOfObjectRefStructure> N populateRefStructure(N netex, Ref ref, boolean withVersion) {
        netex.setRef(NetexIdProducer.getReference(netex, ref));
        if (withVersion) {
            netex.setVersion(ref.version);
        }
        return netex;
    }

    public <N extends VersionOfObjectRefStructure> JAXBElement<N> createRefStructure(N netex, Ref ref, boolean withVersion) {
        populateRefStructure(netex, ref, withVersion);
        return wrapAsJAXBElement(netex);
    }

    public JAXBElement<PublicationDeliveryStructure> createPublicationDelivery(NetexExportContext exportContext, CompositeFrame compositeFrame) {

        PublicationDeliveryStructure.DataObjects dataObjects = objectFactory.createPublicationDeliveryStructureDataObjects();
        dataObjects.getCompositeFrameOrCommonFrame().add(objectFactory.createCompositeFrame(compositeFrame));

        PublicationDeliveryStructure publicationDeliveryStructure = objectFactory.createPublicationDeliveryStructure()
                                                                            .withVersion(netexVersion)
                                                                            .withPublicationTimestamp(dateUtils.toExportLocalDateTime(exportContext.publicationTimestamp))
                                                                            .withParticipantRef(exportContext.provider.getName())
                                                                            .withDescription(createMultilingualString("Flexible lines"))
                                                                            .withDataObjects(dataObjects);
        return objectFactory.createPublicationDelivery(publicationDeliveryStructure);
    }

    // TODO remove unused


    public <F extends Common_VersionFrameStructure> CompositeFrame createCompositeFrame(NetexExportContext context, AvailabilityPeriod availabilityPeriod, F... frames) {
        ValidityConditions_RelStructure validityConditionsStruct = objectFactory.createValidityConditions_RelStructure()
                                                                           .withValidityConditionRefOrValidBetweenOrValidityCondition_(createAvailabilityCondition(availabilityPeriod, context));

        Codespace providerCodespace = createCodespace(context.provider.getCodespace());

        Codespaces_RelStructure codespaces = objectFactory.createCodespaces_RelStructure().withCodespaceRefOrCodespace(providerCodespace);

        LocaleStructure localeStructure = objectFactory.createLocaleStructure()
                                                  .withTimeZone(exportTimeZone.getDefaultTimeZoneId().getId())
                                                  .withDefaultLanguage(DEFAULT_LANGUAGE);

        VersionFrameDefaultsStructure versionFrameDefaultsStructure = objectFactory.createVersionFrameDefaultsStructure()
                                                                              .withDefaultLocale(localeStructure);

        Frames_RelStructure frames_relStructure = null;
        if (frames != null) {
            frames_relStructure = new Frames_RelStructure().withCommonFrame(Arrays.stream(frames).map(this::wrapAsJAXBElement).collect(Collectors.toList()));
        }
        String compositeFrameId = NetexIdProducer.generateId(CompositeFrame.class, context);

        CompositeFrame compositeFrame = objectFactory.createCompositeFrame()
                                                .withVersion(VERSION_ONE)
                                                .withCreated(dateUtils.toExportLocalDateTime(context.publicationTimestamp))
                                                .withId(compositeFrameId)
                                                .withValidityConditions(validityConditionsStruct)
                                                .withFrames(frames_relStructure)
                                                .withCodespaces(codespaces)
                                                .withFrameDefaults(versionFrameDefaultsStructure);


        return compositeFrame;
    }


    public ResourceFrame createResourceFrame(NetexExportContext context, Collection<Authority> authorities, Collection<Operator> operators) {
        String resourceFrameId = NetexIdProducer.generateId(ResourceFrame.class, context);
        OrganisationsInFrame_RelStructure organisationsStruct = objectFactory.createOrganisationsInFrame_RelStructure()
                                                                        .withOrganisation_(authorities.stream().map(this::wrapAsJAXBElement).collect(Collectors.toList()))
                                                                        .withOrganisation_(operators.stream().map(this::wrapAsJAXBElement).collect(Collectors.toList()));

        return objectFactory.createResourceFrame()
                       .withOrganisations(organisationsStruct)
                       .withVersion(VERSION_ONE)
                       .withId(resourceFrameId);
    }

    public SiteFrame createSiteFrame(NetexExportContext context, Collection<FlexibleStopPlace> flexibleStopPlaces) {
        String frameId = NetexIdProducer.generateId(SiteFrame.class, context);
        return objectFactory.createSiteFrame()
                       .withFlexibleStopPlaces(new FlexibleStopPlacesInFrame_RelStructure().withFlexibleStopPlace(flexibleStopPlaces))
                       .withVersion(VERSION_ONE)
                       .withId(frameId);
    }

    public ServiceFrame createCommonServiceFrame(NetexExportContext context, Collection<Network> networks, Collection<RoutePoint> routePoints,
                                                        Collection<ScheduledStopPoint> scheduledStopPoints, Collection<? extends StopAssignment_VersionStructure> stopAssignmentElements,
                                                        Collection<Notice> notices) {

        RoutePointsInFrame_RelStructure routePointStruct = objectFactory.createRoutePointsInFrame_RelStructure()
                                                                   .withRoutePoint(routePoints);

        ScheduledStopPointsInFrame_RelStructure scheduledStopPointsStruct = objectFactory.createScheduledStopPointsInFrame_RelStructure().withScheduledStopPoint(scheduledStopPoints);

        StopAssignmentsInFrame_RelStructure stopAssignmentsStruct = objectFactory.createStopAssignmentsInFrame_RelStructure()
                                                                            .withStopAssignment(stopAssignmentElements.stream().map(this::wrapAsJAXBElement).collect(Collectors.toList()));

        NoticesInFrame_RelStructure noticesInFrame_relStructure = null;
        if (!CollectionUtils.isEmpty(notices)) {
            noticesInFrame_relStructure = objectFactory.createNoticesInFrame_RelStructure().withNotice(notices);
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

        return createServiceFrame(context)
                       .withRoutePoints(routePointStruct)
                       .withScheduledStopPoints(scheduledStopPointsStruct)
                       .withStopAssignments(stopAssignmentsStruct)
                       .withNetwork(network)
                       .withAdditionalNetworks(additionalNetworks)
                       .withNotices(noticesInFrame_relStructure);

    }


    public <N extends Line_VersionStructure> ServiceFrame createLineServiceFrame(NetexExportContext context, N line, List<Route> routes,
                                                                                        Collection<JourneyPattern> journeyPatterns,
                                                                                        Collection<NoticeAssignment> noticeAssignments) {
        RoutesInFrame_RelStructure routesInFrame = objectFactory.createRoutesInFrame_RelStructure();
        for (Route route : routes) {
            JAXBElement<Route> routeElement = objectFactory.createRoute(route);
            routesInFrame.getRoute_().add(routeElement);
        }

        LinesInFrame_RelStructure linesInFrame = objectFactory.createLinesInFrame_RelStructure();
        linesInFrame.getLine_().add(wrapAsJAXBElement(line));

        JourneyPatternsInFrame_RelStructure journeyPatternsInFrame = objectFactory.createJourneyPatternsInFrame_RelStructure();
        for (JourneyPattern journeyPattern : journeyPatterns) {
            JAXBElement<JourneyPattern> journeyPatternElement = objectFactory.createJourneyPattern(journeyPattern);
            journeyPatternsInFrame.getJourneyPattern_OrJourneyPatternView().add(journeyPatternElement);
        }

        return createServiceFrame(context)
                       .withRoutes(routesInFrame)
                       .withLines(linesInFrame)
                       .withJourneyPatterns(journeyPatternsInFrame)
                       .withNoticeAssignments(wrapNoticeAssignments(noticeAssignments));
    }

    private ServiceFrame createServiceFrame(NetexExportContext context) {
        String serviceFrameId = NetexIdProducer.generateId(ServiceFrame.class, context);

        return objectFactory.createServiceFrame()
                       .withVersion(VERSION_ONE)
                       .withId(serviceFrameId);
    }


    public TimetableFrame createTimetableFrame(NetexExportContext context, Collection<ServiceJourney> serviceJourneys, Collection<NoticeAssignment> noticeAssignments) {
        JourneysInFrame_RelStructure journeysInFrameRelStructure = objectFactory.createJourneysInFrame_RelStructure();
        journeysInFrameRelStructure.getDatedServiceJourneyOrDeadRunOrServiceJourney().addAll(serviceJourneys);

        String timetableFrameId = NetexIdProducer.generateId(TimetableFrame.class, context);
        return objectFactory.createTimetableFrame()
                       .withVersion(VERSION_ONE)
                       .withId(timetableFrameId)
                       .withNoticeAssignments(wrapNoticeAssignments(noticeAssignments))
                       .withVehicleJourneys(journeysInFrameRelStructure);

    }

    private NoticeAssignmentsInFrame_RelStructure wrapNoticeAssignments(Collection<NoticeAssignment> noticeAssignments) {
        if (CollectionUtils.isEmpty(noticeAssignments)) {
            return null;
        }
        return new NoticeAssignmentsInFrame_RelStructure().withNoticeAssignment_(noticeAssignments.stream().map(this::wrapAsJAXBElement).collect(Collectors.toList()));
    }

    public ServiceCalendarFrame createServiceCalendarFrame(NetexExportContext context, Collection<DayType> dayTypes, Collection<DayTypeAssignment> dayTypeAssignments,
                                                                  Collection<OperatingPeriod> operatingPeriods) {
        String frameId = NetexIdProducer.generateId(ServiceCalendarFrame.class, context);

        DayTypesInFrame_RelStructure dayTypesStruct = null;
        if (dayTypes != null) {
            dayTypesStruct = new DayTypesInFrame_RelStructure().withDayType_(dayTypes.stream().map(this::wrapAsJAXBElement).collect(Collectors.toList()));
        }

        DayTypeAssignmentsInFrame_RelStructure dayTypeAssignmentsInFrameRelStructure = null;
        if (dayTypeAssignments != null) {
            dayTypeAssignmentsInFrameRelStructure = new DayTypeAssignmentsInFrame_RelStructure().withDayTypeAssignment(dayTypeAssignments);
            dayTypeAssignmentsInFrameRelStructure.getDayTypeAssignment().sort(Comparator.comparing(DayTypeAssignment::getOrder));
        }

        OperatingPeriodsInFrame_RelStructure operatingPeriodsInFrameRelStructure = null;
        if (!CollectionUtils.isEmpty(operatingPeriods)) {
            operatingPeriodsInFrameRelStructure = new OperatingPeriodsInFrame_RelStructure();
            operatingPeriodsInFrameRelStructure.getOperatingPeriodOrUicOperatingPeriod().addAll(operatingPeriods);
            operatingPeriodsInFrameRelStructure.getOperatingPeriodOrUicOperatingPeriod().sort(Comparator.comparing(OperatingPeriod_VersionStructure::getFromDate));
        }

        return objectFactory.createServiceCalendarFrame()
                       .withVersion(VERSION_ONE)
                       .withId(frameId)
                       .withDayTypes(dayTypesStruct)
                       .withDayTypeAssignments(dayTypeAssignmentsInFrameRelStructure)
                       .withOperatingPeriods(operatingPeriodsInFrameRelStructure);

    }

    public JAXBElement<AvailabilityCondition> createAvailabilityCondition(AvailabilityPeriod availabilityPeriod, NetexExportContext context) {
        String availabilityConditionId = NetexIdProducer.generateId(AvailabilityCondition.class, context);

        AvailabilityCondition availabilityCondition = objectFactory.createAvailabilityCondition()
                                                              .withVersion(VERSION_ONE)
                                                              .withId(availabilityConditionId)
                                                              .withFromDate(availabilityPeriod.getFrom().atStartOfDay())
                                                              .withToDate(availabilityPeriod.getTo().atStartOfDay());

        return objectFactory.createAvailabilityCondition(availabilityCondition);
    }

    public <N extends Enum<N>, L extends Enum> List<N> mapEnums(Collection<L> local, Class<N> netexEnumClass) {
        if (local == null) {
            return null;
        }
        return local.stream().map(localEnum -> mapEnum(localEnum, netexEnumClass)).collect(Collectors.toList());
    }

    public <N extends Enum<N>, L extends Enum> N mapEnum(L local, Class<N> netexEnumClass) {
        if (local == null) {
            return null;
        }
        return Enum.valueOf(netexEnumClass, local.name());
    }

    //
//    public Network createNetwork(Instant publicationTimestamp, String airlineIata, String airlineName) {
//        NetexStaticDataSet.OrganisationDataSet avinorDataSet = netexStaticDataSet.getOrganisations()
//                                                                       .get(AVINOR_XMLNS.toLowerCase());
//
//        if (airlineName == null) {
//            NetexStaticDataSet.OrganisationDataSet airlineDataSet = netexStaticDataSet.getOrganisations()
//                                                                            .get(airlineIata.toLowerCase());
//            airlineName = airlineDataSet.getName();
//        }
//
//        String networkId = NetexObjectIdCreator.createNetworkId(AVINOR_XMLNS, airlineIata);
//        String authorityId = NetexObjectIdCreator.createAuthorityId(AVINOR_XMLNS, avinorDataSet.getName());
//
//        AuthorityRefStructure authorityRefStruct = objectFactory.createAuthorityRefStructure()
//                                                           .withVersion(VERSION_ONE)
//                                                           .withRef(authorityId);
//
//        GroupsOfLinesInFrame_RelStructure groupsOfLinesStruct = objectFactory.createGroupsOfLinesInFrame_RelStructure();
//
//        GroupOfLines groupOfLines = objectFactory.createGroupOfLines()
//                                            .withVersion(VERSION_ONE)
//                                            .withId(NetexObjectIdCreator.createGroupOfLinesId(AVINOR_XMLNS, airlineIata))
//                                            .withName(createMultilingualString(airlineName + " Fly"));
//        groupsOfLinesStruct.getGroupOfLines().add(groupOfLines);
//
//        return objectFactory.createNetwork()
//                       .withVersion(VERSION_ONE)
//                       .withChanged(dateUtils.toExportLocalDateTime(publicationTimestamp))
//                       .withId(networkId)
//                       .withName(createMultilingualString(airlineName))
//                       .withTransportOrganisationRef(objectFactory.createAuthorityRef(authorityRefStruct));
//        //.withGroupsOfLines(groupsOfLinesStruct);
//    }
//
//    public JAXBElement<Authority> createAvinorAuthorityElement() {
//        NetexStaticDataSet.OrganisationDataSet avinorDataSet = netexStaticDataSet.getOrganisations()
//                                                                       .get(AVINOR_XMLNS.toLowerCase());
//
//        String authorityId = NetexObjectIdCreator.createAuthorityId(AVINOR_XMLNS, avinorDataSet.getName());
//
//        Authority authority = objectFactory.createAuthority()
//                                      .withVersion(VERSION_ONE)
//                                      .withId(authorityId)
//                                      .withCompanyNumber(avinorDataSet.getCompanyNumber())
//                                      .withName(createMultilingualString(avinorDataSet.getName()))
//                                      .withLegalName(createMultilingualString(avinorDataSet.getLegalName()))
//                                      .withContactDetails(createContactStructure(avinorDataSet.getPhone(), avinorDataSet.getUrl()))
//                                      .withOrganisationType(OrganisationTypeEnumeration.AUTHORITY);
//        return objectFactory.createAuthority(authority);
//    }
//
//    public JAXBElement<Authority> createNsrAuthorityElement() {
//        NetexStaticDataSet.OrganisationDataSet nsrDataSet = netexStaticDataSet.getOrganisations()
//                                                                    .get(NSR_XMLNS.toLowerCase());
//
//        String authorityId = NetexObjectIdCreator.createAuthorityId(NSR_XMLNS, NSR_XMLNS);
//
//        Authority authority = objectFactory.createAuthority()
//                                      .withVersion(VERSION_ONE)
//                                      .withId(authorityId)
//                                      .withCompanyNumber(nsrDataSet.getCompanyNumber())
//                                      .withName(createMultilingualString(nsrDataSet.getName()))
//                                      .withLegalName(createMultilingualString(nsrDataSet.getLegalName()))
//                                      .withContactDetails(createContactStructure(nsrDataSet.getPhone(), nsrDataSet.getUrl()))
//                                      .withOrganisationType(OrganisationTypeEnumeration.AUTHORITY);
//        return objectFactory.createAuthority(authority);
//    }
//
//    public JAXBElement<Operator> createAirlineOperatorElement(String airlineIata) {
//        NetexStaticDataSet.OrganisationDataSet airlineDataSet = netexStaticDataSet.getOrganisations()
//                                                                        .get(airlineIata.toLowerCase());
//
//        String operatorId = NetexObjectIdCreator.createOperatorId(AVINOR_XMLNS, airlineIata);
//
//        Operator operator = objectFactory.createOperator()
//                                    .withVersion(VERSION_ONE)
//                                    .withId(operatorId)
//                                    .withCompanyNumber(airlineDataSet.getCompanyNumber())
//                                    .withName(createMultilingualString(airlineDataSet.getName()))
//                                    .withLegalName(createMultilingualString((airlineDataSet.getLegalName())))
//                                    .withContactDetails(createContactStructure(airlineDataSet.getPhone(), airlineDataSet.getUrl()))
//                                    .withCustomerServiceContactDetails(createContactStructure(airlineDataSet.getPhone(), airlineDataSet.getUrl()))
//                                    .withOrganisationType(OrganisationTypeEnumeration.OPERATOR);
//
//        return objectFactory.createOperator(operator);
//    }
//
//    public JAXBElement<Operator> createAirlineOperatorElement(Operator operator) {
//        return objectFactory.createOperator(operator);
//    }
//
//    public Operator createInfrequentAirlineOperatorElement(String airlineIata, String airlineName, String operatorId) {
//        return objectFactory.createOperator()
//                       .withVersion(VERSION_ONE)
//                       .withId(operatorId)
//                       .withCompanyNumber("999999999")
//                       .withName(createMultilingualString(airlineName.trim()))
//                       .withLegalName(createMultilingualString((airlineName.trim().toUpperCase())))
//                       .withContactDetails(createContactStructure("0047 99999999", String.format("http://%s.no/", airlineIata.toLowerCase())))
//                       .withCustomerServiceContactDetails(createContactStructure("0047 99999999", String.format("http://%s.no/", airlineIata.toLowerCase())))
//                       .withOrganisationType(OrganisationTypeEnumeration.OPERATOR);
//    }
//
//    public ContactStructure createContactStructure(String phone, String url) {
//        return objectFactory.createContactStructure()
//                       .withPhone(phone)
//                       .withUrl(url);
//    }
//

    public Codespace createCodespace(no.entur.uttu.model.Codespace local) {
        return objectFactory.createCodespace()
                       .withId(local.getXmlns().toLowerCase())
                       .withXmlns(local.getXmlns())
                       .withXmlnsUrl(local.getXmlnsUrl());
    }
//
//    public Line createLine(String airlineIata, String lineDesignation, String lineName) {
//        String lineId = NetexObjectIdCreator.createLineId(AVINOR_XMLNS, new String[] {airlineIata, lineDesignation});
//
//        GroupOfLinesRefStructure groupOfLinesRefStruct = objectFactory.createGroupOfLinesRefStructure()
//                                                                 .withRef(NetexObjectIdCreator.createNetworkId(AVINOR_XMLNS, airlineIata));
//
//        if (!isCommonDesignator(airlineIata)) {
//            groupOfLinesRefStruct.setVersion(VERSION_ONE);
//        }
//
//        return objectFactory.createLine()
//                       .withVersion(VERSION_ONE)
//                       .withId(lineId)
//                       .withName(createMultilingualString(lineName))
//                       .withTransportMode(AllVehicleModesOfTransportEnumeration.AIR)
//                       .withTransportSubmode(objectFactory.createTransportSubmodeStructure().withAirSubmode(AirSubmodeEnumeration.DOMESTIC_FLIGHT))
//                       // .withPublicCode(lineDesignation)
//                       .withRepresentedByGroupRef(groupOfLinesRefStruct);
//    }
//
//    public Route createRoute(String lineId, String objectId, String routeName, PointsOnRoute_RelStructure pointsOnRoute) {
//        LineRefStructure lineRefStruct = createLineRefStructure(lineId);
//        JAXBElement<LineRefStructure> lineRefStructElement = objectFactory.createLineRef(lineRefStruct);
//
//        String routeId = NetexObjectIdCreator.createRouteId(AVINOR_XMLNS, objectId);
//
//        return objectFactory.createRoute()
//                       .withVersion(VERSION_ONE)
//                       .withId(routeId)
//                       .withName(createMultilingualString(routeName))
//                       .withLineRef(lineRefStructElement)
//                       .withPointsInSequence(pointsOnRoute);
//    }
//
//    public PointOnRoute createPointOnRoute(String objectId, String routePointId, int order) {
//        String pointOnRouteId = NetexObjectIdCreator.createPointOnRouteId(AVINOR_XMLNS, objectId);
//        RoutePointRefStructure routePointRefStruct = createRoutePointRefStructure(routePointId);
//        JAXBElement<RoutePointRefStructure> routePointRefStructElement = objectFactory.createRoutePointRef(routePointRefStruct);
//
//        return objectFactory.createPointOnRoute()
//                       .withVersion(VERSION_ONE)
//                       .withId(pointOnRouteId)
//                       .withOrder(BigInteger.valueOf(order))
//                       .withPointRef(routePointRefStructElement);
//    }
//
//    public JourneyPattern createJourneyPattern(String objectId, String routeId, PointsInJourneyPattern_RelStructure pointsInJourneyPattern) {
//        String journeyPatternId = NetexObjectIdCreator.createJourneyPatternId(AVINOR_XMLNS, objectId);
//        RouteRefStructure routeRefStructure = createRouteRefStructure(routeId);
//
//        return objectFactory.createJourneyPattern()
//                       .withVersion(VERSION_ONE)
//                       .withId(journeyPatternId)
//                       .withRouteRef(routeRefStructure)
//                       .withPointsInSequence(pointsInJourneyPattern);
//    }
//
//    public StopPointInJourneyPattern createStopPointInJourneyPattern(String objectId, BigInteger orderIndex, String stopPointId) {
//        String stopPointInJourneyPatternId = NetexObjectIdCreator.createStopPointInJourneyPatternId(AVINOR_XMLNS, objectId);
//        ScheduledStopPointRefStructure stopPointRefStruct = createScheduledStopPointRefStructure(stopPointId, Boolean.FALSE);
//        JAXBElement<ScheduledStopPointRefStructure> stopPointRefStructElement = objectFactory.createScheduledStopPointRef(stopPointRefStruct);
//
//        return objectFactory.createStopPointInJourneyPattern()
//                       .withVersion(VERSION_ONE)
//                       .withId(stopPointInJourneyPatternId)
//                       .withOrder(orderIndex)
//                       .withScheduledStopPointRef(stopPointRefStructElement);
//    }
//
//    public DestinationDisplay getDestinationDisplay(String objectId) {
//        if (destinationDisplays.containsKey(objectId)) {
//            return destinationDisplays.get(objectId);
//        } else {
//            throw new RuntimeException("Missing reference to destination display");
//        }
//    }
//
//    public DestinationDisplay createDestinationDisplay(String objectId) {
//        String destinationDisplayId = NetexObjectIdCreator.createDestinationDisplayId(AVINOR_XMLNS, objectId);
//
//        return objectFactory.createDestinationDisplay()
//                       .withVersion(VERSION_ONE)
//                       .withId(destinationDisplayId);
//    }
//
//    public DestinationDisplay getDestinationDisplay(String objectId, String frontText) {
//        return destinationDisplays.computeIfAbsent(objectId, s -> objectFactory.createDestinationDisplay()
//                                                                          .withVersion(VERSION_ONE)
//                                                                          .withId(NetexObjectIdCreator.createDestinationDisplayId(AVINOR_XMLNS, objectId))
//                                                                          .withFrontText(createMultilingualString(frontText)));
//    }
//
//    public DestinationDisplay createDestinationDisplay(String objectId, String frontText, boolean isStopDisplay) {
//        String destinationDisplayId = NetexObjectIdCreator.createDestinationDisplayId(AVINOR_XMLNS, objectId);
//
//        DestinationDisplay destinationDisplay = objectFactory.createDestinationDisplay()
//                                                        .withVersion(VERSION_ONE)
//                                                        .withId(destinationDisplayId)
//                                                        .withFrontText(createMultilingualString(frontText));
//
//        if (isStopDisplay && !destinationDisplays.containsKey(destinationDisplayId)) {
//            destinationDisplays.put(destinationDisplayId, destinationDisplay);
//        }
//
//        return destinationDisplay;
//    }
//
//    public ServiceJourney createServiceJourney(String objectId, String lineId, String flightId, DayTypeRefs_RelStructure dayTypeRefsStruct,
//                                                      String journeyPatternId, TimetabledPassingTimes_RelStructure passingTimesRelStruct, String name) {
//
//        String serviceJourneyId = NetexObjectIdCreator.createServiceJourneyId(AVINOR_XMLNS, objectId);
//
//        JourneyPatternRefStructure journeyPatternRefStruct = objectFactory.createJourneyPatternRefStructure()
//                                                                     .withVersion(VERSION_ONE)
//                                                                     .withRef(journeyPatternId);
//        JAXBElement<JourneyPatternRefStructure> journeyPatternRefStructElement = objectFactory.createJourneyPatternRef(journeyPatternRefStruct);
//
//        LineRefStructure lineRefStruct = createLineRefStructure(lineId);
//        JAXBElement<LineRefStructure> lineRefStructElement = objectFactory.createLineRef(lineRefStruct);
//
//        return objectFactory.createServiceJourney()
//                       .withVersion(VERSION_ONE)
//                       .withId(serviceJourneyId)
//                       .withPublicCode(flightId)
//                       .withName(createMultilingualString(name))
//                       // .withDepartureTime(departureTime)
//                       .withDayTypes(dayTypeRefsStruct)
//                       .withJourneyPatternRef(journeyPatternRefStructElement)
//                       .withLineRef(lineRefStructElement)
//                       .withPassingTimes(passingTimesRelStruct);
//    }
//
//
//    public TimetabledPassingTime createTimetabledPassingTime(String stopPointInJourneyPatternId) {
//        StopPointInJourneyPatternRefStructure stopPointInJourneyPatternRefStruct =
//                createStopPointInJourneyPatternRefStructure(stopPointInJourneyPatternId);
//
//        JAXBElement<StopPointInJourneyPatternRefStructure> stopPointInJourneyPatternRefStructElement = objectFactory
//                                                                                                               .createStopPointInJourneyPatternRef(stopPointInJourneyPatternRefStruct);
//
//        String timetabledPassingTimeId = NetexObjectIdCreator.createTimetabledPassingTimeId(AVINOR_XMLNS, UUID.randomUUID().toString());
//
//        return objectFactory.createTimetabledPassingTime().withId(timetabledPassingTimeId).withVersion(VERSION_ONE)
//                       .withPointInJourneyPatternRef(stopPointInJourneyPatternRefStructElement);
//    }
//
//    public DayType createDayType(String dayTypeId) {
//        return objectFactory.createDayType()
//                       .withVersion(VERSION_ONE)
//                       .withId(dayTypeId);
//    }
//
//    public OperatingPeriod createOperatingPeriod(String operatingPeriodId,LocalDate from, LocalDate to){
//        return new OperatingPeriod().withId(operatingPeriodId).withVersion(VERSION_ONE).withFromDate(from.atStartOfDay()).withToDate(to.atStartOfDay());
//    }
//
//    public DayTypeAssignment createDayTypeAssignment(String objectId, Integer order, LocalDate dateOfOperation, String dayTypeId, boolean available) {
//        String dayTypeAssignmentId = NetexObjectIdCreator.createDayTypeAssignmentId(AVINOR_XMLNS, objectId);
//
//        DayTypeRefStructure dayTypeRefStruct = createDayTypeRefStructure(dayTypeId);
//        JAXBElement<DayTypeRefStructure> dayTypeRefStructElement = objectFactory.createDayTypeRef(dayTypeRefStruct);
//
//        DayTypeAssignment dayTypeAssignment = objectFactory.createDayTypeAssignment()
//                                                      .withVersion(VERSION_ONE)
//                                                      .withId(dayTypeAssignmentId)
//                                                      .withOrder(BigInteger.valueOf(order))
//                                                      .withDate(dateOfOperation == null ? null : dateOfOperation.atStartOfDay())
//                                                      .withDayTypeRef(dayTypeRefStructElement);
//
//        if (!available) {
//            dayTypeAssignment.withIsAvailable(available);
//        }
//
//        return dayTypeAssignment;
//    }
//
//    public DayTypeAssignment createDayTypeAssignment(String objectId, Integer order, String dayTypeId, String operatingPeriodId) {
//
//        OperatingPeriodRefStructure operatingPeriodRefStructure =
//                objectFactory.createOperatingPeriodRefStructure().withRef(operatingPeriodId).withVersion(VERSION_ONE);
//
//        return createDayTypeAssignment(objectId, order, null, dayTypeId, true)
//                       .withOperatingPeriodRef(operatingPeriodRefStructure);
//
//    }

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

    // reference structures creation


    public LineRefStructure createLineRefStructure(String lineId) {
        return objectFactory.createLineRefStructure()
                       .withVersion(VERSION_ONE)
                       .withRef(lineId);
    }

    public OperatorRefStructure createOperatorRefStructure(String operatorId, boolean withRefValidation) {
        OperatorRefStructure operatorRefStruct = objectFactory.createOperatorRefStructure()
                                                         .withRef(operatorId);
        return withRefValidation ? operatorRefStruct.withVersion(VERSION_ONE) : operatorRefStruct;
    }

    public GroupOfLinesRefStructure createGroupOfLinesRefStructure(String groupOfLinesId) {
        return objectFactory.createGroupOfLinesRefStructure().withRef(groupOfLinesId);
    }

    public RouteRefStructure createRouteRefStructure(String routeId) {
        return objectFactory.createRouteRefStructure()
                       .withVersion(VERSION_ONE)
                       .withRef(routeId);
    }

    public List<RouteRefStructure> createRouteRefStructures(List<Route> routes) {
        return routes.stream()
                       .map(Route::getId)
                       .collect(Collectors.toSet()).stream()
                       .map(routeId -> objectFactory.createRouteRefStructure().withVersion(VERSION_ONE).withRef(routeId))
                       .collect(Collectors.toList());
    }

    public StopPlaceRefStructure createStopPlaceRefStructure(String stopPlaceId) {
        return objectFactory.createStopPlaceRefStructure()
                       .withRef(stopPlaceId);
    }

    public QuayRefStructure createQuayRefStructure(String quayId) {
        return objectFactory.createQuayRefStructure()
                       .withRef(quayId);
    }

    public ScheduledStopPointRefStructure createScheduledStopPointRefStructure(String stopPointId, boolean withRefValidation) {
        ScheduledStopPointRefStructure scheduledStopPointRefStruct = objectFactory.createScheduledStopPointRefStructure()
                                                                             .withRef(stopPointId);
        return withRefValidation ? scheduledStopPointRefStruct.withVersion(VERSION_ONE) : scheduledStopPointRefStruct;
    }

    public StopPointInJourneyPatternRefStructure createStopPointInJourneyPatternRefStructure(String stopPointInJourneyPatternId) {
        return objectFactory.createStopPointInJourneyPatternRefStructure()
                       .withVersion(VERSION_ONE)
                       .withRef(stopPointInJourneyPatternId);
    }

    public PointRefStructure createPointRefStructure(String stopPointId, boolean withRefValidation) {
        PointRefStructure pointRefStruct = objectFactory.createPointRefStructure()
                                                   .withRef(stopPointId);
        return withRefValidation ? pointRefStruct.withVersion(VERSION_ONE) : pointRefStruct;
    }

    public RoutePointRefStructure createRoutePointRefStructure(String stopPointId) {
        return objectFactory.createRoutePointRefStructure()
                       //.withVersion(VERSION_ONE)
                       .withRef(stopPointId);
    }

    public DayTypeRefStructure createDayTypeRefStructure(String dayTypeId) {
        return objectFactory.createDayTypeRefStructure()
                       .withVersion(VERSION_ONE)
                       .withRef(dayTypeId);
    }

    public DestinationDisplayRefStructure createDestinationDisplayRefStructure(String destinationDisplayId) {
        return objectFactory.createDestinationDisplayRefStructure()
                       .withVersion(VERSION_ONE)
                       .withRef(destinationDisplayId);
    }


}