package no.entur.uttu.routing.osrm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MutableRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.routing.RouteGeometry;
import no.entur.uttu.routing.RoutingServiceRequestParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OsrmServiceTest {

  private OsrmService osrmService;
  private static final String TEST_ENDPOINT = "http://test.osrm.endpoint";

  @Mock
  private Methanol httpClient;

  @Mock
  private HttpResponse<String> httpResponse;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    List<OsrmProfile> profiles = List.of(
      new OsrmProfile(List.of(VehicleModeEnumeration.BUS), TEST_ENDPOINT)
    );
    osrmService = new OsrmService(profiles);

    // Inject mocked HTTP client
    ReflectionTestUtils.setField(osrmService, "httpClient", httpClient);
  }

  @Test
  void isEnabled_shouldReturnTrueForConfiguredMode() {
    assertTrue(osrmService.isEnabled(VehicleModeEnumeration.BUS));
  }

  @Test
  void isEnabled_shouldReturnFalseForUnconfiguredMode() {
    assertFalse(osrmService.isEnabled(VehicleModeEnumeration.RAIL));
  }

  @Test
  void getRouteGeometry_shouldReturnValidGeometry()
    throws IOException, InterruptedException {
    // Given
    RoutingServiceRequestParams params = new RoutingServiceRequestParams(
      BigDecimal.valueOf(10.39),
      BigDecimal.valueOf(63.43),
      BigDecimal.valueOf(10.40),
      BigDecimal.valueOf(63.44),
      VehicleModeEnumeration.BUS
    );

    String mockResponse =
      """
      {
          "code": "Ok",
          "routes": [{
              "geometry": {
                  "coordinates": [[10.39, 63.43], [10.395, 63.435], [10.40, 63.44]]
              },
              "distance": 1234.56
          }]
      }
      """;
    when(httpClient.send(any(MutableRequest.class), any(BodyHandler.class))).thenReturn(
      httpResponse
    );
    when(httpResponse.body()).thenReturn(mockResponse);

    // When
    RouteGeometry geometry = osrmService.getRouteGeometry(params);

    // Then
    assertNotNull(geometry);
    assertEquals(3, geometry.coordinates().size());
    assertEquals(BigDecimal.valueOf(1234.56), geometry.distance());
    verify(httpClient).send(any(MutableRequest.class), eq(BodyHandlers.ofString()));
  }

  @Test
  void getRouteGeometry_shouldHandleErrorResponse()
    throws IOException, InterruptedException {
    // Given
    RoutingServiceRequestParams params = new RoutingServiceRequestParams(
      BigDecimal.valueOf(10.39),
      BigDecimal.valueOf(63.43),
      BigDecimal.valueOf(10.40),
      BigDecimal.valueOf(63.44),
      VehicleModeEnumeration.BUS
    );

    String mockResponse =
      """
      {
          "code": "NoRoute",
          "message": "No route found"
      }
      """;
    when(httpClient.send(any(MutableRequest.class), any(BodyHandler.class))).thenReturn(
      httpResponse
    );
    when(httpResponse.body()).thenReturn(mockResponse);

    // When
    RouteGeometry geometry = osrmService.getRouteGeometry(params);

    // Then
    assertNotNull(geometry);
    assertEquals(2, geometry.coordinates().size());
    assertTrue(geometry.distance().compareTo(BigDecimal.ZERO) > 0);
    verify(httpClient).send(any(MutableRequest.class), eq(BodyHandlers.ofString()));
  }

  @Test
  void getRouteGeometry_shouldHandleIOException()
    throws IOException, InterruptedException {
    // Given
    RoutingServiceRequestParams params = new RoutingServiceRequestParams(
      BigDecimal.valueOf(10.39),
      BigDecimal.valueOf(63.43),
      BigDecimal.valueOf(10.40),
      BigDecimal.valueOf(63.44),
      VehicleModeEnumeration.BUS
    );

    when(httpClient.send(any(MutableRequest.class), any(BodyHandler.class))).thenThrow(
      new IOException("Network error")
    );

    // When
    RouteGeometry geometry = osrmService.getRouteGeometry(params);

    // Then
    assertNotNull(geometry);
    assertEquals(2, geometry.coordinates().size());
    assertTrue(geometry.distance().compareTo(BigDecimal.ZERO) > 0);
  }

  @Test
  void getRouteGeometry_shouldHandleInterruptedException()
    throws IOException, InterruptedException {
    // Given
    RoutingServiceRequestParams params = new RoutingServiceRequestParams(
      BigDecimal.valueOf(10.39),
      BigDecimal.valueOf(63.43),
      BigDecimal.valueOf(10.40),
      BigDecimal.valueOf(63.44),
      VehicleModeEnumeration.BUS
    );

    when(httpClient.send(any(MutableRequest.class), any(BodyHandler.class))).thenThrow(
      new InterruptedException("Request interrupted")
    );

    // When
    RouteGeometry geometry = osrmService.getRouteGeometry(params);

    // Then
    assertNotNull(geometry);
    assertEquals(2, geometry.coordinates().size());
    assertTrue(geometry.distance().compareTo(BigDecimal.ZERO) > 0);
    assertTrue(
      Thread.currentThread().isInterrupted(),
      "Thread should be marked as interrupted"
    );
  }
}
