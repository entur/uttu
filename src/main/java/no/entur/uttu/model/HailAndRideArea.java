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

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class HailAndRideArea extends IdentifiedEntity {

    @NotNull
    private String startQuayRef;

    @NotNull
    private String endQuayRef;

    public String getStartQuayRef() {
        return startQuayRef;
    }

    public void setStartQuayRef(String startQuayRef) {
        this.startQuayRef = startQuayRef;
    }

    public String getEndQuayRef() {
        return endQuayRef;
    }

    public void setEndQuayRef(String endQuayRef) {
        this.endQuayRef = endQuayRef;
    }

    @Override
    public void checkPersistable() {
        super.checkPersistable();

        Preconditions.checkArgument(startQuayRef != null && endQuayRef != null, "startQuayRef and endQuayRef must be set for HailAndRideArea");
    }
}
