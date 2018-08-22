package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class StopPointInJourneyPattern extends ProviderEntity {

    @ManyToOne
    private FlexibleStopPlace flexibleStopPlace;

    @ManyToOne
    private JourneyPattern journeyPattern;

    private int order;

    private String destinationDisplayFrontText;

    public FlexibleStopPlace getFlexibleStopPlace() {
        return flexibleStopPlace;
    }

    public void setFlexibleStopPlace(FlexibleStopPlace flexibleStopPlace) {
        this.flexibleStopPlace = flexibleStopPlace;
    }

    public String getDestinationDisplayFrontText() {
        return destinationDisplayFrontText;
    }

    public void setDestinationDisplayFrontText(String destinationDisplayFrontText) {
        this.destinationDisplayFrontText = destinationDisplayFrontText;
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
}
