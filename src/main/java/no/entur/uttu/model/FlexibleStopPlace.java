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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.entur.uttu.util.Preconditions;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      name = Constraints.FLEXIBLE_STOP_PLACE_UNIQUE_NAME,
      columnNames = { "provider_pk", "name" }
    ),
  }
)
public class FlexibleStopPlace extends GroupOfEntities_VersionStructure {

  @Enumerated(EnumType.STRING)
  @NotNull
  private VehicleModeEnumeration transportMode;

  @OneToMany(
    mappedBy = "flexibleStopPlace",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER
  )
  @NotNull
  private List<FlexibleArea> flexibleAreas = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL)
  private HailAndRideArea hailAndRideArea;

  public List<FlexibleArea> getFlexibleAreas() {
    return flexibleAreas;
  }

  public FlexibleArea getFlexibleArea() {
    return flexibleAreas.isEmpty() ? null : flexibleAreas.get(0);
  }

  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  protected Map<String, Value> keyValues = new HashMap<>();

  public void setFlexibleAreas(List<FlexibleArea> flexibleAreas) {
    this.flexibleAreas.clear();
    flexibleAreas.forEach(flexibleArea -> flexibleArea.setFlexibleStopPlace(this));
    this.flexibleAreas.addAll(flexibleAreas);
  }

  public HailAndRideArea getHailAndRideArea() {
    return hailAndRideArea;
  }

  public void setHailAndRideArea(HailAndRideArea hailAndRideArea) {
    this.hailAndRideArea = hailAndRideArea;
  }

  public VehicleModeEnumeration getTransportMode() {
    return transportMode;
  }

  public void setTransportMode(VehicleModeEnumeration transportMode) {
    this.transportMode = transportMode;
  }

  public Map<String, Value> getKeyValues() {
    return keyValues;
  }

  public void replaceKeyValues(Map<String, Value> keyValues) {
    this.keyValues.clear();
    this.keyValues.putAll(keyValues);
  }

  @Override
  public void checkPersistable() {
    super.checkPersistable();
    Preconditions.checkArgument(
      !flexibleAreas.isEmpty() ^ (hailAndRideArea != null),
      "%s exactly one of flexibleArea and hailAndRideArea must be set",
      identity()
    );

    if (!flexibleAreas.isEmpty()) {
      flexibleAreas.forEach(IdentifiedEntity::checkPersistable);
    }
    if (hailAndRideArea != null) {
      hailAndRideArea.checkPersistable();
    }
  }
}
