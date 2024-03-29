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
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.util.Preconditions;

@Entity
public class FlexibleLine extends Line {

  @Enumerated(EnumType.STRING)
  @NotNull
  private FlexibleLineTypeEnumeration flexibleLineType;

  @OneToOne(cascade = CascadeType.ALL)
  private BookingArrangement bookingArrangement;

  public FlexibleLineTypeEnumeration getFlexibleLineType() {
    return flexibleLineType;
  }

  public void setFlexibleLineType(FlexibleLineTypeEnumeration flexibleLineType) {
    this.flexibleLineType = flexibleLineType;
  }

  public BookingArrangement getBookingArrangement() {
    return bookingArrangement;
  }

  public void setBookingArrangement(BookingArrangement bookingArrangement) {
    this.bookingArrangement = bookingArrangement;
  }

  @Override
  public void accept(LineVisitor lineVisitor) {
    lineVisitor.visitFlexibleLine(this);
  }

  @Override
  public void checkPersistable() {
    super.checkPersistable();

    Preconditions.checkArgument(
      bookingInformationPresentInHierarchy(),
      CodedError.fromErrorCode(ErrorCodeEnumeration.FLEXIBLE_LINE_REQUIRES_BOOKING),
      "%s requires booking information on line, journey pattern or service journey",
      identity()
    );

    validateBookingInformations();
  }

  private boolean bookingInformationPresentInHierarchy() {
    return (
      this.bookingArrangement != null ||
      this.getJourneyPatterns()
        .stream()
        .anyMatch(jp ->
          jp
            .getPointsInSequence()
            .stream()
            .anyMatch(point -> point.getBookingArrangement() != null) ||
          jp
            .getServiceJourneys()
            .stream()
            .anyMatch(sj -> sj.getBookingArrangement() != null)
        )
    );
  }

  private void validateBookingInformations() {
    validateBookingInformation(this.bookingArrangement);
    this.getJourneyPatterns()
      .stream()
      .forEach(jp -> {
        jp
          .getPointsInSequence()
          .stream()
          .forEach(stopPoint ->
            validateBookingInformation(stopPoint.getBookingArrangement())
          );
        jp
          .getServiceJourneys()
          .stream()
          .forEach(sj -> validateBookingInformation(sj.getBookingArrangement()));
      });
  }

  private void validateBookingInformation(BookingArrangement bookingArrangement) {
    if (bookingArrangement == null) return;
    bookingArrangement.checkPersistable();
  }
}
