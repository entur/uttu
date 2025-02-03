package no.entur.uttu.integration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class InputGenerators {

  public static String generateDateString(LocalDate date) {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
  }

  public static @NotNull Map<String, Object> generateDayTypeInput() {
    return Map.of(
      "daysOfWeek",
      List.of("monday", "tuesday", "wednesday", "thursday", "friday"),
      "dayTypeAssignments",
      Map.of(
        "operatingPeriod",
        Map.of(
          "fromDate",
          generateDateString(LocalDate.now()),
          "toDate",
          generateDateString(LocalDate.now().plusYears(1))
        ),
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

  public static @NotNull Map<String, String> generateBrandingInput(String name) {
    return Map.of("name", name);
  }

  public static @NotNull Map<String, String> generateNetworkInput(
    String name,
    String authorityRef
  ) {
    return Map.of("name", name, "authorityRef", authorityRef);
  }

  public static @NotNull Map<String, Object> generateFlexibleLineInput(
    String name,
    String flexibleLineType,
    String operatorRef,
    String networkId,
    String flexAreaStopPlaceId,
    String hailAndRideStopPlaceId,
    String dayTypeRef
  ) {
    return Map.of(
      "name",
      name,
      "publicCode",
      "TestFlexibleLine",
      "flexibleLineType",
      flexibleLineType,
      "transportMode",
      "bus",
      "transportSubmode",
      "localBus",
      "networkRef",
      networkId != null ? networkId : "TST:Network:1",
      "operatorRef",
      operatorRef,
      "bookingArrangement",
      generateBookingInformationInputStructure(),
      "journeyPatterns",
      List.of(
        Map.of(
          "pointsInSequence",
          List.of(
            Map.of(
              "flexibleStopPlaceRef",
              flexAreaStopPlaceId != null
                ? flexAreaStopPlaceId
                : "TST:FlexibleStopPlace:1",
              "destinationDisplay",
              Map.of("frontText", "krokkus")
            ),
            Map.of(
              "flexibleStopPlaceRef",
              hailAndRideStopPlaceId != null
                ? hailAndRideStopPlaceId
                : "TST:FlexibleStopPlace:2"
            )
          ),
          "serviceJourneys",
          List.of(
            Map.of(
              "name",
              name + "_TestServiceJourney",
              "dayTypesRefs",
              List.of(dayTypeRef),
              "passingTimes",
              List.of(
                Map.of("departureTime", "16:00:00"),
                Map.of("arrivalTime", "16:30:00")
              )
            )
          )
        )
      )
    );
  }

  private static @NotNull Map<String, Object> generateBookingInformationInputStructure() {
    return Map.of(
      "minimumBookingPeriod",
      "PT2H",
      "bookingNote",
      "Notis for booking av linje",
      "bookingMethods",
      List.of("online", "callDriver"),
      "bookingAccess",
      "authorisedPublic",
      "buyWhen",
      List.of("afterBoarding", "beforeBoarding"),
      "bookingContact",
      Map.of(
        "contactPerson",
        "Linjemann Book-Jensen",
        "furtherDetails",
        "Linje: Ytterligere detaljer",
        "email",
        "line@booking.com",
        "phone",
        "line + 577",
        "url",
        "http://line.booking.com"
      )
    );
  }

  public static @NotNull Map<String, Object> generateFlexibleStopPlaceWithFlexibleAreaInput(
    String name
  ) {
    return Map.of(
      "name",
      name,
      "transportMode",
      "water",
      "keyValues",
      List.of(Map.of("key", "foo", "values", List.of("bar", "baz"))),
      "flexibleArea",
      Map.of(
        "polygon",
        Map.of(
          "type",
          "Polygon",
          "coordinates",
          List.of(
            List.of(10.0, 60.0),
            List.of(10.1, 60.0),
            List.of(10.1, 60.1),
            List.of(10.0, 60.0)
          )
        )
      )
    );
  }

  public static @NotNull Map<String, Object> generateFlexibleStopPlaceWithFlexibleAreasInput(
    String name
  ) {
    return Map.of(
      "name",
      name,
      "transportMode",
      "water",
      "keyValues",
      List.of(Map.of("key", "foo", "values", List.of("bar", "baz"))),
      "flexibleAreas",
      List.of(
        Map.of(
          "keyValues",
          List.of(Map.of("key", "foo", "values", List.of("bar"))),
          "polygon",
          Map.of(
            "type",
            "Polygon",
            "coordinates",
            List.of(
              List.of(10.0, 60.0),
              List.of(10.1, 60.0),
              List.of(10.1, 60.1),
              List.of(10.0, 60.0)
            )
          )
        )
      )
    );
  }

  public static @NotNull Map<String, Object> generateFlexibleStopPlaceWithHailAndRideAreaInput(
    String name
  ) {
    return Map.of(
      "name",
      name,
      "description",
      "hail and ride desc",
      "transportMode",
      "bus",
      "keyValues",
      List.of(Map.of("key", "foo", "values", List.of("bar", "baz"))),
      "hailAndRideArea",
      Map.of("startQuayRef", "NSR:Quay:565", "endQuayRef", "NSR:Quay:494")
    );
  }
}
