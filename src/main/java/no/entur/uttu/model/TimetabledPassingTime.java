package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalTime;

@Entity
public class TimetabledPassingTime extends ProviderEntity {

    @ManyToOne
    private ServiceJourney serviceJourney;

    private int order;

    private LocalTime earliestDepartureTime;

    private LocalTime latestArrivalTime;

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
}
