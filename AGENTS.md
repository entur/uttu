# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Project Overview

Uttu is the back-end for Nplan, a timetable editor for flexible transport services. It provides GraphQL APIs for managing lines (both fixed and flexible), journey patterns, service journeys, day types, and exports to NeTEx format. The front-end is Enki.

**Technology Stack:** Spring Boot 3.x, Java 21, PostgreSQL with PostGIS, JPA/Hibernate, GraphQL Java, Jersey (JAX-RS), NeTEx XML model

## Build and Development Commands

### Building
```bash
./mvnw clean install           # Full build with tests
./mvnw clean package          # Build without installing to local repo
./mvnw prettier:write         # Format code (runs automatically on validate phase)
```

### Code Formatting
Uttu uses Prettier Java for code formatting. The prettier plugin runs automatically during the `validate` phase, but you can run it manually:
```bash
./mvnw prettier:write         # Format all Java files
./mvnw -PprettierSkip install # Skip prettier formatting
```

Configuration: 90 character line width, 2-space indentation, no tabs.

### Running Tests
```bash
./mvnw test                           # Run all tests
./mvnw test -Dtest=ClassName          # Run specific test class
./mvnw test -Dtest=ClassName#methodName  # Run specific test method
```

Tests use Testcontainers for PostgreSQL, Google Cloud Pub/Sub emulator, and LocalStack (AWS).

### Running Locally

**Prerequisites:** Docker Compose must be running for local development.

1. Start dependencies:
```bash
docker compose up -d
```

2. Initialize database:
```bash
src/main/resources/db_init.sh
```

3. Run application:
```bash
# Using Maven
./mvnw spring-boot:run

# Full quickstart with all settings
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,local-disk-blobstore,local-no-authentication \
    -Dspring-boot.run.jvmArguments='
      -Duttu.organisations.netex-file-uri=src/test/resources/fixtures/organisations.xml
      -Duttu.stopplace.netex-file-uri=src/test/resources/fixtures/stopplaces.xml
      -Dblobstore.gcs.container.name=foobar
      -Duttu.security.user-context-service=full-access
      -Dspring.cloud.aws.s3.enabled=false
      -Dspring.cloud.gcp.pubsub.enabled=false
      -Dspring.cloud.aws.secretsmanager.enabled=false'
```

Application runs on `http://localhost:11701` by default.

### GraphQL Endpoints
- Provider-independent: `/services/flexible-lines/providers/graphql`
- Provider-specific: `/services/flexible-lines/{providerCode}/graphql`
- Export download: `/services/flexible-lines/{providerCode}/export/`

## Architecture

### Package Structure

Key packages under `no.entur.uttu`:
- **config** - Configuration (Context for ThreadLocal multi-tenancy, timezone, geometry factory)
- **error** - Error handling (coded errors/exceptions, error codes enumeration)
- **export** - NeTEx export system (blob storage, messaging, netex generation, export service)
- **graphql** - GraphQL API layer (schema definitions, fetchers, mappers, resources, scalars)
- **model** - Domain entities (JPA entities, all in `no.entur.uttu.model`)
- **repository** - JPA repositories with custom base class for provider filtering
- **organisation** - Organisation registry integration (authorities, operators)
- **stopplace** - Stop place registry integration
- **security** - Security configuration and user context services
- **service** - Business logic services
- **routing** - OSRM routing integration for service links

### Multi-Tenancy via Provider Context

Uttu implements multi-tenancy through a **Provider** system. Each provider has:
- A unique code (used in URLs and as tenant identifier)
- A codespace (determines NeTEx namespace for XML exports)
- Isolated data through provider-scoped entities

The `Context` class (using ThreadLocal) stores the current provider code and username for each request. All entities extending `ProviderEntity` are automatically scoped to a provider.

**Key Classes:**
- `no.entur.uttu.config.Context` - ThreadLocal provider/user context
- `no.entur.uttu.model.Provider` - Provider entity
- `no.entur.uttu.model.ProviderEntity` - Base class for all provider-scoped entities
- `no.entur.uttu.repository.generic.ProviderEntityRepositoryImpl` - Custom JPA repository base class that automatically filters by provider

### GraphQL Schema Architecture

GraphQL is the primary API interface. The schema is **programmatically defined** (not schema-first):

**Schema Definition:**
- `LinesGraphQLSchema.java` - Defines the complete GraphQL schema for flexible/fixed lines using GraphQL Java builders
- `ProviderGraphQLSchema.java` - Defines provider-level schema
- `GraphQLNames.java` - Constants for all field names to ensure consistency

**Data Layer:**
- Fetchers in `no.entur.uttu.graphql.fetchers.*` implement query/mutation logic
  - `AbstractProviderEntityUpdater<T>` - Base class for mutations, handles both save and delete (distinguished by field name starting with "delete")
- Mappers in `no.entur.uttu.graphql.mappers.*` convert between GraphQL inputs and JPA entities
  - `AbstractProviderEntityMapper<T>` - Base class for entity mapping, uses `ArgumentWrapper` for field extraction
  - For new entities, creates instance and sets provider from Context. For updates, fetches existing by netexId

**Request Flow:**
1. JAX-RS resource (`LinesGraphQLResource`) receives request at `/{providerCode}/graphql`
2. Security check via `@PreAuthorize` validates user access to provider
3. `Context.setProvider(providerCode)` sets ThreadLocal for tenant isolation
4. `GraphQLResourceHelper` wraps execution in Spring transaction
5. DataFetcher called → Mapper converts input → Repository saves (auto-filtered by provider)
6. Transaction commits, response returned

**Important:** When adding new fields or types to the GraphQL API, you must update the Java schema definition in `LinesGraphQLSchema.java` by creating new `GraphQLObjectType` and `GraphQLInputObjectType` definitions.

### Domain Model

Core entities (all in `no.entur.uttu.model`):
- **Line** (abstract) → **FixedLine** and **FlexibleLine** - Transport line definitions
- **JourneyPattern** - Ordered sequence of stop points for a line
- **ServiceJourney** - Specific journey on a pattern with passing times
- **StopPointInJourneyPattern** - Stop point in a journey pattern (references either fixed stops via quayRef or FlexibleStopPlace)
- **FlexibleStopPlace** - Flexible service area (polygon-based or hail-and-ride)
- **DayType** - Defines which days services operate (days of week + date assignments)
- **Network** - Groups lines by transport authority
- **Export** - Represents a NeTEx export job with status tracking

All provider-scoped entities extend `ProviderEntity`, which:
- Auto-generates NeTEx IDs in format `{codespace}:{EntityType}:{UUID}`
- Enforces provider isolation via `@PreUpdate` verification
- Maintains version tracking for optimistic locking

### NeTEx Export System

Exports generate NeTEx XML files conforming to the Nordic NeTEx Profile for flexible transport.

**Flow:**
1. GraphQL mutation triggers `ExportUpdater`
2. Export entity created with status `IN_PROGRESS`
3. Background job processes export via `ExportService.exportDataSet()`:
   - Queries all valid lines for provider and date range (optionally filtered by `exportLineAssociations`)
   - `NetexLineFileProducer` generates NeTEx XML for each line
   - `NetexCommonFileProducer` generates shared data (authorities, operators, networks, day types, stop places)
   - Marshals to XML using JAXB
   - Optional NeTEx schema validation
   - `DataSetProducer` packages files into ZIP
4. Uploads to BlobStore (production + backup files)
5. Optional notification via `MessagingService.notifyExport()` (e.g., to Marduk)
6. Status updated to `SUCCESS` or `FAILED` with validation messages

**BlobStore profiles** (choose one):
- `in-memory-blobstore` - For testing
- `local-disk-blobstore` - Local filesystem storage
- `gcp-blobstore` - Google Cloud Storage
- `s3-blobstore` - Amazon S3

Export can optionally generate `ServiceLink` geometries using OSRM routing if `generateServiceLinks=true`.

### Repository Layer

Custom repository base class `ProviderEntityRepositoryImpl` automatically:
- Sets provider from Context on entity creation
- Filters all queries by current provider (tenant isolation)
- Handles provider verification on updates

When creating new repositories for provider-scoped entities, extend `ProviderEntityRepository<T>`.

### Security and Authentication

Security is pluggable via profiles:
- OAuth2/JWT validation via `uttu.security.jwt.issuer-uri`
- User context service determines permissions (`full-access` gives all permissions)
- `local-no-authentication` profile disables auth for development
- Provider access controlled via UserContextService implementations

### External Integrations

**Organisation Registry:** Required for populating authority/operator references
- Configure via `uttu.organisations.netex-file-uri` (file) or `uttu.organisations.netex-http-uri` (HTTP)
- Can override organisation IDs via `no.entur.uttu.organisations.overrides` property
- See `no.entur.uttu.organisation.spi.OrganisationRegistry`

**Stop Place Registry:** Required for looking up fixed stop places (quays)
- Configure via `uttu.stopplace.netex-file-uri`
- See `no.entur.uttu.stopplace.spi.StopPlaceRegistry`

**Messaging Service:** Optional notification on export completion
- Implement `no.entur.uttu.export.messaging.spi.MessagingService`
- Default implementation is no-op

## Important Patterns and Conventions

### Provider Context Management
Always ensure Context is properly set for provider-scoped operations. The GraphQL resources handle this automatically, but when adding new endpoints or background jobs, use:
```java
Context.setProvider(providerCode);
try {
    // Your provider-scoped operations
} finally {
    Context.clear();
}
```

### Entity Lifecycle
- Entities extending `ProviderEntity` get auto-generated NeTEx IDs on persist (format: `{codespace}:{EntityType}:{UUID}`)
- `@PrePersist` sets created timestamp and user from `Context.getVerifiedUsername()`
- `@PreUpdate` verifies provider matches context and sets changed timestamp/user
- Provider verification happens automatically on update via `ProviderEntity.verifyProvider()`
- Use optimistic locking (`@Version Long version`) to prevent concurrent modification conflicts

### GraphQL Error Codes
Errors may include extension codes from `ErrorCodeEnumeration`. Add new error codes there when creating validation logic.

### Database Migrations
Use Flyway migrations in `src/main/resources/db/migration/`. Name format: `V{number}__{description}.sql`

### Testing Strategy
- Use `@SpringBootTest` with testcontainers for integration tests
- Test fixtures in `src/test/resources/fixtures/`
- GraphQL tests should use `spring-graphql-test` framework

## Common Tasks

### Adding a New GraphQL Field
1. Update the `GraphQLObjectType` definition in `LinesGraphQLSchema.java` using `newFieldDefinition()`
2. Add corresponding field to the entity class
3. Update mapper's `populateEntityFromInput()` method if needed (use `ArgumentWrapper` for extraction)
4. Create Flyway migration if adding to persistence: `src/main/resources/db/migration/V{number}__{description}.sql`
5. Format code with `./mvnw prettier:write`

### Adding a New Entity Type
1. Create entity class extending `ProviderEntity` (for provider-scoped) or `IdentifiedEntity`
2. Create repository interface extending `ProviderEntityRepository<T>` (auto-gets provider filtering)
3. Create GraphQL types in `LinesGraphQLSchema.java`:
   - `GraphQLObjectType` for output type using `newObject()`
   - `GraphQLInputObjectType` for mutation input using `newInputObject()`
4. Create mapper extending `AbstractProviderEntityMapper<T>` in `graphql.mappers` package
   - Implement `createNewEntity()` and `populateEntityFromInput()`
5. Create updater extending `AbstractProviderEntityUpdater<T>` in `graphql.fetchers` package
   - Wire mapper + repository to parent constructor
6. Add query/mutation fields to schema's query/mutation objects with appropriate data fetchers
7. Create Flyway database migration

### Running with Different Profiles
Available Spring profiles:
- `local` - Local development (see `application-local.properties`)
- `local-no-authentication` - Disable authentication
- `local-disk-blobstore` - Store exports on local disk
- `in-memory-blobstore` - In-memory export storage
- `gcp-blobstore` - Google Cloud Storage
- `s3-blobstore` - AWS S3 storage

Combine profiles with comma separation: `-Dspring-boot.run.profiles=local,local-no-authentication`

## Additional Codespaces in Export
Configure additional NeTEx codespaces for exports:
```properties
no.entur.uttu.codespaces.additional.nsr=http://www.rutebanken.org/ns/nsr
no.entur.uttu.codespaces.additional.nog=http://www.rutebanken.org/ns/nog
```
