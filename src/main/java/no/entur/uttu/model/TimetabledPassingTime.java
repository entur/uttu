package no.entur.uttu.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "timetabled_passing_time_unique_order_constraint", columnNames = {"service_journey_pk", "order_val"})})
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

    public List<Notice> getNotices() {
        return notices;
    }

    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }
}
