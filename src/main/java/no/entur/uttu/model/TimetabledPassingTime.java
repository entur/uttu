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

import com.google.common.base.Preconditions;
import no.entur.uttu.util.ValidationHelper;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

@Entity
public class TimetabledPassingTime extends ProviderEntity {

    @NotNull
    @ManyToOne
    private ServiceJourney serviceJourney;

    // Order is reserved word in db
    @Column(name = "order_val")
    @Min(value = 1L, message = "The value must be positive")
    private int order;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Notice> notices;

    private LocalTime departureTime;

    private LocalTime arrivalTime;

    private LocalTime earliestDepartureTime;

    private LocalTime latestArrivalTime;

    private int arrivalDayOffset;

    private int departureDayOffset;

    private int earliestDepartureDayOffset;

    private int latestArrivalDayOffset;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public LocalTime getEarliestDepartureTime() {
        return earliestDepartureTime;
    }

    public void setEarliestDepartureTime(LocalTime earliestDepartureTime) {
        this.earliestDepartureTime = earliestDepartureTime;
    }

    public LocalTime getLatestArrivalTime() {
        return latestArrivalTime;
    }

    public void setLatestArrivalTime(LocalTime latestArrivalTime) {
        this.latestArrivalTime = latestArrivalTime;
    }

    public ServiceJourney getServiceJourney() {
        return serviceJourney;
    }

    public void setServiceJourney(ServiceJourney serviceJourney) {
        this.serviceJourney = serviceJourney;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getArrivalDayOffset() {
        return arrivalDayOffset;
    }

    public void setArrivalDayOffset(int arrivalDayOffset) {
        this.arrivalDayOffset = arrivalDayOffset;
    }

    public int getDepartureDayOffset() {
        return departureDayOffset;
    }

    public void setDepartureDayOffset(int departureDayOffset) {
        this.departureDayOffset = departureDayOffset;
    }

    public int getEarliestDepartureDayOffset() {
        return earliestDepartureDayOffset;
    }

    public void setEarliestDepartureDayOffset(int earliestDepartureDayOffset) {
        this.earliestDepartureDayOffset = earliestDepartureDayOffset;
    }

    public int getLatestArrivalDayOffset() {
        return latestArrivalDayOffset;
    }

    public void setLatestArrivalDayOffset(int latestArrivalDayOffset) {
        this.latestArrivalDayOffset = latestArrivalDayOffset;
    }


    public TimetabledPassingTime withEarliestDepartureTime(LocalTime earliestDepartureTime) {
        this.earliestDepartureTime = earliestDepartureTime;
        return this;
    }

    public TimetabledPassingTime withLatestArrivalTime(LocalTime latestArrivalTime) {
        this.latestArrivalTime = latestArrivalTime;
        return this;
    }


    public TimetabledPassingTime withDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
        return this;
    }

    public TimetabledPassingTime withArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
        return this;
    }

    public TimetabledPassingTime withArrivalDayOffset(int arrivalDayOffset) {
        this.arrivalDayOffset = arrivalDayOffset;
        return this;
    }


    public TimetabledPassingTime withDepartureDayOffset(int departureDayOffset) {
        this.departureDayOffset = departureDayOffset;
        return this;
    }

    public TimetabledPassingTime withEarliestDepartureDayOffset(int earliestDepartureDayOffset) {
        this.earliestDepartureDayOffset = earliestDepartureDayOffset;
        return this;
    }

    public TimetabledPassingTime withLatestArrivalDayOffset(int latestArrivalDayOffset) {
        this.latestArrivalDayOffset = latestArrivalDayOffset;
        return this;
    }


    public List<Notice> getNotices() {
        return notices;
    }

    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }


    @Override
    public void checkPersistable() {
        super.checkPersistable();
        Preconditions.checkArgument(departureTime != null || arrivalTime != null || earliestDepartureTime != null || latestArrivalTime != null,
                "%s must have at least one of the following fields set: arrivalTime, latestArrivalTime, departureTime or earliestDepartureTime", identity());

        Preconditions.checkArgument(ValidationHelper.isNotAfter(arrivalTime, arrivalDayOffset, departureTime, departureDayOffset), "%s arrivalTime cannot be later than departureTime", identity());
        Preconditions.checkArgument(ValidationHelper.isNotAfter(earliestDepartureTime, earliestDepartureDayOffset, departureTime, departureDayOffset), "%s earliestDepartureTime cannot be later than departureTime", identity());
        Preconditions.checkArgument(ValidationHelper.isNotAfter(arrivalTime, arrivalDayOffset, latestArrivalTime, latestArrivalDayOffset), "%s arrivalTime cannot be later than latestArrivalTime", identity());
    }


    /**
     * Check that this TimetabledPassingTime fits chronologically before another TimetablePassingTime in a ServiceJourney.
     *
     * @param other the other TimetabledPassingTime
     */
    public void checkBeforeOther(TimetabledPassingTime other) {
        Preconditions.checkArgument(ValidationHelper.isNotAfter(departureTime, departureDayOffset, other.departureTime, other.departureDayOffset), "%s departureTime cannot be after next elements (%s) departureTime", identity(), other.identity());
        Preconditions.checkArgument(ValidationHelper.isNotAfter(departureTime, departureDayOffset, other.arrivalTime, other.arrivalDayOffset), "%s departureTime cannot be after next elements (%s) arrivalTime", identity(), other.identity());
        Preconditions.checkArgument(ValidationHelper.isNotAfter(arrivalTime, arrivalDayOffset, other.arrivalTime, other.arrivalDayOffset), "%s arrivalTime cannot be after next elements (%s) arrivalTime", identity(), other.identity());
        Preconditions.checkArgument(ValidationHelper.isNotAfter(arrivalTime, arrivalDayOffset, other.departureTime, other.departureDayOffset), "%s arrivalTime cannot be after next elements (%s) departureTime", identity(), other.identity());
        Preconditions.checkArgument(ValidationHelper.isNotAfter(latestArrivalTime, latestArrivalDayOffset, other.latestArrivalTime, other.latestArrivalDayOffset), "%s latestArrivalTime cannot be after later next elements (%s) latestArrivalTime", identity(), other.identity());
        Preconditions.checkArgument(ValidationHelper.isNotAfter(earliestDepartureTime, earliestDepartureDayOffset, other.earliestDepartureTime, other.earliestDepartureDayOffset), "%s earliestDepartureTime cannot be after later next elements (%s) earliestDepartureTime", identity(), other.identity());
    }

}
