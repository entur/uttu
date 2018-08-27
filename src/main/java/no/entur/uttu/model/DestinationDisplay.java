package no.entur.uttu.model;

import javax.persistence.Entity;

@Entity
public class DestinationDisplay extends ProviderEntity {

    private String frontText;

    public String getFrontText() {
        return frontText;
    }

    public void setFrontText(String frontText) {
        this.frontText = frontText;
    }
}
