package no.entur.uttu.model;

import no.entur.uttu.model.Contact;
import no.entur.uttu.model.IdentifiedEntity;
import org.joda.time.Duration;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.time.LocalDate;

@Entity
public class BookingArrangement extends IdentifiedEntity {

    private LocalDate latestBookingTime;

    private Duration minimumBookingPeriod;

    private String bookingNote;

    @OneToOne
    private Contact bookingContact;

    public LocalDate getLatestBookingTime() {
        return latestBookingTime;
    }

    public void setLatestBookingTime(LocalDate latestBookingTime) {
        this.latestBookingTime = latestBookingTime;
    }

    public String getBookingNote() {
        return bookingNote;
    }

    public void setBookingNote(String bookingNote) {
        this.bookingNote = bookingNote;
    }

    public Contact getBookingContact() {
        return bookingContact;
    }

    public void setBookingContact(Contact bookingContact) {
        this.bookingContact = bookingContact;
    }

    public Duration getMinimumBookingPeriod() {
        return minimumBookingPeriod;
    }

    public void setMinimumBookingPeriod(Duration minimumBookingPeriod) {
        this.minimumBookingPeriod = minimumBookingPeriod;
    }
}
