package no.entur.uttu.model;

import no.entur.uttu.config.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

@MappedSuperclass
public abstract class IdentifiedEntity {

    @Id
    @GeneratedValue(generator = "sequence_per_table_generator")
    protected Long pk;

    @Version
    @NotNull
    protected Long version = 1l;

    @NotNull
    protected Instant created;
    @NotNull
    protected Instant changed;
    @NotNull
    protected String createdBy;
    @NotNull
    protected String changedBy;


    /**
     * This is the primary identificator.
     *
     * @return the primary long value of this identitifed entity.
     */
    public Long getPk() {
        return pk;
    }

    private void setPk(Long pk) {
        this.pk = pk;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getChanged() {
        return changed;
    }

    public void setChanged(Instant changed) {
        this.changed = changed;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @PrePersist
    @PreUpdate
    protected void setMetaData() {
        String user = Context.getUsername();
        Instant now = Instant.now();
        this.setChanged(now);
        this.setCreatedBy(user);

        if (this.getCreated() == null) {
            this.setCreated(now);
            this.setChangedBy(user);
        }
    }

    /**
     * Check whether entity is complete and consistent.
     * <p>
     * throws exception if not in a persistable state.
     */
    public void checkPersistable() {
    }

    /**
     * Check whether entity is valid within a period.
     *
     * @return if entity is valid for at least a part of the period.
     */
    public boolean isValid(LocalDate from, LocalDate to) {
        return true;
    }
}
