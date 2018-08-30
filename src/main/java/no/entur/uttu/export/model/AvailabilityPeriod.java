package no.entur.uttu.export.model;

import java.time.LocalDate;

public class AvailabilityPeriod {
    private LocalDate from;

    private LocalDate to;

    public AvailabilityPeriod(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    public AvailabilityPeriod union(AvailabilityPeriod other) {
        if (other == null) {
            return this;
        }
        LocalDate first = from.isBefore(other.from) ? from : other.from;
        LocalDate last = to.isAfter(other.to) ? to : other.to;
        return new AvailabilityPeriod(first, last);
    }
}
