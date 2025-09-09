package no.entur.uttu.export.netex.producer.common;

import static no.entur.uttu.model.VehicleModeEnumeration.BUS;
import static no.entur.uttu.model.VehicleModeEnumeration.TRAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import no.entur.uttu.config.AdditionalCodespacesConfig;
import no.entur.uttu.config.ExportTimeZone;
import no.entur.uttu.export.model.ServiceLinkExportContext;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Codespace;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.Ref;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.routing.RouteGeometry;
import no.entur.uttu.routing.RoutingService;
import no.entur.uttu.routing.RoutingServiceRequestParams;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import no.entur.uttu.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rutebanken.netex.model.LinkSequenceProjection;
import org.rutebanken.netex.model.LocationStructure;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.ServiceLink;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;

class ServiceLinkProducerTest {

  ServiceLinkProducer serviceLinkProducer;
  NetexObjectFactory objectFactory = new NetexObjectFactory(
    new DateUtils(),
    new ExportTimeZone(),
    new AdditionalCodespacesConfig()
  );

  StopPlaceRegistry mockStopPlaceRegistry = mock(StopPlaceRegistry.class);
  RoutingService mockRoutingService = mock(RoutingService.class);

  @BeforeEach
  public void setUp() {
    serviceLinkProducer = new ServiceLinkProducer(
      objectFactory,
      mockStopPlaceRegistry,
      mockRoutingService
    );
  }

  private NetexExportContext createTestContext() {
    Export export = new Export();
    Provider provider = new Provider();
    Codespace codespace = new Codespace();
    codespace.setXmlns("TST");
    provider.setCodespace(codespace);
    export.setProvider(provider);
    return new NetexExportContext(export);
  }

  private void setupMockQuays() {
    Quay quay1 = createQuay("TST:Quay:1", 10.1, 60.2);
    Quay quay2 = createQuay("TST:Quay:2", 10.5, 60.6);
    Quay quay3 = createQuay("TST:Quay:3", 10.9, 61.0);

    when(mockStopPlaceRegistry.getQuayById("TST:Quay:1")).thenReturn(Optional.of(quay1));
    when(mockStopPlaceRegistry.getQuayById("TST:Quay:2")).thenReturn(Optional.of(quay2));
    when(mockStopPlaceRegistry.getQuayById("TST:Quay:3")).thenReturn(Optional.of(quay3));
  }

  private void setupMockRoutingService() {
    var params1to2Bus = new RoutingServiceRequestParams(
      bigDec(10.1),
      bigDec(60.2),
      bigDec(10.5),
      bigDec(60.6),
      BUS
    );
    var params2to3Bus = new RoutingServiceRequestParams(
      bigDec(10.5),
      bigDec(60.6),
      bigDec(10.9),
      bigDec(61.0),
      BUS
    );
    var params1to2Tram = new RoutingServiceRequestParams(
      bigDec(10.1),
      bigDec(60.2),
      bigDec(10.5),
      bigDec(60.6),
      TRAM
    );

    when(mockRoutingService.getRouteGeometry(params1to2Bus)).thenReturn(
      new RouteGeometry(List.of(coord(10.1, 60.2), coord(10.5, 60.6)), bigDec(100))
    );
    when(mockRoutingService.getRouteGeometry(params2to3Bus)).thenReturn(
      new RouteGeometry(List.of(coord(10.5, 60.6), coord(10.9, 61.0)), bigDec(150))
    );
    when(mockRoutingService.getRouteGeometry(params1to2Tram)).thenReturn(
      new RouteGeometry(List.of(coord(10.1, 60.2), coord(10.5, 60.6)), bigDec(100))
    );
  }

  @Test
  void testServiceLinkProducerProducesCorrectLineString() {
    NetexExportContext context = createTestContext();

    context.serviceLinks = Set.of(
      createContext("TST:Quay:1", "TST:Quay:2", BUS, "TST:ServiceLink:1")
    );

    setupMockQuays();

    // GeoJSON uses coordinate pairs in order longitude then latitude
    var params = new RoutingServiceRequestParams(
      bigDec(10.1),
      bigDec(60.2),
      bigDec(10.5),
      bigDec(60.6),
      BUS
    );
    when(mockRoutingService.getRouteGeometry(params)).thenReturn(
      new RouteGeometry(
        List.of(
          coord(10.1, 60.2),
          coord(10.2, 60.3),
          coord(10.3, 60.4),
          coord(10.4, 60.5),
          coord(10.5, 60.6)
        ),
        bigDec(100)
      )
    );

    List<ServiceLink> serviceLinkList = serviceLinkProducer.produce(context);

    LinkSequenceProjection linkSequenceProjection =
      (LinkSequenceProjection) serviceLinkList
        .getFirst()
        .getProjections()
        .getProjectionRefOrProjection()
        .getFirst()
        .getValue();

    assertEquals(
      List.of(
        // NeTEx expects coordinate pairs in order of latitude then longitude
        60.2,
        10.1,
        60.3,
        10.2,
        60.4,
        10.3,
        60.5,
        10.4,
        60.6,
        10.5
      ),
      linkSequenceProjection.getLineString().getPosList().getValue()
    );
  }

  @Test
  void testServiceLinkProducerDeduplicatesDuplicateServiceLinks() {
    NetexExportContext context = createTestContext();

    context.serviceLinks = Set.of(
      createContext("TST:Quay:1", "TST:Quay:2", BUS, "TST:ServiceLink:1"),
      createContext("TST:Quay:1", "TST:Quay:2", BUS, "TST:ServiceLink:2"),
      createContext("TST:Quay:2", "TST:Quay:3", BUS, "TST:ServiceLink:3"),
      createContext("TST:Quay:1", "TST:Quay:2", TRAM, "TST:ServiceLink:4")
    );

    setupMockQuays();
    setupMockRoutingService();

    List<ServiceLink> serviceLinkList = serviceLinkProducer.produce(context);

    assertEquals(
      3,
      serviceLinkList.size(),
      "Should deduplicate identical service links but keep different transport modes"
    );

    boolean hasBusLink1to2 = serviceLinkList
      .stream()
      .anyMatch(
        sl ->
          sl.getFromPointRef().getRef().equals("TST:ScheduledStopPoint:1_UTTU") &&
          sl.getToPointRef().getRef().equals("TST:ScheduledStopPoint:2_UTTU")
      );
    boolean hasBusLink2to3 = serviceLinkList
      .stream()
      .anyMatch(
        sl ->
          sl.getFromPointRef().getRef().equals("TST:ScheduledStopPoint:2_UTTU") &&
          sl.getToPointRef().getRef().equals("TST:ScheduledStopPoint:3_UTTU")
      );

    assertTrue(hasBusLink1to2, "Should have bus link from quay 1 to quay 2");
    assertTrue(hasBusLink2to3, "Should have bus link from quay 2 to quay 3");

    verify(mockRoutingService, Mockito.times(3)).getRouteGeometry(Mockito.any());
  }

  private Quay createQuay(String id, double longitude, double latitude) {
    Quay quay = new Quay();
    quay.setId(id);
    quay.setCentroid(
      new SimplePoint_VersionStructure()
        .withLocation(
          new LocationStructure()
            .withLongitude(bigDec(longitude))
            .withLatitude(bigDec(latitude))
        )
    );
    return quay;
  }

  private BigDecimal bigDec(double value) {
    return BigDecimal.valueOf(value);
  }

  private List<BigDecimal> coord(double longitude, double latitude) {
    return List.of(bigDec(longitude), bigDec(latitude));
  }

  private ServiceLinkExportContext createContext(
    String fromQuay,
    String toQuay,
    VehicleModeEnumeration mode,
    String serviceLinkId
  ) {
    return new ServiceLinkExportContext(
      fromQuay,
      toQuay,
      mode,
      new Ref(serviceLinkId, "1")
    );
  }
}
