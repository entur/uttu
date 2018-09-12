package no.entur.uttu.graphql;

import com.vividsolutions.jts.geom.Geometry;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import no.entur.uttu.graphql.scalars.DateScalar;
import no.entur.uttu.graphql.scalars.DateTimeScalar;
import no.entur.uttu.graphql.scalars.DurationScalar;
import no.entur.uttu.graphql.scalars.GeoJSONCoordinatesScalar;
import no.entur.uttu.graphql.scalars.LocalTimeScalar;
import no.entur.uttu.model.BookingAccessEnumeration;
import no.entur.uttu.model.BookingMethodEnumeration;
import no.entur.uttu.model.DirectionTypeEnumeration;
import no.entur.uttu.model.FlexibleArea;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.FlexibleLineTypeEnumeration;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.model.PurchaseMomentEnumeration;
import no.entur.uttu.model.PurchaseWhenEnumeration;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.FlexibleStopPlaceRepository;
import no.entur.uttu.repository.NetworkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.function.Function;

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
public class FlexibleLinesGraphQLSchema {
    @Autowired
    private DateTimeScalar dateTimeScalar;

    @Autowired
    private DataFetcher<FlexibleStopPlace> flexibleStopPlaceUpdater;

    @Autowired
    private DataFetcher<FlexibleLine> flexibleLineUpdater;

    @Autowired
    private DataFetcher<Network> networkUpdater;


    @Autowired
    private FlexibleStopPlaceRepository flexibleStopPlaceRepository;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private FlexibleLineRepository flexibleLineRepository;

    public static GraphQLEnumType geometryTypeEnum = GraphQLEnumType.newEnum()
                                                             .name("GeometryType")
                                                             .value("Point")
                                                             .value("LineString")
                                                             .value("Polygon")
                                                             .value("MultiPoint")
                                                             .value("MultiLineString")
                                                             .value("MultiPolygon")
                                                             .value("GeometryCollection")
                                                             .build();

    private static GraphQLEnumType dayOfWeekEnum = FlexibleLinesGraphQLSchema.createEnum("DayOfWeekEnumeration", DayOfWeek.values(), (t -> t.name().toLowerCase()));
    private static GraphQLEnumType vehicleModeEnum = FlexibleLinesGraphQLSchema.createEnum("VehicleModeEnumeration", VehicleModeEnumeration.values(), (t -> t.value()));
    private static GraphQLEnumType flexibleLineTypeEnum = FlexibleLinesGraphQLSchema.createEnum("FlexibleLineTypeEnumeration", FlexibleLineTypeEnumeration.values(), (t -> t.value()));
    private static GraphQLEnumType bookingMethodEnum = FlexibleLinesGraphQLSchema.createEnum("BookingMethodEnumeration", BookingMethodEnumeration.values(), (t -> t.value()));
    private static GraphQLEnumType bookingAccessEnum = FlexibleLinesGraphQLSchema.createEnum("BookingAccessEnumeration", BookingAccessEnumeration.values(), (t -> t.value()));
    private static GraphQLEnumType purchaseWhenEnum = FlexibleLinesGraphQLSchema.createEnum("PurchaseWhenEnumeration", PurchaseWhenEnumeration.values(), (t -> t.value()));
    private static GraphQLEnumType purchaseMomentEnum = FlexibleLinesGraphQLSchema.createEnum("PurchaseMomentEnumeration", PurchaseMomentEnumeration.values(), (t -> t.value()));
    private static GraphQLEnumType directionTypeEnum = FlexibleLinesGraphQLSchema.createEnum("DirectionTypeEnumeration", DirectionTypeEnumeration.values(), (t -> t.value()));


    private static <T extends Enum> GraphQLEnumType createEnum(String name, T[] values, Function<T, String> mapping) {
        GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum().name(name);
        Arrays.stream(values).forEach(type -> enumBuilder.value(mapping.apply(type), type));
        return enumBuilder.build();
    }


    private GraphQLObjectType geoJSONObjectType;
    private GraphQLObjectType identifiedEntityObjectType;
    private GraphQLObjectType groupOfEntitiesObjectType;
    private GraphQLObjectType flexibleLineObjectType;
    private GraphQLObjectType flexibleStopPlaceObjectType;
    private GraphQLObjectType flexibleAreaObjectType;

    private GraphQLObjectType networkObjectType;

    private GraphQLObjectType journeyPatternObjectType;
    private GraphQLObjectType serviceJourneyObjectType;
    private GraphQLObjectType stopPointInJourneyPatternObjectType;
    private GraphQLObjectType timetabledPassingTimeObjectType;
    private GraphQLObjectType destinationDisplayObjectType;
    private GraphQLObjectType noticeObjectType;
    private GraphQLObjectType dayTypeObjectType;
    private GraphQLObjectType dayTypeAssignmentObjectType;
    private GraphQLObjectType operatingPeriod;

    private GraphQLObjectType bookingArrangementObjectType;
    private GraphQLObjectType contactObjectType;
    public GraphQLSchema graphQLSchema;

    @PostConstruct
    public void init() {
        initCommonTypes();
        graphQLSchema = GraphQLSchema.newSchema()
                                .query(createQueryObject())
                                .mutation(createMutationObject())
                                .build();
    }


    private void initCommonTypes() {
        GraphQLFieldDefinition idFieldDefinition = newFieldDefinition()
                                                           .name(FIELD_ID)
                                                           .type(new GraphQLNonNull(GraphQLString))
                                                           .dataFetcher(env -> ((ProviderEntity) env.getSource()).getNetexId())
                                                           .build();

        GraphQLFieldDefinition versionField = newFieldDefinition()
                                                      .name(FIELD_VERSION)
                                                      .type(new GraphQLNonNull(GraphQLString))
                                                      .build();

        identifiedEntityObjectType = newObject().name("IdentifiedEntity")
                                             .field(idFieldDefinition)
                                             .field(versionField)
                                             .field(newFieldDefinition().name(FIELD_CREATED_BY).type(new GraphQLNonNull(GraphQLString)))
                                             .field(newFieldDefinition().name(FIELD_CREATED).type(new GraphQLNonNull(dateTimeScalar.getDateTimeScalar())))
                                             .field(newFieldDefinition().name(FIELD_CHANGED_BY).type(new GraphQLNonNull(GraphQLString)))
                                             .field(newFieldDefinition().name(FIELD_CHANGED).type(new GraphQLNonNull(dateTimeScalar.getDateTimeScalar())))
                                             .build();


        geoJSONObjectType = newObject()
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

        identifiedEntityObjectType = newObject().name("IdentifiedEntity")
                                             .field(idFieldDefinition)
                                             .field(versionField)
                                             .field(newFieldDefinition().name(FIELD_CREATED_BY).type(new GraphQLNonNull(GraphQLString)))
                                             .field(newFieldDefinition().name(FIELD_CREATED).type(new GraphQLNonNull(dateTimeScalar.getDateTimeScalar())))
                                             .field(newFieldDefinition().name(FIELD_CHANGED_BY).type(new GraphQLNonNull(GraphQLString)))
                                             .field(newFieldDefinition().name(FIELD_CHANGED).type(new GraphQLNonNull(dateTimeScalar.getDateTimeScalar())))
                                             .build();


        groupOfEntitiesObjectType = newObject(identifiedEntityObjectType).name("GroupOfEntities")
                                            .field(newFieldDefinition().name(FIELD_NAME).type(GraphQLString))
                                            .field(newFieldDefinition().name(FIELD_DESCRIPTION).type(GraphQLString))
                                            .field(newFieldDefinition().name(FIELD_PRIVATE_CODE).type(GraphQLString))
                                            .build();


        networkObjectType = newObject(groupOfEntitiesObjectType).name("Network")
                                    .field(newFieldDefinition().name(FIELD_AUTHORITY_REF).type(new GraphQLNonNull(GraphQLLong)))
                                    .build();


        contactObjectType = newObject().name("Contact")
                                    .field(newFieldDefinition().name(FIELD_CONTACT_PERSON).type(GraphQLString))
                                    .field(newFieldDefinition().name(FIELD_PHONE).type(GraphQLString))
                                    .field(newFieldDefinition().name(FIELD_EMAIL).type(GraphQLString))
                                    .field(newFieldDefinition().name(FIELD_URL).type(GraphQLString))
                                    .field(newFieldDefinition().name(FIELD_FURTHER_DETAILS).type(GraphQLString))
                                    .build();

        bookingArrangementObjectType = newObject().name("BookingArrangement")
                                               .field(newFieldDefinition().name(FIELD_BOOKING_CONTACT).type(contactObjectType))
                                               .field(newFieldDefinition().name(FIELD_BOOKING_NOTE).type(GraphQLString))
                                               .field(newFieldDefinition().name(FIELD_BOOKING_METHODS).type(new GraphQLList(bookingMethodEnum)))
                                               .field(newFieldDefinition().name(FIELD_BOOKING_ACCESS).type(bookingAccessEnum))
                                               .field(newFieldDefinition().name(FIELD_BOOK_WHEN).type(purchaseWhenEnum))
                                               .field(newFieldDefinition().name(FIELD_BUY_WHEN).type(new GraphQLList(purchaseMomentEnum)))
                                               .field(newFieldDefinition().name(FIELD_LATEST_BOOKING_TIME).type(LocalTimeScalar.getLocalTimeScalar()))
                                               .field(newFieldDefinition().name(FIELD_MINIMUM_BOOKING_PERIOD).type(DurationScalar.getDurationScalar()))
                                               .build();


        destinationDisplayObjectType = newObject(identifiedEntityObjectType).name("DestinationDisplay")
                                               .field(newFieldDefinition().name(FIELD_FRONT_TEXT).type(new GraphQLNonNull(GraphQLString)))
                                               .build();

        noticeObjectType = newObject(identifiedEntityObjectType).name("Notice")
                                   .field(newFieldDefinition().name(FIELD_TEXT).type(new GraphQLNonNull(GraphQLString)))
                                   .build();

        flexibleAreaObjectType = newObject().name("FlexibleArea")
                                         .field(newFieldDefinition().name(FIELD_POLYGON).type(new GraphQLNonNull(geoJSONObjectType))
                                                        .dataFetcher(env -> ((FlexibleArea) env.getSource()).getPolygon()))
                                         .field(newFieldDefinition().name(FIELD_TRANSPORT_MODE).type(vehicleModeEnum))
                                         .build();

        flexibleStopPlaceObjectType = newObject(groupOfEntitiesObjectType).name("FlexibleStopPlace")
                                              .field(newFieldDefinition().name(FIELD_TRANSPORT_MODE).type(vehicleModeEnum))
                                              .field(newFieldDefinition().name(FIELD_FLEXIBLE_AREA).type(new GraphQLNonNull(flexibleAreaObjectType)))
                                              .build();

        operatingPeriod = newObject().name("OperatingPeriod")
                                  .field(newFieldDefinition().name(FIELD_FROM_DATE).type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar())))
                                  .field(newFieldDefinition().name(FIELD_TO_DATE).type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar())))
                                  .build();


        dayTypeAssignmentObjectType = newObject().name("DayTypeAssignment")
                                              .field(newFieldDefinition().name(FIELD_IS_AVAILABLE).type(GraphQLBoolean))
                                              .field(newFieldDefinition().name(FIELD_DATE).type(DateScalar.getGraphQLDateScalar()))
                                              .field(newFieldDefinition().name(FIELD_OPERATING_PERIOD).type(operatingPeriod))
                                              .build();


        dayTypeObjectType = newObject(identifiedEntityObjectType).name("DayType")
                                    .field(newFieldDefinition().name(FIELD_DAYS_OF_WEEK).type(new GraphQLList(dayOfWeekEnum)))
                                    .field(newFieldDefinition().name(FIELD_DAY_TYPE_ASSIGNMENTS).type(new GraphQLNonNull(new GraphQLList(dayTypeAssignmentObjectType))))
                                    .build();


        timetabledPassingTimeObjectType = newObject(identifiedEntityObjectType).name("TimetabledPassingTime")
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


        serviceJourneyObjectType = newObject(groupOfEntitiesObjectType).name("ServiceJourney")
                                           .field(newFieldDefinition().name(FIELD_PUBLIC_CODE).type(GraphQLString))
                                           .field(newFieldDefinition().name(FIELD_OPERATOR_REF).type(GraphQLLong))
                                           .field(newFieldDefinition().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementObjectType))
                                           .field(newFieldDefinition().name(FIELD_POINTS_IN_SEQUENCE).type(new GraphQLNonNull(new GraphQLList(timetabledPassingTimeObjectType))))
                                           .field(newFieldDefinition().name(FIELD_DAY_TYPES).type(dayTypeObjectType))
                                           .field(newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType)))
                                           .build();


        stopPointInJourneyPatternObjectType = newObject(identifiedEntityObjectType).name("StopPointInJourneyPattern")
                                                      .field(newFieldDefinition().name(FIELD_FLEXIBLE_STOP_PLACE).type(flexibleStopPlaceObjectType))
                                                      .field(newFieldDefinition().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementObjectType))
                                                      .field(newFieldDefinition().name(FIELD_DESTINATION_DISPLAY).type(destinationDisplayObjectType))
                                                      .field(newFieldDefinition().name(FIELD_FOR_BOARDING).type(GraphQLBoolean))
                                                      .field(newFieldDefinition().name(FIELD_FOR_ALIGHTING).type(GraphQLBoolean))
                                                      .field(newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType)))
                                                      .build();

        journeyPatternObjectType = newObject(groupOfEntitiesObjectType).name("JourneyPattern")
                                           .field(newFieldDefinition().name(FIELD_DIRECTION_TYPE).type(directionTypeEnum))
                                           .field(newFieldDefinition().name(FIELD_POINTS_IN_SEQUENCE).type(new GraphQLNonNull(new GraphQLList(stopPointInJourneyPatternObjectType))))
                                           .field(newFieldDefinition().name(FIELD_SERVICE_JOURNEYS).type(new GraphQLNonNull(new GraphQLList(serviceJourneyObjectType))))
                                           .field(newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType)))
                                           .build();


        flexibleLineObjectType = newObject(groupOfEntitiesObjectType).name("FlexibleLine")
                                         .field(newFieldDefinition().name(FIELD_PUBLIC_CODE).type(new GraphQLNonNull(GraphQLString)))
                                         .field(newFieldDefinition().name(FIELD_TRANSPORT_MODE).type(new GraphQLNonNull(vehicleModeEnum)))
                                         .field(newFieldDefinition().name(FIELD_FLEXIBLE_LINE_TYPE).type(new GraphQLNonNull(flexibleLineTypeEnum)))
                                         .field(newFieldDefinition().name(FIELD_NETWORK).type(new GraphQLNonNull(networkObjectType)))
                                         .field(newFieldDefinition().name(FIELD_OPERATOR_REF).type(GraphQLLong))
                                         .field(newFieldDefinition().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementObjectType))
                                         .field(newFieldDefinition().name(FIELD_JOURNEY_PATTERNS).type(new GraphQLNonNull(new GraphQLList(journeyPatternObjectType))))
                                         .field(newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType)))
                                         .build();


    }

    private GraphQLObjectType createQueryObject() {

        GraphQLObjectType queryType = newObject()
                                              .name("Queries")
                                              .description("Query and search for data")
                                              .field(newFieldDefinition()
                                                             .type(new GraphQLList(flexibleLineObjectType))
                                                             .name("flexibleLines")
                                                             .description("Search for FlexibleLines")
                                                             .dataFetcher(env -> flexibleLineRepository.findAll()))
                                              .field(newFieldDefinition()
                                                             .type(new GraphQLList(flexibleStopPlaceObjectType))
                                                             .name("flexibleStopPlaces")
                                                             .description("Search for FlexibleStopPlaces")
                                                             .dataFetcher(env -> flexibleStopPlaceRepository.findAll()))
                                              .field(newFieldDefinition()
                                                             .type(new GraphQLList(networkObjectType))
                                                             .name("networks")
                                                             .description("Search for Networks")
                                                             .dataFetcher(env -> networkRepository.findAll()))
                                              .build();

        return queryType;
    }

    private GraphQLObjectType createMutationObject() {


        GraphQLInputObjectType identifiedEntityInputType = newInputObject().name("IdentifiedEntityInput")
                                                                   .field(newInputObjectField().name(FIELD_ID).type(GraphQLString))
                                                                   .field(newInputObjectField().name(FIELD_VERSION).type(GraphQLLong))
                                                                   .field(newInputObjectField().name(FIELD_CREATED_BY).type(GraphQLString))
                                                                   .field(newInputObjectField().name(FIELD_CREATED).type(dateTimeScalar.getDateTimeScalar()))
                                                                   .field(newInputObjectField().name(FIELD_CHANGED_BY).type(GraphQLString))
                                                                   .field(newInputObjectField().name(FIELD_CHANGED).type(dateTimeScalar.getDateTimeScalar()))
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
                                                          .field(newInputObjectField().name(FIELD_AUTHORITY_REF).type(new GraphQLNonNull(GraphQLLong)))
                                                          .build();

        GraphQLInputObjectType flexibleAreaInput = newInputObject(groupOfEntitiesInputType)
                                                           .name("FlexibleAreaInput")
                                                           .field(newInputObjectField().name(FIELD_POLYGON).type(new GraphQLNonNull(geoJSONInputType)))
                                                           .build();

        GraphQLInputObjectType flexibleStopPlaceInputType = newInputObject(groupOfEntitiesInputType)
                                                                    .name("FlexibleStopPlaceInput")
                                                                    .field(newInputObjectField().name(FIELD_FLEXIBLE_AREA).type(new GraphQLNonNull(flexibleAreaInput)))
                                                                    .field(newInputObjectField().name(FIELD_TRANSPORT_MODE).type(new GraphQLNonNull(vehicleModeEnum)))
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
                                                                     .field(newInputObjectField().name(FIELD_MINIMUM_BOOKING_PERIOD).type(DurationScalar.getDurationScalar()))
                                                                     .build();


        GraphQLInputObjectType destinationDisplayInputType = newInputObject().name("DestinationDisplayInput")
                                                                     .field(newInputObjectField().name(FIELD_FRONT_TEXT).type(GraphQLString))
                                                                     .build();


        GraphQLInputObjectType operatingPeriod = newInputObject().name("OperatingPeriodInput")
                                                         .field(newInputObjectField().name(FIELD_FROM_DATE).type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar())))
                                                         .field(newInputObjectField().name(FIELD_TO_DATE).type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar())))
                                                         .build();


        GraphQLInputObjectType dayTypeAssignmentInputType = newInputObject().name("DayTypeAssignmentInput")
                                                                    .field(newInputObjectField().name(FIELD_IS_AVAILABLE).type(GraphQLBoolean))
                                                                    .field(newInputObjectField().name(FIELD_DATE).type(DateScalar.getGraphQLDateScalar()))
                                                                    .field(newInputObjectField().name(FIELD_OPERATING_PERIOD).type(operatingPeriod))
                                                                    .build();


        GraphQLInputObjectType dayTypeInputType = newInputObject(groupOfEntitiesInputType).name("DayTypeInput")
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
                                                                 .field(newInputObjectField().name(FIELD_OPERATOR_REF).type(GraphQLLong))
                                                                 .field(newInputObjectField().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementInputType))
                                                                 .field(newInputObjectField().name(FIELD_POINTS_IN_SEQUENCE).type(new GraphQLNonNull(new GraphQLList(timetabledPassingTimeInputType))))
                                                                 .field(newInputObjectField().name(FIELD_DAY_TYPES).type(new GraphQLList(dayTypeInputType)))
                                                                 .field(newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType)))
                                                                 .build();


        GraphQLInputObjectType stopPointInJourneyPatternInputType = newInputObject(groupOfEntitiesInputType).name("StopPointInJourneyPatternInput")
                                                                            .field(newInputObjectField().name(FIELD_FLEXIBLE_STOP_PLACE_REF).type(new GraphQLNonNull(GraphQLString)))
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


        GraphQLInputObjectType flexibleLineInputType = newInputObject(groupOfEntitiesInputType).name("FlexibleLineInput")
                                                               .field(newInputObjectField().name(FIELD_PUBLIC_CODE).type(new GraphQLNonNull(GraphQLString)))
                                                               .field(newInputObjectField().name(FIELD_TRANSPORT_MODE).type(new GraphQLNonNull(vehicleModeEnum)))
                                                               .field(newInputObjectField().name(FIELD_FLEXIBLE_LINE_TYPE).type(new GraphQLNonNull(flexibleLineTypeEnum)))
                                                               .field(newInputObjectField().name(FIELD_NETWORK_REF).type(new GraphQLNonNull(GraphQLString)))
                                                               .field(newInputObjectField().name(FIELD_OPERATOR_REF).type(GraphQLLong))
                                                               .field(newInputObjectField().name(FIELD_BOOKING_ARRANGEMENT).type(bookingArrangementInputType))
                                                               .field(newInputObjectField().name(FIELD_JOURNEY_PATTERNS).type(new GraphQLNonNull(new GraphQLList(journeyPatternInputType))))
                                                               .field(newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType)))
                                                               .build();


        GraphQLObjectType mutationType = newObject()
                                                 .name("Mutations")
                                                 .description("Create and edit FlexibleLine timetable data")
                                                 .field(newFieldDefinition()
                                                                .type(new GraphQLNonNull(networkObjectType))
                                                                .name("mutateNetwork")
                                                                .description("Create new or update existing Network")
                                                                .argument(GraphQLArgument.newArgument()
                                                                                  .name("input")
                                                                                  .type(networkInputType))
                                                                .dataFetcher(networkUpdater))
                                                 .field(newFieldDefinition()
                                                                .type(new GraphQLNonNull(flexibleLineObjectType))
                                                                .name("mutateFlexibleLine")
                                                                .description("Create new or update existing FlexibleLine")
                                                                .argument(GraphQLArgument.newArgument()
                                                                                  .name("input")
                                                                                  .type(flexibleLineInputType))
                                                                .dataFetcher(flexibleLineUpdater))
                                                 .field(newFieldDefinition()
                                                                .type(new GraphQLNonNull(flexibleStopPlaceObjectType))
                                                                .name("mutateFlexibleStopPlace")
                                                                .description("Create new or update existing flexibleStopPlace")
                                                                .argument(GraphQLArgument.newArgument()
                                                                                  .name("input")
                                                                                  .type(flexibleStopPlaceInputType))
                                                                .dataFetcher(flexibleStopPlaceUpdater))

                                                 .build();

        return mutationType;
    }

}

