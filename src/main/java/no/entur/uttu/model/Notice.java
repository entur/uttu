package no.entur.uttu.model;

import javax.persistence.Entity;

@Entity
public class Notice extends ProviderEntity {

    private String text;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
