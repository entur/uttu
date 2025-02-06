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

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.scalars.ExtendedScalars.GraphQLBigDecimal;
import static graphql.scalars.ExtendedScalars.GraphQLLong;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;
import static no.entur.uttu.graphql.GraphQLNames.*;

import graphql.Scalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import no.entur.uttu.config.Context;
import no.entur.uttu.export.linestatistics.ExportedLineStatisticsService;
import no.entur.uttu.graphql.fetchers.DayTypeServiceJourneyCountFetcher;
import no.entur.uttu.graphql.fetchers.ExportedPublicLinesFetcher;
import no.entur.uttu.graphql.model.Organisation;
import no.entur.uttu.graphql.model.ServiceLink;
import no.entur.uttu.graphql.model.StopPlace;
import no.entur.uttu.graphql.scalars.DateScalar;
import no.entur.uttu.graphql.scalars.DateTimeScalar;
import no.entur.uttu.graphql.scalars.DurationScalar;
import no.entur.uttu.graphql.scalars.GeoJSONCoordinatesScalar;
import no.entur.uttu.graphql.scalars.LocalTimeScalar;
import no.entur.uttu.model.BookingAccessEnumeration;
import no.entur.uttu.model.BookingMethodEnumeration;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DayTypeAssignment;
import no.entur.uttu.model.DirectionTypeEnumeration;
import no.entur.uttu.model.ExportedLineStatistics;
import no.entur.uttu.model.FixedLine;
import no.entur.uttu.model.FlexibleArea;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.FlexibleLineTypeEnumeration;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.Network;
import no.entur.uttu.model.PurchaseMomentEnumeration;
import no.entur.uttu.model.PurchaseWhenEnumeration;
import no.entur.uttu.model.TimetabledPassingTime;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.model.VehicleSubmodeEnumeration;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.model.job.ExportStatusEnumeration;
import no.entur.uttu.model.job.SeverityEnumeration;
import no.entur.uttu.profile.Profile;
import no.entur.uttu.repository.DataSpaceCleaner;
import no.entur.uttu.repository.DayTypeRepository;
import no.entur.uttu.repository.ExportRepository;
import no.entur.uttu.repository.FixedLineRepository;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.FlexibleStopPlaceRepository;
import no.entur.uttu.repository.NetworkRepository;
import org.locationtech.jts.geom.Geometry;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
import org.rutebanken.netex.model.OrganisationTypeEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
  private DataFetcher<DayType> dayTypeUpdater;

  @Autowired
  private DataFetcher<List<DayType>> dayTypesBulkUpdater;

  @Autowired
  private DayTypeServiceJourneyCountFetcher dayTypeServiceJourneyCountFetcher;

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

  @Autowired
  private DayTypeRepository dayTypeRepository;

  @Autowired
  private DataFetcher<List<Organisation>> organisationsFetcher;

  @Autowired
  private DataFetcher<TimetabledPassingTime.StopPlace> quayRefSearchFetcher;

  @Autowired
  private DataFetcher<List<StopPlace>> stopPlacesFetcher;

  @Autowired
  private DataFetcher<ServiceLink> routingFetcher;

  private <T extends Enum> GraphQLEnumType createEnum(
    String name,
    T[] values,
    Function<T, String> mapping
  ) {
    return createEnum(name, Arrays.asList(values), mapping);
  }

  private <T extends Enum> GraphQLEnumType createEnum(
    String name,
    Collection<T> values,
    Function<T, String> mapping
  ) {
    GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum().name(name);
    values.forEach(type -> enumBuilder.value(mapping.apply(type), type));
    return enumBuilder.build();
  }

  private GraphQLEnumType geometryTypeEnum = GraphQLEnumType
    .newEnum()
    .name("GeometryType")
    .value("Point")
    .value("LineString")
    .value("Polygon")
    .value("MultiPoint")
    .value("MultiLineString")
    .value("MultiPolygon")
    .value("GeometryCollection")
    .build();

  private GraphQLEnumType dayOfWeekEnum = createEnum(
    "DayOfWeekEnumeration",
    DayOfWeek.values(),
    (t -> t.name().toLowerCase())
  );
  private GraphQLEnumType exportStatusEnum = createEnum(
    "ExportStatusEnumeration",
    ExportStatusEnumeration.values(),
    (t -> t.name().toLowerCase())
  );
  private GraphQLEnumType severityEnum = createEnum(
    "SeverityEnumeration",
    SeverityEnumeration.values(),
    (t -> t.name().toLowerCase())
  );

  private GraphQLEnumType vehicleModeEnum;
  private GraphQLEnumType vehicleSubmodeEnum;
  private GraphQLEnumType flexibleLineTypeEnum = createEnum(
    "FlexibleLineTypeEnumeration",
    FlexibleLineTypeEnumeration.values(),
    (FlexibleLineTypeEnumeration::value)
  );
  private GraphQLEnumType bookingMethodEnum = createEnum(
    "BookingMethodEnumeration",
    BookingMethodEnumeration.values(),
    (BookingMethodEnumeration::value)
  );
  private GraphQLEnumType bookingAccessEnum = createEnum(
    "BookingAccessEnumeration",
    BookingAccessEnumeration.values(),
    (BookingAccessEnumeration::value)
  );
  private GraphQLEnumType purchaseWhenEnum = createEnum(
    "PurchaseWhenEnumeration",
    PurchaseWhenEnumeration.values(),
    (PurchaseWhenEnumeration::value)
  );
  private GraphQLEnumType purchaseMomentEnum = createEnum(
    "PurchaseMomentEnumeration",
    PurchaseMomentEnumeration.values(),
    (PurchaseMomentEnumeration::value)
  );
  private GraphQLEnumType directionTypeEnum = createEnum(
    "DirectionTypeEnumeration",
    DirectionTypeEnumeration.values(),
    (DirectionTypeEnumeration::value)
  );
  private GraphQLEnumType transportModeEnum = createEnum(
    "TransportModeEnumeration",
    AllVehicleModesOfTransportEnumeration.values(),
    (AllVehicleModesOfTransportEnumeration::value)
  );

  private GraphQLEnumType organisationTypeEnum = createEnum(
    "OrganisationType",
    OrganisationTypeEnumeration.values(),
    (OrganisationTypeEnumeration::value)
  );

  private GraphQLObjectType lineObjectType;
  private GraphQLObjectType fixedLineObjectType;
  private GraphQLObjectType flexibleLineObjectType;
  private GraphQLObjectType dayTypeObjectType;
  private GraphQLObjectType flexibleStopPlaceObjectType;
  private GraphQLObjectType networkObjectType;
  private GraphQLObjectType exportObjectType;
  private GraphQLObjectType exportedLineStatisticsObjectType;

  private GraphQLObjectType stopPlaceObjectType;

  private GraphQLObjectType organisationObjectType;
  private GraphQLObjectType routeGeometryObjectType;
  private GraphQLObjectType serviceLinkObjectType;

  private GraphQLArgument idArgument;
  private GraphQLArgument idsArgument;
  private GraphQLArgument providerArgument;
  private GraphQLSchema graphQLSchema;

  @PostConstruct
  public void init() {
    vehicleModeEnum =
      createEnum(
        "VehicleModeEnumeration",
        profile.getLegalVehicleModes(),
        (VehicleModeEnumeration::value)
      );
    vehicleSubmodeEnum =
      createEnum(
        "VehicleSubmodeEnumeration",
        profile.getLegalVehicleSubmodes(),
        (VehicleSubmodeEnumeration::value)
      );

    initCommonTypes();

    graphQLSchema =
      GraphQLSchema
        .newSchema()
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
      .build();

    GraphQLFieldDefinition versionField = newFieldDefinition()
      .name(FIELD_VERSION)
      .type(new GraphQLNonNull(GraphQLString))
      .build();

    idArgument =
      GraphQLArgument
        .newArgument()
        .name(FIELD_ID)
        .type(new GraphQLNonNull(GraphQLID))
        .description("Id for entity")
        .build();

    idsArgument =
      GraphQLArgument
        .newArgument()
        .name(FIELD_IDS)
        .type(new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(GraphQLID))))
        .description("Ids for entities")
        .build();

    providerArgument =
      GraphQLArgument
        .newArgument()
        .name(FIELD_PROVIDER_CODE)
        .type(GraphQLID)
        .description("Provider code, f.eks 'rut' for Ruter.")
        .build();

    GraphQLObjectType geoJSONObjectType = newObject()
      .name("GeoJSON")
      .description(
        "Geometry-object as specified in the GeoJSON-standard (http://geojson.org/geojson-spec.html)."
      )
      .field(
        newFieldDefinition()
          .name("type")
          .type(new GraphQLNonNull(geometryTypeEnum))
          .dataFetcher(env -> {
            if (env.getSource() instanceof Geometry) {
              return env.getSource().getClass().getSimpleName();
            }
            return null;
          })
      )
      .field(
        newFieldDefinition()
          .name("coordinates")
          .type(
            new GraphQLNonNull(
              GeoJSONCoordinatesScalar.getGraphQGeoJSONCoordinatesScalar()
            )
          )
      )
      .build();

    GraphQLObjectType keyValuesObjectType = newObject()
      .name("KeyValues")
      .field(newFieldDefinition().name(FIELD_KEY).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_VALUES).type(new GraphQLList(GraphQLString)))
      .build();

    GraphQLObjectType identifiedEntityObjectType = newObject()
      .name("IdentifiedEntity")
      .field(idFieldDefinition)
      .field(versionField)
      .field(
        newFieldDefinition()
          .name(FIELD_CREATED_BY)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newFieldDefinition()
          .name(FIELD_CREATED)
          .type(new GraphQLNonNull(dateTimeScalar.getDateTimeScalar()))
      )
      .field(
        newFieldDefinition()
          .name(FIELD_CHANGED_BY)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newFieldDefinition()
          .name(FIELD_CHANGED)
          .type(new GraphQLNonNull(dateTimeScalar.getDateTimeScalar()))
      )
      .build();

    GraphQLObjectType groupOfEntitiesObjectType = newObject(identifiedEntityObjectType)
      .name("GroupOfEntities")
      .field(newFieldDefinition().name(FIELD_NAME).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_DESCRIPTION).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_PRIVATE_CODE).type(GraphQLString))
      .build();

    networkObjectType =
      newObject(groupOfEntitiesObjectType)
        .name("Network")
        .field(
          newFieldDefinition()
            .name(FIELD_AUTHORITY_REF)
            .type(new GraphQLNonNull(GraphQLString))
        )
        .build();

    GraphQLObjectType contactObjectType = newObject()
      .name("Contact")
      .field(newFieldDefinition().name(FIELD_CONTACT_PERSON).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_PHONE).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_EMAIL).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_URL).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_FURTHER_DETAILS).type(GraphQLString))
      .build();

    GraphQLObjectType bookingArrangementObjectType = newObject()
      .name("BookingArrangement")
      .field(newFieldDefinition().name(FIELD_BOOKING_CONTACT).type(contactObjectType))
      .field(newFieldDefinition().name(FIELD_BOOKING_NOTE).type(GraphQLString))
      .field(
        newFieldDefinition()
          .name(FIELD_BOOKING_METHODS)
          .type(new GraphQLList(bookingMethodEnum))
      )
      .field(newFieldDefinition().name(FIELD_BOOKING_ACCESS).type(bookingAccessEnum))
      .field(newFieldDefinition().name(FIELD_BOOK_WHEN).type(purchaseWhenEnum))
      .field(
        newFieldDefinition()
          .name(FIELD_BUY_WHEN)
          .type(new GraphQLList(purchaseMomentEnum))
      )
      .field(
        newFieldDefinition()
          .name(FIELD_LATEST_BOOKING_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(
        newFieldDefinition()
          .name(FIELD_MINIMUM_BOOKING_PERIOD)
          .type(DurationScalar.getDurationScalar())
      )
      .build();

    GraphQLObjectType destinationDisplayObjectType = newObject(identifiedEntityObjectType)
      .name("DestinationDisplay")
      .field(
        newFieldDefinition()
          .name(FIELD_FRONT_TEXT)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    GraphQLObjectType noticeObjectType = newObject(identifiedEntityObjectType)
      .name("Notice")
      .field(
        newFieldDefinition().name(FIELD_TEXT).type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    GraphQLObjectType flexibleAreaObjectType = newObject()
      .name("FlexibleArea")
      .field(
        newFieldDefinition()
          .name(FIELD_KEY_VALUES)
          .type(new GraphQLList(keyValuesObjectType))
          .dataFetcher(env ->
            ((FlexibleArea) env.getSource()).getKeyValues()
              .entrySet()
              .stream()
              .map(entry -> new KeyValuesWrapper(entry.getKey(), entry.getValue()))
              .collect(Collectors.toList())
          )
      )
      .field(
        newFieldDefinition()
          .name(FIELD_POLYGON)
          .type(new GraphQLNonNull(geoJSONObjectType))
          .dataFetcher(env -> ((FlexibleArea) env.getSource()).getPolygon())
      )
      .build();

    GraphQLObjectType hailAndRideAreaType = newObject()
      .name("HailAndRideArea")
      .field(
        newFieldDefinition()
          .name(FIELD_START_QUAY_REF)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newFieldDefinition()
          .name(FIELD_END_QUAY_REF)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    flexibleStopPlaceObjectType =
      newObject(groupOfEntitiesObjectType)
        .name("FlexibleStopPlace")
        .field(newFieldDefinition().name(FIELD_TRANSPORT_MODE).type(vehicleModeEnum))
        .field(
          newFieldDefinition()
            .name(FIELD_FLEXIBLE_AREA)
            .type(flexibleAreaObjectType)
            .deprecate("Use 'flexibleAreas' instead")
        )
        .field(
          newFieldDefinition()
            .name(FIELD_FLEXIBLE_AREAS)
            .type(new GraphQLList(flexibleAreaObjectType))
        )
        .field(
          newFieldDefinition().name(FIELD_HAIL_AND_RIDE_AREA).type(hailAndRideAreaType)
        )
        .field(
          newFieldDefinition()
            .name(FIELD_KEY_VALUES)
            .type(new GraphQLList(keyValuesObjectType))
            .dataFetcher(env ->
              ((FlexibleStopPlace) env.getSource()).getKeyValues()
                .entrySet()
                .stream()
                .map(entry -> new KeyValuesWrapper(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList())
            )
        )
        .build();

    GraphQLObjectType operatingPeriod = newObject()
      .name("OperatingPeriod")
      .field(
        newFieldDefinition()
          .name(FIELD_FROM_DATE)
          .type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar()))
      )
      .field(
        newFieldDefinition()
          .name(FIELD_TO_DATE)
          .type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar()))
      )
      .build();

    GraphQLObjectType dayTypeAssignmentObjectType = newObject()
      .name("DayTypeAssignment")
      .field(
        newFieldDefinition()
          .name(FIELD_IS_AVAILABLE)
          .dataFetcher(env -> ((DayTypeAssignment) env.getSource()).getAvailable())
          .type(GraphQLBoolean)
      )
      .field(
        newFieldDefinition().name(FIELD_DATE).type(DateScalar.getGraphQLDateScalar())
      )
      .field(newFieldDefinition().name(FIELD_OPERATING_PERIOD).type(operatingPeriod))
      .build();

    dayTypeObjectType =
      newObject(identifiedEntityObjectType)
        .name("DayType")
        .field(
          newFieldDefinition()
            .name(FIELD_DAYS_OF_WEEK)
            .type(new GraphQLList(dayOfWeekEnum))
        )
        .field(
          newFieldDefinition()
            .name(FIELD_DAY_TYPE_ASSIGNMENTS)
            .type(new GraphQLNonNull(new GraphQLList(dayTypeAssignmentObjectType)))
        )
        .field(
          newFieldDefinition()
            .name(FIELD_NUMBER_OF_SERVICE_JOURNEYS)
            .dataFetcher(dayTypeServiceJourneyCountFetcher)
            .type(GraphQLLong)
        )
        .field(newFieldDefinition().name(FIELD_NAME).type(GraphQLString))
        .build();

    GraphQLObjectType timetabledPassingTimeObjectType = newObject(
      identifiedEntityObjectType
    )
      .name("TimetabledPassingTime")
      .field(
        newFieldDefinition()
          .name(FIELD_ARRIVAL_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(newFieldDefinition().name(FIELD_ARRIVAL_DAY_OFFSET).type(GraphQLInt))
      .field(
        newFieldDefinition()
          .name(FIELD_DEPARTURE_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(newFieldDefinition().name(FIELD_DEPARTURE_DAY_OFFSET).type(GraphQLInt))
      .field(
        newFieldDefinition()
          .name(FIELD_LATEST_ARRIVAL_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(newFieldDefinition().name(FIELD_LATEST_ARRIVAL_DAY_OFFSET).type(GraphQLInt))
      .field(
        newFieldDefinition()
          .name(FIELD_EARLIEST_DEPARTURE_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(
        newFieldDefinition().name(FIELD_EARLIEST_DEPARTURE_DAY_OFFSET).type(GraphQLInt)
      )
      .field(
        newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType))
      )
      .build();

    GraphQLObjectType serviceJourneyObjectType = newObject(groupOfEntitiesObjectType)
      .name("ServiceJourney")
      .field(newFieldDefinition().name(FIELD_PUBLIC_CODE).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_OPERATOR_REF).type(GraphQLString))
      .field(
        newFieldDefinition()
          .name(FIELD_BOOKING_ARRANGEMENT)
          .type(bookingArrangementObjectType)
      )
      .field(
        newFieldDefinition()
          .name(FIELD_PASSING_TIMES)
          .type(new GraphQLNonNull(new GraphQLList(timetabledPassingTimeObjectType)))
      )
      .field(
        newFieldDefinition()
          .name(FIELD_DAY_TYPES)
          .type(new GraphQLList(dayTypeObjectType))
      )
      .field(
        newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType))
      )
      .build();

    GraphQLObjectType stopPointInJourneyPatternObjectType = newObject(
      identifiedEntityObjectType
    )
      .name("StopPointInJourneyPattern")
      .field(
        newFieldDefinition()
          .name(FIELD_FLEXIBLE_STOP_PLACE)
          .type(flexibleStopPlaceObjectType)
      )
      .field(newFieldDefinition().name(FIELD_QUAY_REF).type(GraphQLString))
      .field(
        newFieldDefinition()
          .name(FIELD_BOOKING_ARRANGEMENT)
          .type(bookingArrangementObjectType)
      )
      .field(
        newFieldDefinition()
          .name(FIELD_DESTINATION_DISPLAY)
          .type(destinationDisplayObjectType)
      )
      .field(newFieldDefinition().name(FIELD_FOR_BOARDING).type(GraphQLBoolean))
      .field(newFieldDefinition().name(FIELD_FOR_ALIGHTING).type(GraphQLBoolean))
      .field(
        newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType))
      )
      .build();

    GraphQLObjectType journeyPatternObjectType = newObject(groupOfEntitiesObjectType)
      .name("JourneyPattern")
      .field(newFieldDefinition().name(FIELD_DIRECTION_TYPE).type(directionTypeEnum))
      .field(
        newFieldDefinition()
          .name(FIELD_POINTS_IN_SEQUENCE)
          .type(new GraphQLNonNull(new GraphQLList(stopPointInJourneyPatternObjectType)))
      )
      .field(
        newFieldDefinition()
          .name(FIELD_SERVICE_JOURNEYS)
          .type(new GraphQLNonNull(new GraphQLList(serviceJourneyObjectType)))
      )
      .field(
        newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType))
      )
      .build();

    lineObjectType =
      newObject(groupOfEntitiesObjectType)
        .name("Line")
        .field(
          newFieldDefinition()
            .name(FIELD_PUBLIC_CODE)
            .type(new GraphQLNonNull(GraphQLString))
        )
        .field(
          newFieldDefinition()
            .name(FIELD_TRANSPORT_MODE)
            .type(new GraphQLNonNull(vehicleModeEnum))
        )
        .field(
          newFieldDefinition()
            .name(FIELD_TRANSPORT_SUBMODE)
            .type(new GraphQLNonNull(vehicleSubmodeEnum))
        )
        .field(
          newFieldDefinition()
            .name(FIELD_NETWORK)
            .type(new GraphQLNonNull(networkObjectType))
        )
        .field(newFieldDefinition().name(FIELD_OPERATOR_REF).type(GraphQLString))
        .field(
          newFieldDefinition()
            .name(FIELD_JOURNEY_PATTERNS)
            .type(new GraphQLNonNull(new GraphQLList(journeyPatternObjectType)))
        )
        .field(
          newFieldDefinition().name(FIELD_NOTICES).type(new GraphQLList(noticeObjectType))
        )
        .build();

    fixedLineObjectType = newObject(lineObjectType).name("FixedLine").build();

    flexibleLineObjectType =
      newObject(lineObjectType)
        .name("FlexibleLine")
        .field(
          newFieldDefinition()
            .name(FIELD_FLEXIBLE_LINE_TYPE)
            .type(new GraphQLNonNull(flexibleLineTypeEnum))
        )
        .field(
          newFieldDefinition()
            .name(FIELD_BOOKING_ARRANGEMENT)
            .type(bookingArrangementObjectType)
        )
        .build();

    GraphQLObjectType exportMessageObjectType = newObject()
      .name("Message")
      .field(
        newFieldDefinition().name(FIELD_SEVERITY).type(new GraphQLNonNull(severityEnum))
      )
      .field(
        newFieldDefinition().name(FIELD_MESSAGE).type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    GraphQLObjectType exportLineAssociationObjectType = newObject()
      .name("ExportLineAssociation")
      .field(
        newFieldDefinition().name(FIELD_LINE).type(new GraphQLNonNull(lineObjectType))
      )
      .build();

    GraphQLObjectType exportedDayTypeObjectType = newObject()
      .name("ExportedDayType")
      .field(newFieldDefinition().name(FIELD_DAY_TYPE_NETEX_ID).type(GraphQLString))
      .field(
        newFieldDefinition()
          .name(FIELD_OPERATING_DATE_FROM)
          .type(DateScalar.getGraphQLDateScalar())
      )
      .field(
        newFieldDefinition()
          .name(FIELD_OPERATING_DATE_TO)
          .type(DateScalar.getGraphQLDateScalar())
      )
      .field(newFieldDefinition().name(FIELD_SERVICE_JOURNEY_NAME).type(GraphQLString))
      .build();

    GraphQLObjectType exportedLineObjectType = newObject()
      .name("ExportedLine")
      .field(newFieldDefinition().name(FIELD_LINE_NAME).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_LINE_TYPE).type(GraphQLString))
      .field(
        newFieldDefinition()
          .name(FIELD_OPERATING_DATE_FROM)
          .type(DateScalar.getGraphQLDateScalar())
      )
      .field(
        newFieldDefinition()
          .name(FIELD_OPERATING_DATE_TO)
          .type(DateScalar.getGraphQLDateScalar())
      )
      .field(
        newFieldDefinition()
          .name(FIELD_EXPORTED_DAY_TYPES_STATISTICS)
          .type(new GraphQLList(exportedDayTypeObjectType))
          .dataFetcher(env -> {
            ExportedLineStatistics exportedLineStatistics = env.getSource();
            return exportedLineStatistics.getExportedDayTypesStatistics();
          })
      )
      .build();

    GraphQLObjectType publicLineObjectType = newObject()
      .name("PublicLine")
      .field(
        newFieldDefinition()
          .name(FIELD_OPERATING_DATE_FROM)
          .type(DateScalar.getGraphQLDateScalar())
      )
      .field(
        newFieldDefinition()
          .name(FIELD_OPERATING_DATE_TO)
          .type(DateScalar.getGraphQLDateScalar())
      )
      .field(newFieldDefinition().name(FIELD_PUBLIC_CODE).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_PROVIDER_CODE).type(GraphQLString))
      .field(
        newFieldDefinition()
          .name(FIELD_LINES)
          .type(new GraphQLList(exportedLineObjectType))
      )
      .build();

    exportedLineStatisticsObjectType =
      newObject()
        .name("ExportedLineStatistics")
        .field(
          newFieldDefinition()
            .name(FIELD_START_DATE)
            .type(DateScalar.getGraphQLDateScalar())
            .dataFetcher(env -> LocalDate.now())
        )
        .field(
          newFieldDefinition()
            .name(FIELD_PUBLIC_LINES)
            .type(new GraphQLList(publicLineObjectType))
            .dataFetcher(new ExportedPublicLinesFetcher())
        )
        .build();

    exportObjectType =
      newObject(identifiedEntityObjectType)
        .name("Export")
        .field(newFieldDefinition().name(FIELD_NAME).type(GraphQLString))
        .field(newFieldDefinition().name(FIELD_EXPORT_STATUS).type(exportStatusEnum))
        .field(newFieldDefinition().name(FIELD_DRY_RUN).type(GraphQLBoolean))
        .field(
          newFieldDefinition()
            .name(FIELD_DOWNLOAD_URL)
            .type(GraphQLString)
            .dataFetcher(env -> {
              Export export = env.getSource();
              if (export == null || !StringUtils.hasText(export.getFileName())) {
                return null;
              }
              return (
                export.getProvider().getCode().toLowerCase() +
                "/export/" +
                export.getNetexId() +
                "/download"
              );
            })
        )
        .field(
          newFieldDefinition()
            .name(FIELD_MESSAGES)
            .type(new GraphQLList(exportMessageObjectType))
        )
        .field(
          newFieldDefinition()
            .name(FIELD_EXPORT_LINE_ASSOCIATIONS)
            .type(new GraphQLList(exportLineAssociationObjectType))
            .dataFetcher(env -> {
              Export export = env.getSource();
              return export.getExportLineAssociations();
            })
        )
        .build();

    GraphQLObjectType locationObjectType = newObject()
      .name("Location")
      .field(newFieldDefinition().name("longitude").type(GraphQLString))
      .field(newFieldDefinition().name("latitude").type(GraphQLString))
      .build();

    GraphQLObjectType centroidObjectType = newObject()
      .name("Centroid")
      .field(newFieldDefinition().name("location").type(locationObjectType))
      .build();

    GraphQLObjectType multilingualStringObjectType = newObject()
      .name("MultilingualString")
      .field(newFieldDefinition().name("lang").type(GraphQLString))
      .field(newFieldDefinition().name("value").type(GraphQLString))
      .build();

    GraphQLObjectType quayObjectType = newObject()
      .name("Quay")
      .field(newFieldDefinition().name(FIELD_ID).type(GraphQLString))
      .field(newFieldDefinition().name(FIELD_PUBLIC_CODE).type(GraphQLString))
      .field(newFieldDefinition().name("name").type(multilingualStringObjectType))
      .field(newFieldDefinition().name("centroid").type(centroidObjectType))
      .build();

    stopPlaceObjectType =
      newObject()
        .name("StopPlace")
        .field(newFieldDefinition().name(FIELD_ID).type(GraphQLID))
        .field(newFieldDefinition().name(FIELD_NAME).type(multilingualStringObjectType))
        .field(newFieldDefinition().name(FIELD_TRANSPORT_MODE).type(transportModeEnum))
        .field(newFieldDefinition().name("centroid").type(centroidObjectType))
        .field(newFieldDefinition().name("quays").type(new GraphQLList(quayObjectType)))
        .build();

    organisationObjectType =
      newObject()
        .name("Organisation")
        .field(newFieldDefinition().name(FIELD_ID).type(GraphQLID))
        .field(versionField)
        .field(newFieldDefinition().name(FIELD_NAME).type(multilingualStringObjectType))
        .field(newFieldDefinition().name("type").type(organisationTypeEnum))
        .build();

    routeGeometryObjectType =
      newObject()
        .name("RouteGeometry")
        .field(
          newFieldDefinition()
            .name("coordinates")
            .type(new GraphQLList(new GraphQLList(GraphQLBigDecimal)))
        )
        .field(newFieldDefinition().name("distance").type(GraphQLBigDecimal))
        .build();

    serviceLinkObjectType =
      newObject()
        .name("ServiceLink")
        .field(newFieldDefinition().name("routeGeometry").type(routeGeometryObjectType))
        .field(newFieldDefinition().name("quayRefFrom").type(GraphQLString))
        .field(newFieldDefinition().name("quayRefTo").type(GraphQLString))
        .field(newFieldDefinition().name("serviceLinkRef").type(GraphQLString))
        .build();
  }

  private GraphQLObjectType createQueryObject() {
    return newObject()
      .name("Queries")
      .description("Query and search for data")
      .field(
        newFieldDefinition()
          .type(new GraphQLList(lineObjectType))
          .name("lines")
          .description("List of lines")
          .dataFetcher(env -> fixedLineRepository.findAll())
      )
      .field(
        newFieldDefinition()
          .type(lineObjectType)
          .name("line")
          .description("Get line by id")
          .argument(idArgument)
          .dataFetcher(env -> fixedLineRepository.getOne(env.getArgument(FIELD_ID)))
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(fixedLineObjectType))
          .name("fixedLines")
          .description("List fixed lines")
          .deprecate("Use 'lines' instead")
          .dataFetcher(env -> fixedLineRepository.findAll())
      )
      .field(
        newFieldDefinition()
          .type(fixedLineObjectType)
          .name("fixedLine")
          .description("Get fixedLine by id")
          .deprecate("Use 'line' instead")
          .argument(idArgument)
          .dataFetcher(env -> fixedLineRepository.getOne(env.getArgument(FIELD_ID)))
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(flexibleLineObjectType))
          .name("flexibleLines")
          .description("List flexibleLines")
          .dataFetcher(env -> flexibleLineRepository.findAll())
      )
      .field(
        newFieldDefinition()
          .type(flexibleLineObjectType)
          .name("flexibleLine")
          .description("Get flexibleLine by id")
          .argument(idArgument)
          .dataFetcher(env -> flexibleLineRepository.getOne(env.getArgument(FIELD_ID)))
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(dayTypeObjectType))
          .name("dayTypes")
          .description("List dayTypes")
          .dataFetcher(env -> dayTypeRepository.findAll())
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(dayTypeObjectType))
          .name("dayTypesByIds")
          .description("List dayTypes by ids")
          .argument(idsArgument)
          .dataFetcher(env -> dayTypeRepository.findByIds(env.getArgument(FIELD_IDS)))
      )
      .field(
        newFieldDefinition()
          .type(dayTypeObjectType)
          .name("dayType")
          .description("Get dayType by id")
          .argument(idArgument)
          .dataFetcher(env -> dayTypeRepository.getOne(env.getArgument(FIELD_ID)))
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(flexibleStopPlaceObjectType))
          .name("flexibleStopPlaces")
          .description("List flexibleStopPlaces")
          .dataFetcher(env -> flexibleStopPlaceRepository.findAll())
      )
      .field(
        newFieldDefinition()
          .type(flexibleStopPlaceObjectType)
          .name("flexibleStopPlace")
          .description("Get flexibleStopPlace by id")
          .argument(idArgument)
          .dataFetcher(env ->
            flexibleStopPlaceRepository.getOne(env.getArgument(FIELD_ID))
          )
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(stopPlaceObjectType))
          .name("stopPlaces")
          .argument(
            GraphQLArgument
              .newArgument()
              .name(FIELD_TRANSPORT_MODE)
              .type(transportModeEnum)
              .description("Transport mode, e.g. train, bus etc.")
              .build()
          )
          .argument(
            GraphQLArgument
              .newArgument()
              .name(FIELD_SEARCH_TEXT)
              .type(GraphQLString)
              .description("Search e.g. by stop place id/name or quay id")
              .build()
          )
          .argument(
            GraphQLArgument
              .newArgument()
              .name(FIELD_NORTH_EAST_LAT)
              .type(GraphQLBigDecimal)
              .description("Bounding box's north east latitude")
              .build()
          )
          .argument(
            GraphQLArgument
              .newArgument()
              .name(FIELD_NORTH_EAST_LNG)
              .type(GraphQLBigDecimal)
              .description("Bounding box's north east longitude")
              .build()
          )
          .argument(
            GraphQLArgument
              .newArgument()
              .name(FIELD_SOUTH_WEST_LAT)
              .type(GraphQLBigDecimal)
              .description("Bounding box's south west latitude")
              .build()
          )
          .argument(
            GraphQLArgument
              .newArgument()
              .name(FIELD_SOUTH_WEST_LNG)
              .type(GraphQLBigDecimal)
              .description("Bounding box's south west longitude")
              .build()
          )
          .description(
            "List all stop places of a certain transport mode, with quays included"
          )
          .dataFetcher(stopPlacesFetcher)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(networkObjectType))
          .name("networks")
          .description("List networks")
          .dataFetcher(env -> networkRepository.findAll())
      )
      .field(
        newFieldDefinition()
          .type(networkObjectType)
          .name("network")
          .description("Get network by id")
          .argument(idArgument)
          .dataFetcher(env -> networkRepository.getOne(env.getArgument(FIELD_ID)))
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(exportObjectType))
          .name("exports")
          .argument(
            GraphQLArgument
              .newArgument()
              .type(GraphQLLong)
              .name("historicDays")
              .defaultValue(30L)
              .description("Number historic to fetch data for")
          )
          .description("List exports")
          .dataFetcher(env ->
            exportRepository.findByCreatedAfterAndProviderCode(
              OffsetDateTime.now().minusDays(env.getArgument("historicDays")).toInstant(),
              Context.getProvider()
            )
          )
      )
      .field(
        newFieldDefinition()
          .type(exportObjectType)
          .name("export")
          .description("Get export by id")
          .argument(idArgument)
          .dataFetcher(env -> exportRepository.getOne(env.getArgument(FIELD_ID)))
      )
      .field(
        newFieldDefinition()
          .type(exportedLineStatisticsObjectType)
          .name("lineStatistics")
          .description("Get line statistics")
          .argument(providerArgument)
          .dataFetcher(env -> {
            String providerCode = env.getArgument(FIELD_PROVIDER_CODE);
            return providerCode != null
              ? exportedLineStatisticsService.getLineStatisticsForProvider(providerCode)
              : exportedLineStatisticsService.getLineStatisticsForAllProviders();
          })
      )
      .field(
        newFieldDefinition()
          .type(stopPlaceObjectType)
          .name("stopPlaceByQuayRef")
          .description("Get a stop place of a quay")
          .argument(idArgument)
          .dataFetcher(quayRefSearchFetcher)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(organisationObjectType))
          .name("organisations")
          .description("List all organisations")
          .dataFetcher(organisationsFetcher)
      )
      .field(
        newFieldDefinition()
          .type(serviceLinkObjectType)
          .name("serviceLink")
          .argument(
            GraphQLArgument
              .newArgument()
              .name("quayRefFrom")
              .type(GraphQLString)
              .description("First stop point's id")
              .build()
          )
          .argument(
            GraphQLArgument
              .newArgument()
              .name("quayRefTo")
              .type(GraphQLString)
              .description("Second stop point's id")
              .build()
          )
          .description("Fetch service link containing route geometry")
          .dataFetcher(routingFetcher)
      )
      .build();
  }

  private GraphQLObjectType createMutationObject() {
    String ignoredInputFieldDesc =
      "Value is ignored for mutation calls. Included for convenient copying of output to input with minimal modifications.";
    GraphQLInputObjectType identifiedEntityInputType = newInputObject()
      .name("IdentifiedEntityInput")
      .field(newInputObjectField().name(FIELD_ID).type(Scalars.GraphQLID))
      .field(newInputObjectField().name(FIELD_VERSION).type(GraphQLLong))
      .description(ignoredInputFieldDesc)
      .field(newInputObjectField().name(FIELD_CREATED_BY).type(GraphQLString))
      .description(ignoredInputFieldDesc)
      .field(
        newInputObjectField().name(FIELD_CREATED).type(dateTimeScalar.getDateTimeScalar())
      )
      .description(ignoredInputFieldDesc)
      .field(newInputObjectField().name(FIELD_CHANGED_BY).type(GraphQLString))
      .description(ignoredInputFieldDesc)
      .field(
        newInputObjectField().name(FIELD_CHANGED).type(dateTimeScalar.getDateTimeScalar())
      )
      .description(ignoredInputFieldDesc)
      .build();

    GraphQLInputObjectType groupOfEntitiesInputType = newInputObject(
      identifiedEntityInputType
    )
      .name("GroupOfEntities")
      .field(newInputObjectField().name(FIELD_NAME).type(GraphQLString))
      .field(newInputObjectField().name(FIELD_DESCRIPTION).type(GraphQLString))
      .field(newInputObjectField().name(FIELD_PRIVATE_CODE).type(GraphQLString))
      .build();

    GraphQLInputObjectType geoJSONInputType = newInputObject()
      .name("GeoJSONInput")
      .description(
        "Geometry-object as specified in the GeoJSON-standard (http://geojson.org/geojson-spec.html)."
      )
      .field(
        newInputObjectField().name("type").type(new GraphQLNonNull(geometryTypeEnum))
      )
      .field(
        newInputObjectField()
          .name("coordinates")
          .type(GeoJSONCoordinatesScalar.getGraphQGeoJSONCoordinatesScalar())
      )
      .build();

    GraphQLInputObjectType networkInputType = newInputObject(groupOfEntitiesInputType)
      .name("NetworkInput")
      .field(
        newInputObjectField()
          .name(FIELD_AUTHORITY_REF)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    GraphQLInputObjectType keyValuesInputType = newInputObject()
      .name("KeyValuesInput")
      .field(newInputObjectField().name(FIELD_KEY).type(GraphQLString))
      .field(
        newInputObjectField().name(FIELD_VALUES).type(new GraphQLList(GraphQLString))
      )
      .build();

    GraphQLInputObjectType flexibleAreaInput = newInputObject(groupOfEntitiesInputType)
      .name("FlexibleAreaInput")
      .field(
        newInputObjectField()
          .name(FIELD_KEY_VALUES)
          .type(new GraphQLList(keyValuesInputType))
      )
      .field(
        newInputObjectField()
          .name(FIELD_POLYGON)
          .type(new GraphQLNonNull(geoJSONInputType))
      )
      .build();

    GraphQLInputObjectType hailAndRideAreaInput = newInputObject(groupOfEntitiesInputType)
      .name("HailAndRideAreaInput")
      .field(
        newInputObjectField()
          .name(FIELD_START_QUAY_REF)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newInputObjectField()
          .name(FIELD_END_QUAY_REF)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    GraphQLInputObjectType flexibleStopPlaceInputType = newInputObject(
      groupOfEntitiesInputType
    )
      .name("FlexibleStopPlaceInput")
      .field(
        newInputObjectField()
          .name(FIELD_TRANSPORT_MODE)
          .type(new GraphQLNonNull(vehicleModeEnum))
      )
      .field(
        newInputObjectField()
          .name(FIELD_FLEXIBLE_AREA)
          .type(flexibleAreaInput)
          .deprecate("Use 'flexibleAreas' instead")
      )
      .field(
        newInputObjectField()
          .name(FIELD_FLEXIBLE_AREAS)
          .type(new GraphQLList(flexibleAreaInput))
      )
      .field(
        newInputObjectField().name(FIELD_HAIL_AND_RIDE_AREA).type(hailAndRideAreaInput)
      )
      .field(
        newInputObjectField()
          .name(FIELD_KEY_VALUES)
          .type(new GraphQLList(keyValuesInputType))
      )
      .build();

    GraphQLInputObjectType contactInputType = newInputObject(groupOfEntitiesInputType)
      .name("ContactInput")
      .field(newInputObjectField().name(FIELD_CONTACT_PERSON).type(GraphQLString))
      .field(newInputObjectField().name(FIELD_PHONE).type(GraphQLString))
      .field(newInputObjectField().name(FIELD_EMAIL).type(GraphQLString))
      .field(newInputObjectField().name(FIELD_URL).type(GraphQLString))
      .field(newInputObjectField().name(FIELD_FURTHER_DETAILS).type(GraphQLString))
      .build();

    GraphQLInputObjectType bookingArrangementInputType = newInputObject(
      groupOfEntitiesInputType
    )
      .name("BookingArrangementInput")
      .field(newInputObjectField().name(FIELD_BOOKING_CONTACT).type(contactInputType))
      .field(newInputObjectField().name(FIELD_BOOKING_NOTE).type(GraphQLString))
      .field(
        newInputObjectField()
          .name(FIELD_BOOKING_METHODS)
          .type(new GraphQLList(bookingMethodEnum))
      )
      .field(newInputObjectField().name(FIELD_BOOKING_ACCESS).type(bookingAccessEnum))
      .field(newInputObjectField().name(FIELD_BOOK_WHEN).type(purchaseWhenEnum))
      .field(
        newInputObjectField()
          .name(FIELD_BUY_WHEN)
          .type(new GraphQLList(purchaseMomentEnum))
      )
      .field(
        newInputObjectField()
          .name(FIELD_LATEST_BOOKING_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(
        newInputObjectField()
          .name(FIELD_MINIMUM_BOOKING_PERIOD)
          .type(DurationScalar.getDurationScalar())
      )
      .build();

    GraphQLInputObjectType destinationDisplayInputType = newInputObject()
      .name("DestinationDisplayInput")
      .field(newInputObjectField().name(FIELD_FRONT_TEXT).type(GraphQLString))
      .build();

    GraphQLInputObjectType operatingPeriodInputType = newInputObject()
      .name("OperatingPeriodInput")
      .field(
        newInputObjectField()
          .name(FIELD_FROM_DATE)
          .type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar()))
      )
      .field(
        newInputObjectField()
          .name(FIELD_TO_DATE)
          .type(new GraphQLNonNull(DateScalar.getGraphQLDateScalar()))
      )
      .build();

    GraphQLInputObjectType dayTypeAssignmentInputType = newInputObject()
      .name("DayTypeAssignmentInput")
      .field(newInputObjectField().name(FIELD_IS_AVAILABLE).type(GraphQLBoolean))
      .field(
        newInputObjectField().name(FIELD_DATE).type(DateScalar.getGraphQLDateScalar())
      )
      .field(
        newInputObjectField().name(FIELD_OPERATING_PERIOD).type(operatingPeriodInputType)
      )
      .build();

    GraphQLInputObjectType dayTypeInputType = newInputObject(identifiedEntityInputType)
      .name("DayTypeInput")
      .field(
        newInputObjectField()
          .name(FIELD_DAYS_OF_WEEK)
          .type(new GraphQLList(dayOfWeekEnum))
      )
      .field(
        newInputObjectField()
          .name(FIELD_DAY_TYPE_ASSIGNMENTS)
          .type(new GraphQLNonNull(new GraphQLList(dayTypeAssignmentInputType)))
      )
      .field(newInputObjectField().name(FIELD_NAME).type(GraphQLString))
      .build();

    GraphQLInputObjectType noticeInputType = newInputObject(identifiedEntityInputType)
      .name("NoticeInput")
      .field(
        newInputObjectField().name(FIELD_TEXT).type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    GraphQLInputObjectType timetabledPassingTimeInputType = newInputObject(
      groupOfEntitiesInputType
    )
      .name("TimetabledPassingTimeInput")
      .field(
        newInputObjectField()
          .name(FIELD_ARRIVAL_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(newInputObjectField().name(FIELD_ARRIVAL_DAY_OFFSET).type(GraphQLInt))
      .field(
        newInputObjectField()
          .name(FIELD_DEPARTURE_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(newInputObjectField().name(FIELD_DEPARTURE_DAY_OFFSET).type(GraphQLInt))
      .field(
        newInputObjectField()
          .name(FIELD_LATEST_ARRIVAL_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(newInputObjectField().name(FIELD_LATEST_ARRIVAL_DAY_OFFSET).type(GraphQLInt))
      .field(
        newInputObjectField()
          .name(FIELD_EARLIEST_DEPARTURE_TIME)
          .type(LocalTimeScalar.getLocalTimeScalar())
      )
      .field(
        newInputObjectField().name(FIELD_EARLIEST_DEPARTURE_DAY_OFFSET).type(GraphQLInt)
      )
      .field(
        newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType))
      )
      .build();

    GraphQLInputObjectType serviceJourneyInputType = newInputObject(
      groupOfEntitiesInputType
    )
      .name("ServiceJourneyInput")
      .field(newInputObjectField().name(FIELD_PUBLIC_CODE).type(GraphQLString))
      .field(newInputObjectField().name(FIELD_OPERATOR_REF).type(GraphQLString))
      .field(
        newInputObjectField()
          .name(FIELD_BOOKING_ARRANGEMENT)
          .type(bookingArrangementInputType)
      )
      .field(
        newInputObjectField()
          .name(FIELD_PASSING_TIMES)
          .type(new GraphQLNonNull(new GraphQLList(timetabledPassingTimeInputType)))
      )
      .field(
        newInputObjectField()
          .name(FIELD_DAY_TYPES_REFS)
          .type(new GraphQLList(GraphQLString))
      )
      .field(
        newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType))
      )
      .build();

    GraphQLInputObjectType stopPointInJourneyPatternInputType = newInputObject(
      groupOfEntitiesInputType
    )
      .name("StopPointInJourneyPatternInput")
      .field(
        newInputObjectField().name(FIELD_FLEXIBLE_STOP_PLACE_REF).type(GraphQLString)
      )
      .field(newInputObjectField().name(FIELD_QUAY_REF).type(GraphQLString))
      .field(
        newInputObjectField()
          .name(FIELD_BOOKING_ARRANGEMENT)
          .type(bookingArrangementInputType)
      )
      .field(
        newInputObjectField()
          .name(FIELD_DESTINATION_DISPLAY)
          .type(destinationDisplayInputType)
      )
      .field(newInputObjectField().name(FIELD_FOR_BOARDING).type(GraphQLBoolean))
      .field(newInputObjectField().name(FIELD_FOR_ALIGHTING).type(GraphQLBoolean))
      .field(
        newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType))
      )
      .build();

    GraphQLInputObjectType journeyPatternInputType = newInputObject(
      groupOfEntitiesInputType
    )
      .name("JourneyPatternInput")
      .field(newInputObjectField().name(FIELD_DIRECTION_TYPE).type(directionTypeEnum))
      .field(
        newInputObjectField()
          .name(FIELD_POINTS_IN_SEQUENCE)
          .type(new GraphQLNonNull(new GraphQLList(stopPointInJourneyPatternInputType)))
      )
      .field(
        newInputObjectField()
          .name(FIELD_SERVICE_JOURNEYS)
          .type(new GraphQLNonNull(new GraphQLList(serviceJourneyInputType)))
      )
      .field(
        newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType))
      )
      .build();

    GraphQLInputObjectType lineInputType = newInputObject(groupOfEntitiesInputType)
      .name("LineInput")
      .field(
        newInputObjectField()
          .name(FIELD_PUBLIC_CODE)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newInputObjectField()
          .name(FIELD_TRANSPORT_MODE)
          .type(new GraphQLNonNull(vehicleModeEnum))
      )
      .field(
        newInputObjectField()
          .name(FIELD_TRANSPORT_SUBMODE)
          .type(new GraphQLNonNull(vehicleSubmodeEnum))
      )
      .field(
        newInputObjectField()
          .name(FIELD_NETWORK_REF)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .field(newInputObjectField().name(FIELD_OPERATOR_REF).type(GraphQLString))
      .field(
        newInputObjectField()
          .name(FIELD_JOURNEY_PATTERNS)
          .type(new GraphQLNonNull(new GraphQLList(journeyPatternInputType)))
      )
      .field(
        newInputObjectField().name(FIELD_NOTICES).type(new GraphQLList(noticeInputType))
      )
      .build();

    GraphQLInputObjectType fixedLineInputType = newInputObject(lineInputType)
      .name("FixedLineInput")
      .build();

    GraphQLInputObjectType flexibleLineInputType = newInputObject(lineInputType)
      .name("FlexibleLineInput")
      .field(
        newInputObjectField()
          .name(FIELD_FLEXIBLE_LINE_TYPE)
          .type(new GraphQLNonNull(flexibleLineTypeEnum))
      )
      .field(
        newInputObjectField()
          .name(FIELD_BOOKING_ARRANGEMENT)
          .type(bookingArrangementInputType)
      )
      .build();

    GraphQLInputObjectType exportLineAssociationInputType = newInputObject()
      .name("ExportLineAssociationInput")
      .field(
        newInputObjectField().name(FIELD_LINE_REF).type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    GraphQLInputObjectType exportInputType = newInputObject()
      .name("ExportInput")
      .field(newInputObjectField().name(FIELD_NAME).type(GraphQLString))
      .field(
        newInputObjectField()
          .name(FIELD_DRY_RUN)
          .type(GraphQLBoolean)
          .defaultValue(Boolean.FALSE)
      )
      .field(
        newInputObjectField()
          .name(FIELD_EXPORT_LINE_ASSOCIATIONS)
          .type(new GraphQLList(exportLineAssociationInputType))
      )
      .build();

    return newObject()
      .name("Mutations")
      .description("Create and edit FlexibleLine timetable data")
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(networkObjectType))
          .name("mutateNetwork")
          .description("Create new or update existing network")
          .argument(
            GraphQLArgument.newArgument().name(FIELD_INPUT).type(networkInputType)
          )
          .dataFetcher(networkUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(networkObjectType))
          .name("deleteNetwork")
          .description("Delete an existing network")
          .argument(idArgument)
          .dataFetcher(networkUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(lineObjectType))
          .name("mutateLine")
          .description("Create new or update existing line")
          .argument(GraphQLArgument.newArgument().name(FIELD_INPUT).type(lineInputType))
          .dataFetcher(fixedLineUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(lineObjectType))
          .name("deleteLine")
          .description("Delete an existing line")
          .argument(idArgument)
          .dataFetcher(fixedLineUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(fixedLineObjectType))
          .name("mutateFixedLine")
          .description("Create new or update existing fixedLine")
          .deprecate("Use 'mutateLine' instead")
          .argument(
            GraphQLArgument.newArgument().name(FIELD_INPUT).type(fixedLineInputType)
          )
          .dataFetcher(fixedLineUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(fixedLineObjectType))
          .name("deleteFixedLine")
          .description("Delete an existing fixedLine")
          .deprecate("Use 'deleteLine' instead")
          .argument(idArgument)
          .dataFetcher(fixedLineUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(flexibleLineObjectType))
          .name("mutateFlexibleLine")
          .description("Create new or update existing flexibleLine")
          .argument(
            GraphQLArgument.newArgument().name(FIELD_INPUT).type(flexibleLineInputType)
          )
          .dataFetcher(flexibleLineUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(flexibleLineObjectType))
          .name("deleteFlexibleLine")
          .description("Delete an existing flexibleLine")
          .argument(idArgument)
          .dataFetcher(flexibleLineUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(dayTypeObjectType))
          .name("mutateDayType")
          .description("Create new or update existing dayType")
          .argument(
            GraphQLArgument.newArgument().name(FIELD_INPUT).type(dayTypeInputType)
          )
          .dataFetcher(dayTypeUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(dayTypeObjectType))
          .name("deleteDayType")
          .description("Delete an existing dayType")
          .argument(idArgument)
          .dataFetcher(dayTypeUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(new GraphQLList(dayTypeObjectType)))
          .name("deleteDayTypes")
          .argument(idsArgument)
          .dataFetcher(dayTypesBulkUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(flexibleStopPlaceObjectType))
          .name("mutateFlexibleStopPlace")
          .description("Create new or update existing flexibleStopPlace")
          .argument(
            GraphQLArgument
              .newArgument()
              .name(FIELD_INPUT)
              .type(flexibleStopPlaceInputType)
          )
          .dataFetcher(flexibleStopPlaceUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(flexibleStopPlaceObjectType))
          .name("deleteFlexibleStopPlace")
          .description("Delete an existing flexibleStopPlace")
          .argument(idArgument)
          .dataFetcher(flexibleStopPlaceUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(exportObjectType))
          .name("export")
          .description("Start a new export")
          .argument(GraphQLArgument.newArgument().name(FIELD_INPUT).type(exportInputType))
          .dataFetcher(exportUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(GraphQLString))
          .name("cleanDataSpace")
          .description("Delete all data in provider data space!")
          .dataFetcher(env -> {
            dataSpaceCleaner.clean();
            return "OK";
          })
      )
      .build();
  }
}
