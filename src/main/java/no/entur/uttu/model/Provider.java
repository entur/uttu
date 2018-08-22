package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Provider extends IdentifiedEntity {

    private String name;

    @ManyToOne
    private CodeSpace codeSpace;

    // TODO settings


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CodeSpace getCodeSpace() {
        return codeSpace;
    }

    public void setCodeSpace(CodeSpace codeSpace) {
        this.codeSpace = codeSpace;
    }
}
