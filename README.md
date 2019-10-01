# Uttu [![CircleCI](https://circleci.com/gh/entur/uttu/tree/master.svg?style=svg)](https://circleci.com/gh/entur/uttu/tree/master)

Back end for FlexibleLine with FlexibleAreas timetable data editor and export module

## Graphql 
https://api.dev.entur.io/timetable-admin/v1/flexible-lines/providers/graphql

When running locally
http://localhost:11701/services/flexible-lines/rut/graphql

### Error code extension

Some errors are augmented with a code extension. See [ErrorCodeEnumeration](src/main/java/no/entur/uttu/error/ErrorCodeEnumeration.java) for complete list of codes.

The extension appears in the response as follows (example is trimmed):

        {
            "errors": [
                {
                    "extensions": {
                        "code": "INVALID_OPERATOR"
                    }
                }
            ]
        }

## Netex Export
This api exports generated netex file to gcp storage, which is used to build graph.
