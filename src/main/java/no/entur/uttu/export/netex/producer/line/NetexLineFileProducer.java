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

package no.entur.uttu.export.netex.producer.line;

import jakarta.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;
import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.NetexFile;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.JourneyPattern;
import no.entur.uttu.model.Line;
import no.entur.uttu.util.ExportUtil;
import org.rutebanken.netex.model.*;
import org.springframework.stereotype.Component;

@Component
public class NetexLineFileProducer {

  private final NetexObjectFactory objectFactory;
  private final LineProducer lineProducer;
  private final RouteProducer routeProducer;
  private final JourneyPatternProducer journeyPatternProducer;
  private final ServiceJourneyProducer serviceJourneyProducer;
  private final DatedServiceJourneyProducer datedServiceJourneyProducer;

  public NetexLineFileProducer(
    NetexObjectFactory objectFactory,
    LineProducer lineProducer,
    RouteProducer routeProducer,
    JourneyPatternProducer journeyPatternProducer,
    ServiceJourneyProducer serviceJourneyProducer,
    DatedServiceJourneyProducer datedServiceJourneyProducer
  ) {
    this.objectFactory = objectFactory;
    this.lineProducer = lineProducer;
    this.routeProducer = routeProducer;
    this.journeyPatternProducer = journeyPatternProducer;
    this.serviceJourneyProducer = serviceJourneyProducer;
    this.datedServiceJourneyProducer = datedServiceJourneyProducer;
  }

  public NetexFile toNetexFile(Line line, NetexExportContext context) {
    String fileName = ExportUtil.createLineFilename(line);

    ServiceFrame serviceFrame = createServiceFrame(line, context);
    TimetableFrame timetableFrame = createTimetableFrame(line, context);

    AvailabilityPeriod availabilityPeriod =
      NetexLineUtilities.calculateAvailabilityPeriodForLine(line);

    if (availabilityPeriod != null) {
      context.updateAvailabilityPeriod(availabilityPeriod);
    }

    CompositeFrame compositeFrame = objectFactory.createCompositeFrame(
      context,
      availabilityPeriod,
      serviceFrame,
      timetableFrame
    );

    JAXBElement<PublicationDeliveryStructure> publicationDelivery =
      objectFactory.createPublicationDelivery(context, compositeFrame);

    return new NetexFile(fileName, publicationDelivery);
  }

  private ServiceFrame createServiceFrame(Line line, NetexExportContext context) {
    List<NoticeAssignment> noticeAssignments = new ArrayList<>();
    org.rutebanken.netex.model.Line_VersionStructure netexLine = lineProducer.produce(
      line,
      noticeAssignments,
      context
    );
    return createServiceFrame(line, noticeAssignments, netexLine, context);
  }

  private ServiceFrame createServiceFrame(
    Line line,
    List<NoticeAssignment> noticeAssignments,
    org.rutebanken.netex.model.Line_VersionStructure netexLine,
    NetexExportContext context
  ) {
    List<Route> netexRoutes = routeProducer.produce(line, context);
    List<org.rutebanken.netex.model.JourneyPattern> netexJourneyPatterns = line
      .getJourneyPatterns()
      .stream()
      .filter(context::isValid)
      .map(jp -> journeyPatternProducer.produce(jp, noticeAssignments, context))
      .toList();
    return objectFactory.createLineServiceFrame(
      context,
      netexLine,
      netexRoutes,
      netexJourneyPatterns,
      noticeAssignments
    );
  }

  private TimetableFrame createTimetableFrame(Line line, NetexExportContext context) {
    List<NoticeAssignment> noticeAssignments = new ArrayList<>();

    List<no.entur.uttu.model.ServiceJourney> localServiceJourneys = line
      .getJourneyPatterns()
      .stream()
      .map(JourneyPattern::getServiceJourneys)
      .flatMap(List::stream)
      .filter(context::isValid)
      .toList();

    List<org.rutebanken.netex.model.ServiceJourney> netexServiceJourneys =
      localServiceJourneys
        .stream()
        .map(sj -> serviceJourneyProducer.produce(sj, noticeAssignments, context))
        .toList();
    List<ServiceJourney_VersionStructure> journeys = new ArrayList<>(
      netexServiceJourneys
    );

    if (context.shouldIncludeDatedServiceJourneys()) {
      localServiceJourneys
        .stream()
        .map(sj -> datedServiceJourneyProducer.produce(sj, context))
        .flatMap(List::stream)
        .forEach(journeys::add);
    }

    return objectFactory.createTimetableFrame(context, journeys, noticeAssignments);
  }
}
