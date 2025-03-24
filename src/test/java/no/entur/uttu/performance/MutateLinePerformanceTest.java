package no.entur.uttu.performance;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.entur.uttu.integration.AbstractGraphQLIntegrationTest;
import no.entur.uttu.stubs.UserContextServiceStub;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Performance test for the mutateLine GraphQL mutation.
 * This is a standalone application that measures the performance of saving a line.
 * Run with: mvn test -Dtest=MutateLinePerformanceTest
 *
 * The test uses TestContainers for PostgreSQL with PostGIS extension, which is configured in the application-test.properties file.
 * It measures the performance of the mutateLine GraphQL mutation by creating prerequisite entities and then timing the mutation operation.
 */
@ActiveProfiles({ "test", "in-memory-blobstore" })
public class MutateLinePerformanceTest extends AbstractGraphQLIntegrationTest {

  private static final Logger logger = LoggerFactory.getLogger(
    MutateLinePerformanceTest.class
  );
  private static final int WARMUP_ITERATIONS = 5;
  private static final int TEST_ITERATIONS = 20;

  @Value("${local.server.port}")
  private int port;

  @Autowired
  private GraphQlTester graphQlTester;

  @Autowired
  private UserContextServiceStub userContextServiceStub;

  /**
   * Setup method to initialize the test environment.
   */
  @Before
  public void setup() {
    userContextServiceStub.setPreferredName("Performance Tester");
    userContextServiceStub.setAdmin(false);
    userContextServiceStub.setHasAccessToProvider("tst", true);

    WebTestClient.Builder clientBuilder = WebTestClient
      .bindToServer()
      .baseUrl("http://localhost:" + port + "/services/flexible-lines/tst/graphql");

    graphQlTester =
      HttpGraphQlTester
        .builder(clientBuilder)
        .headers(headers -> headers.setBasicAuth("admin", "topsecret"))
        .build();
  }

  /**
   * Main performance test method.
   * This will create prerequisite entities, perform warm-up iterations,
   * and then measure the performance of the mutateLine operation.
   */
  @Test
  public void testCreateLinePerformance() {
    logger.info("Starting performance test on port {}", port);

    // Create prerequisite entities
    String networkId = createNetwork();
    String dayTypeId = createDayType();
    String brandingId = createBranding();

    logger.info(
      "Created prerequisite entities: Network {}, DayType {}, Branding {}",
      networkId,
      dayTypeId,
      brandingId
    );

    // Warm-up phase
    logger.info("Starting warm-up phase ({} iterations)...", WARMUP_ITERATIONS);
    for (int i = 0; i < WARMUP_ITERATIONS; i++) {
      String lineName = "WarmupLine_" + i;
      Map<String, Object> lineInput = generateLineInput(
        lineName,
        networkId,
        dayTypeId,
        brandingId
      );
      mutateLine(lineInput);
    }

    // Performance test phase
    logger.info("Starting CREATE performance test ({} iterations)...", TEST_ITERATIONS);
    List<Long> executionTimes = new ArrayList<>();

    for (int i = 0; i < TEST_ITERATIONS; i++) {
      String lineName = "TestLine_" + i;
      Map<String, Object> lineInput = generateLineInput(
        lineName,
        networkId,
        dayTypeId,
        brandingId
      );

      Instant start = Instant.now();
      String lineId = mutateLine(lineInput);
      Instant end = Instant.now();

      long executionTimeMs = Duration.between(start, end).toMillis();
      executionTimes.add(executionTimeMs);

      logger.info(
        "Iteration {}: Created line {} in {} ms",
        i + 1,
        lineId,
        executionTimeMs
      );
    }

    // Calculate statistics
    calculateAndPrintStatistics(executionTimes, "CREATE");
  }

  /**
   * Test the performance of updating lines.
   */
  @Test
  public void testUpdateLinePerformance() throws InterruptedException {
    // Create prerequisite entities
    String networkId = createNetwork();
    String brandingId = createBranding();
    String dayTypeId = createDayType();
    logger.info("Created day type with ID: {}", dayTypeId);

    // Create a line first
    String lineId = createLineForUpdate(networkId, brandingId, dayTypeId);
    logger.info("Created line with ID: {}", lineId);

    // Fetch the line data
    Map<String, Object> lineData = fetchLine(lineId);
    logger.info("Fetched line data: {}", lineData);

    // Convert the line data to mutation input
    Map<String, Object> input = convertLineDataToMutationInput(lineData);

    Thread.sleep(5000);

    // Measure the time it takes to update the line
    long startTime = System.currentTimeMillis();
    String updatedLineId = mutateLine(input);
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // Log the results
    logger.info("Updated line with ID: {}", updatedLineId);
    logger.info("Update took {} ms", duration);

    // Verify the update was successful
    org.junit.Assert.assertNotNull(updatedLineId);
    org.junit.Assert.assertEquals(lineId, updatedLineId);
  }

  /**
   * Calculate and print performance statistics.
   *
   * @param executionTimes List of execution times in milliseconds
   * @param operationType Type of operation (CREATE or UPDATE)
   */
  private void calculateAndPrintStatistics(
    List<Long> executionTimes,
    String operationType
  ) {
    if (executionTimes.isEmpty()) {
      logger.info("No execution times to calculate statistics for.");
      return;
    }

    // Calculate statistics
    long sum = 0;
    long min = Long.MAX_VALUE;
    long max = 0;

    for (long time : executionTimes) {
      sum += time;
      min = Math.min(min, time);
      max = Math.max(max, time);
    }

    double avg = (double) sum / executionTimes.size();

    // Sort for percentiles
    Collections.sort(executionTimes);
    long median = executionTimes.get(executionTimes.size() / 2);
    long p95 = executionTimes.get((int) (executionTimes.size() * 0.95));
    long p99 = executionTimes.get((int) (executionTimes.size() * 0.99));

    // Print statistics
    logger.info(
      "\n=== {} Performance Statistics (ms) ===\n" +
      "  Iterations: {}\n" +
      "  Min: {}\n" +
      "  Max: {}\n" +
      "  Avg: {:.2f}\n" +
      "  Median: {}\n" +
      "  P95: {}\n" +
      "  P99: {}",
      operationType,
      executionTimes.size(),
      min,
      max,
      avg,
      median,
      p95,
      p99
    );
  }

  /**
   * Load a GraphQL document from the resources directory.
   *
   * @param filename The name of the GraphQL document file
   * @return The GraphQL document as a string
   */
  private String loadGraphQlDocument(String filename) {
    // We don't need this method anymore since we're using graphQlTester.documentName
    return null;
  }

  private String createNetwork() {
    String networkName = "PerformanceTestNetwork_" + System.currentTimeMillis();
    return graphQlTester
      .documentName("mutateNetwork")
      .variable("network", Map.of("name", networkName, "authorityRef", "NOG:Authority:1"))
      .execute()
      .path("mutateNetwork.id")
      .entity(String.class)
      .get();
  }

  /**
   * Create a day type for testing.
   *
   * @return The ID of the created day type
   */
  private String createDayType() {
    Map<String, Object> input = new HashMap<>();
    input.put(
      "daysOfWeek",
      List.of("monday", "tuesday", "wednesday", "thursday", "friday")
    );

    // Create day type assignment with operating period
    List<Map<String, Object>> dayTypeAssignments = new ArrayList<>();
    Map<String, Object> assignment = new HashMap<>();

    Map<String, Object> operatingPeriod = new HashMap<>();
    LocalDate now = LocalDate.now();
    operatingPeriod.put("fromDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE));
    operatingPeriod.put(
      "toDate",
      now.plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
    );

    assignment.put("operatingPeriod", operatingPeriod);
    assignment.put("isAvailable", true);
    dayTypeAssignments.add(assignment);

    input.put("dayTypeAssignments", dayTypeAssignments);

    return graphQlTester
      .documentName("mutateDayType")
      .variable("input", input)
      .execute()
      .path("mutateDayType.id")
      .entity(String.class)
      .get();
  }

  private String createBranding() {
    return graphQlTester
      .documentName("mutateBranding")
      .variable("branding", Map.of("name", "PerformanceTestBranding"))
      .execute()
      .path("mutateBranding.id")
      .entity(String.class)
      .get();
  }

  private Map<String, Object> generateDayTypeInput() {
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

  private Map<String, Object> generateLineInput(
    String name,
    String networkId,
    String dayTypeRef,
    String brandingId
  ) {
    Map<String, Object> lineInput = new HashMap<>();
    lineInput.put("name", name);
    lineInput.put("publicCode", "PerfTest");
    lineInput.put("transportMode", "bus");
    lineInput.put("transportSubmode", "localBus");
    lineInput.put("networkRef", networkId);
    lineInput.put("brandingRef", brandingId);
    lineInput.put("operatorRef", "NOG:Operator:1");

    // Add journey patterns with points and service journeys
    List<Map<String, Object>> journeyPatterns = new ArrayList<>();
    Map<String, Object> journeyPattern = new HashMap<>();

    // Points in sequence
    List<Map<String, Object>> pointsInSequence = new ArrayList<>();
    Map<String, Object> point1 = new HashMap<>();
    point1.put("quayRef", "NSR:Quay:494");
    point1.put("destinationDisplay", Map.of("frontText", "First stop"));
    pointsInSequence.add(point1);

    Map<String, Object> point2 = new HashMap<>();
    point2.put("quayRef", "NSR:Quay:563");
    pointsInSequence.add(point2);

    journeyPattern.put("pointsInSequence", pointsInSequence);

    // Service journeys
    List<Map<String, Object>> serviceJourneys = new ArrayList<>();
    Map<String, Object> serviceJourney = new HashMap<>();
    serviceJourney.put("name", name + "_Journey");
    if (dayTypeRef != null) {
      serviceJourney.put("dayTypesRefs", List.of(dayTypeRef));
    }

    // Passing times
    List<Map<String, Object>> passingTimes = new ArrayList<>();
    passingTimes.add(Map.of("departureTime", "07:00:00"));
    passingTimes.add(Map.of("arrivalTime", "07:15:00"));
    serviceJourney.put("passingTimes", passingTimes);

    serviceJourneys.add(serviceJourney);
    journeyPattern.put("serviceJourneys", serviceJourneys);

    journeyPatterns.add(journeyPattern);
    lineInput.put("journeyPatterns", journeyPatterns);

    return lineInput;
  }

  private String generateDateString(LocalDate date) {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
  }

  /**
   * Execute the mutateLine mutation with the given input.
   *
   * @param input The input for the mutation
   * @return The ID of the mutated line
   */
  private String mutateLine(Map<String, Object> input) {
    logger.info("Mutation variables: {}", input);

    // Log specific parts of the input that might be causing issues
    if (input.containsKey("journeyPatterns")) {
      List<Map<String, Object>> journeyPatterns = (List<Map<String, Object>>) input.get(
        "journeyPatterns"
      );
      for (Map<String, Object> jp : journeyPatterns) {
        if (jp.containsKey("serviceJourneys")) {
          List<Map<String, Object>> serviceJourneys = (List<Map<String, Object>>) jp.get(
            "serviceJourneys"
          );
          for (Map<String, Object> sj : serviceJourneys) {
            if (sj.containsKey("dayTypesRefs")) {
              logger.info(
                "Service journey {} has dayTypesRefs: {}",
                sj.get("id"),
                sj.get("dayTypesRefs")
              );
            }
          }
        }
      }
    }

    try {
      return graphQlTester
        .documentName("mutateLine")
        .variable("input", input)
        .execute()
        .path("mutateLine.id")
        .entity(String.class)
        .get();
    } catch (Exception e) {
      logger.error("Error executing mutateLine mutation", e);
      throw e;
    }
  }

  /**
   * Convert the line data from the GraphQL response to a format suitable for the mutation input.
   *
   * @param lineData The line data from the GraphQL response
   * @return The line data in a format suitable for the mutation input
   */
  private Map<String, Object> convertLineDataToMutationInput(
    Map<String, Object> lineData
  ) {
    Map<String, Object> input = new HashMap<>();
    input.put("id", lineData.get("id"));
    input.put("name", lineData.get("name"));
    input.put("publicCode", lineData.get("publicCode"));
    input.put("transportMode", lineData.get("transportMode"));
    input.put("transportSubmode", lineData.get("transportSubmode"));
    input.put("operatorRef", lineData.get("operatorRef"));

    Map<String, Object> network = (Map<String, Object>) lineData.get("network");
    input.put("networkRef", network.get("id"));

    Map<String, Object> branding = (Map<String, Object>) lineData.get("branding");
    input.put("brandingRef", branding.get("id"));

    // Add journey patterns
    List<Map<String, Object>> journeyPatterns = (List<Map<String, Object>>) lineData.get(
      "journeyPatterns"
    );
    List<Map<String, Object>> journeyPatternInputs = new ArrayList<>();

    for (Map<String, Object> journeyPattern : journeyPatterns) {
      Map<String, Object> journeyPatternInput = new HashMap<>();
      journeyPatternInput.put("id", journeyPattern.get("id"));

      // Add points in sequence
      List<Map<String, Object>> pointsInSequence =
        (List<Map<String, Object>>) journeyPattern.get("pointsInSequence");
      List<Map<String, Object>> pointsInSequenceInputs = new ArrayList<>();

      for (Map<String, Object> point : pointsInSequence) {
        Map<String, Object> pointInput = new HashMap<>();
        pointInput.put("id", point.get("id"));
        pointInput.put("quayRef", point.get("quayRef"));

        // Add destination display if it exists
        Map<String, Object> destinationDisplay = (Map<String, Object>) point.get(
          "destinationDisplay"
        );
        if (destinationDisplay != null) {
          Map<String, Object> destinationDisplayInput = new HashMap<>();
          destinationDisplayInput.put("frontText", destinationDisplay.get("frontText"));
          pointInput.put("destinationDisplay", destinationDisplayInput);
        }

        pointsInSequenceInputs.add(pointInput);
      }

      journeyPatternInput.put("pointsInSequence", pointsInSequenceInputs);

      // Add service journeys
      List<Map<String, Object>> serviceJourneys =
        (List<Map<String, Object>>) journeyPattern.get("serviceJourneys");
      List<Map<String, Object>> serviceJourneyInputs = new ArrayList<>();

      for (Map<String, Object> serviceJourney : serviceJourneys) {
        Map<String, Object> serviceJourneyInput = new HashMap<>();
        serviceJourneyInput.put("id", serviceJourney.get("id"));
        serviceJourneyInput.put("name", serviceJourney.get("name"));

        // Add day types
        List<Map<String, Object>> dayTypes =
          (List<Map<String, Object>>) serviceJourney.get("dayTypes");
        List<String> dayTypeRefs = new ArrayList<>();
        for (Map<String, Object> dayType : dayTypes) {
          dayTypeRefs.add((String) dayType.get("id"));
        }
        serviceJourneyInput.put("dayTypesRefs", dayTypeRefs);

        // Add passing times
        List<Map<String, Object>> passingTimes =
          (List<Map<String, Object>>) serviceJourney.get("passingTimes");
        List<Map<String, Object>> passingTimeInputs = new ArrayList<>();

        for (Map<String, Object> passingTime : passingTimes) {
          Map<String, Object> passingTimeInput = new HashMap<>();
          passingTimeInput.put("id", passingTime.get("id"));
          passingTimeInput.put("departureTime", passingTime.get("departureTime"));
          passingTimeInput.put("arrivalTime", passingTime.get("arrivalTime"));
          passingTimeInputs.add(passingTimeInput);
        }

        serviceJourneyInput.put("passingTimes", passingTimeInputs);
        serviceJourneyInputs.add(serviceJourneyInput);
      }

      journeyPatternInput.put("serviceJourneys", serviceJourneyInputs);
      journeyPatternInputs.add(journeyPatternInput);
    }

    input.put("journeyPatterns", journeyPatternInputs);

    return input;
  }

  /**
   * Create a line for update testing.
   *
   * @param networkId The ID of the network to use
   * @param brandingId The ID of the branding to use
   * @param dayTypeId The ID of the day type to use
   * @return The ID of the created line
   */
  private String createLineForUpdate(
    String networkId,
    String brandingId,
    String dayTypeId
  ) {
    Map<String, Object> input = new HashMap<>();
    input.put("name", "UpdateTestLine");
    input.put("publicCode", "PerfTest");
    input.put("transportMode", "bus");
    input.put("transportSubmode", "localBus");
    input.put("operatorRef", "NOG:Operator:1");
    input.put("networkRef", networkId);
    input.put("brandingRef", brandingId);

    // Create journey pattern
    List<Map<String, Object>> journeyPatterns = new ArrayList<>();
    Map<String, Object> journeyPattern = new HashMap<>();

    // Create points in sequence
    List<Map<String, Object>> pointsInSequence = new ArrayList<>();

    Map<String, Object> point1 = new HashMap<>();
    point1.put("quayRef", "NSR:Quay:494");
    Map<String, Object> destinationDisplay = new HashMap<>();
    destinationDisplay.put("frontText", "First stop");
    point1.put("destinationDisplay", destinationDisplay);
    pointsInSequence.add(point1);

    Map<String, Object> point2 = new HashMap<>();
    point2.put("quayRef", "NSR:Quay:563");
    pointsInSequence.add(point2);

    journeyPattern.put("pointsInSequence", pointsInSequence);

    // Create service journey
    List<Map<String, Object>> serviceJourneys = new ArrayList<>();
    Map<String, Object> serviceJourney = new HashMap<>();
    serviceJourney.put("name", "UpdateTestLine_Journey");
    if (dayTypeId != null) {
      serviceJourney.put("dayTypesRefs", List.of(dayTypeId));
    }

    // Create passing times
    List<Map<String, Object>> passingTimes = new ArrayList<>();
    Map<String, Object> passingTime1 = new HashMap<>();
    passingTime1.put("departureTime", "07:00:00");
    passingTimes.add(passingTime1);

    Map<String, Object> passingTime2 = new HashMap<>();
    passingTime2.put("arrivalTime", "07:15:00");
    passingTimes.add(passingTime2);

    serviceJourney.put("passingTimes", passingTimes);
    serviceJourneys.add(serviceJourney);

    journeyPattern.put("serviceJourneys", serviceJourneys);
    journeyPatterns.add(journeyPattern);

    input.put("journeyPatterns", journeyPatterns);

    return mutateLine(input);
  }

  private Map<String, Object> fetchLine(String lineId) {
    return graphQlTester
      .documentName("lineById")
      .variable("id", lineId)
      .execute()
      .path("line")
      .entity(Map.class)
      .get();
  }
}
