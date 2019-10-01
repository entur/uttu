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

import no.entur.uttu.util.Preconditions;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = Constraints.FLEXIBLE_STOP_PLACE_UNIQUE_NAME, columnNames = {"provider_pk", "name"})})
public class FlexibleStopPlace extends GroupOfEntities_VersionStructure {

    @Enumerated(EnumType.STRING)
    @NotNull
    private VehicleModeEnumeration transportMode;

    @OneToOne(cascade = CascadeType.ALL)
    private FlexibleArea flexibleArea;

    @OneToOne(cascade = CascadeType.ALL)
    private HailAndRideArea hailAndRideArea;

    public FlexibleArea getFlexibleArea() {
        return flexibleArea;
    }

    public void setFlexibleArea(FlexibleArea flexibleArea) {
        this.flexibleArea = flexibleArea;
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


    @Override
    public void checkPersistable() {
        super.checkPersistable();
        Preconditions.checkArgument(flexibleArea != null ^ hailAndRideArea != null, "%s exactly one of flexibleArea and hailAndRideArea must be set", identity());

        if (flexibleArea != null) {
            flexibleArea.checkPersistable();
        }
        if (hailAndRideArea != null) {
            hailAndRideArea.checkPersistable();
        }
    }
}
