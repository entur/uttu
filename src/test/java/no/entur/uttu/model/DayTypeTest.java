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

import org.junit.Assert;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DayTypeTest {


    @Test
    public void checkPersistable_emptyDayTypesAndDayTypeAssignmentWithOperatingPeriod_givesException() {
        DayType dayType = new DayType();
        DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
        OperatingPeriod period = new OperatingPeriod();
        period.setFromDate(LocalDate.MIN);
        period.setToDate(LocalDate.MAX);
        dayTypeAssignment.setOperatingPeriod(period);

        dayType.getDayTypeAssignments().add(dayTypeAssignment);
        assertCheckPersistableFails(dayType);
    }

    @Test
    public void checkPersistable_nonEmptyDayTypesAndDayTypeAssignmentWithOperatingPeriod_success() {
        DayType dayType = new DayType();
        dayType.getDaysOfWeek().add(DayOfWeek.TUESDAY);
        DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
        OperatingPeriod period = new OperatingPeriod();
        period.setFromDate(LocalDate.MIN);
        period.setToDate(LocalDate.MAX);
        dayTypeAssignment.setOperatingPeriod(period);

        dayType.getDayTypeAssignments().add(dayTypeAssignment);
        dayType.checkPersistable();
    }


    private void assertCheckPersistableFails(DayType entity) {
        try {
            entity.checkPersistable();
            Assert.fail("Expected exception for non-persistable entity");
        } catch (IllegalArgumentException iae) {
            //  OK
        }
    }
}
