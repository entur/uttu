package no.entur.uttu.model;

import com.vividsolutions.jts.geom.Polygon;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(
        uniqueConstraints = {
                                    @UniqueConstraint(name = "flexible_stop_place_unique_name_constraint", columnNames = {"provider_pk", "name"})}
)
public class FlexibleStopPlace extends GroupOfEntities_VersionStructure {


    // TODO create StopPlace superclass, change all references to this.

    /**
     * Polygon is wrapped in PersistablePolygon.
     * Because we want to fetch polygons lazily and using lazy property fetching with byte code enhancement breaks tests.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private PersistablePolygon polygon;

    @Enumerated(EnumType.STRING)
    @NotNull
    private VehicleModeEnumeration transportMode;


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

    public VehicleModeEnumeration getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(VehicleModeEnumeration transportMode) {
        this.transportMode = transportMode;
    }
}
