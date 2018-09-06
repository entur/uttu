package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class Notice extends ProviderEntity {

    @NotNull
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
