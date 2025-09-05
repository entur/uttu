# Uttu
[![CI Build](https://github.com/entur/uttu/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/entur/uttu/actions/workflows/ci.yml) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entur_uttu&metric=alert_status)](https://sonarcloud.io/dashboard?id=entur_uttu) [![codecov](https://codecov.io/gh/entur/uttu/branch/master/graph/badge.svg?token=NC29GYIN2K)](https://codecov.io/gh/entur/uttu)

Back-end for Nplan, a simple timetable editor. Front-end is [Enki](https://github.com/entur/enki).

## Codestyle

Uttu uses [Prettier Java](https://github.com/jhipster/prettier-java). Use `./mvnw prettier:write` to reformat code before
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

Alternatively, organisations data can be fetched over HTTP in NeTEx xml format:

```properties
uttu.organisations.netex-http-uri=<path-to-http-endpoint>
```

Note that the HTTP strategy requires an organisations api WebClient bean called
`orgRegisterClient`. A basic default is provided, but if you need anything more
than that, you should provide your own bean.

Refer to [`src/test/resources/fixtures/organisations.xml`](src/test/resources/fixtures/organisations.xml) for an example
of a NeTEx file with organisations.

You can also provide your own implementation of the [`OrganisationRegistry`](src/main/java/no/entur/uttu/organisation/spi/OrganisationRegistry.java)
interface.

### Override exported authority or operator id

It is possible to override the exported authority or operator id for a given organisation like this:

```properties
no.entur.uttu.organisations.overrides={\
    'SomeInternalId': {\
        'Operator': 'KOL:Operator:BAR',\
        'Authority': 'KOL:Authority:BAR'\
    }\
}
```

This is useful if the organisation registry uses internal IDs, but you need to map them to "NeTEx" IDs.

You can also map an existing NeTEx ID to something else:

```properties
no.entur.uttu.organisations.overrides={\
    'FOO:Operator:BAR': {\
        'Operator': 'FOO:Operator:BAZ'\
    }\
}
```

Overrides can also be provided per provider/codespace. The following override only applies
within the FOO codespace during export.

```properties
no.entur.uttu.organisations.overrides.provider={\
    'FOO': {\
        'OldId': 'FOO:Operator:BAZ'\
    }\
}
```



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

See [Docker Compose reference](https://docs.docker.com/compose/reference/) for official reference.

See [Supported Docker Compose Profiles](#supported-docker-compose-profiles) for more information on provided profiles.

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
(src/main/resources/db_init.sh)
```

### Run

**IntelliJ**: Right-click on `App.java` and choose Run (or Cmd+Shift+F10). Open Run -> Edit configurations, choose the
correct configuration (Spring Boot -> App), and add `local` to Active profiles. Save the configuration.

**Command line**: `./mvnw spring-boot:run`

Uttu web server will expose APIs on port 11701 by default.

### Local quickstart

```
docker compose up -d
src/main/resources/db_init.sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,local-disk-blobstore,local-no-authentication \
    -Dspring-boot.run.jvmArguments='
      -Duttu.organisations.netex-file-uri=src/test/resources/fixtures/organisations.xml
      -Duttu.stopplace.netex-file-uri=src/test/resources/fixtures/stopplaces.xml
      -Dblobstore.gcs.container.name=foobar
      -Duttu.security.user-context-service=full-access
      -Dspring.cloud.aws.s3.enabled=false
      -Dspring.cloud.gcp.pubsub.enabled=false
      -Dspring.cloud.aws.secretsmanager.enabled=false'
curl -X POST http://localhost:11701/services/flexible-lines/providers/graphql -H "Content-Type: application/json" \
     -d '{"query": "{ __schema { types { name fields { name } } } }"}' | jq .
```

### GraphQL endpoints

Provider-independent GraphQL endpoint:

    /services/flexible-lines/providers/graphql

Provider-specific GraphQL endpoint (replace {providerCode} with provider's codespace code):

    /services/flexible-lines/{providerCode}/graphql

## NeTEx Export

Uttu exports (via provider specific GraphQL API) generated NeTEx file to a blobstore repository.
Choose one from the available implementations with matching profile:

- `in-memory-blobstore` - stores exports in memory, exports are lost on restarts, suitable for development and testing
- `local-disk-blobstore` - stores exports on disk
- `gcp-blobstore` - stores exports in Google Cloud Storage, requires additional configuration
- `s3-blobstore` - stores exports in Amazon Web Services Simple Storage Service (AWS S3), requires additional 
  configuration

Alternatively, provide a
[`BlobStoreRepository`](https://github.com/entur/rutebanken-helpers/blob/master/storage/src/main/java/org/rutebanken/helper/storage/repository/BlobStoreRepository.java)
bean for custom behavior.

The following endpoint exposes exports for direct download:

    /services/flexible-lines/{providerCode}/export/

### Additional Codespaces in Export

By default, the NeTEx export includes the provider's codespace in the CompositeFrame. You can configure additional 
codespaces to be included in the export using the property `no.entur.uttu.codespaces.additional` as follows:

```properties
no.entur.uttu.codespaces.additional.[code]=[xml namespace uri]
```

Example:

```properties
no.entur.uttu.codespaces.additional.nsr=http://www.rutebanken.org/ns/nsr
no.entur.uttu.codespaces.additional.nog=http://www.rutebanken.org/ns/nog
```

This allows you to add any required codespace references to the exported NeTEx file.

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

## Supported Docker Compose Profiles

Uttu's [`docker-compose.yml`](./docker-compose.yml) comes with built-in profiles for various use cases. The profiles are
mostly optional, default profile contains all mandatory configuration while the named profiles add features on top of 
that. You can always active zero or more profiles at the same time, e.g.

```shell
docker compose --profile aws --profile routing up
# or
COMPOSE_PROFILES=aws,routing docker compose up
```

### Default profile (no activation key)

Starts up PostGIS server with settings matching the ones in [`application-local.properties`](./src/main/resources/application-local.properties).

### `aws` profile

Starts up [LocalStack](https://www.localstack.cloud/) meant for developing AWS specific features.

See also [Disable AWS S3 Autoconfiguration](#disable-aws-s3-autoconfiguration), [NeTEx Export](#netex-export).

### `routing` profile

Provides pre-made configuration for running (presumably) [OSRM Server](https://project-osrm.org/) to be used for 
navigation routing features. **However**, the image used is not the default, but instead named `osrm-routing` to allow 
the use of custom internal/external image with precalculated data. As this is very dependent on how the image is 
created, we recommend that you use the provided `Dockerfile` below as base and adapt accordingly:

```Dockerfile
ARG IMAGE_TAG=v5.27.1
FROM ghcr.io/project-osrm/osrm-backend:$IMAGE_TAG AS build

RUN apt-get -y update \
    && apt-get -y install curl \
    && apt-get clean
RUN mkdir /data \
    && cd /data \
    && curl -O 'https://download.geofabrik.de/europe/norway-latest.osm.pbf'
RUN ls -lah /data
RUN osrm-extract -p /opt/car.lua /data/norway-latest.osm.pbf
RUN osrm-partition /data/norway-latest.osrm
RUN osrm-customize /data/norway-latest.osrm

FROM ghcr.io/project-osrm/osrm-backend:$IMAGE_TAG
COPY --from=build /data /data
ENTRYPOINT osrm-routed --algorithm mld /data/norway-latest.osrm
```

Save the above into `Dockerfile-osrm-routing` and build the image with
```shell
docker build --platform linux/amd64 -f Dockerfile-osrm-routing -t osrm-routing .

See also [Running locally](#running-locally).
