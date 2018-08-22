package no.entur.uttu.model;

import com.vividsolutions.jts.geom.Polygon;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
public class PersistablePolygon implements Serializable {

    @Id
    @GeneratedValue(generator = "sequence_per_table_generator")
    protected Long id;

    @NotNull
    private Polygon polygon;

    public PersistablePolygon() {
    }

    public PersistablePolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }
}
