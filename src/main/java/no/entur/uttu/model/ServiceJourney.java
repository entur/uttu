package no.entur.uttu.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

@Entity
@Table(
        uniqueConstraints = {
                                    @UniqueConstraint(name = "service_journey_unique_name_constraint", columnNames = {"provider_pk", "name"})}
)
public class ServiceJourney extends GroupOfEntities_VersionStructure {

    private String publicCode;

    private String operatorRef;

    @ManyToOne
    private JourneyPattern journeyPattern;

    @OneToMany(mappedBy = "serviceJourney", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimetabledPassingTime> timetabledPassingTimes;


    public JourneyPattern getJourneyPattern() {
        return journeyPattern;
    }

    public void setJourneyPattern(JourneyPattern journeyPattern) {
        this.journeyPattern = journeyPattern;
    }

    public List<TimetabledPassingTime> getTimetabledPassingTimes() {
        return timetabledPassingTimes;
    }

    public void setTimetabledPassingTimes(List<TimetabledPassingTime> timetabledPassingTimes) {
        this.timetabledPassingTimes = timetabledPassingTimes;
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
}
