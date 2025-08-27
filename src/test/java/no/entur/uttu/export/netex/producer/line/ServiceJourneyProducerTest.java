package no.entur.uttu.export.netex.producer.line;

import static no.entur.uttu.model.DayTypeTest.period;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import no.entur.uttu.config.ExportTimeZone;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.export.netex.producer.common.OrganisationProducer;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.model.job.Export;
import no.entur.uttu.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.NoticeAssignment;

class ServiceJourneyProducerTest {

  private NetexObjectFactory objectFactory;
  private ContactStructureProducer contactStructureProducer;
  private OrganisationProducer organisationProducer;
  private ServiceJourneyProducer producer;
  private Export export;
  private NetexExportContext context;
  private ServiceJourney local;
  private JourneyPattern journeyPattern;
  private List<NoticeAssignment> noticeAssignments;

  @BeforeEach
  void setUp() {
    objectFactory = new NetexObjectFactory(new DateUtils(), new ExportTimeZone());
    contactStructureProducer = new ContactStructureProducer(objectFactory);
    organisationProducer = mock(OrganisationProducer.class);
    producer = new ServiceJourneyProducer(
      objectFactory,
      contactStructureProducer,
      organisationProducer
    );
    local = mock(ServiceJourney.class, RETURNS_DEEP_STUBS);
    journeyPattern = mock(JourneyPattern.class);
    no.entur.uttu.model.Ref jpRef = new no.entur.uttu.model.Ref("JP:1", "1");
    when(journeyPattern.getRef()).thenReturn(jpRef);
    when(local.getJourneyPattern()).thenReturn(journeyPattern);
    when(local.getPassingTimes()).thenReturn(Collections.emptyList());
    when(local.getName()).thenReturn("Test Journey");
    when(local.getNotices()).thenReturn(Collections.emptyList());
    when(local.getBookingArrangement()).thenReturn(null);
    when(local.getPublicCode()).thenReturn("PCODE");
    when(local.getRef()).thenReturn(new no.entur.uttu.model.Ref("SJ:1", "1"));
    noticeAssignments = new ArrayList<>();
  }

  private void setDayTypes(Set<DayType> dayTypes) {
    local.getDayTypes().addAll(dayTypes);
  }

  @Test
  void produce_shouldNotSetDayTypes_whenIncludeDatedServiceJourneys() {
    export = new Export();
    export.setIncludeDatedServiceJourneys(true);
    context = new NetexExportContext(export);

    DayType dayType = new DayType();
    dayType.getDayTypeAssignments().add(period(LocalDate.MIN, LocalDate.MAX));
    setDayTypes(Set.of(dayType));

    org.rutebanken.netex.model.ServiceJourney sj = producer.produce(
      local,
      noticeAssignments,
      context
    );
    assertNull(
      sj.getDayTypes(),
      "DayTypes should not be set when includeDatedServiceJourneys is true"
    );
  }

  @Test
  void produce_shouldSetDayTypes_whenNotIncludeDatedServiceJourneys() {
    export = new Export();
    export.setIncludeDatedServiceJourneys(false);
    context = new NetexExportContext(export);

    DayType dayType = new DayType();
    dayType.setNetexId("DT:1");
    dayType.setVersion(1L);
    dayType.getDayTypeAssignments().add(period(LocalDate.MIN, LocalDate.MAX));
    setDayTypes(Set.of(dayType));

    org.rutebanken.netex.model.ServiceJourney sj = producer.produce(
      local,
      noticeAssignments,
      context
    );
    assertNotNull(
      sj.getDayTypes(),
      "DayTypes should be set when includeDatedServiceJourneys is false"
    );
  }
}
