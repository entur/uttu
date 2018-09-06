package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "network_name_index", columnNames = {"provider_pk", "name"})})
public class Network extends GroupOfEntities_VersionStructure {

    /**
     * Reference to Authority in organisation registry.
     */
    @NotNull
    // TODO this should probably be change to Long?
    private String authorityRef;

    public String getAuthorityRef() {
        return authorityRef;
    }

    public void setAuthorityRef(String authorityRef) {
        this.authorityRef = authorityRef;
    }
}
