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

import static no.entur.uttu.model.Constraints.NETWORK_UNIQUE_NAME;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import no.entur.uttu.util.Preconditions;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = NETWORK_UNIQUE_NAME,
      columnNames = { "provider_pk", "name" }
    ),
  }
)
@BatchSize(size = 100)
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

  @Override
  public void checkPersistable() {
    super.checkPersistable();
    Preconditions.checkArgument(
      authorityRef != null,
      "% authorityRef not set",
      identity()
    );
  }
}
