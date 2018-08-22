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
import no.entur.uttu.graphql.scalars.GeoJSONCoordinatesScalar;
import no.entur.uttu.model.CodeSpace;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.FlexibleLineTypeEnumeration;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.repository.CodeSpaceRepository;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.FlexibleStopPlaceRepository;
import no.entur.uttu.repository.NetworkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.function.Function;

import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;

@Component
public class FlexibleLineGraphQLSchema {
    @Autowired
    private DateScalar dateScalar;

    @Autowired
    private DataFetcher<FlexibleStopPlace> flexibleStopPlaceUpdater;

    @Autowired
    private DataFetcher<FlexibleLine> flexibleLineUpdater;

    @Autowired
    private CodeSpaceRepository codeSpaceRepository;

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

    private static GraphQLEnumType vehicleModeEnum = FlexibleLineGraphQLSchema.createEnum("VehicleModeEnumeration", VehicleModeEnumeration.values(), (t -> t.value()));

    private static GraphQLEnumType flexibleLineTypeEnum = FlexibleLineGraphQLSchema.createEnum("FlexibleLineTypeEnumeration", FlexibleLineTypeEnumeration.values(), (t -> t.value()));

    private static <T extends Enum> GraphQLEnumType createEnum(String name, T[] values, Function<T, String> mapping) {
        GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum().name(name);
        Arrays.stream(values).forEach(type -> enumBuilder.value(mapping.apply(type), type));
        return enumBuilder.build();
    }


    private GraphQLFieldDefinition idFieldDefinition;
    private GraphQLFieldDefinition versionField;
    private GraphQLFieldDefinition nameFieldDefinition;
    private GraphQLObjectType geoJSONObjectType;
    private GraphQLObjectType identifiedEntityObjectType;
    private GraphQLObjectType codeSpaceObjectType;
    private GraphQLObjectType groupOfEntitiesObjectType;
    private GraphQLObjectType flexibleLineObjectType;
    private GraphQLObjectType flexibleStopPlaceObjectType;

    private GraphQLObjectType networkObjectType;

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
        idFieldDefinition = newFieldDefinition()
                                    .name("id")
                                    .type(GraphQLString)
                                    .dataFetcher(env -> ((ProviderEntity) env.getSource()).getNetexId())
                                    .build();

        versionField = newFieldDefinition()
                               .name("version")
                               .type(GraphQLString)
                               .build();

        nameFieldDefinition = newFieldDefinition()
                                      .name("name")
                                      .type(GraphQLString)
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
                                             .field(newFieldDefinition().name("createdBy").type(new GraphQLNonNull(GraphQLString)))
                                             .field(newFieldDefinition().name("created").type(new GraphQLNonNull(dateScalar.getGraphQLDateScalar())))
                                             .field(newFieldDefinition().name("changedBy").type(new GraphQLNonNull(GraphQLString)))
                                             .field(newFieldDefinition().name("changed").type(new GraphQLNonNull(dateScalar.getGraphQLDateScalar())))
                                             .build();


        codeSpaceObjectType = newObject(identifiedEntityObjectType).name("CodeSpace")
                                      .field(newFieldDefinition().name("xmlns").type(GraphQLString).dataFetcher(env -> ((CodeSpace) env.getSource()).getXmlns()).build())
                                      .field(newFieldDefinition().name("xmlnsUrl").type(GraphQLString).dataFetcher(env -> ((CodeSpace) env.getSource()).getXmlns()).build())
                                      .build();


        groupOfEntitiesObjectType = newObject(identifiedEntityObjectType).name("GroupOfEntities")
                                            .field(newFieldDefinition().name("name").type(GraphQLString))
                                            .field(newFieldDefinition().name("description").type(GraphQLString))
                                            .field(newFieldDefinition().name("privateCode").type(GraphQLString))
                                            .build();


        networkObjectType = newObject(groupOfEntitiesObjectType).name("Network")
                                    .field(newFieldDefinition().name("authorityRef").type(new GraphQLNonNull(GraphQLString)))
                                    .build();

        flexibleLineObjectType = newObject(groupOfEntitiesObjectType).name("FlexibleLine")
                                         .field(newFieldDefinition().name("publicCode").type(new GraphQLNonNull(GraphQLString)))
                                         .build();


        flexibleStopPlaceObjectType = newObject(groupOfEntitiesObjectType).name("FlexibleStopPlace")
                                              .field(newFieldDefinition().name("flexibleArea").type(new GraphQLNonNull(geoJSONObjectType))
                                                             .dataFetcher(env -> ((FlexibleStopPlace) env.getSource()).getPolygon()))
                                              .build();

    }

    private GraphQLObjectType createQueryObject() {


        GraphQLObjectType queryType = newObject()
                                              .name("FlexibleLinesQuery")
                                              .description("Query and search for data")
                                              .field(newFieldDefinition()
                                                             .type(new GraphQLList(codeSpaceObjectType))
                                                             .name("codeSpaces")
                                                             .description("Search for CodeSpaces")
                                                             .dataFetcher(env -> codeSpaceRepository.findAll()))
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
                                                             .type(new GraphQLList(flexibleStopPlaceObjectType))
                                                             .name("networks")
                                                             .description("Search for Networks")
                                                             .dataFetcher(env -> networkRepository.findAll()))
                                              .build();

        return queryType;
    }

    private GraphQLObjectType createMutationObject() {


        GraphQLInputObjectType identifiedEntityInputType = newInputObject().name("IdentifiedEntityInput")
                                                                   .field(newInputObjectField().name("id").type(GraphQLString))
                                                                   .field(newInputObjectField().name("version").type(GraphQLLong))
                                                                   .field(newInputObjectField().name("createdBy").type(GraphQLString))
                                                                   .field(newInputObjectField().name("created").type(dateScalar.getGraphQLDateScalar()))
                                                                   .field(newInputObjectField().name("changedBy").type(GraphQLString))
                                                                   .field(newInputObjectField().name("changed").type(dateScalar.getGraphQLDateScalar()))
                                                                   .build();

        GraphQLInputObjectType groupOfEntitiesInputType = newInputObject(identifiedEntityInputType).name("GroupOfEntities")
                                                                  .field(newInputObjectField().name("name").type(GraphQLString))
                                                                  .field(newInputObjectField().name("description").type(GraphQLString))
                                                                  .field(newInputObjectField().name("privateCode").type(GraphQLString))
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
                                                          .field(newInputObjectField().name("authorityRef").type(new GraphQLNonNull(geoJSONInputType)))
                                                          .build();


        GraphQLInputObjectType flexibleStopPlaceInputType = newInputObject(groupOfEntitiesInputType)
                                                                    .name("FlexibleStopPlaceInput")
                                                                    .field(newInputObjectField().name("flexibleArea").type(new GraphQLNonNull(geoJSONInputType)))
                                                                    .field(newInputObjectField().name("transportMode").type(new GraphQLNonNull(vehicleModeEnum)))
                                                                    .build();

        GraphQLInputObjectType flexibleLineInputType = newInputObject(groupOfEntitiesInputType)
                                                               .name("FlexibleLineInput")
                                                               .field(newInputObjectField().name("transportMode").type(new GraphQLNonNull(vehicleModeEnum)))
                                                               .build();

        GraphQLObjectType mutationType = newObject()
                                                 .name("FlexibleLinesMutation")
                                                 .description("Create and edit FlexibleLine timetable data")
                                                 .field(newFieldDefinition()
                                                                .type(new GraphQLNonNull(flexibleLineObjectType))
                                                                .name("mutateFlexibleLine")
                                                                .description("Create new or update existing FlexibleLine")
                                                                .argument(GraphQLArgument.newArgument()
                                                                                  .name("flexibleLine")
                                                                                  .type(flexibleLineInputType))
                                                                .dataFetcher(flexibleLineUpdater))
                                                 .field(newFieldDefinition()
                                                                .type(new GraphQLNonNull(flexibleStopPlaceObjectType))
                                                                .name("mutateFlexibleStopPlace")
                                                                .description("Create new or update existing flexibleStopPlace")
                                                                .argument(GraphQLArgument.newArgument()
                                                                                  .name("flexibleStopPlace")
                                                                                  .type(flexibleStopPlaceInputType))
                                                                .dataFetcher(flexibleStopPlaceUpdater))
                                                 .field(newFieldDefinition()
                                                                .type(new GraphQLNonNull(flexibleStopPlaceObjectType))
                                                                .name("mutateNetwork")
                                                                .description("Create new or update existing network")
                                                                .argument(GraphQLArgument.newArgument()
                                                                                  .name("network")
                                                                                  .type(flexibleStopPlaceInputType))
                                                                .dataFetcher(flexibleStopPlaceUpdater))
                                                 .build();

        return mutationType;
    }

}

