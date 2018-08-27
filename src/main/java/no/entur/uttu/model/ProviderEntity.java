package no.entur.uttu.model;

import com.google.common.base.Preconditions;
import no.entur.uttu.config.Context;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

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


    @PrePersist
    public void setNetexIdIfMissing() {
        this.setNetexId(this.getProvider().getCodeSpace().getXmlns() + ":" + this.getClass().getSimpleName() + ":" + UUID.randomUUID());
    }


    @PreUpdate
    protected void verifyProvider() {
        Long providerId = Context.getVerifiedProviderId();
        Preconditions.checkArgument(Objects.equals(this.getProvider().getPk(), providerId),
                "Provider mismatch, attempting to store entity[½s] in context of provider[%s] .", this, providerId);
    }


}
