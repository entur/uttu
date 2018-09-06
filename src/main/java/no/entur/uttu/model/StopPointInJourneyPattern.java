package no.entur.uttu.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "stop_point_in_jp_unique_order_constraint", columnNames = {"journey_pattern_pk", "order_val"})})
public class StopPointInJourneyPattern extends ProviderEntity {

    @ManyToOne
    private FlexibleStopPlace flexibleStopPlace;

    // Reference to quay in external stop place registry (NSR), either this or flexibleStopPlace must be set
    private String quayRef;

    @NotNull
    @ManyToOne
    private JourneyPattern journeyPattern;


    @OneToOne(cascade = CascadeType.ALL)
    private BookingArrangement bookingArrangement;


    // Order is reserved word in db
    @Column(name = "order_val")
    @Min(value = 1L, message = "The value must be positive")
    private int order;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private DestinationDisplay destinationDisplay;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Notice> notices;


    private Boolean forAlighting;
    private Boolean forBoarding;

    public FlexibleStopPlace getFlexibleStopPlace() {
        return flexibleStopPlace;
    }

    public void setFlexibleStopPlace(FlexibleStopPlace flexibleStopPlace) {
        this.flexibleStopPlace = flexibleStopPlace;
    }

    public DestinationDisplay getDestinationDisplay() {
        return destinationDisplay;
    }

    public void setDestinationDisplay(DestinationDisplay destinationDisplay) {
        this.destinationDisplay = destinationDisplay;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public JourneyPattern getJourneyPattern() {
        return journeyPattern;
    }

    public void setJourneyPattern(JourneyPattern journeyPattern) {
        this.journeyPattern = journeyPattern;
    }

    public BookingArrangement getBookingArrangement() {
        return bookingArrangement;
    }

    public void setBookingArrangement(BookingArrangement bookingArrangement) {
        this.bookingArrangement = bookingArrangement;
    }

    public String getQuayRef() {
        return quayRef;
    }

    public void setQuayRef(String quayRef) {
        this.quayRef = quayRef;
    }

    public List<Notice> getNotices() {
        return notices;
    }

    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }

    public Boolean getForAlighting() {
        return forAlighting;
    }

    public void setForAlighting(Boolean forAlighting) {
        this.forAlighting = forAlighting;
    }

    public Boolean getForBoarding() {
        return forBoarding;
    }

    public void setForBoarding(Boolean forBoarding) {
        this.forBoarding = forBoarding;
    }
}
