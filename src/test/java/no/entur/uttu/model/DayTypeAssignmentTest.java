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

import org.junit.Test;

import java.time.LocalDate;

import static no.entur.uttu.model.ModelTestUtil.assertCheckPersistableFails;

public class DayTypeAssignmentTest {

    @Test
    public void checkPersistable_onlyDateSet_success() {
        DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
        dayTypeAssignment.setDate(LocalDate.now());
        dayTypeAssignment.checkPersistable();
    }

    @Test
    public void checkPersistable_onlyPeriodSet_success() {
        DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
        dayTypeAssignment.setOperatingPeriod(period());
        dayTypeAssignment.checkPersistable();
    }

    @Test
    public void checkPersistable_neitherDateNorPeriodSet_givesException() {
        DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
        assertCheckPersistableFails(dayTypeAssignment);
    }

    @Test
    public void checkPersistable_bothDateAndPeriodSet_givesException() {
        DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
        dayTypeAssignment.setDate(LocalDate.now());
        OperatingPeriod operatingPeriod = period();
        dayTypeAssignment.setOperatingPeriod(operatingPeriod);
        assertCheckPersistableFails(dayTypeAssignment);
    }

    private OperatingPeriod period() {
        OperatingPeriod operatingPeriod = new OperatingPeriod();
        operatingPeriod.setFromDate(LocalDate.MIN);
        operatingPeriod.setToDate(LocalDate.MAX);
        return operatingPeriod;
    }


}
