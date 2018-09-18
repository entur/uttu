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

import java.time.LocalDate;

import static no.entur.uttu.model.ModelTestUtil.assertCheckPersistableFails;

public class OperatingPeriodTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static LocalDate YESTERDAY = TODAY.minusDays(1);


    @Test
    public void checkPersistable_success() {
        period(TODAY, TODAY).checkPersistable();
    }

    @Test
    public void checkPersistable_missingDate_givesException() {
        assertCheckPersistableFails(period(null, null));
        assertCheckPersistableFails(period(TODAY, null));
        assertCheckPersistableFails(period(null, TODAY));
    }


    @Test
    public void checkPersistable_toDateBeforeFrom_givesException() {
        assertCheckPersistableFails(period(TODAY, YESTERDAY));
    }

    @Test
    public void isValid_whenPeriodEndingBeforeFromDate_thenReturnFalse() {
        Assert.assertFalse(period(YESTERDAY, TODAY).isValid(TODAY.plusDays(1), TODAY.plusDays(2)));
    }

    @Test
    public void isValid_whenPeriodStartAfterToDate_thenReturnFalse() {
        Assert.assertFalse(period(YESTERDAY, TODAY).isValid(YESTERDAY.minusDays(2), YESTERDAY.minusDays(1)));
    }

    @Test
    public void isValid_whenPeriodIncludesFromAndToDate_thenReturnTrue() {
        Assert.assertTrue(period(YESTERDAY, TODAY.plusDays(10)).isValid(TODAY.plusDays(2), TODAY.plusDays(3)));
    }

    @Test
    public void isValid_whenPeriodIncludesFromDate_thenReturnTrue() {
        Assert.assertTrue(period(YESTERDAY, TODAY.plusDays(2)).isValid(TODAY.plusDays(1), TODAY.plusDays(30)));
    }

    @Test
    public void isValid_whenPeriodIncludesToDate_thenReturnTrue() {
        Assert.assertTrue(period(YESTERDAY, TODAY.plusDays(2)).isValid(YESTERDAY.minusDays(10), TODAY));
    }

    @Test
    public void isValid_whenPeriodBetweenFromDateAndToDate_thenReturnTrue() {
        Assert.assertTrue(period(YESTERDAY, TODAY).isValid(YESTERDAY.minusDays(30), TODAY.plusDays(30)));
    }

    private OperatingPeriod period(LocalDate from, LocalDate to) {
        OperatingPeriod period = new OperatingPeriod();
        period.setFromDate(from);
        period.setToDate(to);
        return period;
    }
}
