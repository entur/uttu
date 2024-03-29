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
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.locationtech.jts.geom.Polygon;

@Entity
public class FlexibleArea extends IdentifiedEntity {

  @ManyToOne
  @NotNull
  private FlexibleStopPlace flexibleStopPlace;

  /**
   * Polygon is wrapped in PersistablePolygon.
   * Because we want to fetch polygons lazily and using lazy property fetching with byte code enhancement breaks tests.
   */
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @NotNull
  private PersistablePolygon polygon;

  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  protected Map<String, Value> keyValues = new HashMap<>();

  public Polygon getPolygon() {
    if (polygon == null) {
      return null;
    }
    return polygon.getPolygon();
  }

  public void setPolygon(Polygon polygon) {
    if (polygon == null) {
      this.polygon = null;
    } else {
      this.polygon = new PersistablePolygon(polygon);
    }
  }

  public void setFlexibleStopPlace(FlexibleStopPlace flexibleStopPlace) {
    this.flexibleStopPlace = flexibleStopPlace;
  }

  public FlexibleStopPlace getFlexibleStopPlace() {
    return flexibleStopPlace;
  }

  public Map<String, Value> getKeyValues() {
    return keyValues;
  }

  public void replaceKeyValues(Map<String, Value> keyValues) {
    this.keyValues.clear();
    this.keyValues.putAll(keyValues);
  }
}
