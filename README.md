# Uttu
[![CircleCI](https://circleci.com/gh/entur/uttu/tree/master.svg?style=svg)](https://circleci.com/gh/entur/uttu/tree/master) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entur_uttu&metric=alert_status)](https://sonarcloud.io/dashboard?id=entur_uttu)

Back-end for Nplan, a simple timetable editor. Front-end is [Enki](https://github.com/entur/enki).

## Graphql 
https://api.dev.entur.io/timetable-admin/v1/flexible-lines/providers/graphql

## Running locally
### Build
You need Java 11, Maven 3 or higher, and a `settings.xml`-file that gives you access to the internal repo (ask a friend).

### Database

#### Via local installation of database
Install Postgres, either via [brew](https://gist.github.com/ibraheem4/ce5ccd3e4d7a65589ce84f2a3b7c23a3), [postgresapp.com](http://postgresapp.com/),
or [postgresql.org](https://www.postgresql.org/download/).

For brew install, also install PostGIS: `brew install postgis` (should be included in Postgres.app and EnterpriseDB Postgres installations).
Create folder for database `mkdir db`
Initialise database `initdb ./db`
Start db service: `pg_ctl -D ./db/ -l logfile start`
Create db with your username: `createdb `whoami``
Create uttu database: `createdb uttu`

Create uttu user: `createuser -s uttu` (you might also have to create a `postgres` user)

Run the [script](./src/main/resources/db_init.sh)


#### Via Docker

Install Docker.

```bash
docker run --name=uttu -d -e POSTGRES_USER=uttu -e POSTGRES_PASS=uttu -e POSTGRES_DBNAME=uttu -e ALLOW_IP_RANGE=0.0.0.0/0 -p 5432:5432 -v db_local:/var/lib/postgresql --restart=always kartoza/postgis:9.6-2.4
```

Now a Docker container is running in the background. Check its status with `docker ps`.

To stop, find its ID from `docker ps`, and run `docker stop theid` (beginning of hash). To restart it, find the ID from `docker container list` and run `docker restart theid`.

Run the [script](./src/main/resources/db_init.sh).


### Run
**IntelliJ**: Right-click on `App.java` and choose Run (or Cmd+Shift+F10). Open Run -> Edit configurations, choose the
correct configuration (Spring Boot -> App), and add `local` to Active profiles. Save the configuration.

If you want to run with google pubsub emulator also add `google-pubsub-emulator` to Active profiles.

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
