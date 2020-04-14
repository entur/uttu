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

package no.entur.uttu.graphql

import io.restassured.response.ValidatableResponse

abstract class AbstractFixedLinesGraphQLIntegrationTest extends AbstractGraphQLResourceIntegrationTest {
    protected String getUrl() {
        return "/services/flexible-lines/tst/graphql"
    }



    ValidatableResponse createFixedLine(String name) {
        String networkId = getNetworkId(createNetwork(name))

        String query = """
            mutation MutateFixedLine(\$input: FixedLineInput!) {
                mutateFixedLine(input: \$input) {
                    id
                    name
                    journeyPatterns {
                        id
                        pointsInSequence {
                            quayRef   
                        }
                      serviceJourneys {
                        id
                        dayTypes {
                          daysOfWeek
                          dayTypeAssignments {
                            date
                            operatingPeriod {
                              fromDate
                              toDate
                            }
                            isAvailable
                          }
                        }
                        passingTimes {
                          departureTime
                          arrivalTime
                        }
                      }
                   }
              }
            }
        """

        String variables = """
            {
              "input": {
                "name": "$name",
                "description": "Testing fixed line mutation from insomnia client",
                "publicCode": "TestFixedLine",
                "transportMode": "bus",
                "transportSubmode": "localBus",
                "networkRef": "$networkId",
                "operatorRef": "22",
                "journeyPatterns": [
                  {
                    "pointsInSequence": [
                      {
                        "quayRef": "NSR:Quay:69",
                        "destinationDisplay": {
                          "frontText": "Første stopp"
                        }
                      },
                      {
                        "quayRef": "NSR:Quay:70"
                      }
                    ],
                    "serviceJourneys": [
                      {
                        "name": "Hverdager3",
                        "dayTypes": {
                          "daysOfWeek": [
                            "monday",
                            "tuesday",
                            "wednesday",
                            "thursday",
                            "friday"
                          ],
                          "dayTypeAssignments": {
                            "operatingPeriod": {
                              "fromDate": "2020-04-01",
                              "toDate": "2020-05-01"
                            },
                            "isAvailable": true
                          }
                        },
                        "passingTimes": [
                          {
                            "departureTime": "07:00:00"
                          },
                          {
                            "arrivalTime": "07:15:00"
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            }
        """

        executeGraphQL(query, variables)
    }
}
