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

package no.entur.uttu.graphql;

import graphql.Scalars;
import graphql.schema.*;
import no.entur.uttu.config.Context;
import no.entur.uttu.export.lineStatistics.ExportedLineStatisticsService;
import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.graphql.scalars.DateScalar;
import no.entur.uttu.graphql.scalars.DateTimeScalar;
import no.entur.uttu.graphql.scalars.DurationScalar;
import no.entur.uttu.graphql.scalars.GeoJSONCoordinatesScalar;
import no.entur.uttu.graphql.scalars.LocalTimeScalar;
import no.entur.uttu.model.*;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.model.job.ExportStatusEnumeration;
import no.entur.uttu.model.job.SeverityEnumeration;
import no.entur.uttu.profile.Profile;
import no.entur.uttu.repository.*;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;
import static no.entur.uttu.graphql.GraphQLNames.*;

/**
 * GraphQL schema for FlexibleLines and related entities.
 */
@Component
public class LinesGraphQLSchema {
    @Autowired
    private DateTimeScalar dateTimeScalar;

    @Autowired
    private DataFetcher<Export> exportUpdater;

    @Autowired
    private DataFetcher<FlexibleStopPlace> flexibleStopPlaceUpdater;

    @Autowired
    private DataFetcher<FlexibleLine> flexibleLineUpdater;

    @Autowired
    private DataFetcher<FixedLine> fixedLineUpdater;

    @Autowired
    private DataFetcher<Network> networkUpdater;

    @Autowired
    private FlexibleStopPlaceRepository flexibleStopPlaceRepository;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private ExportRepository exportRepository;

    @Autowired
    private ExportedLineStatisticsService exportedLineStatisticsService;

    @Autowired
    private FlexibleLineRepository flexibleLineRepository;

    @Autowired
    private FixedLineRepository fixedLineRepository;

    @Autowired
    private DataSpaceCleaner dataSpaceCleaner;

    @Autowired
    private Profile profile;

    private <T extends Enum> GraphQLEnumType createEnum(String name, T[] values, Function<T, String> mapping) {
        return createEnum(name, Arrays.asList(values), mapping);
    }

    private <T extends Enum> GraphQLEnumType createEnum(String name, Collection<T> values, Function<T, String> mapping) {
        GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum().name(name);
        values.forEach(type -> enumBuilder.value(mapping.apply(type), type));
        return enumBuilder.build();
    }

    private GraphQLEnumType geometryTypeEnum = GraphQLEnumType.newEnum()
            .name("GeometryType")
            .value("Point")
            .value("LineString")
            .value("Polygon")
            .value("MultiPoint")
            .value("MultiLineString")
            .value("MultiPolygon")
            .value("GeometryCollection")
            .build();

    private GraphQLEnumType dayOfWeekEnum = createEnum("DayOfWeekEnumeration", DayOfWeek.values(), (t -> t.name().toLowerCase()));
    private GraphQLEnumType exportStatusEnum = createEnum("ExportStatusEnumeration", ExportStatusEnumeration.values(), (t -> t.name().toLowerCase()));
    private GraphQLEnumType severityEnum = createEnum("SeverityEnumeration", SeverityEnumeration.values(), (t -> t.name().toLowerCase()));

    private GraphQLEnumType vehicleModeEnum;
    private GraphQLEnumType vehicleSubmodeEnum;
    private GraphQLEnumType flexibleLineTypeEnum = createEnum("FlexibleLineTypeEnumeration", FlexibleLineTypeEnumeration.values(), (FlexibleLineTypeEnumeration::value));
    private GraphQLEnumType bookingMethodEnum = createEnum("BookingMethodEnumeration", BookingMethodEnumeration.values(), (BookingMethodEnumeration::value));
    private GraphQLEnumType bookingAccessEnum = createEnum("BookingAccessEnumeration", BookingAccessEnumeration.values(), (BookingAccessEnumeration::value));
    private GraphQLEnumType purchaseWhenEnum = createEnum("PurchaseWhenEnumeration", PurchaseWhenEnumeration.values(), (PurchaseWhenEnumeration::value));
    private GraphQLEnumType purchaseMomentEnum = createEnum("PurchaseMomentEnumeration", PurchaseMomentEnumeration.values(), (PurchaseMomentEnumeration::value));
    private GraphQLEnumType directionTypeEnum = createEnum("DirectionTypeEnumeration", DirectionTypeEnumeration.values(), (DirectionTypeEnumeration::value));

    private GraphQLObjectType lineObjectType;
    private GraphQLObjectType fixedLineObjectType;
    private GraphQLObjectType flexibleLineObjectType;
    private GraphQLObjectType flexibleStopPlaceObjectType;
    private GraphQLObjectType networkObjectType;
    private GraphQLObjectType exportObjectType;
    private GraphQLObjectType exportedLineStatisticsObjectType;

    private GraphQLArgument idArgument;
    private GraphQLArgument providerArgument;
    private GraphQLSchema graphQLSchema;

    @PostConstruct
    public void init() {
        vehicleModeEnum = createEnum("VehicleModeEnumeration", profile.getLegalVehicleModes(), (VehicleModeEnumeration::value));
        vehicleSubmodeEnum = createEnum("VehicleSubmodeEnumeration", profile.getLegalVehicleSubmodes(), (VehicleSubmodeEnumeration::value));

        initCommonTypes();

        graphQLSchema = GraphQLSchema.newSchema()
                .query(createQueryObject())
                .mutation(createMutationObject())
                .build();
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    private void initCommonTypes() {
        GraphQLFieldDefinition idFieldDefinition = newFieldDefinition()
                .name(FIELD_ID)
                .type(new GraphQLNonNull(GraphQLID))
                .dataFetcher(env -> ((ProviderEntity) env.getSource()).getNetexId())
                .build();

        GraphQLFieldDefinition versionField = newFieldDefinition()
                .name(FIELD_VERSION)
                .type(new GraphQLNonNull(GraphQLString))
                .build();

        idArgument = GraphQLArgument.newArgument().name(FIELD_ID)
                .type(new GraphQLNonNull(GraphQLID))
                .description("Id for entity").build();

        providerArgument = GraphQLArgument.newArgument().name(FIELD_PROVIDER_CODE)
                .type(GraphQLID)
                .description("Provider code, f.eks 'rut' for Ruter.").build();

        GraphQLObjectType geoJSONObjectType = newObject()
                .name("GeoJSON")
                .description("Geometry-object as specified in the GeoJSON-standard (http://geojson.org/geojson-spec.html).")
                .field(newFieldDefinition()
                        .name("type")
                        .type(new GraphQLNonNull(geometryTypeEnum))
                        .dataFetcher(env -> {
                            if (env.getSource() instanceof Geometry) {
                                return env.getSource().getClass().getSimpleName();
                            }
                            return null;
                        }))
                .field(newFieldDefinition()
                        .name("coordinates")
                        .type(new GraphQLNonNull(GeoJSONCoordinatesScalar.getGraphQGeoJSONCoordinatesScalar())))
                .build();

        GraphQLObjectType keyValuesObjectType = newObject().name("KeyValues")
                .field(newFieldDefinition().name(FIELD_KEY).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_VALUES).type(new GraphQLList(GraphQLString)))
                .build();

        GraphQLObjectType identifiedEntityObjectType = newObject().name("IdentifiedEntity")
                .field(idFieldDefinition)
                .field(versionField)
                .field(newFieldDefinition().name(FIELD_CREATED_BY).type(new GraphQLNonNull(GraphQLString)))
                .field(newFieldDefinition().name(FIELD_CREATED).type(new GraphQLNonNull(dateTimeScalar.getDateTimeScalar())))
                .field(newFieldDefinition().name(FIELD_CHANGED_BY).type(new GraphQLNonNull(GraphQLString)))
                .field(newFieldDefinition().name(FIELD_CHANGED).type(new GraphQLNonNull(dateTimeScalar.getDateTimeScalar())))
                .build();

        GraphQLObjectType groupOfEntitiesObjectType = newObject(identifiedEntityObjectType).name("GroupOfEntities")
                .field(newFieldDefinition().name(FIELD_NAME).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_DESCRIPTION).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_PRIVATE_CODE).type(GraphQLString))
                .build();

        networkObjectType = newObject(groupOfEntitiesObjectType).name("Network")
                .field(newFieldDefinition().name(FIELD_AUTHORITY_REF).type(new GraphQLNonNull(GraphQLString)))
                .build();

        GraphQLObjectType contactObjectType = newObject().name("Contact")
                .field(newFieldDefinition().name(FIELD_CONTACT_PERSON).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_PHONE).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_EMAIL).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_URL).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_FURTHER_DETAILS).type(GraphQLString))
                .build();

        GraphQLObjectType bookingArrangementObjectType = newObject().name("BookingArrangement")
                .field(newFieldDefinition().name(FIELD_BOOKING_CONTACT).type(contactObjectType))
                .field(newFieldDefinition().name(FIELD_BOOKING_NOTE).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_BOOKING_METHODS).type(new GraphQLList(bookingMethodEnum)))
                .field(newFieldDefinition().name(FIELD_BOOKING_ACCESS).type(bookingAccessEnum))
                .field(newFieldDefinition().name(FIELD_BOOK_WHEN).type(purchaseWhenEnum))
                .field(newFieldDefinition().name(FIELD_BUY_WHEN).type(new GraphQLList(purchaseMomentEnum)))
                .field(newFieldDefinition().name(FIELD_LATEST_BOOKING_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newFieldDefinition().name(FIELD_MINIMUM_BOOKING_PERIOD).type(DurationScalar.getDurationScalar()))
                .build();

        GraphQLObjectType destinationDisplayObjectType = newObject(identifiedEntityObjectType).name("DestinationDisplay")
                .field(newFieldDefinition().name(FIELD_FRONT_TEXT).type(new GraphQLNonNull(GraphQLString)))
                .build();

        GraphQLObjectType noticeObjectType = newObject(identifiedEntityObjectType).name("Notice")
                .field(newFieldDefinition().name(FIELD_TEXT).type(new GraphQLNonNull(GraphQLString)))
                .build();

        GraphQLObjectType flexibleAreaObjectType = newObject().name("FlexibleArea")
                .field(newFieldDefinition().name(FIELD_POLYGON).type(new GraphQLNonNull(geoJSONObjectType))
                        .dataFetcher(env -> ((FlexibleArea) env.getSource()).getPolygon()))
                .build();

        GraphQLObjectType hailAndRideAreaType = newObject().name("HailAndRideArea")
                .field(newFieldDefinition().name(FIELD_START_QUAY_REF).type(new GraphQLNonNull(GraphQLString)))
                .field(newFieldDefinition().name(FIELD_END_QUAY_REF).type(new GraphQLNonNull(GraphQLString)))
                .build();

        flexibleStopPlaceObjectType = newObject(groupOfEntitiesObjectType).name("FlexibleStopPlace")
                .field(newFieldDefinition().name(FIELD_TRANSPORT_MODE).type(vehicleModeEnum))
                .field(newFieldDefinition().name(FIELD_FLEXIBLE_AREA).type(flexibleAreaObjectType))
                .field(newFieldDefinition().name(FIELD_HAIL_AND_RIDE_AREA).type(hailAndRideAreaType))
                .field(newFieldDefinition().name(FIELD_KEY_VALUES).type(new GraphQLList(keyValuesObjectType))
                        .dataFetcher(env -> ((FlexibleStopPlace) env.getSource()).getKeyValues().entrySet().stream().map(entry -> new KeyValuesWrapper(entry.getKey(), entry.getValue())).collect(Collectors.toList())))
                .build();

        GraphQLObjectType operatingPeriod = newObject().name("OperatingPeriod")
                .field(newFieldDefinition().name(FIELD_FROM_DATE).type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar())))
                .field(newFieldDefinition().name(FIELD_TO_DATE).type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar())))
                .build();

        GraphQLObjectType dayTypeAssignmentObjectType = newObject().name("DayTypeAssignment")
                .field(newFieldDefinition().name(FIELD_IS_AVAILABLE).dataFetcher(env -> ((DayTypeAssignment) env.getSource()).getAvailable()).type(GraphQLBoolean))
                .field(newFieldDefinition().name(FIELD_DATE).type(DateScalar.getGraphQLDateScalar()))
                .field(newFieldDefinition().name(FIELD_OPERATING_PERIOD).type(operatingPeriod))
                .build();

        GraphQLObjectType dayTypeObjectType = newObject(identifiedEntityObjectType).name("DayType")
                .field(newFieldDefinition().name(FIELD_DAYS_OF_WEEK).type(new GraphQLList(dayOfWeekEnum)))
                .field(newFieldDefinition().name(FIELD_DAY_TYPE_ASSIGNMENTS).type(new GraphQLNonNull(new GraphQLList(dayTypeAssignmentObjectType))))
                .build();

        GraphQLObjectType timetabledPassingTimeObjectType = newObject(identifiedEntityObjectType).name("TimetabledPassingTime")
                .field(newFieldDefinition().name(FIELD_ARRIVAL_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newFieldDefinition().name(FIELD_ARRIVAL_DAY_OFFSET).type(GraphQLInt))
                .field(newFieldDefinition().name(FIELD_DEPARTURE_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newFieldDefinition().name(FIELD_DEPARTURE_DAY_OFFSET).type(GraphQLInt))
                .field(newFieldDefinition().name(FIELD_LATEST_ARRIVAL_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newFieldDefinition().name(FIELD_LATEST_ARRIVAL_DAY_OFFSET).type(GraphQLInt))
                .field(newFieldDefinition().name(FIELD_EARLIEST_DEPARTURE_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newFieldDefinition().name(FIELD_EARLIEST_DEPARTURE_DAY_OFFSET).type(GraphQLInt))
                .field(newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType)))
                .build();

        GraphQLObjectType serviceJourneyObjectType = newObject(groupOfEntitiesObjectType).name("ServiceJourney")
                .field(newFieldDefinition().name(FIELD_PUBLIC_CODE).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_OPERATOR_REF).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementObjectType))
                .field(newFieldDefinition().name(FIELD_PASSING_TIMES).type(new GraphQLNonNull(new GraphQLList(timetabledPassingTimeObjectType))))
                .field(newFieldDefinition().name(FIELD_DAY_TYPES).type(new GraphQLList(dayTypeObjectType)))
                .field(newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType)))
                .build();

        GraphQLObjectType stopPointInJourneyPatternObjectType = newObject(identifiedEntityObjectType).name("StopPointInJourneyPattern")
                .field(newFieldDefinition().name(FIELD_FLEXIBLE_STOP_PLACE).type(flexibleStopPlaceObjectType))
                .field(newFieldDefinition().name(FIELD_QUAY_REF).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementObjectType))
                .field(newFieldDefinition().name(FIELD_DESTINATION_DISPLAY).type(destinationDisplayObjectType))
                .field(newFieldDefinition().name(FIELD_FOR_BOARDING).type(GraphQLBoolean))
                .field(newFieldDefinition().name(FIELD_FOR_ALIGHTING).type(GraphQLBoolean))
                .field(newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType)))
                .build();

        GraphQLObjectType journeyPatternObjectType = newObject(groupOfEntitiesObjectType).name("JourneyPattern")
                .field(newFieldDefinition().name(FIELD_DIRECTION_TYPE).type(directionTypeEnum))
                .field(newFieldDefinition().name(FIELD_POINTS_IN_SEQUENCE).type(new GraphQLNonNull(new GraphQLList(stopPointInJourneyPatternObjectType))))
                .field(newFieldDefinition().name(FIELD_SERVICE_JOURNEYS).type(new GraphQLNonNull(new GraphQLList(serviceJourneyObjectType))))
                .field(newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType)))
                .build();

        lineObjectType = newObject(groupOfEntitiesObjectType).name("Line")
                .field(newFieldDefinition().name(FIELD_PUBLIC_CODE).type(new GraphQLNonNull(GraphQLString)))
                .field(newFieldDefinition().name(FIELD_TRANSPORT_MODE).type(new GraphQLNonNull(vehicleModeEnum)))
                .field(newFieldDefinition().name(FIELD_TRANSPORT_SUBMODE).type(new GraphQLNonNull(vehicleSubmodeEnum)))
                .field(newFieldDefinition().name(FIELD_NETWORK).type(new GraphQLNonNull(networkObjectType)))
                .field(newFieldDefinition().name(FIELD_OPERATOR_REF).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_JOURNEY_PATTERNS).type(new GraphQLNonNull(new GraphQLList(journeyPatternObjectType))))
                .field(newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType)))
                .build();

        fixedLineObjectType = newObject(lineObjectType).name("FixedLine")
                .build();

        flexibleLineObjectType = newObject(lineObjectType).name("FlexibleLine")
                .field(newFieldDefinition().name(FIELD_FLEXIBLE_LINE_TYPE).type(new GraphQLNonNull(flexibleLineTypeEnum)))
                .field(newFieldDefinition().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementObjectType))
                .build();

        GraphQLObjectType exportMessageObjectType = newObject().name("Message")
                .field(newFieldDefinition().name(FIELD_SEVERITY).type(new GraphQLNonNull(severityEnum)))
                .field(newFieldDefinition().name(FIELD_MESSAGE).type(new GraphQLNonNull(GraphQLString)))
                .build();

        GraphQLObjectType exportLineAssociationObjectType = newObject().name("ExportLineAssociation")
                .field(newFieldDefinition().name(FIELD_LINE).type(new GraphQLNonNull(lineObjectType)))
                .build();

        GraphQLObjectType exportedDayTypeObjectType = newObject().name("ExportedDayType")
                .field(newFieldDefinition().name(FIELD_DAY_TYPE_NETEX_ID).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_OPERATING_DATE_FROM).type(DateScalar.getGraphQLDateScalar()))
                .field(newFieldDefinition().name(FIELD_OPERATING_DATE_TO).type(DateScalar.getGraphQLDateScalar()))
                .build();

        GraphQLObjectType exportedLineObjectType = newObject().name("ExportedLine")
                .field(newFieldDefinition().name(FIELD_LINE_NAME).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_OPERATING_DATE_FROM).type(DateScalar.getGraphQLDateScalar()))
                .field(newFieldDefinition().name(FIELD_OPERATING_DATE_TO).type(DateScalar.getGraphQLDateScalar()))
                .field(newFieldDefinition().name(FIELD_EXPORTED_DAY_TYPES_STATISTICS).type(new GraphQLList(exportedDayTypeObjectType))
                        .dataFetcher(env -> {
                            ExportedLineStatistics exportedLineStatistics = env.getSource();
                            return exportedLineStatistics.getExportedDayTypesStatistics();
                        }))
                .build();

        GraphQLObjectType publicLineObjectType = newObject().name("PublicLine")
                .field(newFieldDefinition().name(FIELD_OPERATING_DATE_FROM).type(DateScalar.getGraphQLDateScalar()))
                .field(newFieldDefinition().name(FIELD_OPERATING_DATE_TO).type(DateScalar.getGraphQLDateScalar()))
                .field(newFieldDefinition().name(FIELD_PUBLIC_CODE).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_PROVIDER_CODE).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_LINES).type(new GraphQLList(exportedLineObjectType)))
                .build();

        exportedLineStatisticsObjectType = newObject().name("ExportedLineStatistics")
                .field(newFieldDefinition().name(FIELD_START_DATE).type(DateScalar.getGraphQLDateScalar()).dataFetcher(env -> LocalDate.now()))
                .field(newFieldDefinition().name(FIELD_PUBLIC_LINES).type(new GraphQLList(publicLineObjectType))
                        .dataFetcher(env -> {
                            List<ExportedLineStatistics> exportedLineStatistics = env.getSource();

                            return exportedLineStatistics.stream()
                                    .collect(
                                            Collectors.groupingBy(lineStatistics -> lineStatistics.getExport().getProvider().getCode(),
                                                    Collectors.groupingBy(ExportedLineStatistics::getPublicCode))).entrySet().stream()
                                    .flatMap(lineStatisticsByProviderEntry -> lineStatisticsByProviderEntry.getValue().entrySet().stream()
                                            .map(lineStatisticsByPublicCodeEntry -> {
                                                AvailabilityPeriod availabilityPeriodForPublicLine = lineStatisticsByPublicCodeEntry.getValue().stream()
                                                        .map(exportedLine -> new AvailabilityPeriod(exportedLine.getOperatingPeriodFrom(), exportedLine.getOperatingPeriodTo()))
                                                        .reduce(AvailabilityPeriod::union).orElse(null);

                                                ExportedPublicLine exportedPublicLine = new ExportedPublicLine();
                                                exportedPublicLine.setOperatingPeriodFrom(Objects.requireNonNull(availabilityPeriodForPublicLine).getFrom());
                                                exportedPublicLine.setOperatingPeriodTo(availabilityPeriodForPublicLine.getTo());
                                                exportedPublicLine.setPublicCode(lineStatisticsByPublicCodeEntry.getKey());
                                                exportedPublicLine.setLines(lineStatisticsByPublicCodeEntry.getValue());
                                                exportedPublicLine.setProviderCode(lineStatisticsByProviderEntry.getKey());
                                                return exportedPublicLine;
                                            })
                                    ).collect(Collectors.toList());
                        }))
                .build();

        exportObjectType = newObject(identifiedEntityObjectType).name("Export")
                .field(newFieldDefinition().name(FIELD_NAME).type(GraphQLString))
                .field(newFieldDefinition().name(FIELD_EXPORT_STATUS).type(exportStatusEnum))
                .field(newFieldDefinition().name(FIELD_DRY_RUN).type(GraphQLBoolean))
                .field(newFieldDefinition().name(FIELD_DOWNLOAD_URL).type(GraphQLString).dataFetcher(env -> {
                    Export export = env.getSource();
                    if (export == null || !StringUtils.hasText(export.getFileName())) {
                        return null;
                    }
                    return export.getProvider().getCode().toLowerCase() + "/export/" + export.getNetexId() + "/download";
                }))
                .field(newFieldDefinition().name(FIELD_MESSAGES).type(new GraphQLList(exportMessageObjectType)))
                .field(newFieldDefinition().name(FIELD_EXPORT_LINE_ASSOCIATIONS).type(new GraphQLList(exportLineAssociationObjectType))
                        .dataFetcher(env -> {
                            Export export = env.getSource();
                            return export.getExportLineAssociations();
                        }))
                .build();
    }

    private GraphQLObjectType createQueryObject() {
        return newObject()
                .name("Queries")
                .description("Query and search for data")
                .field(newFieldDefinition()
                        .type(new GraphQLList(lineObjectType))
                        .name("lines")
                        .description("List of lines")
                        .dataFetcher(env -> fixedLineRepository.findAll()))
                .field(newFieldDefinition()
                        .type(lineObjectType)
                        .name("line")
                        .description("Get line by id")
                        .argument(idArgument)
                        .dataFetcher(env -> fixedLineRepository.getOne(env.getArgument(FIELD_ID))))
                .field(newFieldDefinition()
                        .type(new GraphQLList(fixedLineObjectType))
                        .name("fixedLines")
                        .description("List fixed lines")
                        .deprecate("Use 'lines' instead")
                        .dataFetcher(env -> fixedLineRepository.findAll()))
                .field(newFieldDefinition()
                        .type(fixedLineObjectType)
                        .name("fixedLine")
                        .description("Get fixedLine by id")
                        .deprecate("Use 'line' instead")
                        .argument(idArgument)
                        .dataFetcher(env -> fixedLineRepository.getOne(env.getArgument(FIELD_ID))))
                .field(newFieldDefinition()
                        .type(new GraphQLList(flexibleLineObjectType))
                        .name("flexibleLines")
                        .description("List flexibleLines")
                        .dataFetcher(env -> flexibleLineRepository.findAll()))
                .field(newFieldDefinition()
                        .type(flexibleLineObjectType)
                        .name("flexibleLine")
                        .description("Get flexibleLine by id")
                        .argument(idArgument)
                        .dataFetcher(env -> flexibleLineRepository.getOne(env.getArgument(FIELD_ID))))
                .field(newFieldDefinition()
                        .type(new GraphQLList(flexibleStopPlaceObjectType))
                        .name("flexibleStopPlaces")
                        .description("List flexibleStopPlaces")
                        .dataFetcher(env -> flexibleStopPlaceRepository.findAll()))
                .field(newFieldDefinition()
                        .type(flexibleStopPlaceObjectType)
                        .name("flexibleStopPlace")
                        .description("Get flexibleStopPlace by id")
                        .argument(idArgument)
                        .dataFetcher(env -> flexibleStopPlaceRepository.getOne(env.getArgument(FIELD_ID))))
                .field(newFieldDefinition()
                        .type(new GraphQLList(networkObjectType))
                        .name("networks")
                        .description("List networks")
                        .dataFetcher(env -> networkRepository.findAll()))
                .field(newFieldDefinition()
                        .type(networkObjectType)
                        .name("network")
                        .description("Get network by id")
                        .argument(idArgument)
                        .dataFetcher(env -> networkRepository.getOne(env.getArgument(FIELD_ID))))
                .field(newFieldDefinition()
                        .type(new GraphQLList(exportObjectType))
                        .name("exports")
                        .argument(GraphQLArgument.newArgument()
                                .type(GraphQLLong)
                                .name("historicDays")
                                .defaultValue(30L)
                                .description("Number historic to fetch data for"))
                        .description("List exports")
                        .dataFetcher(env -> exportRepository.findByCreatedAfterAndProviderCode(OffsetDateTime.now().minusDays(env.getArgument("historicDays")).toInstant(), Context.getProvider())))
                .field(newFieldDefinition()
                        .type(exportObjectType)
                        .name("export")
                        .description("Get export by id")
                        .argument(idArgument)
                        .dataFetcher(env -> exportRepository.getOne(env.getArgument(FIELD_ID))))
                .field(newFieldDefinition()
                        .type(exportedLineStatisticsObjectType)
                        .name("lineStatistics")
                        .description("Get line statistics")
                        .argument(providerArgument)
                        .dataFetcher(env -> {
                            String providerCode = env.getArgument(FIELD_PROVIDER_CODE);
                            return providerCode != null
                                    ? exportedLineStatisticsService.getLineStatisticsForProvider(providerCode)
                                    : exportedLineStatisticsService.getLineStatisticsForAllProviders();
                        }))
                .build();
    }

    private GraphQLObjectType createMutationObject() {

        String ignoredInputFieldDesc = "Value is ignored for mutation calls. Included for convenient copying of output to input with minimal modifications.";
        GraphQLInputObjectType identifiedEntityInputType = newInputObject().name("IdentifiedEntityInput")
                .field(newInputObjectField().name(FIELD_ID).type(Scalars.GraphQLID))
                .field(newInputObjectField().name(FIELD_VERSION).type(GraphQLLong)).description(ignoredInputFieldDesc)
                .field(newInputObjectField().name(FIELD_CREATED_BY).type(GraphQLString)).description(ignoredInputFieldDesc)
                .field(newInputObjectField().name(FIELD_CREATED).type(dateTimeScalar.getDateTimeScalar())).description(ignoredInputFieldDesc)
                .field(newInputObjectField().name(FIELD_CHANGED_BY).type(GraphQLString)).description(ignoredInputFieldDesc)
                .field(newInputObjectField().name(FIELD_CHANGED).type(dateTimeScalar.getDateTimeScalar())).description(ignoredInputFieldDesc)
                .build();

        GraphQLInputObjectType groupOfEntitiesInputType = newInputObject(identifiedEntityInputType).name("GroupOfEntities")
                .field(newInputObjectField().name(FIELD_NAME).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_DESCRIPTION).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_PRIVATE_CODE).type(GraphQLString))
                .build();

        GraphQLInputObjectType geoJSONInputType = newInputObject()
                .name("GeoJSONInput")
                .description("Geometry-object as specified in the GeoJSON-standard (http://geojson.org/geojson-spec.html).")
                .field(newInputObjectField()
                        .name("type")
                        .type(new GraphQLNonNull(geometryTypeEnum)))
                .field(newInputObjectField()
                        .name("coordinates")
                        .type(GeoJSONCoordinatesScalar.getGraphQGeoJSONCoordinatesScalar()))
                .build();

        GraphQLInputObjectType networkInputType = newInputObject(groupOfEntitiesInputType)
                .name("NetworkInput")
                .field(newInputObjectField().name(FIELD_AUTHORITY_REF).type(new GraphQLNonNull(GraphQLString)))
                .build();

        GraphQLInputObjectType flexibleAreaInput = newInputObject(groupOfEntitiesInputType)
                .name("FlexibleAreaInput")
                .field(newInputObjectField().name(FIELD_POLYGON).type(new GraphQLNonNull(geoJSONInputType)))
                .build();
        GraphQLInputObjectType hailAndRideAreaInput = newInputObject(groupOfEntitiesInputType)
                .name("HailAndRideAreaInput")
                .field(newInputObjectField().name(FIELD_START_QUAY_REF).type(new GraphQLNonNull(GraphQLString)))
                .field(newInputObjectField().name(FIELD_END_QUAY_REF).type(new GraphQLNonNull(GraphQLString)))
                .build();

        GraphQLInputObjectType keyValuesInputType = newInputObject().name("KeyValuesInput")
                .field(newInputObjectField().name(FIELD_KEY).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_VALUES).type(new GraphQLList(GraphQLString)))
                .build();

        GraphQLInputObjectType flexibleStopPlaceInputType = newInputObject(groupOfEntitiesInputType)
                .name("FlexibleStopPlaceInput")
                .field(newInputObjectField().name(FIELD_TRANSPORT_MODE).type(new GraphQLNonNull(vehicleModeEnum)))
                .field(newInputObjectField().name(FIELD_FLEXIBLE_AREA).type(flexibleAreaInput))
                .field(newInputObjectField().name(FIELD_HAIL_AND_RIDE_AREA).type(hailAndRideAreaInput))
                .field(newInputObjectField().name(FIELD_KEY_VALUES).type(new GraphQLList(keyValuesInputType)))
                .build();

        GraphQLInputObjectType contactInputType = newInputObject(groupOfEntitiesInputType).name("ContactInput")
                .field(newInputObjectField().name(FIELD_CONTACT_PERSON).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_PHONE).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_EMAIL).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_URL).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_FURTHER_DETAILS).type(GraphQLString))
                .build();

        GraphQLInputObjectType bookingArrangementInputType = newInputObject(groupOfEntitiesInputType).name("BookingArrangementInput")
                .field(newInputObjectField().name(FIELD_BOOKING_CONTACT).type(contactInputType))
                .field(newInputObjectField().name(FIELD_BOOKING_NOTE).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_BOOKING_METHODS).type(new GraphQLList(bookingMethodEnum)))
                .field(newInputObjectField().name(FIELD_BOOKING_ACCESS).type(bookingAccessEnum))
                .field(newInputObjectField().name(FIELD_BOOK_WHEN).type(purchaseWhenEnum))
                .field(newInputObjectField().name(FIELD_BUY_WHEN).type(new GraphQLList(purchaseMomentEnum)))
                .field(newInputObjectField().name(FIELD_LATEST_BOOKING_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newInputObjectField().name(FIELD_MINIMUM_BOOKING_PERIOD).type(DurationScalar.getDurationScalar()))
                .build();

        GraphQLInputObjectType destinationDisplayInputType = newInputObject().name("DestinationDisplayInput")
                .field(newInputObjectField().name(FIELD_FRONT_TEXT).type(GraphQLString))
                .build();

        GraphQLInputObjectType operatingPeriodInputType = newInputObject().name("OperatingPeriodInput")
                .field(newInputObjectField().name(FIELD_FROM_DATE).type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar())))
                .field(newInputObjectField().name(FIELD_TO_DATE).type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar())))
                .build();

        GraphQLInputObjectType dayTypeAssignmentInputType = newInputObject().name("DayTypeAssignmentInput")
                .field(newInputObjectField().name(FIELD_IS_AVAILABLE).type(GraphQLBoolean))
                .field(newInputObjectField().name(FIELD_DATE).type(DateScalar.getGraphQLDateScalar()))
                .field(newInputObjectField().name(FIELD_OPERATING_PERIOD).type(operatingPeriodInputType))
                .build();

        GraphQLInputObjectType dayTypeInputType = newInputObject(identifiedEntityInputType).name("DayTypeInput")
                .field(newInputObjectField().name(FIELD_DAYS_OF_WEEK).type(new GraphQLList(dayOfWeekEnum)))
                .field(newInputObjectField().name(FIELD_DAY_TYPE_ASSIGNMENTS).type(new GraphQLNonNull(new GraphQLList(dayTypeAssignmentInputType))))
                .build();

        GraphQLInputObjectType noticeInputType = newInputObject(identifiedEntityInputType).name("NoticeInput")
                .field(newInputObjectField().name(FIELD_TEXT).type(new GraphQLNonNull(GraphQLString)))
                .build();

        GraphQLInputObjectType timetabledPassingTimeInputType = newInputObject(groupOfEntitiesInputType).name("TimetabledPassingTimeInput")
                .field(newInputObjectField().name(FIELD_ARRIVAL_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newInputObjectField().name(FIELD_ARRIVAL_DAY_OFFSET).type(GraphQLInt))
                .field(newInputObjectField().name(FIELD_DEPARTURE_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newInputObjectField().name(FIELD_DEPARTURE_DAY_OFFSET).type(GraphQLInt))
                .field(newInputObjectField().name(FIELD_LATEST_ARRIVAL_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newInputObjectField().name(FIELD_LATEST_ARRIVAL_DAY_OFFSET).type(GraphQLInt))
                .field(newInputObjectField().name(FIELD_EARLIEST_DEPARTURE_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                .field(newInputObjectField().name(FIELD_EARLIEST_DEPARTURE_DAY_OFFSET).type(GraphQLInt))
                .field(newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType)))
                .build();

        GraphQLInputObjectType serviceJourneyInputType = newInputObject(groupOfEntitiesInputType).name("ServiceJourneyInput")
                .field(newInputObjectField().name(FIELD_PUBLIC_CODE).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_OPERATOR_REF).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementInputType))
                .field(newInputObjectField().name(FIELD_PASSING_TIMES).type(new GraphQLNonNull(new GraphQLList(timetabledPassingTimeInputType))))
                .field(newInputObjectField().name(FIELD_DAY_TYPES).type(new GraphQLList(dayTypeInputType)))
                .field(newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType)))
                .build();

        GraphQLInputObjectType stopPointInJourneyPatternInputType = newInputObject(groupOfEntitiesInputType).name("StopPointInJourneyPatternInput")
                .field(newInputObjectField().name(FIELD_FLEXIBLE_STOP_PLACE_REF).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_QUAY_REF).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementInputType))
                .field(newInputObjectField().name(FIELD_DESTINATION_DISPLAY).type(destinationDisplayInputType))
                .field(newInputObjectField().name(FIELD_FOR_BOARDING).type(GraphQLBoolean))
                .field(newInputObjectField().name(FIELD_FOR_ALIGHTING).type(GraphQLBoolean))
                .field(newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType)))
                .build();

        GraphQLInputObjectType journeyPatternInputType = newInputObject(groupOfEntitiesInputType).name("JourneyPatternInput")
                .field(newInputObjectField().name(FIELD_DIRECTION_TYPE).type(directionTypeEnum))
                .field(newInputObjectField().name(FIELD_POINTS_IN_SEQUENCE).type(new GraphQLNonNull(new GraphQLList(stopPointInJourneyPatternInputType))))
                .field(newInputObjectField().name(FIELD_SERVICE_JOURNEYS).type(new GraphQLNonNull(new GraphQLList(serviceJourneyInputType))))
                .field(newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType)))
                .build();

        GraphQLInputObjectType lineInputType = newInputObject(groupOfEntitiesInputType).name("LineInput")
                .field(newInputObjectField().name(FIELD_PUBLIC_CODE).type(new GraphQLNonNull(GraphQLString)))
                .field(newInputObjectField().name(FIELD_TRANSPORT_MODE).type(new GraphQLNonNull(vehicleModeEnum)))
                .field(newInputObjectField().name(FIELD_TRANSPORT_SUBMODE).type(new GraphQLNonNull(vehicleSubmodeEnum)))
                .field(newInputObjectField().name(FIELD_NETWORK_REF).type(new GraphQLNonNull(GraphQLString)))
                .field(newInputObjectField().name(FIELD_OPERATOR_REF).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_JOURNEY_PATTERNS).type(new GraphQLNonNull(new GraphQLList(journeyPatternInputType))))
                .field(newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType)))
                .build();

        GraphQLInputObjectType fixedLineInputType = newInputObject(lineInputType).name("FixedLineInput")
                .build();

        GraphQLInputObjectType flexibleLineInputType = newInputObject(lineInputType).name("FlexibleLineInput")
                .field(newInputObjectField().name(FIELD_FLEXIBLE_LINE_TYPE).type(new GraphQLNonNull(flexibleLineTypeEnum)))
                .field(newInputObjectField().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementInputType))
                .build();

        GraphQLInputObjectType exportLineAssociationInputType = newInputObject().name("ExportLineAssociationInput")
                .field(newInputObjectField().name(FIELD_LINE_REF).type(new GraphQLNonNull(GraphQLString)))
                .build();

        GraphQLInputObjectType exportInputType = newInputObject().name("ExportInput")
                .field(newInputObjectField().name(FIELD_NAME).type(GraphQLString))
                .field(newInputObjectField().name(FIELD_DRY_RUN).type(GraphQLBoolean).defaultValue(Boolean.FALSE))
                .field(newInputObjectField().name(FIELD_EXPORT_LINE_ASSOCIATIONS).type(new GraphQLList(exportLineAssociationInputType)))
                .build();

        return newObject()
                .name("Mutations")
                .description("Create and edit FlexibleLine timetable data")
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(networkObjectType))
                        .name("mutateNetwork")
                        .description("Create new or update existing network")
                        .argument(GraphQLArgument.newArgument()
                                .name(FIELD_INPUT)
                                .type(networkInputType))
                        .dataFetcher(networkUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(networkObjectType))
                        .name("deleteNetwork")
                        .description("Delete an existing network")
                        .argument(idArgument)
                        .dataFetcher(networkUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(lineObjectType))
                        .name("mutateLine")
                        .description("Create new or update existing line")
                        .argument(GraphQLArgument.newArgument()
                                .name(FIELD_INPUT)
                                .type(lineInputType))
                        .dataFetcher(fixedLineUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(lineObjectType))
                        .name("deleteLine")
                        .description("Delete an existing line")
                        .argument(idArgument)
                        .dataFetcher(fixedLineUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(fixedLineObjectType))
                        .name("mutateFixedLine")
                        .description("Create new or update existing fixedLine")
                        .deprecate("Use 'mutateLine' instead")
                        .argument(GraphQLArgument.newArgument()
                                .name(FIELD_INPUT)
                                .type(fixedLineInputType))
                        .dataFetcher(fixedLineUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(fixedLineObjectType))
                        .name("deleteFixedLine")
                        .description("Delete an existing fixedLine")
                        .deprecate("Use 'deleteLine' instead")
                        .argument(idArgument)
                        .dataFetcher(fixedLineUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(flexibleLineObjectType))
                        .name("mutateFlexibleLine")
                        .description("Create new or update existing flexibleLine")
                        .argument(GraphQLArgument.newArgument()
                                .name(FIELD_INPUT)
                                .type(flexibleLineInputType))
                        .dataFetcher(flexibleLineUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(flexibleLineObjectType))
                        .name("deleteFlexibleLine")
                        .description("Delete an existing flexibleLine")
                        .argument(idArgument)
                        .dataFetcher(flexibleLineUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(flexibleStopPlaceObjectType))
                        .name("mutateFlexibleStopPlace")
                        .description("Create new or update existing flexibleStopPlace")
                        .argument(GraphQLArgument.newArgument()
                                .name(FIELD_INPUT)
                                .type(flexibleStopPlaceInputType))
                        .dataFetcher(flexibleStopPlaceUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(flexibleStopPlaceObjectType))
                        .name("deleteFlexibleStopPlace")
                        .description("Delete an existing flexibleStopPlace")
                        .argument(idArgument)
                        .dataFetcher(flexibleStopPlaceUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(exportObjectType))
                        .name("export")
                        .description("Start a new export")
                        .argument(GraphQLArgument.newArgument()
                                .name(FIELD_INPUT)
                                .type(exportInputType))
                        .dataFetcher(exportUpdater))
                .field(newFieldDefinition()
                        .type(new GraphQLNonNull(GraphQLString))
                        .name("cleanDataSpace")
                        .description("Delete all data in provider data space!")
                        .dataFetcher(env -> {
                            dataSpaceCleaner.clean();
                            return "OK";
                        }))

                .build();
    }
}
