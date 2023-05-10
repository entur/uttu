package no.entur.uttu.model;

import static no.entur.uttu.model.ModelTestUtil.assertCheckPersistableFails;

import java.time.Duration;
import java.time.LocalTime;
import org.junit.Test;

public class BookingArrangementTest {

  @Test
  public void checkPersistable_LatestBookingTimeWithoutBookWhen_givesException() {
    BookingArrangement bookingArrangement = new BookingArrangement();
    bookingArrangement.setLatestBookingTime(LocalTime.NOON);
    assertCheckPersistableFails(bookingArrangement);
  }

  @Test
  public void checkPersistable_LatestBookingTimeWithBookWhen_succeeds() {
    BookingArrangement bookingArrangement = new BookingArrangement();
    bookingArrangement.setLatestBookingTime(LocalTime.NOON);
    bookingArrangement.setBookWhen(PurchaseWhenEnumeration.DAY_OF_TRAVEL_ONLY);
    bookingArrangement.checkPersistable();
  }

  @Test
  public void checkPersistable_MinimumBookingPeriodWithBookWhen_givesException() {
    BookingArrangement bookingArrangement = new BookingArrangement();
    bookingArrangement.setMinimumBookingPeriod(Duration.ofSeconds(3600));
    bookingArrangement.setBookWhen(PurchaseWhenEnumeration.DAY_OF_TRAVEL_ONLY);
    assertCheckPersistableFails(bookingArrangement);
  }

  @Test
  public void checkPersistable_MinimumBookingPeriodWithoutBookWhen_succeeds() {
    BookingArrangement bookingArrangement = new BookingArrangement();
    bookingArrangement.setMinimumBookingPeriod(Duration.ofSeconds(3600));
    bookingArrangement.checkPersistable();
  }

  @Test
  public void checkPersistable_WithoutBookWhenOrMinimumBookingPeriod_givesException() {
    BookingArrangement bookingArrangement = new BookingArrangement();
    assertCheckPersistableFails(bookingArrangement);
  }

  @Test
  public void checkPersistable_WithBookWhenOnly_givesException() {
    BookingArrangement bookingArrangement = new BookingArrangement();
    bookingArrangement.setBookWhen(PurchaseWhenEnumeration.DAY_OF_TRAVEL_ONLY);
    assertCheckPersistableFails(bookingArrangement);
  }
}
