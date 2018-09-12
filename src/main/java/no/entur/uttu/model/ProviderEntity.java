package no.entur.uttu.model;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import no.entur.uttu.config.Context;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
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

    public String getNetexVersion() {
        return Objects.toString(version);
    }

    @PrePersist
    public void setNetexIdIfMissing() {
        this.setNetexId(Joiner.on(":").join(getProvider().getCodespace().getXmlns(), this.getClass().getSimpleName(), UUID.randomUUID()));
    }


    @PreUpdate
    protected void verifyProvider() {
        String providerCode = Context.getVerifiedProviderCode();
        Preconditions.checkArgument(Objects.equals(this.getProvider().getCode(), providerCode),
                "Provider mismatch, attempting to store entity[½s] in context of provider[%s] .", this, providerCode);
    }

    public Ref getRef() {
        return new Ref(getNetexId(), getNetexVersion());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProviderEntity that = (ProviderEntity) o;

        if (netexId != null ? !netexId.equals(that.netexId) : that.netexId != null) return false;
        return version != null ? version.equals(that.version) : that.version == null;
    }

    @Override
    public int hashCode() {
        int result = netexId != null ? netexId.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    public void checkPersistable() {}


    protected String identity() {
        return MessageFormat.format("{0}[{1}]", getClass().getSimpleName(), getNetexId());
    }
}
