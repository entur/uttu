# Uttu
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/entur/uttu/tree/master.svg?style=svg&circle-token=a7e5de16c44926fd9d7dbb3e045dac39904005b2)](https://dl.circleci.com/status-badge/redirect/gh/entur/uttu/tree/master) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entur_uttu&metric=alert_status)](https://sonarcloud.io/dashboard?id=entur_uttu)

Back-end for Nplan, a simple timetable editor. Front-end is [Enki](https://github.com/entur/enki).

## Codestyle
Uttu uses [Prettier Java](https://github.com/jhipster/prettier-java). Use `mvn prettier:write` to reformat code before
pushing changes. You can also configure your IDE to reformat code when you save a file.

## Security

Running uttu with vanilla security features requires an Oauth2 issuer, which can be set with the following property:

    uttu.security.jwt.issuer-uri=https://my-jwt-issuer

In addition, a UserContextService implementation must be selected. The following gives full access to all authenticated users:

    uttu.security.user-context-service=full-access

### Run without authentication

For the purpose of running locally, authentication can be switched off altogether by combining the
full-access property above with the `local-no-authentication` profile.

## Running locally
### Build
To build the project from source, you need Java 21 and Maven 3.

To run the unit tests you need additionally to install the [Google PubSub emulator](https://cloud.google.com/pubsub/docs/emulator).  
The emulator can be installed locally with the following commands:
```
gcloud components install pubsub-emulator
gcloud components update
```
You can then locate the installation directory of the GCloud tool kit with the following command:

```
gcloud info --format="value(installation.sdk_root)"
```
The emulator executable is located under the sub-folder ```platform/pubsub-emulator/lib```  
and can be used in unit tests by setting the following property in the Spring Boot properties file:
```
entur.pubsub.emulator.path=/usr/lib/google-cloud-sdk/platform/pubsub-emulator/lib/cloud-pubsub-emulator-0.6.0.jar
```

### Database
#### Via Docker

Install Docker.

```bash
docker run --name=uttu -d -e POSTGRES_USER=uttu -e POSTGRES_PASSWORD=uttu -e POSTGRES_DB=uttu -p 5432:5432 -v db_local:/var/lib/postgresql --restart=always postgis/postgis:13-3.3
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

## Error code extension

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
