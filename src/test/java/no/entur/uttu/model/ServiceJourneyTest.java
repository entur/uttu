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

package no.entur.uttu.model;

import static no.entur.uttu.model.ModelTestUtil.assertCheckPersistableFails;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ServiceJourneyTest {

  @Test
  public void setPassingTimes_assignsOrder() {
    ServiceJourney serviceJourney = new ServiceJourney();

    List<TimetabledPassingTime> passingTimes = Arrays.asList(
      new TimetabledPassingTime(),
      new TimetabledPassingTime(),
      new TimetabledPassingTime()
    );
    serviceJourney.setPassingTimes(passingTimes);

    Assert.assertEquals(3, serviceJourney.getPassingTimes().size());
    Assert.assertEquals(1, serviceJourney.getPassingTimes().get(0).getOrder());
    Assert.assertEquals(2, serviceJourney.getPassingTimes().get(1).getOrder());
    Assert.assertEquals(3, serviceJourney.getPassingTimes().get(2).getOrder());
  }

  @Test
  public void checkPersistable_success() {
    validServiceJourney().checkPersistable();
  }

  @Test
  public void checkPersistable_whenTooFewPassingTimes_thenThrowException() {
    ServiceJourney serviceJourney = validServiceJourney();
    serviceJourney.setJourneyPattern(createJP(3));
    assertCheckPersistableFails(serviceJourney);
  }

  @Test
  public void checkPersistable_whenTooManyPassingTimes_thenThrowException() {
    ServiceJourney serviceJourney = validServiceJourney();
    serviceJourney.setJourneyPattern(createJP(1));
    assertCheckPersistableFails(serviceJourney);
  }

  @Test
  public void checkPersistable_whenNoOperatorRef_thenThrowException() {
    ServiceJourney serviceJourney = validServiceJourney();
    serviceJourney.getJourneyPattern().getLine().setOperatorRef(null);
    assertCheckPersistableFails(serviceJourney);
  }

  @Test
  public void checkPersistable_whenOperatorRefOnServiceJourney_thenSuccess() {
    ServiceJourney serviceJourney = validServiceJourney();
    serviceJourney.getJourneyPattern().getLine().setOperatorRef(null);
    serviceJourney.setOperatorRef("11");
    serviceJourney.checkPersistable();
  }

  @Test
  public void checkPersistable_whenNoDepartureTimeForFirstPassingTime_thenThrowException() {
    ServiceJourney serviceJourney = validServiceJourney();
    serviceJourney.getPassingTimes().get(0).setDepartureTime(null);
    assertCheckPersistableFails(serviceJourney);
  }

  @Test
  public void checkPersistable_whenNoArrivalTimeForLastPassingTime_thenThrowException() {
    ServiceJourney serviceJourney = validServiceJourney();
    serviceJourney
      .getPassingTimes()
      .get(serviceJourney.getPassingTimes().size() - 1)
      .setArrivalTime(null);
    assertCheckPersistableFails(serviceJourney);
  }

  @Test
  public void checkPersistable_whenPassingTimesNotChronologically_thenThrowException() {
    ServiceJourney serviceJourney = validServiceJourney();

    TimetabledPassingTime first = serviceJourney.getPassingTimes().get(0);
    TimetabledPassingTime last = serviceJourney
      .getPassingTimes()
      .get(serviceJourney.getPassingTimes().size() - 1);

    last.setArrivalTime(first.getDepartureTime().minusMinutes(5));
    assertCheckPersistableFails(serviceJourney);
  }

  @Test
  public void checkPersistable_whenFixedArrivalBeforeEndOfTimeWindowOfPreviousStop_thenThrowException() {
    ServiceJourney serviceJourney = validServiceJourney();

    serviceJourney.setPassingTimes(
      List.of(
        // Time window 10:00 - 11:00
        timeWindowPassingTime(LocalTime.of(10, 0), LocalTime.of(11, 0)),
        // Arrival at 10:30
        passingTime(LocalTime.of(10, 30), null)
      )
    );

    assertCheckPersistableFails(serviceJourney);
  }

  @Test
  public void checkPersistable_whenFixedArrivalIsAfterEndOfTimeWindowOfNextStop_thenSuccess() {
    ServiceJourney serviceJourney = validServiceJourney();

    serviceJourney.setPassingTimes(
      List.of(
        // Time window 10:00 - 11:00
        timeWindowPassingTime(LocalTime.of(10, 0), LocalTime.of(11, 0)),
        // Arrival at 11:15
        passingTime(LocalTime.of(11, 15), null)
      )
    );

    serviceJourney.checkPersistable();
  }

  @Test
  public void checkPersistable_whenFixedDepartureIsAfterStartOfTimeWindowOfNextStop_thenThrowException() {
    ServiceJourney serviceJourney = validServiceJourney();

    serviceJourney.setPassingTimes(
      List.of(
        // Departs at 10:15
        passingTime(null, LocalTime.of(10, 15)),
        // Time window 10:00 - 11:00
        timeWindowPassingTime(LocalTime.of(10, 0), LocalTime.of(11, 0))
      )
    );

    assertCheckPersistableFails(serviceJourney);
  }

  @Test
  public void checkPersistable_whenFixedDepartureIsBeforeStartOfTimeWindowOfNextStop_thenSuccess() {
    ServiceJourney serviceJourney = validServiceJourney();

    serviceJourney.setPassingTimes(
      List.of(
        // Departs at 09:45
        passingTime(null, LocalTime.of(9, 45)),
        // Time window 10:00 - 11:00
        timeWindowPassingTime(LocalTime.of(10, 0), LocalTime.of(11, 0))
      )
    );

    serviceJourney.checkPersistable();
  }

  protected static ServiceJourney validServiceJourney() {
    ServiceJourney serviceJourney = new ServiceJourney();
    serviceJourney.setJourneyPattern(createJP(2));

    List<TimetabledPassingTime> passingTimes = Arrays.asList(
      passingTime(null, LocalTime.of(10, 0)),
      passingTime(LocalTime.of(11, 0), null)
    );

    serviceJourney.setPassingTimes(passingTimes);

    return serviceJourney;
  }

  private static JourneyPattern createJP(int size) {
    JourneyPattern journeyPattern = new JourneyPattern();
    journeyPattern.setLine(new FlexibleLine());
    journeyPattern.getLine().setOperatorRef("34");
    for (int i = 0; i < size; i++) {
      StopPointInJourneyPattern spijp = new StopPointInJourneyPattern();
      journeyPattern.getPointsInSequence().add(spijp);
    }

    return journeyPattern;
  }

  private static TimetabledPassingTime passingTime(
    LocalTime arrivalTime,
    LocalTime departureTime
  ) {
    return new TimetabledPassingTime()
      .withArrivalTime(arrivalTime)
      .withDepartureTime(departureTime);
  }

  private static TimetabledPassingTime timeWindowPassingTime(
    LocalTime earliestDepartureTime,
    LocalTime latestArrivalTime
  ) {
    return new TimetabledPassingTime()
      .withEarliestDepartureTime(earliestDepartureTime)
      .withLatestArrivalTime(latestArrivalTime);
  }
}
