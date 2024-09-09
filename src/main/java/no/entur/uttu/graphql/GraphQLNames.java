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

package no.entur.uttu.graphql;

public class GraphQLNames {

  public static final String FIELD_INPUT = "input";
  public static final String FIELD_ID = "id";
  public static final String FIELD_IDS = "ids";
  public static final String FIELD_VERSION = "version";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_DESCRIPTION = "description";
  public static final String FIELD_PRIVATE_CODE = "privateCode";
  public static final String FIELD_PUBLIC_CODE = "publicCode";
  public static final String FIELD_CREATED = "created";
  public static final String FIELD_CREATED_BY = "createdBy";
  public static final String FIELD_CHANGED = "changed";
  public static final String FIELD_CHANGED_BY = "changedBy";
  public static final String FIELD_TRANSPORT_MODE = "transportMode";
  public static final String FIELD_TRANSPORT_SUBMODE = "transportSubmode";
  public static final String FIELD_OPERATOR_REF = "operatorRef";
  public static final String FIELD_POINTS_IN_SEQUENCE = "pointsInSequence";
  public static final String FIELD_BOOKING_ARRANGEMENT = "bookingArrangement";
  public static final String FIELD_NOTICES = "notices";
  public static final String FIELD_FROM_DATE = "fromDate";
  public static final String FIELD_TO_DATE = "toDate";
  public static final String FIELD_KEY_VALUES = "keyValues";

  // Codespace
  public static final String FIELD_XMLNS = "xmlns";
  public static final String FIELD_XMLNS_URL = "xmlnsUrl";

  // Provider
  public static final String FIELD_CODE = "code";
  public static final String FIELD_CODE_SPACE = "codespace";
  public static final String FIELD_CODE_SPACE_XMLNS = "codespaceXmlns";

  // FlexibleStopPlace
  public static final String FIELD_FLEXIBLE_AREA = "flexibleArea";

  public static final String FIELD_FLEXIBLE_AREAS = "flexibleAreas";
  public static final String FIELD_HAIL_AND_RIDE_AREA = "hailAndRideArea";
  public static final String FIELD_POLYGON = "polygon";
  public static final String FIELD_START_QUAY_REF = "startQuayRef";
  public static final String FIELD_END_QUAY_REF = "endQuayRef";

  // Network
  public static final String FIELD_AUTHORITY_REF = "authorityRef";

  // Notice
  public static final String FIELD_TEXT = "text";

  // FlexibleLine
  public static final String FIELD_FLEXIBLE_LINE_TYPE = "flexibleLineType";
  public static final String FIELD_NETWORK = "network";
  public static final String FIELD_NETWORK_REF = "networkRef";
  public static final String FIELD_JOURNEY_PATTERNS = "journeyPatterns";

  // JourneyPattern
  public static final String FIELD_DIRECTION_TYPE = "directionType";
  public static final String FIELD_SERVICE_JOURNEYS = "serviceJourneys";

  // ServiceJourney
  public static final String FIELD_PASSING_TIMES = "passingTimes";

  // DayType
  public static final String FIELD_DAY_TYPES = "dayTypes";
  public static final String FIELD_DAY_TYPES_REFS = "dayTypesRefs";

  // StopPointInJourneyPattern
  public static final String FIELD_FLEXIBLE_STOP_PLACE = "flexibleStopPlace";
  public static final String FIELD_QUAY_REF = "quayRef";
  public static final String FIELD_FLEXIBLE_STOP_PLACE_REF = "flexibleStopPlaceRef";
  public static final String FIELD_DESTINATION_DISPLAY = "destinationDisplay";
  public static final String FIELD_FOR_ALIGHTING = "forAlighting";
  public static final String FIELD_FOR_BOARDING = "forBoarding";

  // TimetabledPassingTime
  public static final String FIELD_ARRIVAL_TIME = "arrivalTime";
  public static final String FIELD_ARRIVAL_DAY_OFFSET = "arrivalDayOffset";
  public static final String FIELD_DEPARTURE_TIME = "departureTime";
  public static final String FIELD_DEPARTURE_DAY_OFFSET = "departureDayOffset";
  public static final String FIELD_LATEST_ARRIVAL_TIME = "latestArrivalTime";
  public static final String FIELD_LATEST_ARRIVAL_DAY_OFFSET = "latestArrivalDayOffset";
  public static final String FIELD_EARLIEST_DEPARTURE_TIME = "earliestDepartureTime";
  public static final String FIELD_EARLIEST_DEPARTURE_DAY_OFFSET =
    "earliestDepartureDayOffset";

  // BookingArrangement
  public static final String FIELD_BOOKING_NOTE = "bookingNote";
  public static final String FIELD_BOOKING_CONTACT = "bookingContact";
  public static final String FIELD_BOOKING_METHODS = "bookingMethods";
  public static final String FIELD_BOOKING_ACCESS = "bookingAccess";
  public static final String FIELD_BOOK_WHEN = "bookWhen";
  public static final String FIELD_BUY_WHEN = "buyWhen";
  public static final String FIELD_EMAIL = "email";
  public static final String FIELD_URL = "url";
  public static final String FIELD_PHONE = "phone";
  public static final String FIELD_FURTHER_DETAILS = "furtherDetails";
  public static final String FIELD_CONTACT_PERSON = "contactPerson";
  public static final String FIELD_LATEST_BOOKING_TIME = "latestBookingTime";
  public static final String FIELD_MINIMUM_BOOKING_PERIOD = "minimumBookingPeriod";

  // DestinationDisplay
  public static final String FIELD_FRONT_TEXT = "frontText";

  // DayType
  public static final String FIELD_DAY_TYPE_ASSIGNMENTS = "dayTypeAssignments";
  public static final String FIELD_DAYS_OF_WEEK = "daysOfWeek";
  public static final String FIELD_NUMBER_OF_SERVICE_JOURNEYS = "numberOfServiceJourneys";

  // DayTypeAssignments
  public static final String FIELD_DATE = "date";
  public static final String FIELD_OPERATING_PERIOD = "operatingPeriod";
  public static final String FIELD_IS_AVAILABLE = "isAvailable";

  // Export
  public static final String FIELD_EXPORT_STATUS = "exportStatus";
  public static final String FIELD_MESSAGES = "messages";
  public static final String FIELD_MESSAGE = "message";
  public static final String FIELD_SEVERITY = "severity";
  public static final String FIELD_DRY_RUN = "dryRun";
  public static final String FIELD_DOWNLOAD_URL = "downloadUrl";
  public static final String FIELD_EXPORT_LINE_ASSOCIATIONS = "lineAssociations";

  // Export line association
  public static final String FIELD_LINE = "line";
  public static final String FIELD_LINE_REF = "lineRef";

  // Exported line statistics
  public static final String FIELD_LINE_NAME = "lineName";
  public static final String FIELD_LINE_TYPE = "lineType";
  public static final String FIELD_OPERATING_DATE_FROM = "operatingPeriodFrom";
  public static final String FIELD_OPERATING_DATE_TO = "operatingPeriodTo";
  public static final String FIELD_PROVIDER_CODE = "providerCode";
  public static final String FIELD_EXPORTED_DAY_TYPES_STATISTICS =
    "exportedDayTypesStatistics";
  public static final String FIELD_DAY_TYPE_NETEX_ID = "dayTypeNetexId";
  public static final String FIELD_START_DATE = "startDate";
  public static final String FIELD_LINES = "lines";
  public static final String FIELD_PUBLIC_LINES = "publicLines";
  public static final String FIELD_SERVICE_JOURNEY_NAME = "serviceJourneyName";

  // Key-values
  public static final String FIELD_KEY = "key";
  public static final String FIELD_VALUES = "values";

  public static final String FIELD_SEARCH_TEXT = "searchText";
}
