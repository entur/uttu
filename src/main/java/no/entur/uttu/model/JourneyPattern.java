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
                                    @UniqueConstraint(name = "journey_pattern_unique_name_constraint", columnNames = {"provider_pk", "name"})}
)
public class JourneyPattern extends GroupOfEntities_VersionStructure {

    @ManyToOne
    private FlexibleLine flexibleLine;

    @OneToMany(mappedBy = "journeyPattern", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceJourney> serviceJourneys;

    @OneToMany(mappedBy = "journeyPattern", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StopPointInJourneyPattern> stopPointInJourneyPatterns;

    public FlexibleLine getFlexibleLine() {
        return flexibleLine;
    }

    public void setFlexibleLine(FlexibleLine flexibleLine) {
        this.flexibleLine = flexibleLine;
    }

    public List<ServiceJourney> getServiceJourneys() {
        return serviceJourneys;
    }

    public void setServiceJourneys(List<ServiceJourney> serviceJourneys) {
        this.serviceJourneys = serviceJourneys;
    }

    public List<StopPointInJourneyPattern> getStopPointInJourneyPatterns() {
        return stopPointInJourneyPatterns;
    }

    public void setStopPointInJourneyPatterns(List<StopPointInJourneyPattern> stopPointInJourneyPatterns) {
        this.stopPointInJourneyPatterns = stopPointInJourneyPatterns;
    }
}
