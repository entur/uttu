package no.entur.uttu.model;

import com.google.common.base.Preconditions;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class OperatingPeriod extends IdentifiedEntity {
    @NotNull
    private LocalDate fromDate;
    @NotNull
    private LocalDate toDate;

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    @Override
    public void checkPersistable() {
        super.checkPersistable();

        Preconditions.checkArgument(fromDate != null && toDate != null && !toDate.isBefore(fromDate), "fromDate (%s) cannot be later than toDate(%s)", fromDate, toDate);
    }


    public boolean isValid(LocalDate from, LocalDate to) {
        return !(fromDate.isAfter(to) || toDate.isBefore(from));
    }

}
