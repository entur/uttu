package no.entur.uttu.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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


    @Enumerated(EnumType.STRING)
    @NotNull
    private VehicleModeEnumeration transportMode;


    @OneToOne(cascade = CascadeType.ALL)
    private FlexibleArea flexibleArea;

    public FlexibleArea getFlexibleArea() {
        return flexibleArea;
    }

    public void setFlexibleArea(FlexibleArea flexibleArea) {
        this.flexibleArea = flexibleArea;
    }

    public VehicleModeEnumeration getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(VehicleModeEnumeration transportMode) {
        this.transportMode = transportMode;
    }
}
