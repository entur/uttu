package no.entur.uttu.integration;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class InputGenerators {

  public static @NotNull Map<String, Object> generateDayTypeInput() {
    return Map.of(
      "daysOfWeek",
      List.of("monday", "tuesday", "wednesday", "thursday", "friday"),
      "dayTypeAssignments",
      Map.of(
        "operatingPeriod",
        Map.of("fromDate", "2020-04-01", "toDate", "2020-05-01"),
        "isAvailable",
        true
      )
    );
  }

  public static @NotNull Map<String, Object> generateFixedLineInput(
    String name,
    String networkId,
    String dayTypeRef
  ) {
    return Map.of(
      "name",
      name,
      "publicCode",
      "TestFixedLine",
      "transportMode",
      "bus",
      "transportSubmode",
      "localBus",
      "networkRef",
      networkId,
      "operatorRef",
      "NOG:Operator:1",
      "journeyPatterns",
      List.of(
        Map.of(
          "pointsInSequence",
          List.of(
            Map.of(
              "quayRef",
              "NSR:Quay:494",
              "destinationDisplay",
              Map.of("frontText", "Første stopp")
            ),
            Map.of("quayRef", "NSR:Quay:563")
          ),
          "serviceJourneys",
          List.of(
            Map.of(
              "name",
              "Hverdager3-" + System.currentTimeMillis(),
              "dayTypesRefs",
              List.of(dayTypeRef),
              "passingTimes",
              List.of(
                Map.of("departureTime", "07:00:00"),
                Map.of("arrivalTime", "07:15:00")
              )
            )
          )
        )
      )
    );
  }

  public static @NotNull Map<String, String> generateNetworkInput(
    String name,
    String authorityRef
  ) {
    return Map.of("name", name, "authorityRef", authorityRef);
  }
}
