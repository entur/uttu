package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class DestinationDisplay extends ProviderEntity {

    @NotNull
    private String frontText;

    public String getFrontText() {
        return frontText;
    }

    public void setFrontText(String frontText) {
        this.frontText = frontText;
    }
}
