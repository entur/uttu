/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import no.entur.uttu.config.Context;

@MappedSuperclass
public abstract class IdentifiedEntity {

  @Id
  @GeneratedValue(generator = "sequence_per_table_generator")
  protected Long pk;

  @Version
  @NotNull
  protected Long version;

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
    this.created = Objects.requireNonNull(created).truncatedTo(ChronoUnit.MICROS);
  }

  public Instant getChanged() {
    return changed;
  }

  public void setChanged(Instant changed) {
    this.changed = Objects.requireNonNull(changed).truncatedTo(ChronoUnit.MICROS);
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
    String user = Context.getVerifiedUsername();
    Instant now = Instant.now();
    this.setChanged(now);
    this.setChangedBy(user);

    if (this.getCreated() == null) {
      this.setCreated(now);
      this.setCreatedBy(user);
    }
  }

  /**
   * Check whether entity is complete and consistent.
   * <p>
   * throws exception if not in a persistable state.
   */
  public void checkPersistable() {}

  /**
   * Check whether entity is valid within a period.
   *
   * @return if entity is valid for at least a part of the period.
   */
  public boolean isValid(LocalDate from, LocalDate to) {
    return true;
  }

  @Override
  public String toString() {
    return (
      "pk=" +
      pk +
      ", version=" +
      version +
      ", created=" +
      created +
      ", changed=" +
      changed +
      ", createdBy='" +
      createdBy +
      '\'' +
      ", changedBy='" +
      changedBy +
      '\''
    );
  }
}
