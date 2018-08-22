package no.entur.uttu.model;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
public class OperatingPeriod extends ProviderEntity {

    private LocalDate fromDate;

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
}
