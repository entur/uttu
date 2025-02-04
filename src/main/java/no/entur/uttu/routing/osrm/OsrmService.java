package no.entur.uttu.routing.osrm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MutableRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import no.entur.uttu.routing.RouteGeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsrmService implements no.entur.uttu.routing.RoutingService {

  private static final Logger logger = LoggerFactory.getLogger(OsrmService.class);

  private final String osrmApiEndpoint;

  private final Methanol httpClient;
  private final ObjectMapper objectMapper;

  public OsrmService(String osrmApiEndpoint) {
    this.osrmApiEndpoint = osrmApiEndpoint;
    this.objectMapper = initializeObjectMapper();
    this.httpClient = initializeHttpClient();
    logger.info("OsrmService got initialised, osrmApiEndpoint is: {}", osrmApiEndpoint);
  }

  private static ObjectMapper initializeObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return om;
  }

  private static Methanol initializeHttpClient() {
    return Methanol
      .newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .requestTimeout(Duration.ofSeconds(5))
      .headersTimeout(Duration.ofSeconds(5))
      .readTimeout(Duration.ofSeconds(5))
      .followRedirects(HttpClient.Redirect.NORMAL)
      .userAgent("Entur Uttu/" + LocalDate.now().format(DateTimeFormatter.ISO_DATE))
      .build();
  }

  public boolean isEnabled() {
    return osrmApiEndpoint != null && !osrmApiEndpoint.isBlank();
  }

  private MutableRequest getRoutingRequest(
    BigDecimal longitudeFrom,
    BigDecimal latitudeFrom,
    BigDecimal longitudeTo,
    BigDecimal latitudeTo
  ) {
    return MutableRequest
      .GET(
        osrmApiEndpoint +
        "/route/v1/driving/" +
        longitudeFrom +
        "," +
        latitudeFrom +
        ";" +
        longitudeTo +
        "," +
        latitudeTo +
        "?alternatives=false&steps=false&overview=full&geometries=geojson"
      )
      .header("Content-Type", "application/json");
  }

  public RouteGeometry getRouteGeometry(
    BigDecimal longitudeFrom,
    BigDecimal latitudeFrom,
    BigDecimal longitudeTo,
    BigDecimal latitudeTo
  ) {
    List<List<BigDecimal>> routeCoordinates = new ArrayList<>();
    MutableRequest request = getRoutingRequest(
      longitudeFrom,
      latitudeFrom,
      longitudeTo,
      latitudeTo
    );
    try {
      HttpResponse<String> response = httpClient.send(
        request,
        HttpResponse.BodyHandlers.ofString()
      );
      JsonNode responseJsonNode = objectMapper.readTree(response.body());

      if (!"Ok".equals(responseJsonNode.get("code").asText())) {
        logger.warn(
          "OSRM route {} error for [{},{}] - [{},{}] : {}",
          responseJsonNode.get("code").asText(),
          longitudeFrom,
          latitudeFrom,
          longitudeTo,
          latitudeTo,
          responseJsonNode.get("message").asText()
        );
        return new RouteGeometry(routeCoordinates, BigDecimal.ZERO);
      }
      JsonNode coordinates = responseJsonNode
        .get("routes")
        .get(0)
        .get("geometry")
        .get("coordinates");
      JsonNode distance = responseJsonNode.get("routes").get(0).get("distance");
      String coordinatesString = objectMapper.writeValueAsString(coordinates);
      String distanceString = objectMapper.writeValueAsString(distance);
      return new RouteGeometry(
        objectMapper.readValue(coordinatesString, new TypeReference<>() {}),
        objectMapper.readValue(distanceString, BigDecimal.class)
      );
    } catch (IOException e) {
      logger.warn(
        "I/O error during OSRM API request for [{},{}] - [{},{}]",
        longitudeFrom,
        latitudeFrom,
        longitudeTo,
        latitudeTo,
        e
      );
    } catch (InterruptedException e) {
      logger.warn(
        "InterruptedException error during OSRM API request from [{},{}] to [{},{}]",
        longitudeFrom,
        latitudeFrom,
        longitudeTo,
        latitudeTo,
        e
      );
      Thread.currentThread().interrupt();
    }
    return new RouteGeometry(routeCoordinates, BigDecimal.ZERO);
  }
}
