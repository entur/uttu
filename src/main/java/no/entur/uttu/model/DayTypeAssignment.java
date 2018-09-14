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

        Preconditions.checkArgument(date != null || operatingPeriod != null, "One of date or operationPeriod must be set for DayTypeAssignment");

        if (operatingPeriod != null) {
            operatingPeriod.checkPersistable();
        }

    }


    public boolean isValid(LocalDate from, LocalDate to) {
        boolean valid = false;
        if (date != null) {
            valid &= !(from.isAfter(date) || to.isBefore(date));
        }

        if (operatingPeriod != null) {
            valid = operatingPeriod.isValid(from, to);
        }

        return valid && super.isValid(from, to);
    }
}
