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

import com.vividsolutions.jts.geom.Polygon;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
public class FlexibleArea extends IdentifiedEntity {

    /**
     * Polygon is wrapped in PersistablePolygon.
     * Because we want to fetch polygons lazily and using lazy property fetching with byte code enhancement breaks tests.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private PersistablePolygon polygon;


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
}
