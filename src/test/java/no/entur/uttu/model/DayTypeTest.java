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

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

public class DayTypeTest {

  private static final LocalDate TODAY = LocalDate.now();
  private static LocalDate YESTERDAY = TODAY.minusDays(1);

  @Test
  public void checkPersistable_emptyDayTypesAndDayTypeAssignmentWithOperatingPeriod_givesException() {
    DayType dayType = new DayType();
    dayType.getDayTypeAssignments().add(period(LocalDate.MIN, LocalDate.MAX));
    assertCheckPersistableFails(dayType);
  }

  @Test
  public void checkPersistable_nonEmptyDayTypesAndDayTypeAssignmentWithOperatingPeriod_success() {
    DayType dayType = new DayType();
    dayType.getDaysOfWeek().add(DayOfWeek.TUESDAY);

    dayType.getDayTypeAssignments().add(period(LocalDate.MIN, LocalDate.MAX));
    dayType.checkPersistable();
  }

  @Test
  public void isValid_whenOneDateIsWithinFromDateAndToDate_theReturnTrue() {
    DayType dayType = new DayType();
    dayType.getDayTypeAssignments().add(date(YESTERDAY));
    dayType.getDayTypeAssignments().add(date(TODAY));
    dayType.getDayTypeAssignments().add(date(YESTERDAY.minusDays(10)));
    Assert.assertTrue(dayType.isValid(YESTERDAY, YESTERDAY));
  }

  @Test
  public void isValid_whenOnePeriodOverlapsWithFromDateAndToDate_theReturnTrue() {
    DayType dayType = new DayType();
    dayType.getDayTypeAssignments().add(period(YESTERDAY.minusDays(1), YESTERDAY));
    dayType.getDayTypeAssignments().add(date(YESTERDAY.minusDays(10)));
    Assert.assertTrue(dayType.isValid(YESTERDAY, YESTERDAY));
  }

  @Test
  public void isValid_whenOnlyNoDatesOrPeriodsWithinFromDateAndToDate_theReturnFalse() {
    DayType dayType = new DayType();
    dayType.getDayTypeAssignments().add(period(TODAY, TODAY.plusDays(1)));
    dayType
      .getDayTypeAssignments()
      .add(period(YESTERDAY.minusDays(2), YESTERDAY.minusDays(1)));
    dayType.getDayTypeAssignments().add(date(TODAY));
    dayType.getDayTypeAssignments().add(date(YESTERDAY.minusDays(10)));
    Assert.assertFalse(dayType.isValid(YESTERDAY, YESTERDAY));
  }

  private DayTypeAssignment date(LocalDate date) {
    DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
    dayTypeAssignment.setDate(date);
    return dayTypeAssignment;
  }

  public static DayTypeAssignment period(LocalDate from, LocalDate to) {
    DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
    OperatingPeriod period = new OperatingPeriod();
    period.setFromDate(from);
    period.setToDate(to);
    dayTypeAssignment.setOperatingPeriod(period);
    return dayTypeAssignment;
  }
}
