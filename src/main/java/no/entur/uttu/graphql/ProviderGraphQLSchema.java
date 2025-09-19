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
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.scalars.ExtendedScalars.GraphQLLong;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLObjectType.newObject;
import static no.entur.uttu.graphql.GraphQLNames.*;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import no.entur.uttu.graphql.model.UserContext;
import no.entur.uttu.graphql.scalars.DateTimeScalar;
import no.entur.uttu.graphql.scalars.ProviderCodeScalar;
import no.entur.uttu.model.Codespace;
import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.CodespaceRepository;
import no.entur.uttu.migration.LineMigrationService;
import no.entur.uttu.migration.LineMigrationService.ConflictResolutionStrategy;
import org.springframework.stereotype.Component;

/**
 * GraphQL schema for common meta data, providers and code spaces.
 */
@Component
public class ProviderGraphQLSchema {

  private final CodespaceRepository codespaceRepository;
  private final DataFetcher<Codespace> codespaceUpdater;
  private final DataFetcher<Provider> providerUpdater;
  private final DataFetcher<List<Provider>> providerFetcher;
  private final DataFetcher<UserContext> userContextFetcher;
  private final DataFetcher<
    LineMigrationService.LineMigrationResult
  > lineMigrationFetcher;
  private final DateTimeScalar dateTimeScalar;

  private GraphQLSchema graphQLSchema;
  private GraphQLObjectType identifiedEntityObjectType;
  private GraphQLObjectType codespaceObjectType;
  private GraphQLObjectType providerObjectType;
  private GraphQLObjectType userContextObjectType;
  private GraphQLEnumType conflictResolutionStrategyEnum;
  private GraphQLInputObjectType lineMigrationOptionsType;
  private GraphQLInputObjectType lineMigrationInputType;
  private GraphQLObjectType lineMigrationSummaryType;
  private GraphQLObjectType lineMigrationWarningType;
  private GraphQLObjectType lineMigrationResultType;

  public ProviderGraphQLSchema(
    CodespaceRepository codespaceRepository,
    DataFetcher<Codespace> codespaceUpdater,
    DataFetcher<Provider> providerUpdater,
    DataFetcher<List<Provider>> providerFetcher,
    DataFetcher<UserContext> userContextFetcher,
    DataFetcher<LineMigrationService.LineMigrationResult> lineMigrationFetcher,
    DateTimeScalar dateTimeScalar
  ) {
    this.codespaceRepository = codespaceRepository;
    this.codespaceUpdater = codespaceUpdater;
    this.providerUpdater = providerUpdater;
    this.providerFetcher = providerFetcher;
    this.userContextFetcher = userContextFetcher;
    this.lineMigrationFetcher = lineMigrationFetcher;
    this.dateTimeScalar = dateTimeScalar;
  }

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

  @PostConstruct
  public void init() {
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
    identifiedEntityObjectType = newObject()
      .name("IdentifiedEntity")
      .field(
        newFieldDefinition().name(FIELD_VERSION).type(new GraphQLNonNull(GraphQLString))
      )
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

    codespaceObjectType = newObject(identifiedEntityObjectType)
      .name("Codespace")
      .field(
        newFieldDefinition().name(FIELD_XMLNS).type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newFieldDefinition().name(FIELD_XMLNS_URL).type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    providerObjectType = newObject(identifiedEntityObjectType)
      .name("Provider")
      .field(
        newFieldDefinition()
          .name(FIELD_CODE)
          .type(new GraphQLNonNull(ProviderCodeScalar.PROVIDER_CODE))
      )
      .field(
        newFieldDefinition().name(FIELD_NAME).type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newFieldDefinition()
          .name(FIELD_CODE_SPACE)
          .type(new GraphQLNonNull(codespaceObjectType))
      )
      .build();

    userContextObjectType = newObject()
      .name("UserContext")
      .description("Context-aware object")
      .field(
        newFieldDefinition()
          .name("preferredName")
          .description("User's preferred (display) name")
          .type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newFieldDefinition().name("isAdmin").type(new GraphQLNonNull(GraphQLBoolean))
      )
      .field(
        newFieldDefinition()
          .name("providers")
          .description("List of providers for which this user has access to manage data")
          .type(new GraphQLList(providerObjectType))
          .dataFetcher(providerFetcher)
      )
      .build();

    // Line migration types
    conflictResolutionStrategyEnum = createEnum(
      "ConflictResolutionStrategy",
      ConflictResolutionStrategy.values(),
      (ConflictResolutionStrategy::name)
    );

    lineMigrationOptionsType = newInputObject()
      .name("LineMigrationOptions")
      .description("Options for line migration")
      .field(
        newInputObjectField()
          .name("conflictResolution")
          .type(conflictResolutionStrategyEnum)
          .description("How to handle naming conflicts (default: FAIL)")
      )
      .field(
        newInputObjectField()
          .name("includeDayTypes")
          .type(GraphQLBoolean)
          .description("Whether to migrate associated day types (default: true)")
      )
      .field(
        newInputObjectField()
          .name("dryRun")
          .type(GraphQLBoolean)
          .description(
            "Perform validation only without persisting changes (default: false)"
          )
      )
      .build();

    lineMigrationInputType = newInputObject()
      .name("LineMigrationInput")
      .description("Input for migrating a line between providers")
      .field(
        newInputObjectField()
          .name("sourceLineId")
          .type(new GraphQLNonNull(GraphQLString))
          .description("NeTEx ID of the line to migrate")
      )
      .field(
        newInputObjectField()
          .name("targetProviderId")
          .type(new GraphQLNonNull(GraphQLString))
          .description("ID of the target provider")
      )
      .field(
        newInputObjectField()
          .name("targetNetworkId")
          .type(new GraphQLNonNull(GraphQLString))
          .description("NeTEx ID of the network in the target provider")
      )
      .field(
        newInputObjectField()
          .name("options")
          .type(lineMigrationOptionsType)
          .description("Migration options")
      )
      .build();

    lineMigrationSummaryType = newObject()
      .name("LineMigrationSummary")
      .description("Summary of the migration operation")
      .field(
        newFieldDefinition()
          .name("entitiesMigrated")
          .type(new GraphQLNonNull(GraphQLInt))
          .description("Total number of entities migrated")
      )
      .field(
        newFieldDefinition()
          .name("warningsCount")
          .type(new GraphQLNonNull(GraphQLInt))
          .description("Number of warnings generated")
      )
      .field(
        newFieldDefinition()
          .name("executionTimeMs")
          .type(new GraphQLNonNull(GraphQLLong))
          .description("Execution time in milliseconds")
      )
      .build();

    lineMigrationWarningType = newObject()
      .name("LineMigrationWarning")
      .description("Warning generated during migration")
      .field(
        newFieldDefinition()
          .name("type")
          .type(new GraphQLNonNull(GraphQLString))
          .description("Warning type")
      )
      .field(
        newFieldDefinition()
          .name("message")
          .type(new GraphQLNonNull(GraphQLString))
          .description("Warning message")
      )
      .field(
        newFieldDefinition()
          .name("entityId")
          .type(GraphQLString)
          .description("ID of the affected entity")
      )
      .build();

    lineMigrationResultType = newObject()
      .name("LineMigrationResult")
      .description("Result of a line migration operation")
      .field(
        newFieldDefinition()
          .name("success")
          .type(new GraphQLNonNull(GraphQLBoolean))
          .description("Whether the migration succeeded")
      )
      .field(
        newFieldDefinition()
          .name("migratedLineId")
          .type(GraphQLString)
          .description("NeTEx ID of the migrated line in the target provider")
      )
      .field(
        newFieldDefinition()
          .name("summary")
          .type(lineMigrationSummaryType)
          .description("Migration summary statistics")
      )
      .field(
        newFieldDefinition()
          .name("warnings")
          .type(new GraphQLList(lineMigrationWarningType))
          .description("Warnings generated during migration")
      )
      .field(
        newFieldDefinition()
          .name("errorMessage")
          .type(GraphQLString)
          .description("Error message if migration failed")
      )
      .build();
  }

  private GraphQLObjectType createQueryObject() {
    return newObject()
      .name("Queries")
      .description("Query and search for data")
      .field(
        newFieldDefinition()
          .type(new GraphQLList(codespaceObjectType))
          .name("codespaces")
          .description("Search for Codespaces")
          .dataFetcher(env -> codespaceRepository.findAll())
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLList(providerObjectType))
          .name("providers")
          .description("Search for Providers")
          .dataFetcher(providerFetcher)
      )
      .field(
        newFieldDefinition()
          .type(userContextObjectType)
          .name("userContext")
          .description("Context-aware user object")
          .dataFetcher(userContextFetcher)
      )
      .build();
  }

  private GraphQLObjectType createMutationObject() {
    String ignoredInputFieldDesc =
      "Value is ignored for mutation calls. Included for convenient copying of output to input with minimal modifications.";
    GraphQLInputObjectType identifiedEntityInputType = newInputObject()
      .name("IdentifiedEntityInput")
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

    GraphQLInputObjectType codespaceInputType = newInputObject(identifiedEntityInputType)
      .name("CodespaceInput")
      .field(
        newInputObjectField().name(FIELD_XMLNS).type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newInputObjectField()
          .name(FIELD_XMLNS_URL)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    GraphQLInputObjectType providerInputType = newInputObject(identifiedEntityInputType)
      .name("ProviderInput")
      .field(
        newInputObjectField()
          .name(FIELD_CODE)
          .type(new GraphQLNonNull(ProviderCodeScalar.PROVIDER_CODE))
      )
      .field(
        newInputObjectField().name(FIELD_NAME).type(new GraphQLNonNull(GraphQLString))
      )
      .field(
        newInputObjectField()
          .name(FIELD_CODE_SPACE_XMLNS)
          .type(new GraphQLNonNull(GraphQLString))
      )
      .build();

    GraphQLObjectType mutationType = newObject()
      .name("Mutations")
      .description("Create and edit Provider data")
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(codespaceObjectType))
          .name("mutateCodespace")
          .description("Create new or update existing Codespace")
          .argument(
            GraphQLArgument.newArgument().name(FIELD_INPUT).type(codespaceInputType)
          )
          .dataFetcher(codespaceUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(providerObjectType))
          .name("mutateProvider")
          .description("Create new or update existing Provider")
          .argument(
            GraphQLArgument.newArgument().name(FIELD_INPUT).type(providerInputType)
          )
          .dataFetcher(providerUpdater)
      )
      .field(
        newFieldDefinition()
          .type(new GraphQLNonNull(lineMigrationResultType))
          .name("migrateLine")
          .description("Migrate a line from one provider to another")
          .argument(
            GraphQLArgument.newArgument()
              .name(FIELD_INPUT)
              .type(new GraphQLNonNull(lineMigrationInputType))
          )
          .dataFetcher(lineMigrationFetcher)
      )
      .build();

    return mutationType;
  }
}
