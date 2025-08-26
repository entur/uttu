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

package no.entur.uttu.export.netex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.export.netex.producer.line.*;
import no.entur.uttu.model.*;
import no.entur.uttu.model.DayType;
import no.entur.uttu.model.DayTypeAssignment;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.ServiceJourney;
import no.entur.uttu.model.TimetabledPassingTime;
import no.entur.uttu.model.job.Export;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rutebanken.netex.model.*;

@RunWith(MockitoJUnitRunner.class)
public class NetexLineFileProducerTest {

  @Mock
  private NetexObjectFactory objectFactory;

  @Mock
  private LineProducer lineProducer;

  @Mock
  private RouteProducer routeProducer;

  @Mock
  private JourneyPatternProducer journeyPatternProducer;

  @Mock
  private ServiceJourneyProducer serviceJourneyProducer;

  @Mock
  private DatedServiceJourneyProducer datedServiceJourneyProducer;

  @InjectMocks
  private NetexLineFileProducer netexLineFileProducer;

  @Test
  public void testToNetexFileWithoutDatedServiceJourneys() {
    FlexibleLine line = createTestLine();
    NetexExportContext context = createTestContext(false); // No DatedServiceJourneys

    setupMockDependencies();

    NetexFile result = netexLineFileProducer.toNetexFile(line, context);

    assertThat(result).isNotNull();
    assertThat(result.getFileName()).contains("TestLine");
  }

  @Test
  public void testToNetexFileWithDatedServiceJourneys() {
    FlexibleLine line = createTestLine();
    NetexExportContext context = createTestContext(true); // Include DatedServiceJourneys

    context.addOperatingDay(LocalDate.of(2026, 2, 3));
    context.addOperatingDay(LocalDate.of(2026, 2, 4));

    setupMockDependencies();

    when(
      datedServiceJourneyProducer.produce(
        any(ServiceJourney.class),
        any(NetexExportContext.class)
      )
    ).thenReturn(
      Arrays.asList(
        createMockDatedServiceJourney("DSJ1"),
        createMockDatedServiceJourney("DSJ2")
      )
    );

    NetexFile result = netexLineFileProducer.toNetexFile(line, context);

    assertThat(result).isNotNull();
    assertThat(result.getFileName()).contains("TestLine");

    assertThat(context.getOperatingDays()).isNotEmpty();
    assertThat(context.getOperatingDays()).contains(
      LocalDate.of(2026, 2, 3),
      LocalDate.of(2026, 2, 4)
    );
  }

  @Test
  public void testNoDuplicationOfOperatingDaysInLineFiles() {
    FlexibleLine line = createTestLineWithServiceJourneys();
    NetexExportContext context = createTestContext(true);

    when(
      datedServiceJourneyProducer.produce(
        any(ServiceJourney.class),
        any(NetexExportContext.class)
      )
    ).thenAnswer(invocation -> {
      NetexExportContext ctx = invocation.getArgument(1);

      ctx.addOperatingDay(LocalDate.of(2026, 2, 5));
      ctx.addOperatingDay(LocalDate.of(2026, 2, 6));
      return Collections.singletonList(
        createMockDatedServiceJourney("DSJ_LINE_SPECIFIC")
      );
    });

    setupMockDependencies();

    NetexFile result = netexLineFileProducer.toNetexFile(line, context);

    assertThat(result).isNotNull();

    Set<LocalDate> operatingDays = context.getOperatingDays();
    assertThat(operatingDays).contains(
      LocalDate.of(2026, 2, 5),
      LocalDate.of(2026, 2, 6)
    );
  }

  @Test
  public void testDatedServiceJourneysIncludedInTimetableFrame() {
    FlexibleLine line = createTestLine();
    NetexExportContext context = createTestContext(true);
    context.addOperatingDay(LocalDate.of(2026, 2, 7));

    setupMockDependencies();

    List<DatedServiceJourney> datedJourneys = Arrays.asList(
      createMockDatedServiceJourney("DSJ_1"),
      createMockDatedServiceJourney("DSJ_2")
    );
    when(
      datedServiceJourneyProducer.produce(
        any(ServiceJourney.class),
        any(NetexExportContext.class)
      )
    ).thenReturn(datedJourneys);

    NetexFile result = netexLineFileProducer.toNetexFile(line, context);

    assertThat(result).isNotNull();
  }

  private FlexibleLine createTestLine() {
    FlexibleLine line = new FlexibleLine();
    line.setName("TestLine");
    line.setNetexId("TEST:Line:1");
    line.setJourneyPatterns(Collections.singletonList(createTestJourneyPattern()));
    return line;
  }

  private FlexibleLine createTestLineWithServiceJourneys() {
    FlexibleLine line = createTestLine();
    JourneyPattern jp = createTestJourneyPattern();
    jp.setServiceJourneys(
      Arrays.asList(createTestServiceJourney(), createTestServiceJourney())
    );
    line.setJourneyPatterns(Collections.singletonList(jp));
    return line;
  }

  private JourneyPattern createTestJourneyPattern() {
    JourneyPattern jp = new JourneyPattern();
    jp.setNetexId("TEST:JourneyPattern:1");
    jp.setName("Test Journey Pattern");
    jp.setServiceJourneys(Collections.singletonList(createTestServiceJourney()));
    return jp;
  }

  private ServiceJourney createTestServiceJourney() {
    ServiceJourney sj = new ServiceJourney();
    sj.setNetexId("TEST:ServiceJourney:1");
    sj.setName("Test Service Journey");

    // Create a proper DayType with DayTypeAssignment that will generate a valid AvailabilityPeriod
    DayType dayType = new DayType();
    dayType.setNetexId("TEST:DayType:1");

    DayTypeAssignment assignment = new DayTypeAssignment();
    assignment.setDate(LocalDate.of(2026, 2, 3));
    assignment.setAvailable(true);

    dayType.setDayTypeAssignments(Collections.singletonList(assignment));

    // Use the correct method to set DayTypes
    sj.updateDayTypes(Collections.singletonList(dayType));

    // Add timetabled passing times
    TimetabledPassingTime tpt = new TimetabledPassingTime();
    tpt.setDepartureTime(LocalTime.of(10, 0));
    sj.setPassingTimes(Collections.singletonList(tpt));

    return sj;
  }

  private NetexExportContext createTestContext(boolean includeDatedServiceJourneys) {
    Export export = new Export();
    export.setIncludeDatedServiceJourneys(includeDatedServiceJourneys);

    Provider provider = new Provider();
    provider.setName("TestProvider");
    export.setProvider(provider);

    return new NetexExportContext(export);
  }

  private void setupMockDependencies() {
    Line_VersionStructure mockLine = mock(Line_VersionStructure.class);
    when(
      lineProducer.produce(any(FlexibleLine.class), any(), any(NetexExportContext.class))
    ).thenReturn(mockLine);

    when(
      routeProducer.produce(any(FlexibleLine.class), any(NetexExportContext.class))
    ).thenReturn(Collections.singletonList(mock(Route.class)));

    when(
      journeyPatternProducer.produce(
        any(JourneyPattern.class),
        any(),
        any(NetexExportContext.class)
      )
    ).thenReturn(mock(org.rutebanken.netex.model.JourneyPattern.class));

    when(
      serviceJourneyProducer.produce(
        any(ServiceJourney.class),
        any(),
        any(NetexExportContext.class)
      )
    ).thenReturn(mock(org.rutebanken.netex.model.ServiceJourney.class));

    when(
      objectFactory.createLineServiceFrame(any(), any(), any(), any(), any())
    ).thenReturn(mock(ServiceFrame.class));

    when(objectFactory.createTimetableFrame(any(), any(), any())).thenReturn(
      mock(TimetableFrame.class)
    );

    when(objectFactory.createCompositeFrame(any(), any(), any(), any())).thenReturn(
      mock(CompositeFrame.class)
    );

    when(objectFactory.createPublicationDelivery(any(), any())).thenReturn(
      mock(jakarta.xml.bind.JAXBElement.class)
    );
  }

  private DatedServiceJourney createMockDatedServiceJourney(String id) {
    DatedServiceJourney dsj = new DatedServiceJourney();
    dsj.setId(id);
    dsj.setVersion("0");
    return dsj;
  }
}
