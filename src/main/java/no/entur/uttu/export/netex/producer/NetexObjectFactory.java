package no.entur.uttu.export.netex.producer;


import no.entur.uttu.export.netex.NetexExportContext;
import org.rutebanken.netex.model.CompositeFrame;
import org.rutebanken.netex.model.DayOfWeekEnumeration;
import org.rutebanken.netex.model.DayTypeRefStructure;
import org.rutebanken.netex.model.DestinationDisplayRefStructure;
import org.rutebanken.netex.model.LineRefStructure;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.OperatorRefStructure;
import org.rutebanken.netex.model.PointRefStructure;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.QuayRefStructure;
import org.rutebanken.netex.model.Route;
import org.rutebanken.netex.model.RoutePointRefStructure;
import org.rutebanken.netex.model.RouteRefStructure;
import org.rutebanken.netex.model.ScheduledStopPointRefStructure;
import org.rutebanken.netex.model.StopPlaceRefStructure;
import org.rutebanken.netex.model.StopPointInJourneyPatternRefStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(value = "netexObjectFactory")
public class NetexObjectFactory {

    private static final String VERSION_ONE = "1";

    @Value("${netex.export.version:1.08:NO-NeTEx-networktimetable:1.3}")
    private String netexVersion;


    private ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    private DateUtils dateUtils;

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

    public JAXBElement<PublicationDeliveryStructure> createPublicationDelivery(NetexExportContext exportContext, CompositeFrame compositeFrame) {

        PublicationDeliveryStructure.DataObjects dataObjects = objectFactory.createPublicationDeliveryStructureDataObjects();
        dataObjects.getCompositeFrameOrCommonFrame().add(objectFactory.createCompositeFrame(compositeFrame));

        PublicationDeliveryStructure publicationDeliveryStructure = objectFactory.createPublicationDeliveryStructure()
                                                                            .withVersion(netexVersion)
                                                                            .withPublicationTimestamp(dateUtils.toExportLocalDateTime(exportContext.publicationTimestamp))
                                                                            .withParticipantRef(exportContext.provider.getName())
                                                                            .withDescription(createMultilingualString("Flexible lines")) // TODO ?
                                                                            .withDataObjects(dataObjects);

        return objectFactory.createPublicationDelivery(publicationDeliveryStructure);
    }

    // TODO remove unused

//
//    public JAXBElement<PublicationDeliveryStructure> createPublicationDeliveryStructureElement(
//                                                                                                      Instant publicationTimestamp, JAXBElement<CompositeFrame> compositeFrame, String description) {
//
//        NetexStaticDataSet.OrganisationDataSet avinorDataSet = netexStaticDataSet.getOrganisations().get(AVINOR_XMLNS.toLowerCase());
//
//        PublicationDeliveryStructure.DataObjects dataObjects = objectFactory.createPublicationDeliveryStructureDataObjects();
//        dataObjects.getCompositeFrameOrCommonFrame().add(compositeFrame);
//
//        PublicationDeliveryStructure publicationDeliveryStructure = objectFactory.createPublicationDeliveryStructure()
//                                                                            .withVersion(NETEX_PROFILE_VERSION)
//                                                                            .withPublicationTimestamp(dateUtils.toExportLocalDateTime(publicationTimestamp))
//                                                                            .withParticipantRef(avinorDataSet.getName())
//                                                                            //.withDescription(createMultilingualString(description)) // TODO find out if needed
//                                                                            .withDataObjects(dataObjects);
//
//        return objectFactory.createPublicationDelivery(publicationDeliveryStructure);
//    }
//
//    public JAXBElement<CompositeFrame> createCompositeFrame(Instant publicationTimestamp,
//                                                                   AvailabilityPeriod availabilityPeriod, String airlineIata, String lineDesignation, Frames_RelStructure frames) {
//
//        ValidityConditions_RelStructure validityConditionsStruct = objectFactory.createValidityConditions_RelStructure()
//                                                                           .withValidityConditionRefOrValidBetweenOrValidityCondition_(createAvailabilityCondition(availabilityPeriod));
//
//        Codespace avinorCodespace = createCodespace(AVINOR_XMLNS, AVINOR_XMLNSURL);
//        Codespace nsrCodespace = createCodespace(NSR_XMLNS, NSR_XMLNSURL);
//
//        Codespaces_RelStructure codespaces = objectFactory.createCodespaces_RelStructure()
//                                                     .withCodespaceRefOrCodespace(Arrays.asList(avinorCodespace, nsrCodespace));
//
//        LocaleStructure localeStructure = objectFactory.createLocaleStructure()
//                                                  .withTimeZone(DEFAULT_ZONE_ID)
//                                                  .withDefaultLanguage(DEFAULT_LANGUAGE);
//
//        VersionFrameDefaultsStructure versionFrameDefaultsStructure = objectFactory.createVersionFrameDefaultsStructure()
//                                                                              .withDefaultLocale(localeStructure);
//
//        String compositeFrameId = NetexObjectIdCreator.createCompositeFrameId(
//                AVINOR_XMLNS, Joiner.on(DASH).skipNulls().join(airlineIata, lineDesignation));
//
//        CompositeFrame compositeFrame = objectFactory.createCompositeFrame()
//                                                .withVersion(VERSION_ONE)
//                                                .withCreated(dateUtils.toExportLocalDateTime(publicationTimestamp))
//                                                .withId(compositeFrameId)
//                                                .withValidityConditions(validityConditionsStruct)
//                                                .withCodespaces(codespaces)
//                                                .withFrameDefaults(versionFrameDefaultsStructure)
//                                                .withFrames(frames);
//
//        return objectFactory.createCompositeFrame(compositeFrame);
//    }
//
//    public JAXBElement<CompositeFrame> createCompositeFrameElement(Instant publicationTimestamp, Frames_RelStructure frames, AvailabilityPeriod availabilityPeriod, Codespace... codespaces) {
//
//        ValidityConditions_RelStructure validityConditionsStruct = objectFactory.createValidityConditions_RelStructure()
//                                                                           .withValidityConditionRefOrValidBetweenOrValidityCondition_(createAvailabilityCondition(availabilityPeriod));
//
//        Codespaces_RelStructure codespacesStruct = objectFactory.createCodespaces_RelStructure()
//                                                           .withCodespaceRefOrCodespace((Object[]) codespaces);
//
//        LocaleStructure localeStructure = objectFactory.createLocaleStructure()
//                                                  .withTimeZone(DEFAULT_ZONE_ID)
//                                                  .withDefaultLanguage(DEFAULT_LANGUAGE);
//
//        VersionFrameDefaultsStructure versionFrameDefaultsStructure = objectFactory.createVersionFrameDefaultsStructure()
//                                                                              .withDefaultLocale(localeStructure);
//
//        String compositeFrameId = NetexObjectIdCreator.createCompositeFrameId(AVINOR_XMLNS,
//                String.valueOf(NetexObjectIdCreator.generateRandomId(DEFAULT_START_INCLUSIVE, DEFAULT_END_EXCLUSIVE)));
//
//        CompositeFrame compositeFrame = objectFactory.createCompositeFrame()
//                                                .withVersion(VERSION_ONE)
//                                                .withCreated(dateUtils.toExportLocalDateTime(publicationTimestamp))
//                                                .withId(compositeFrameId)
//                                                .withValidityConditions(validityConditionsStruct)
//                                                .withCodespaces(codespacesStruct)
//                                                .withFrameDefaults(versionFrameDefaultsStructure)
//                                                .withFrames(frames);
//
//        return objectFactory.createCompositeFrame(compositeFrame);
//    }
//
//    public JAXBElement<ResourceFrame> createResourceFrameElement(Collection<JAXBElement<Authority>> authorityElements,
//                                                                        Collection<JAXBElement<Operator>> operatorElements) {
//
//        OrganisationsInFrame_RelStructure organisationsStruct = objectFactory.createOrganisationsInFrame_RelStructure();
//
//        String resourceFrameId = NetexObjectIdCreator.createResourceFrameId(AVINOR_XMLNS,
//                String.valueOf(NetexObjectIdCreator.generateRandomId(DEFAULT_START_INCLUSIVE, DEFAULT_END_EXCLUSIVE)));
//
//        ResourceFrame resourceFrame = objectFactory.createResourceFrame()
//                                              .withVersion(VERSION_ONE)
//                                              .withId(resourceFrameId);
//        resourceFrame.setOrganisations(organisationsStruct);
//
//        for (Iterator<JAXBElement<Authority>> iterator = authorityElements.iterator(); iterator.hasNext(); ) {
//            JAXBElement<Authority> authorityElement = iterator.next();
//            resourceFrame.getOrganisations().getOrganisation_().add(authorityElement);
//        }
//
//        for (Iterator<JAXBElement<Operator>> iterator = operatorElements.iterator(); iterator.hasNext(); ) {
//            JAXBElement<Operator> operatorElement = iterator.next();
//            resourceFrame.getOrganisations().getOrganisation_().add(operatorElement);
//        }
//
//        return objectFactory.createResourceFrame(resourceFrame);
//    }
//
//    public JAXBElement<ResourceFrame> createResourceFrameElement(Operator operator) {
//        OrganisationsInFrame_RelStructure organisationsStruct = objectFactory.createOrganisationsInFrame_RelStructure();
//        organisationsStruct.getOrganisation_().add(createAirlineOperatorElement(operator));
//
//        String resourceFrameId = NetexObjectIdCreator.createResourceFrameId(AVINOR_XMLNS,
//                String.valueOf(NetexObjectIdCreator.generateRandomId(DEFAULT_START_INCLUSIVE, DEFAULT_END_EXCLUSIVE)));
//
//        ResourceFrame resourceFrame = objectFactory.createResourceFrame()
//                                              .withVersion(VERSION_ONE)
//                                              .withId(resourceFrameId);
//        resourceFrame.setOrganisations(organisationsStruct);
//
//        return objectFactory.createResourceFrame(resourceFrame);
//    }
//
//
//    public JAXBElement<ServiceFrame> createCommonServiceFrameElement(Collection<Network> networks, List<RoutePoint> routePoints,
//                                                                            List<ScheduledStopPoint> scheduledStopPoints, List<JAXBElement<PassengerStopAssignment>> stopAssignmentElements) {
//
//        String serviceFrameId = NetexObjectIdCreator.createServiceFrameId(AVINOR_XMLNS,
//                String.valueOf(NetexObjectIdCreator.generateRandomId(DEFAULT_START_INCLUSIVE, DEFAULT_END_EXCLUSIVE)));
//
//        RoutePointsInFrame_RelStructure routePointStruct = objectFactory.createRoutePointsInFrame_RelStructure()
//                                                                   .withRoutePoint(routePoints);
//
//        ScheduledStopPointsInFrame_RelStructure scheduledStopPointsStruct = objectFactory.createScheduledStopPointsInFrame_RelStructure();
//        scheduledStopPoints.forEach(stopPoint -> scheduledStopPointsStruct.getScheduledStopPoint().add(stopPoint));
//
//        StopAssignmentsInFrame_RelStructure stopAssignmentsStruct = objectFactory.createStopAssignmentsInFrame_RelStructure();
//        stopAssignmentElements.forEach(stopAssignmentElement -> stopAssignmentsStruct.getStopAssignment().add(stopAssignmentElement));
//
//        ServiceFrame serviceFrame = objectFactory.createServiceFrame()
//                                            .withVersion(VERSION_ONE)
//                                            .withId(serviceFrameId)
//                                            .withRoutePoints(routePointStruct)
//                                            .withScheduledStopPoints(scheduledStopPointsStruct)
//                                            .withStopAssignments(stopAssignmentsStruct);
//
//        if (!CollectionUtils.isEmpty(networks)) {
//            Iterator<Network> networkIterator=networks.iterator();
//            serviceFrame.withNetwork(networkIterator.next());
//
//            if (networkIterator.hasNext()) {
//                NetworksInFrame_RelStructure additionalNetworks = new NetworksInFrame_RelStructure();
//                while (networkIterator.hasNext()) {
//                    additionalNetworks.getNetwork().add(networkIterator.next());
//                }
//                serviceFrame.withAdditionalNetworks(additionalNetworks);
//            }
//        }
//
//        return objectFactory.createServiceFrame(serviceFrame);
//    }
//
//    public JAXBElement<ServiceFrame> createServiceFrame(Instant publicationTimestamp, String airlineName,
//                                                               String airlineIata, List<Route> routes, Line line, List<DestinationDisplay> destinationDisplays, List<JourneyPattern> journeyPatterns) {
//
//        Network network = null;
//        if (!isCommonDesignator(airlineIata)) {
//            network = createNetwork(publicationTimestamp, airlineIata, airlineName);
//        }
//
//        RoutesInFrame_RelStructure routesInFrame = objectFactory.createRoutesInFrame_RelStructure();
//        for (Route route : routes) {
//            JAXBElement<Route> routeElement = objectFactory.createRoute(route);
//            routesInFrame.getRoute_().add(routeElement);
//        }
//
//        LinesInFrame_RelStructure linesInFrame = objectFactory.createLinesInFrame_RelStructure();
//        linesInFrame.getLine_().add(objectFactory.createLine(line));
//
//        DestinationDisplaysInFrame_RelStructure destinationDisplayStruct = objectFactory.createDestinationDisplaysInFrame_RelStructure()
//                                                                                   .withDestinationDisplay(destinationDisplays);
//
//        JourneyPatternsInFrame_RelStructure journeyPatternsInFrame = objectFactory.createJourneyPatternsInFrame_RelStructure();
//        for (JourneyPattern journeyPattern : journeyPatterns) {
//            JAXBElement<JourneyPattern> journeyPatternElement = objectFactory.createJourneyPattern(journeyPattern);
//            journeyPatternsInFrame.getJourneyPattern_OrJourneyPatternView().add(journeyPatternElement);
//        }
//
//        String serviceFrameId = NetexObjectIdCreator.createServiceFrameId(AVINOR_XMLNS,
//                String.valueOf(NetexObjectIdCreator.generateRandomId(DEFAULT_START_INCLUSIVE, DEFAULT_END_EXCLUSIVE)));
//
//        ServiceFrame serviceFrame = objectFactory.createServiceFrame()
//                                            .withVersion(VERSION_ONE)
//                                            .withId(serviceFrameId)
//                                            .withRoutes(routesInFrame)
//                                            .withLines(linesInFrame)
//                                            .withDestinationDisplays(destinationDisplayStruct)
//                                            .withJourneyPatterns(journeyPatternsInFrame);
//
//        if (network != null) {
//            serviceFrame.setNetwork(network);
//        }
//
//        return objectFactory.createServiceFrame(serviceFrame);
//    }
//
//
//
//    public JAXBElement<TimetableFrame> createTimetableFrame(List<ServiceJourney> serviceJourneys) {
//        JourneysInFrame_RelStructure journeysInFrameRelStructure = objectFactory.createJourneysInFrame_RelStructure();
//        journeysInFrameRelStructure.getDatedServiceJourneyOrDeadRunOrServiceJourney().addAll(serviceJourneys);
//
//        String timetableFrameId = NetexObjectIdCreator.createTimetableFrameId(AVINOR_XMLNS,
//                String.valueOf(NetexObjectIdCreator.generateRandomId(DEFAULT_START_INCLUSIVE, DEFAULT_END_EXCLUSIVE)));
//
//        TimetableFrame timetableFrame = objectFactory.createTimetableFrame()
//                                                .withVersion(VERSION_ONE)
//                                                .withId(timetableFrameId)
//                                                .withVehicleJourneys(journeysInFrameRelStructure);
//
//        return objectFactory.createTimetableFrame(timetableFrame);
//    }
//
//    public JAXBElement<ServiceCalendarFrame> createServiceCalendarFrame(Map<String, DayType> dayTypes, Map<String, DayTypeAssignment> dayTypeAssignments, Map<String, OperatingPeriod> operatingPeriods) {
//        DayTypesInFrame_RelStructure dayTypesStruct = objectFactory.createDayTypesInFrame_RelStructure();
//        for (DayType dayType : dayTypes.values()) {
//            JAXBElement<DayType> dayTypeElement = objectFactory.createDayType(dayType);
//            dayTypesStruct.getDayType_().add(dayTypeElement);
//        }
//
//        String serviceCalendarFrameId = NetexObjectIdCreator.createServiceCalendarFrameId(AVINOR_XMLNS,
//                String.valueOf(NetexObjectIdCreator.generateRandomId(DEFAULT_START_INCLUSIVE, DEFAULT_END_EXCLUSIVE)));
//
//        ServiceCalendarFrame serviceCalendarFrame = objectFactory.createServiceCalendarFrame()
//                                                            .withVersion(VERSION_ONE)
//                                                            .withId(serviceCalendarFrameId)
//                                                            .withDayTypes(dayTypesStruct);
//
//        if (!dayTypeAssignments.isEmpty()) {
//            List<DayTypeAssignment> dayTypeAssignmentList = new ArrayList<>(dayTypeAssignments.values());
//            dayTypeAssignmentList.sort(Comparator.comparing(DayTypeAssignment::getOrder));
//            DayTypeAssignmentsInFrame_RelStructure dayTypeAssignmentsStruct = objectFactory.createDayTypeAssignmentsInFrame_RelStructure();
//            dayTypeAssignmentList.forEach(dayTypeAssignment -> dayTypeAssignmentsStruct.getDayTypeAssignment().add(dayTypeAssignment));
//
//            serviceCalendarFrame.withDayTypeAssignments(dayTypeAssignmentsStruct);
//        }
//
//        if (!operatingPeriods.isEmpty()) {
//            OperatingPeriodsInFrame_RelStructure operatingPeriodStruct = objectFactory.createOperatingPeriodsInFrame_RelStructure();
//            operatingPeriodStruct.getOperatingPeriodOrUicOperatingPeriod().addAll(operatingPeriods.values());
//            operatingPeriodStruct.getOperatingPeriodOrUicOperatingPeriod().sort(Comparator.comparing(OperatingPeriod_VersionStructure::getFromDate));
//            serviceCalendarFrame.withOperatingPeriods(operatingPeriodStruct);
//        }
//
//        return objectFactory.createServiceCalendarFrame(serviceCalendarFrame);
//    }
//
//    public JAXBElement<AvailabilityCondition> createAvailabilityCondition(AvailabilityPeriod availabilityPeriod) {
//        String availabilityConditionId = NetexObjectIdCreator.createAvailabilityConditionId(AVINOR_XMLNS,
//                String.valueOf(NetexObjectIdCreator.generateRandomId(DEFAULT_START_INCLUSIVE, DEFAULT_END_EXCLUSIVE)));
//
//        AvailabilityCondition availabilityCondition = objectFactory.createAvailabilityCondition()
//                                                              .withVersion(VERSION_ONE)
//                                                              .withId(availabilityConditionId)
//                                                              .withFromDate(availabilityPeriod.getPeriodFromDateTime())
//                                                              .withToDate(availabilityPeriod.getPeriodToDateTime());
//
//        return objectFactory.createAvailabilityCondition(availabilityCondition);
//    }
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
//    public Codespace createCodespace(String id, String xmlnsUrl) {
//        return objectFactory.createCodespace()
//                       .withId(id.toLowerCase())
//                       .withXmlns(id)
//                       .withXmlnsUrl(xmlnsUrl);
//    }
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
        return objectFactory.createMultilingualString().withValue(value);
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