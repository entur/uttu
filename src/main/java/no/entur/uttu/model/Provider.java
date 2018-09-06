package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "provider_code_index", columnNames = "code")})
public class Provider extends IdentifiedEntity {
    @NotNull
    private String code;

    @NotNull
    private String name;

    @NotNull
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


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
