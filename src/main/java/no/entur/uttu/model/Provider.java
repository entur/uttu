package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
public class Provider extends IdentifiedEntity {

    private String name;

    @ManyToOne
    private Codespace codespace;

    // TODO settings


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Codespace getCodespace() {
        return codespace;
    }

    public void setCodespace(Codespace codespace) {
        this.codespace = codespace;
    }
}
