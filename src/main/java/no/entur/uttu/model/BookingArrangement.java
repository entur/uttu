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

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import no.entur.uttu.util.Preconditions;

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

  @Override
  public void checkPersistable() {
    super.checkPersistable();

    // notExist(BookWhen xor LatestBookingTime)
    Preconditions.checkArgument(
      getLatestBookingTime() == null || getBookWhen() != null,
      "%s booking information must have BookWhen when LatestBookingTime is defined",
      this
    );
    Preconditions.checkArgument(
      getBookWhen() == null || getLatestBookingTime() != null,
      "%s booking information must have LatestBookingTime when BookWhen is defined",
      this
    );

    Preconditions.checkArgument(
      // notExist(BookWhen and MinimumBookingPeriod)
      getMinimumBookingPeriod() == null || getBookWhen() == null,
      "%s booking information can't have BookWhen when MinimumBookingPeriod is defined",
      this
    );

    Preconditions.checkArgument(
      // notExist( not(BookWhen) and not(MinimumBookingPeriod))
      getBookWhen() != null || getMinimumBookingPeriod() != null,
      "%s booking information must have BookWhen or MinimumBookingPeriod",
      this
    );
  }
}
