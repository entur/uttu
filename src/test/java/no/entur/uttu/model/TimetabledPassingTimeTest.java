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
import org.junit.Assert;
import org.junit.Test;

public class TimetabledPassingTimeTest {

  private static final LocalTime T0 = LocalTime.of(10, 0);
  private static final LocalTime T1 = LocalTime.of(10, 15);
  private static final LocalTime T2 = LocalTime.of(10, 30);
  private static final LocalTime T3 = LocalTime.of(10, 45);

  @Test
  public void checkPersistable_minimumFieldsSet_success() {
    new TimetabledPassingTime().withArrivalTime(T0).checkPersistable();
    new TimetabledPassingTime().withDepartureTime(T0).checkPersistable();
    new TimetabledPassingTime().withEarliestDepartureTime(T0).checkPersistable();
    new TimetabledPassingTime().withLatestArrivalTime(T0).checkPersistable();
  }

  @Test
  public void checkPersistable_maxFieldsSet_success() {
    new TimetabledPassingTime()
      .withEarliestDepartureTime(T0)
      .withDepartureTime(T0)
      .checkPersistable();
    new TimetabledPassingTime()
      .withArrivalTime(T0)
      .withLatestArrivalTime(T1)
      .withEarliestDepartureTime(T2)
      .withDepartureTime(T3)
      .checkPersistable();

    new TimetabledPassingTime()
      .withArrivalTime(T3)
      .withArrivalDayOffset(0)
      .withLatestArrivalTime(T2)
      .withLatestArrivalDayOffset(1)
      .withEarliestDepartureTime(T1)
      .withEarliestDepartureDayOffset(2)
      .withDepartureTime(T0)
      .withDepartureDayOffset(3)
      .checkPersistable();
  }

  @Test
  public void checkPersistable_departureBeforeArrival_givesException() {
    assertCheckPersistableFails(
      new TimetabledPassingTime().withArrivalTime(T1).withDepartureTime(T0)
    );
  }

  @Test
  public void checkPersistable_departureBeforeEarliestDeparture_givesException() {
    assertCheckPersistableFails(
      new TimetabledPassingTime().withDepartureTime(T0).withEarliestDepartureTime(T1)
    );
  }

  @Test
  public void checkPersistable_arrivalAfterLatestArrival_givesException() {
    assertCheckPersistableFails(
      new TimetabledPassingTime().withArrivalTime(T1).withLatestArrivalTime(T0)
    );
  }

  @Test
  public void checkPersistable_arrivalEqualsDeparture_givesException() {
    assertCheckPersistableFails(
      new TimetabledPassingTime().withArrivalTime(T0).withDepartureTime(T0)
    );
  }

  @Test
  public void checkBeforeOther_departureOneAfterDepartureTwo_givesException() {
    TimetabledPassingTime one = new TimetabledPassingTime()
      .withDepartureTime(T1)
      .withDepartureDayOffset(1);
    TimetabledPassingTime two = new TimetabledPassingTime()
      .withDepartureTime(T0)
      .withDepartureDayOffset(1);
    assertCheckBeforeOtherFails(one, two);
  }

  @Test
  public void checkBeforeOther_arrivalOneAfterArrivalTwo_givesException() {
    TimetabledPassingTime one = new TimetabledPassingTime().withArrivalTime(T1);
    TimetabledPassingTime two = new TimetabledPassingTime().withArrivalTime(T0);
    assertCheckBeforeOtherFails(one, two);
  }

  @Test
  public void checkBeforeOther_departureOneAfterArrivalTwo_givesException() {
    TimetabledPassingTime one = new TimetabledPassingTime().withDepartureTime(T1);
    TimetabledPassingTime two = new TimetabledPassingTime().withArrivalTime(T0);
    assertCheckBeforeOtherFails(one, two);
  }

  @Test
  public void checkBeforeOther_arrivalOneAfterDepartureTwo_givesException() {
    TimetabledPassingTime one = new TimetabledPassingTime().withArrivalTime(T1);
    TimetabledPassingTime two = new TimetabledPassingTime().withDepartureTime(T0);
    assertCheckBeforeOtherFails(one, two);
  }

  @Test
  public void checkBeforeOther_latestArrivalOneAfterLatestArrivalTwo_givesException() {
    TimetabledPassingTime one = new TimetabledPassingTime().withLatestArrivalTime(T1);
    TimetabledPassingTime two = new TimetabledPassingTime().withLatestArrivalTime(T0);
    assertCheckBeforeOtherFails(one, two);
  }

  @Test
  public void checkBeforeOther_earliestDepartureOneAfterEarliestDepartureTwo_givesException() {
    TimetabledPassingTime one = new TimetabledPassingTime().withEarliestDepartureTime(T1);
    TimetabledPassingTime two = new TimetabledPassingTime().withEarliestDepartureTime(T0);
    assertCheckBeforeOtherFails(one, two);
  }

  @Test
  public void checkBeforeOther_minimumFieldsSet_success() {
    TimetabledPassingTime one = new TimetabledPassingTime().withDepartureTime(T0);
    TimetabledPassingTime two = new TimetabledPassingTime().withArrivalTime(T0);
    one.checkBeforeOther(two);
  }

  @Test
  public void checkBeforeOther_maxFieldsSetAllEquals_success() {
    TimetabledPassingTime one = new TimetabledPassingTime()
      .withArrivalTime(T0)
      .withLatestArrivalTime(T0)
      .withEarliestDepartureTime(T0)
      .withDepartureTime(T0);
    TimetabledPassingTime two = new TimetabledPassingTime()
      .withArrivalTime(T0)
      .withLatestArrivalTime(T0)
      .withEarliestDepartureTime(T0)
      .withDepartureTime(T0);
    one.checkBeforeOther(two);
  }

  @Test
  public void checkBeforeOther_maxFieldsSetWithDifferentDayOffset_success() {
    TimetabledPassingTime one = new TimetabledPassingTime()
      .withArrivalTime(T3)
      .withArrivalDayOffset(0)
      .withLatestArrivalTime(T2)
      .withLatestArrivalDayOffset(1)
      .withEarliestDepartureTime(T1)
      .withEarliestDepartureDayOffset(2)
      .withDepartureTime(T0)
      .withDepartureDayOffset(3);
    TimetabledPassingTime two = new TimetabledPassingTime()
      .withArrivalTime(T0)
      .withArrivalDayOffset(3)
      .withLatestArrivalTime(T0)
      .withLatestArrivalDayOffset(4)
      .withEarliestDepartureTime(T0)
      .withEarliestDepartureDayOffset(5)
      .withDepartureTime(T0)
      .withDepartureDayOffset(6);
    one.checkBeforeOther(two);
  }

  private void assertCheckBeforeOtherFails(
    TimetabledPassingTime one,
    TimetabledPassingTime two
  ) {
    try {
      one.checkBeforeOther(two);
      Assert.fail("Expected when non chronological passing time pair");
    } catch (IllegalArgumentException iae) {
      //  OK
    }
  }
}
