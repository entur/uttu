package no.entur.uttu.model;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(
        indexes = {
                          @Index(name = "network_name_index", columnList = "name")
        }
)
public class Network extends GroupOfEntities_VersionStructure {

    /**
     * Reference to Authority in organisation registry.
     */
    @NotNull
    private String authorityRef;

    public String getAuthorityRef() {
        return authorityRef;
    }

    public void setAuthorityRef(String authorityRef) {
        this.authorityRef = authorityRef;
    }
}
