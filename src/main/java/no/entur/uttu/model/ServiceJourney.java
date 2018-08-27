package no.entur.uttu.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        uniqueConstraints = {
                                    @UniqueConstraint(name = "service_journey_unique_name_constraint", columnNames = {"provider_pk", "name"})}
)
public class ServiceJourney extends GroupOfEntities_VersionStructure {

    private String publicCode;

    private String operatorRef;

    @OneToOne(cascade = CascadeType.ALL)
    private BookingArrangement bookingArrangement;

    @NotNull
    @ManyToOne
    private JourneyPattern journeyPattern;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DayType> dayTypes;


    @OneToMany(mappedBy = "serviceJourney", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<TimetabledPassingTime> pointsInSequence = new ArrayList<>();


    public JourneyPattern getJourneyPattern() {
        return journeyPattern;
    }

    public void setJourneyPattern(JourneyPattern journeyPattern) {
        this.journeyPattern = journeyPattern;
    }

    public List<TimetabledPassingTime> getPointsInSequence() {
        return pointsInSequence;
    }

    public void setPointsInSequence(List<TimetabledPassingTime> pointsInSequence) {
        this.pointsInSequence.clear();
        if (pointsInSequence != null) {
            pointsInSequence.stream().forEach(ttpt -> ttpt.setServiceJourney(this));
            this.pointsInSequence.addAll(pointsInSequence);
        }
    }

    public String getPublicCode() {
        return publicCode;
    }

    public void setPublicCode(String publicCode) {
        this.publicCode = publicCode;
    }

    public String getOperatorRef() {
        return operatorRef;
    }

    public void setOperatorRef(String operatorRef) {
        this.operatorRef = operatorRef;
    }

    public BookingArrangement getBookingArrangement() {
        return bookingArrangement;
    }

    public void setBookingArrangement(BookingArrangement bookingArrangement) {
        this.bookingArrangement = bookingArrangement;
    }

    public List<DayType> getDayTypes() {
        return dayTypes;
    }

    public void setDayTypes(List<DayType> dayTypes) {
        this.dayTypes = dayTypes;
    }
}
