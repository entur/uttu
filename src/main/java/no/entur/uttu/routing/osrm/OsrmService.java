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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.routing.RouteGeometry;
import no.entur.uttu.routing.RoutingServiceRequestParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsrmService implements no.entur.uttu.routing.RoutingService {

  private static final Logger logger = LoggerFactory.getLogger(OsrmService.class);

  private final Map<VehicleModeEnumeration, String> endpointMap = new EnumMap<>(
    VehicleModeEnumeration.class
  );

  private final Methanol httpClient = initializeHttpClient();
  private final ObjectMapper objectMapper = initializeObjectMapper();

  public OsrmService(List<OsrmProfile> profiles) {
    profiles.forEach(profile ->
      profile.getModes().forEach(mode -> endpointMap.put(mode, profile.getEndpoint()))
    );
    logger.info("OsrmService initialised with profiles={}", profiles);
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

  @Override
  public boolean isEnabled(VehicleModeEnumeration mode) {
    return endpointMap.containsKey(mode);
  }

  @Override
  public RouteGeometry getRouteGeometry(RoutingServiceRequestParams requestParams) {
    var routingRequest = getRoutingRequest(requestParams);
    return getRouteGeometry(requestParams, routingRequest);
  }

  private MutableRequest getRoutingRequest(RoutingServiceRequestParams requestParams) {
    return MutableRequest
      .GET(
        endpointMap.get(requestParams.mode()) +
        "/route/v1/driving/" +
        requestParams.longitudeFrom() +
        "," +
        requestParams.latitudeFrom() +
        ";" +
        requestParams.longitudeTo() +
        "," +
        requestParams.latitudeTo() +
        "?alternatives=false&steps=false&overview=full&geometries=geojson"
      )
      .header("Content-Type", "application/json");
  }

  private RouteGeometry getRouteGeometry(
    RoutingServiceRequestParams requestParams,
    MutableRequest request
  ) {
    List<List<BigDecimal>> routeCoordinates = new ArrayList<>();
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
          requestParams.longitudeFrom(),
          requestParams.latitudeFrom(),
          requestParams.longitudeTo(),
          requestParams.latitudeTo(),
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
        requestParams.longitudeFrom(),
        requestParams.latitudeFrom(),
        requestParams.longitudeTo(),
        requestParams.latitudeTo(),
        e
      );
    } catch (InterruptedException e) {
      logger.warn(
        "InterruptedException error during OSRM API request from [{},{}] to [{},{}]",
        requestParams.longitudeFrom(),
        requestParams.latitudeFrom(),
        requestParams.longitudeTo(),
        requestParams.latitudeTo(),
        e
      );
      Thread.currentThread().interrupt();
    }
    return new RouteGeometry(routeCoordinates, BigDecimal.ZERO);
  }
}
