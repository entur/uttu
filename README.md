# Uttu [![CircleCI](https://circleci.com/gh/entur/uttu/tree/master.svg?style=svg)](https://circleci.com/gh/entur/uttu/tree/master)

Back end for [Flexible Transport Editor](https://github.com/entur/flexible-transport)

## Graphql 
https://api.dev.entur.io/timetable-admin/v1/flexible-lines/providers/graphql

## Running locally
### Build
You need Java 11, Maven 3 or higher, and a `settings.xml`-file that gives you access to the internal repo (ask a friend).

### Database
Install Postgres, either via [brew](https://gist.github.com/ibraheem4/ce5ccd3e4d7a65589ce84f2a3b7c23a3), [postgresapp.com](http://postgresapp.com/),
or [postgresql.org](https://www.postgresql.org/download/).

For brew install, also install PostGIS: `brew install postgis` (should be included in Postgres.app and EnterpriseDB Postgres installations).

Create uttu database: `createdb uttu`

Create uttu user: `createuser -s uttu` (you might also have to create a `postgres` user)

Run the [migrations](src/main/resources/db.migration) in order.

Run the initial [data import](src/main/resources/import.sql)

### Run
**IntelliJ**: Right-click on `App.java` and choose Run (or Cmd+Shift+F10). Open Run -> Edit configurations, choose the
correct configuration (Spring Boot -> App), and add `local` to Active profiles. Save the configuration.

**Command line**: `mvn spring-boot:run`

### GraphQL endpoint
http://localhost:11701/services/flexible-lines/rut/graphql

## Netex Export
This api exports generated netex file to gcp storage, which is used to build graph.

### Error code extension

Some errors are augmented with a code extension. See [ErrorCodeEnumeration](src/main/java/no/entur/uttu/error/ErrorCodeEnumeration.java) for complete list of codes.

The code is optionally accompanied by a key-value metadata map under the `metadata` extension.

The extension appears in the response as follows (example is trimmed):

        {
            "errors": [
                {
                    "extensions": {
                        "code": "ORGANISATION_NOT_VALID_OPERATOR"
                    }
                }
            ]
        }

With metadata: 
        
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

