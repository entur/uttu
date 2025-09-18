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

package no.entur.uttu.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.entur.uttu.model.*;
import org.springframework.stereotype.Component;

@Component
public class EntityCloner {

  private final Map<String, String> idMappings = new HashMap<>();

  public void clearMappings() {
    idMappings.clear();
  }

  public <T extends Line> T cloneLine(
    T sourceLine,
    Provider targetProvider,
    Network targetNetwork
  ) {
    LineCloningVisitor visitor = new LineCloningVisitor(targetProvider, targetNetwork);
    sourceLine.accept(visitor);
    return (T) visitor.getClonedLine();
  }

  public JourneyPattern cloneJourneyPattern(JourneyPattern source, Line targetLine) {
    JourneyPattern clone = new JourneyPattern();

    copyProviderEntityFields(source, clone, targetLine.getProvider());

    clone.setLine(targetLine);
    clone.setDirectionType(source.getDirectionType());

    if (source.getNotices() != null) {
      List<Notice> clonedNotices = new ArrayList<>();
      for (Notice notice : source.getNotices()) {
        clonedNotices.add(cloneNotice(notice, targetLine.getProvider()));
      }
      clone.setNotices(clonedNotices);
    }

    if (source.getPointsInSequence() != null) {
      List<StopPointInJourneyPattern> clonedStops = new ArrayList<>();
      for (StopPointInJourneyPattern stopPoint : source.getPointsInSequence()) {
        clonedStops.add(cloneStopPointInJourneyPattern(stopPoint, clone));
      }
      clone.setPointsInSequence(clonedStops);
    }

    if (source.getServiceJourneys() != null) {
      List<ServiceJourney> clonedJourneys = new ArrayList<>();
      for (ServiceJourney serviceJourney : source.getServiceJourneys()) {
        clonedJourneys.add(cloneServiceJourney(serviceJourney, clone));
      }
      clone.setServiceJourneys(clonedJourneys);
    }

    return clone;
  }

  public ServiceJourney cloneServiceJourney(
    ServiceJourney source,
    JourneyPattern targetPattern
  ) {
    ServiceJourney clone = new ServiceJourney();

    copyProviderEntityFields(source, clone, targetPattern.getProvider());

    clone.setJourneyPattern(targetPattern);
    clone.updateDayTypes(new ArrayList<>(source.getDayTypes()));
    clone.setPublicCode(source.getPublicCode());
    clone.setOperatorRef(source.getOperatorRef());

    if (source.getNotices() != null) {
      List<Notice> clonedNotices = new ArrayList<>();
      for (Notice notice : source.getNotices()) {
        clonedNotices.add(cloneNotice(notice, targetPattern.getProvider()));
      }
      clone.setNotices(clonedNotices);
    }

    if (source.getPassingTimes() != null) {
      List<TimetabledPassingTime> clonedPassingTimes = new ArrayList<>();
      for (TimetabledPassingTime passingTime : source.getPassingTimes()) {
        clonedPassingTimes.add(cloneTimetabledPassingTime(passingTime, clone));
      }
      clone.setPassingTimes(clonedPassingTimes);
    }

    if (source.getBookingArrangement() != null) {
      clone.setBookingArrangement(
        cloneBookingArrangement(
          source.getBookingArrangement(),
          targetPattern.getProvider()
        )
      );
    }

    return clone;
  }

  public Notice cloneNotice(Notice source, Provider targetProvider) {
    Notice clone = new Notice();

    copyIdentifiedEntityFields(source, clone);
    clone.setProvider(targetProvider);
    clone.setNetexId(null);

    clone.setText(source.getText());

    return clone;
  }

  public DayType cloneDayType(DayType source, Provider targetProvider) {
    DayType clone = new DayType();

    copyProviderEntityFields(source, clone, targetProvider);

    clone.setDaysOfWeek(source.getDaysOfWeek());
    clone.setDayTypeAssignments(source.getDayTypeAssignments());

    return clone;
  }

  public DayTypeAssignment cloneDayTypeAssignment(
    DayTypeAssignment source,
    Provider targetProvider
  ) {
    DayTypeAssignment clone = new DayTypeAssignment();

    copyIdentifiedEntityFields(source, clone);

    clone.setDate(source.getDate());
    clone.setAvailable(source.getAvailable());

    return clone;
  }

  public BookingArrangement cloneBookingArrangement(
    BookingArrangement source,
    Provider targetProvider
  ) {
    BookingArrangement clone = new BookingArrangement();

    copyIdentifiedEntityFields(source, clone);

    clone.setBookingContact(source.getBookingContact());
    clone.setBookingMethods(source.getBookingMethods());
    clone.setBookingAccess(source.getBookingAccess());
    clone.setBookWhen(source.getBookWhen());
    clone.setBuyWhen(source.getBuyWhen());
    clone.setLatestBookingTime(source.getLatestBookingTime());
    clone.setMinimumBookingPeriod(source.getMinimumBookingPeriod());

    return clone;
  }

  public DestinationDisplay cloneDestinationDisplay(
    DestinationDisplay source,
    Provider targetProvider
  ) {
    DestinationDisplay clone = new DestinationDisplay();

    copyProviderEntityFields(source, clone, targetProvider);

    clone.setFrontText(source.getFrontText());

    return clone;
  }

  private StopPointInJourneyPattern cloneStopPointInJourneyPattern(
    StopPointInJourneyPattern source,
    JourneyPattern targetPattern
  ) {
    StopPointInJourneyPattern clone = new StopPointInJourneyPattern();

    copyProviderEntityFields(source, clone, targetPattern.getProvider());

    clone.setJourneyPattern(targetPattern);
    clone.setOrder(source.getOrder());
    clone.setQuayRef(source.getQuayRef());
    clone.setForAlighting(source.getForAlighting());
    clone.setForBoarding(source.getForBoarding());

    if (source.getFlexibleStopPlace() != null) {
      clone.setFlexibleStopPlace(source.getFlexibleStopPlace());
    }

    if (source.getBookingArrangement() != null) {
      clone.setBookingArrangement(
        cloneBookingArrangement(
          source.getBookingArrangement(),
          targetPattern.getProvider()
        )
      );
    }

    if (source.getDestinationDisplay() != null) {
      clone.setDestinationDisplay(
        cloneDestinationDisplay(
          source.getDestinationDisplay(),
          targetPattern.getProvider()
        )
      );
    }

    if (source.getNotices() != null) {
      List<Notice> clonedNotices = new ArrayList<>();
      for (Notice notice : source.getNotices()) {
        clonedNotices.add(cloneNotice(notice, targetPattern.getProvider()));
      }
      clone.setNotices(clonedNotices);
    }

    return clone;
  }

  private TimetabledPassingTime cloneTimetabledPassingTime(
    TimetabledPassingTime source,
    ServiceJourney targetJourney
  ) {
    TimetabledPassingTime clone = new TimetabledPassingTime();

    copyProviderEntityFields(source, clone, targetJourney.getProvider());

    clone.setServiceJourney(targetJourney);
    clone.setOrder(source.getOrder());
    clone.setArrivalTime(source.getArrivalTime());
    clone.setArrivalDayOffset(source.getArrivalDayOffset());
    clone.setDepartureTime(source.getDepartureTime());
    clone.setDepartureDayOffset(source.getDepartureDayOffset());
    clone.setLatestArrivalTime(source.getLatestArrivalTime());
    clone.setLatestArrivalDayOffset(source.getLatestArrivalDayOffset());
    clone.setEarliestDepartureTime(source.getEarliestDepartureTime());
    clone.setEarliestDepartureDayOffset(source.getEarliestDepartureDayOffset());

    if (source.getNotices() != null) {
      List<Notice> clonedNotices = new ArrayList<>();
      for (Notice notice : source.getNotices()) {
        clonedNotices.add(cloneNotice(notice, targetJourney.getProvider()));
      }
      clone.setNotices(clonedNotices);
    }

    return clone;
  }

  private void copyProviderEntityFields(
    ProviderEntity source,
    ProviderEntity target,
    Provider targetProvider
  ) {
    copyIdentifiedEntityFields(source, target);
    target.setProvider(targetProvider);
    target.setNetexId(null);

    if (
      source instanceof GroupOfEntities_VersionStructure &&
      target instanceof GroupOfEntities_VersionStructure
    ) {
      GroupOfEntities_VersionStructure sourceGroup =
        (GroupOfEntities_VersionStructure) source;
      GroupOfEntities_VersionStructure targetGroup =
        (GroupOfEntities_VersionStructure) target;
      targetGroup.setName(sourceGroup.getName());
      targetGroup.setDescription(sourceGroup.getDescription());
      targetGroup.setPrivateCode(sourceGroup.getPrivateCode());
    }
  }

  private void copyIdentifiedEntityFields(
    IdentifiedEntity source,
    IdentifiedEntity target
  ) {
    target.setVersion(source.getVersion());
    target.setCreatedBy(source.getCreatedBy());
    target.setCreated(source.getCreated());
    target.setChangedBy(source.getChangedBy());
    target.setChanged(source.getChanged());
  }

  private class LineCloningVisitor implements LineVisitor {

    private final Provider targetProvider;
    private final Network targetNetwork;
    private Line clonedLine;

    public LineCloningVisitor(Provider targetProvider, Network targetNetwork) {
      this.targetProvider = targetProvider;
      this.targetNetwork = targetNetwork;
    }

    @Override
    public void visitFixedLine(FixedLine fixedLine) {
      FixedLine clone = new FixedLine();
      copyLineFields(fixedLine, clone);
      this.clonedLine = clone;
    }

    @Override
    public void visitFlexibleLine(FlexibleLine flexibleLine) {
      FlexibleLine clone = new FlexibleLine();
      copyLineFields(flexibleLine, clone);

      clone.setFlexibleLineType(flexibleLine.getFlexibleLineType());

      if (flexibleLine.getBookingArrangement() != null) {
        clone.setBookingArrangement(
          cloneBookingArrangement(flexibleLine.getBookingArrangement(), targetProvider)
        );
      }

      this.clonedLine = clone;
    }

    private void copyLineFields(Line source, Line target) {
      copyProviderEntityFields(source, target, targetProvider);

      target.setNetwork(targetNetwork);
      target.setPublicCode(source.getPublicCode());
      target.setTransportMode(source.getTransportMode());
      target.setTransportSubmode(source.getTransportSubmode());
      target.setOperatorRef(source.getOperatorRef());

      if (source.getBranding() != null) {
        target.setBranding(source.getBranding());
      }

      if (source.getNotices() != null) {
        List<Notice> clonedNotices = new ArrayList<>();
        for (Notice notice : source.getNotices()) {
          clonedNotices.add(cloneNotice(notice, targetProvider));
        }
        target.setNotices(clonedNotices);
      }

      if (source.getJourneyPatterns() != null) {
        List<JourneyPattern> clonedPatterns = new ArrayList<>();
        for (JourneyPattern pattern : source.getJourneyPatterns()) {
          clonedPatterns.add(cloneJourneyPattern(pattern, target));
        }
        target.setJourneyPatterns(clonedPatterns);
      }
    }

    public Line getClonedLine() {
      return clonedLine;
    }
  }
}
