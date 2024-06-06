# Uttu
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/entur/uttu/tree/master.svg?style=svg&circle-token=a7e5de16c44926fd9d7dbb3e045dac39904005b2)](https://dl.circleci.com/status-badge/redirect/gh/entur/uttu/tree/master) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entur_uttu&metric=alert_status)](https://sonarcloud.io/dashboard?id=entur_uttu)

Back-end for Nplan, a simple timetable editor. Front-end is [Enki](https://github.com/entur/enki).

## Codestyle

Uttu uses [Prettier Java](https://github.com/jhipster/prettier-java). Use `mvn prettier:write` to reformat code before
pushing changes. You can also configure your IDE to reformat code when you save a file.

## Security

Running uttu with vanilla security features requires an OAuth2 issuer, which can be set with the following property:

```properties
uttu.security.jwt.issuer-uri=https://my-jwt-issuer
```

In addition, a UserContextService implementation must be selected. The following gives full access to all authenticated 
users:

```properties
uttu.security.user-context-service=full-access
```

### Run without authentication

For the purpose of running locally, authentication can be switched off altogether by combining the
full-access property above with the `local-no-authentication` profile.

## Organisation registry

Uttu needs an organisation registry in order to populate authority and operator references. You may
provide a NeTEx file of organisations with 

```properties
uttu.organisations.netex-file-uri=<path-to-file>
```

or provide your own implementation of the [`OrganisationRegistry`](src/main/java/no/entur/uttu/organisation/spi/OrganisationRegistry.java) interface.

Refer to [`src/test/resources/fixtures/organisations.xml`](src/test/resources/fixtures/organisations.xml) for an example 
of a NeTEx file with organisations.

## Stop place registry

Uttu needs a stop place registry in order to allow lookup of stop places from quay refs, used when creating
journey patterns with fixed transit stops, and with hail-and-ride areas.

You may provide a NeTEx file of stop places with

```properties
uttu.stopplace.netex-file-uri=<path-to-file>
```

or provide your own implementation of the [`StopPlaceRegistry`](src/main/java/no/entur/uttu/stopplace/spi/StopPlaceRegistry.java) interface.

Refer to [`src/test/resources/fixtures/stopplace.xml`](src/test/resources/fixtures/stopplace.xml) for an example of a 
NeTEx file with stop places.

## Optional export notification message

If you want to notify an external system about a NeTEx file export, you can
provide an implementation of the `MessagingService` interface –– see
`src/main/java/no/entur/uttu/export/messaging/spi/MessagingService.java`.

The default MessagingService implementation is a noop.

## Disable Google PubSub autoconfiguration

If you don't use Google PubSub, set this property:

    # This property is needed to avoid pubsub autoconfiguration
    spring.cloud.gcp.pubsub.enabled=false

## Disable AWS S3 autoconfiguration

If you don't use AWS S3 through AWSpring, set this property:

    # This property is needed to avoid AWS S3 autoconfiguration
    spring.cloud.aws.s3.enabled=false

This feature is automatically enabled due to transitive dependency activation from 
`spring-cloud-aws-starter-secrets-manager` and should never be needed when running uttu in any of the available 
configurations.

## Running locally

### Local Environment through Docker Compose

Uttu has [docker-compose.yml](./docker-compose.yml) which contains all necessary dependent services for running uttu in
various configurations. It is assumed this environment is always running when the service is being run locally 
(see below).

> **Note!** This uses the compose version included with modern versions of Docker, not the separately installable 
> `docker-compose` command.

All Docker Compose commands run in relation to the `docker-compose.yml` file located in the same directory in which the 
command is executed.

```shell
# run with defaults - use ^C to shutdown containers
docker compose up
# run with additional profiles, e.g. with LocalStack based AWS simulator
docker compose --profile aws up
# run in background
docker compose up -d # or --detach
# shutdown containers
docker compose down
# shutdown containers included in specific profile
docker compose --profile aws down
```

See [Docker Compose reference](https://docs.docker.com/compose/reference/) for more details.

### Build

To build the project from source, you need Java 21 and Maven 3.

### Database

#### Via Docker Compose

Ensure database is up with
```shell
docker compose up -d
```

Run the [database initialization script](./src/main/resources/db_init.sh).

```shell
(cd src/main/resources && ./db_init.sh)
```

### Run

**IntelliJ**: Right-click on `App.java` and choose Run (or Cmd+Shift+F10). Open Run -> Edit configurations, choose the
correct configuration (Spring Boot -> App), and add `local` to Active profiles. Save the configuration.

**Command line**: `mvn spring-boot:run`

Uttu web server will expose APIs on port 11701 by default.

### GraphQL endpoints

Provider-independent GraphQL endpoint:

    /services/flexible-lines/providers/graphql

Provider-specific GraphQL endpoint (replace {providerCode} with provider's codespace code):

    /services/flexible-lines/{providerCode}/graphql

## NeTEx Export

Uttu exports (via provider specific GraphQL API) generated NeTEx file to a blobstore repository.
Choose one from the available implementations with matching profile:

- `in-memory-blobstore` - stores exports in memory, exports are lost on restarts, suitable for development and testing
- `disk-blobstore` - stores exports on disk
- `gcp-blobstore` - stores exports in Google Cloud Storage, requires additional configuration
- `s3-blobstore` - stores exports in Amazon Web Services Simple Storage Service (AWS S3), requires additional 
  configuration

Alternatively, provide a
[`BlobStoreRepository`](https://github.com/entur/rutebanken-helpers/blob/master/storage/src/main/java/org/rutebanken/helper/storage/repository/BlobStoreRepository.java)
bean for custom behavior.

The following endpoint exposes exports for direct download:

    /services/flexible-lines/{providerCode}/export/

## Error code extension

Some errors are augmented with a code extension. See [`ErrorCodeEnumeration`](src/main/java/no/entur/uttu/error/codes/ErrorCodeEnumeration.java) 
for complete list of codes.

The code is optionally accompanied by a key-value metadata map under the `metadata` extension.

The extension appears in the response as follows (example is trimmed):

```json
{
    "errors": [
        {
            "extensions": {
                "code": "ORGANISATION_NOT_VALID_OPERATOR"
            }
        }
    ]
}
```

With metadata: 
       
```json
{
    "errors": [
        {
            "extensions": {
                "code": "ENTITY_IS_REFERENCED",
                "metadata": {
                    "numberOfReferences": 1
                 }
            }
        }
    ]
}
``` 
