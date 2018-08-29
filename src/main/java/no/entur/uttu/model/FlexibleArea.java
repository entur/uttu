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
