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

import no.entur.uttu.util.Preconditions;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

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

        Preconditions.checkArgument(bookingInformationPresentInHierarchy(),
                "%s requires booking information on line, journey pattern or service journey", identity());
    }

    private boolean bookingInformationPresentInHierarchy() {
        return this.bookingArrangement != null ||
                this.getJourneyPatterns().stream().anyMatch(jp ->
                        jp.getPointsInSequence().stream().anyMatch(point -> point.getBookingArrangement() != null) ||
                                jp.getServiceJourneys().stream().anyMatch(sj -> sj.getBookingArrangement() != null)
                );

    }
}
