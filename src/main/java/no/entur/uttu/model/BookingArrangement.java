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


import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Entity
public class BookingArrangement extends IdentifiedEntity {

    private LocalTime latestBookingTime;

    private Duration minimumBookingPeriod;

    private String bookingNote;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<BookingMethodEnumeration> bookingMethods;
    @Enumerated(EnumType.STRING)
    private BookingAccessEnumeration bookingAccess;
    @Enumerated(EnumType.STRING)
    private PurchaseWhenEnumeration bookWhen;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<PurchaseMomentEnumeration> buyWhen;

    @OneToOne(cascade = CascadeType.ALL)
    private Contact bookingContact;

    public LocalTime getLatestBookingTime() {
        return latestBookingTime;
    }

    public void setLatestBookingTime(LocalTime latestBookingTime) {
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

    public List<BookingMethodEnumeration> getBookingMethods() {
        return bookingMethods;
    }

    public void setBookingMethods(List<BookingMethodEnumeration> bookingMethods) {
        this.bookingMethods = bookingMethods;
    }

    public BookingAccessEnumeration getBookingAccess() {
        return bookingAccess;
    }

    public void setBookingAccess(BookingAccessEnumeration bookingAccess) {
        this.bookingAccess = bookingAccess;
    }

    public PurchaseWhenEnumeration getBookWhen() {
        return bookWhen;
    }

    public void setBookWhen(PurchaseWhenEnumeration bookWhen) {
        this.bookWhen = bookWhen;
    }

    public List<PurchaseMomentEnumeration> getBuyWhen() {
        return buyWhen;
    }

    public void setBuyWhen(List<PurchaseMomentEnumeration> buyWhen) {
        this.buyWhen = buyWhen;
    }
}
