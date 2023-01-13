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

package no.entur.uttu.stubs;

import no.entur.uttu.stopplace.StopPlace;
import no.entur.uttu.stopplace.StopPlaceRegistry;
import org.springframework.stereotype.Component;

@Component
public class StopPlaceRegistryStub implements StopPlaceRegistry {

    @Override
    public StopPlace getStopPlaceByQuayRef(String quayRef) {
        StopPlace stopPlace = new StopPlace();
        stopPlace.setId("NSR:StopPlace:1");
        return stopPlace;
    }
}
