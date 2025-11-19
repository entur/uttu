package no.entur.uttu.export.netex.producer.line;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.entur.uttu.config.AdditionalCodespacesConfig;
import no.entur.uttu.config.ExportTimeZone;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Codespace;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.StopPointInJourneyPattern;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.routing.RoutingService;
import no.entur.uttu.service.FlexibleAreaValidationService;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import no.entur.uttu.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.LinkInLinkSequence_VersionedChildStructure;
import org.rutebanken.netex.model.NoticeAssignment;
import org.rutebanken.netex.model.ServiceLinkInJourneyPattern_VersionedChildStructure;
import org.rutebanken.netex.model.StopPlace;

class JourneyPatternProducerTest {

  private JourneyPatternProducer producer;
  private NetexObjectFactory objectFactory;
  private ContactStructureProducer contactStructureProducer;
  private StopPlaceRegistry mockStopPlaceRegistry;
  private RoutingService mockRoutingService;
  private FlexibleAreaValidationService mockFlexibleAreaValidationService;
  private NetexExportContext context;

  @BeforeEach
  void setUp() {
    objectFactory = new NetexObjectFactory(
      new DateUtils(),
      new ExportTimeZone(),
      new AdditionalCodespacesConfig()
    );
    contactStructureProducer = new ContactStructureProducer(objectFactory);
    mockStopPlaceRegistry = mock(StopPlaceRegistry.class);
    mockRoutingService = mock(RoutingService.class);
    mockFlexibleAreaValidationService = mock(FlexibleAreaValidationService.class);

    producer = new JourneyPatternProducer(
      objectFactory,
      contactStructureProducer,
      mockStopPlaceRegistry,
      mockRoutingService,
      mockFlexibleAreaValidationService
    );

    Export export = new Export();
    export.setGenerateServiceLinks(true);
    Provider provider = new Provider();
    Codespace codespace = new Codespace();
    codespace.setXmlns("TST");
    provider.setCodespace(codespace);
    export.setProvider(provider);
    context = new NetexExportContext(export);
  }

  @Test
  void testServiceLinkInJourneyPatternIdsAreUniqueAcrossJourneyPatterns() {
    // Setup mock to return valid stop place for quay refs
    when(mockStopPlaceRegistry.getStopPlaceByQuayRef(anyString())).thenReturn(
      Optional.of(new StopPlace())
    );
    when(mockRoutingService.isEnabled(any())).thenReturn(true);

    // Create first journey pattern with stops A -> B
    JourneyPattern jp1 = createJourneyPattern(
      "TST:JourneyPattern:JP1",
      "TST:Line:1",
      List.of("TST:Quay:A", "TST:Quay:B")
    );

    // Create second journey pattern with same stops A -> B
    JourneyPattern jp2 = createJourneyPattern(
      "TST:JourneyPattern:JP2",
      "TST:Line:1",
      List.of("TST:Quay:A", "TST:Quay:B")
    );

    List<NoticeAssignment> noticeAssignments1 = new ArrayList<>();
    List<NoticeAssignment> noticeAssignments2 = new ArrayList<>();

    // Produce NeTEx journey patterns
    org.rutebanken.netex.model.JourneyPattern netexJp1 = producer.produce(
      jp1,
      noticeAssignments1,
      context
    );
    org.rutebanken.netex.model.JourneyPattern netexJp2 = producer.produce(
      jp2,
      noticeAssignments2,
      context
    );

    // Get service link in journey pattern from each
    assertNotNull(netexJp1.getLinksInSequence());
    assertNotNull(netexJp2.getLinksInSequence());

    List<LinkInLinkSequence_VersionedChildStructure> links1 = netexJp1
      .getLinksInSequence()
      .getServiceLinkInJourneyPatternOrTimingLinkInJourneyPattern();
    List<LinkInLinkSequence_VersionedChildStructure> links2 = netexJp2
      .getLinksInSequence()
      .getServiceLinkInJourneyPatternOrTimingLinkInJourneyPattern();

    assertEquals(1, links1.size(), "JP1 should have one service link");
    assertEquals(1, links2.size(), "JP2 should have one service link");

    ServiceLinkInJourneyPattern_VersionedChildStructure slInJp1 =
      (ServiceLinkInJourneyPattern_VersionedChildStructure) links1.get(0);
    ServiceLinkInJourneyPattern_VersionedChildStructure slInJp2 =
      (ServiceLinkInJourneyPattern_VersionedChildStructure) links2.get(0);

    // The service link in JP IDs should be different (include JP ID in the suffix)
    assertNotEquals(
      slInJp1.getId(),
      slInJp2.getId(),
      "ServiceLinkInJourneyPattern IDs should be unique across journey patterns"
    );

    // Verify the IDs contain the journey pattern identifier
    assertTrue(
      slInJp1.getId().contains("JP1"),
      "ServiceLinkInJourneyPattern ID should contain JP1 identifier"
    );
    assertTrue(
      slInJp2.getId().contains("JP2"),
      "ServiceLinkInJourneyPattern ID should contain JP2 identifier"
    );

    // Both should reference the same service link (same quay pair)
    String serviceLinkRef1 = slInJp1.getServiceLinkRef().getRef();
    String serviceLinkRef2 = slInJp2.getServiceLinkRef().getRef();

    assertEquals(
      serviceLinkRef1,
      serviceLinkRef2,
      "Both ServiceLinkInJourneyPatterns should reference the same ServiceLink"
    );

    // Only one service link should be generated for the same stop pair
    assertEquals(
      1,
      context.serviceLinks.size(),
      "Should have only one service link for the same quay pair"
    );
  }

  @Test
  void testServiceLinkInJourneyPatternIdFormat() {
    when(mockStopPlaceRegistry.getStopPlaceByQuayRef(anyString())).thenReturn(
      Optional.of(new StopPlace())
    );
    when(mockRoutingService.isEnabled(any())).thenReturn(true);

    JourneyPattern jp = createJourneyPattern(
      "TST:JourneyPattern:123",
      "TST:Line:456",
      List.of("TST:Quay:A", "TST:Quay:B")
    );

    List<NoticeAssignment> noticeAssignments = new ArrayList<>();
    org.rutebanken.netex.model.JourneyPattern netexJp = producer.produce(
      jp,
      noticeAssignments,
      context
    );

    assertNotNull(netexJp.getLinksInSequence());
    List<LinkInLinkSequence_VersionedChildStructure> links = netexJp
      .getLinksInSequence()
      .getServiceLinkInJourneyPatternOrTimingLinkInJourneyPattern();

    assertEquals(1, links.size());

    ServiceLinkInJourneyPattern_VersionedChildStructure slInJp =
      (ServiceLinkInJourneyPattern_VersionedChildStructure) links.get(0);

    // ID should be in format: codespace:ServiceLinkInJourneyPattern:JP_ID_FROM_TO
    String id = slInJp.getId();
    assertTrue(id.contains("123"), "ID should contain journey pattern suffix '123'");
    assertTrue(id.contains("A"), "ID should contain from quay suffix 'A'");
    assertTrue(id.contains("B"), "ID should contain to quay suffix 'B'");
  }

  @Test
  void testNoServiceLinksGeneratedWhenRoutingServiceDisabled() {
    when(mockStopPlaceRegistry.getStopPlaceByQuayRef(anyString())).thenReturn(
      Optional.of(new StopPlace())
    );
    when(mockRoutingService.isEnabled(any())).thenReturn(false);

    JourneyPattern jp = createJourneyPattern(
      "TST:JourneyPattern:1",
      "TST:Line:1",
      List.of("TST:Quay:A", "TST:Quay:B")
    );

    List<NoticeAssignment> noticeAssignments = new ArrayList<>();
    org.rutebanken.netex.model.JourneyPattern netexJp = producer.produce(
      jp,
      noticeAssignments,
      context
    );

    assertNull(
      netexJp.getLinksInSequence(),
      "No links should be generated when routing service is disabled"
    );
  }

  private JourneyPattern createJourneyPattern(
    String jpId,
    String lineId,
    List<String> quayRefs
  ) {
    JourneyPattern jp = new JourneyPattern();
    jp.setNetexId(jpId);
    jp.setVersion(1L);

    FlexibleLine line = new FlexibleLine();
    line.setNetexId(lineId);
    line.setVersion(1L);
    line.setTransportMode(VehicleModeEnumeration.BUS);
    jp.setLine(line);

    List<StopPointInJourneyPattern> stopPoints = new ArrayList<>();
    for (int i = 0; i < quayRefs.size(); i++) {
      StopPointInJourneyPattern spijp = new StopPointInJourneyPattern();
      spijp.setNetexId(jpId + "_SP" + (i + 1));
      spijp.setVersion(1L);
      spijp.setOrder(i + 1);
      spijp.setQuayRef(quayRefs.get(i));
      spijp.setNotices(Collections.emptyList());
      stopPoints.add(spijp);
    }
    jp.setPointsInSequence(stopPoints);
    jp.setNotices(Collections.emptyList());

    return jp;
  }
}
