package no.entur.uttu.export.netex.producer.common;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.junit.jupiter.api.Assertions;
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
    new ExportTimeZone()
  );

  StopPlaceRegistry mockStopPlaceRegistry = Mockito.mock(StopPlaceRegistry.class);
  RoutingService mockRoutingService = Mockito.mock(RoutingService.class);

  @BeforeEach
  public void setUp() {
    serviceLinkProducer = new ServiceLinkProducer(
      objectFactory,
      mockStopPlaceRegistry,
      mockRoutingService
    );
  }

  @Test
  void testServiceLinkProducerProducesCorrectLineString() {
    Export export = new Export();
    Provider provider = new Provider();
    Codespace codespace = new Codespace();
    codespace.setXmlns("TST");
    provider.setCodespace(codespace);
    export.setProvider(provider);
    NetexExportContext context = new NetexExportContext(export);

    context.serviceLinks = Set.of(
      new ServiceLinkExportContext(
        "TST:Quay:1",
        "TST:Quay:2",
        VehicleModeEnumeration.BUS,
        new Ref("TST:ServiceLink:1", "1")
      )
    );

    Quay quayFrom = new Quay();
    quayFrom.setId("TST:Quay:1");
    quayFrom.setCentroid(
      new SimplePoint_VersionStructure()
        .withLocation(
          new LocationStructure()
            .withLongitude(BigDecimal.valueOf(10.1))
            .withLatitude(BigDecimal.valueOf(60.2))
        )
    );

    Quay quayTo = new Quay();
    quayTo.setId("TST:Quay:2");
    quayTo.setCentroid(
      new SimplePoint_VersionStructure()
        .withLocation(
          new LocationStructure()
            .withLongitude(BigDecimal.valueOf(10.5))
            .withLatitude(BigDecimal.valueOf(60.6))
        )
    );

    when(mockStopPlaceRegistry.getQuayById("TST:Quay:1")).thenReturn(
      Optional.of(quayFrom)
    );
    when(mockStopPlaceRegistry.getQuayById("TST:Quay:2")).thenReturn(Optional.of(quayTo));

    // GeoJSON uses coordinate pairs in order longitude then latitude
    var params = new RoutingServiceRequestParams(
      BigDecimal.valueOf(10.1),
      BigDecimal.valueOf(60.2),
      BigDecimal.valueOf(10.5),
      BigDecimal.valueOf(60.6),
      VehicleModeEnumeration.BUS
    );
    when(mockRoutingService.getRouteGeometry(params)).thenReturn(
      new RouteGeometry(
        List.of(
          List.of(BigDecimal.valueOf(10.1), BigDecimal.valueOf(60.2)),
          List.of(BigDecimal.valueOf(10.2), BigDecimal.valueOf(60.3)),
          List.of(BigDecimal.valueOf(10.3), BigDecimal.valueOf(60.4)),
          List.of(BigDecimal.valueOf(10.4), BigDecimal.valueOf(60.5)),
          List.of(BigDecimal.valueOf(10.5), BigDecimal.valueOf(60.6))
        ),
        BigDecimal.valueOf(100)
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

    Assertions.assertEquals(
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
}
