package no.entur.uttu.model;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

/**
 * Abstract superclass for all entities belong to a provider.
 */
@MappedSuperclass
public abstract class ProviderEntity extends IdentifiedEntity {

    @ManyToOne
    @NotNull
    protected Provider provider;

    @NotNull
    @Column(unique = true)
    protected String netexId;

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getNetexId() {
        return netexId;
    }

    public void setNetexId(String netexId) {
        this.netexId = netexId;
    }


}
