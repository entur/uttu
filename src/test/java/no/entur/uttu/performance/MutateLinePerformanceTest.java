package no.entur.uttu.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.entur.uttu.integration.AbstractGraphQLIntegrationTest;
import no.entur.uttu.stubs.UserContextServiceStub;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
   * Test the performance of creating lines using real input data.
   */
  @Test
  public void testCreateLineWithRealData() throws Exception {
    logger.info("Starting performance test with real data on port {}", port);

    // Load the real input data from the JSON file
    String jsonContent = new String(
      Files.readAllBytes(Paths.get("src/test/resources/performance/input.json"))
    );
    Map<String, Object> jsonMap = new ObjectMapper().readValue(jsonContent, Map.class);
    Map<String, Object> lineInput = (Map<String, Object>) jsonMap.get("input");

    // Create a day type first since the input references a day type
    String dayTypeId = createDayType();
    logger.info("Created day type with ID: {}", dayTypeId);

    // Update day type references in the input
    updateDayTypeReferences(lineInput, dayTypeId);

    // Update operator references in the input to use a valid operator ID from the fixtures
    String operatorId = "NOG:Operator:1";
    updateOperatorReferences(lineInput, operatorId);
    logger.info("Updated operator references to use ID: {}", operatorId);

    // Create a network if needed
    if (lineInput.containsKey("networkRef") && lineInput.get("networkRef") != null) {
      String networkId = createNetwork();
      lineInput.put("networkRef", networkId);
      logger.info("Updated networkRef to: {}", networkId);
    }

    // Measure the time it takes to create the line
    long startTime = System.currentTimeMillis();
    String lineId = mutateLine(lineInput);
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    // Log the results
    logger.info("Created line with ID: {}", lineId);
    logger.info("Creation took {} ms", duration);

    // Verify the creation was successful
    org.junit.Assert.assertNotNull(lineId);
  }

  /**
   * Test the performance of updating lines using real input data.
   */
  @Test
  public void testUpdateLineWithRealData() throws Exception {
    logger.info("Starting update performance test with real data on port {}", port);

    // Load the real input data from the JSON file
    String jsonContent = new String(
      Files.readAllBytes(Paths.get("src/test/resources/performance/input.json"))
    );
    Map<String, Object> jsonMap = new ObjectMapper().readValue(jsonContent, Map.class);
    Map<String, Object> lineInput = (Map<String, Object>) jsonMap.get("input");

    // Create a day type first since the input references a day type
    String dayTypeId = createDayType();
    logger.info("Created day type with ID: {}", dayTypeId);

    // Update day type references in the input
    updateDayTypeReferences(lineInput, dayTypeId);

    // Update operator references in the input to use a valid operator ID from the fixtures
    String operatorId = "NOG:Operator:1";
    updateOperatorReferences(lineInput, operatorId);
    logger.info("Updated operator references to use ID: {}", operatorId);

    // Create a network if needed
    if (lineInput.containsKey("networkRef") && lineInput.get("networkRef") != null) {
      String networkId = createNetwork();
      lineInput.put("networkRef", networkId);
      logger.info("Updated networkRef to: {}", networkId);
    }

    // Create the line first
    String lineId = mutateLine(lineInput);
    logger.info("Created line with ID: {}", lineId);

    // Fetch the line data
    Map<String, Object> fetchedLineData = fetchLine(lineId);
    logger.info("Fetched line data for update");

    // Modify the line data slightly for the update
    Map<String, Object> updateInput = convertLineDataToMutationInput(fetchedLineData);
    updateInput.put("name", updateInput.get("name") + " (Updated)");

    // Log the update input
    logger.info("Update input: {}", updateInput);

    // Measure the time it takes to update the line
    long startTime = System.currentTimeMillis();
    String updatedLineId = mutateLine(updateInput);
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
   * Update all day type references in the input to use the given day type ID.
   *
   * @param input The input data
   * @param dayTypeId The day type ID to use
   */
  private void updateDayTypeReferences(Map<String, Object> input, String dayTypeId) {
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
            // Replace the day type references with our created day type
            sj.put("dayTypesRefs", List.of(dayTypeId));
          }
        }
      }
    }
  }

  private void updateOperatorReferences(Map<String, Object> input, String operatorId) {
    if (input.containsKey("operatorRef")) {
      input.put("operatorRef", operatorId);
    }
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
            if (sj.containsKey("operatorRef")) {
              sj.put("operatorRef", operatorId);
            }
          }
        }
      }
    }
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
          for (Map<String, Object> sp : (List<Map<String, Object>>) jp.get(
            "pointsInSequence"
          )) {
            logger.info("Stop point in jp {}", sp.get("id"));
          }
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

    // Add branding reference only if branding exists
    Map<String, Object> branding = (Map<String, Object>) lineData.get("branding");
    if (branding != null) {
      input.put("brandingRef", branding.get("id"));
    }

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
        pointInput.put("forBoarding", point.get("forBoarding"));
        pointInput.put("forAlighting", point.get("forAlighting"));
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
