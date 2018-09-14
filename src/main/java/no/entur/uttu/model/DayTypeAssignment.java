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

import com.google.common.base.Preconditions;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.time.LocalDate;


@Entity
public class DayTypeAssignment extends IdentifiedEntity {


    // Whether this period is to be included or excluded. Belongs to DayTypeAssignment in Transmodel. Added here as a simplification.
    private Boolean available;

    private LocalDate date;

    @OneToOne(cascade = CascadeType.ALL)
    private OperatingPeriod operatingPeriod;

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public OperatingPeriod getOperatingPeriod() {
        return operatingPeriod;
    }

    public void setOperatingPeriod(OperatingPeriod operatingPeriod) {
        this.operatingPeriod = operatingPeriod;
    }


    @Override
    public void checkPersistable() {
        super.checkPersistable();

        Preconditions.checkArgument(date != null ^ operatingPeriod != null, "Exactly one of date or operationPeriod must be set for DayTypeAssignment");

        if (operatingPeriod != null) {
            operatingPeriod.checkPersistable();
        }

    }


    public boolean isValid(LocalDate from, LocalDate to) {
        boolean dateValid = false;
        if (date != null) {
            dateValid = !(from.isAfter(date) || to.isBefore(date));
        }
        boolean operatingPeriodValid = false;
        if (operatingPeriod != null) {
            operatingPeriodValid = operatingPeriod.isValid(from, to);
        }

        return (dateValid || operatingPeriodValid) && super.isValid(from, to);
    }
}
