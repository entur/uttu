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

package no.entur.uttu.stopplace;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StopPlaceRegistry {

    private String stopPlaceRegistryUrl;

    public StopPlaceRegistry(@Value("${stopplace.registry.url:https://api-test.entur.org/stop_places/1.0/graphql}") String stopPlaceRegistryUrl) {
        this.stopPlaceRegistryUrl = stopPlaceRegistryUrl;
    }
    /**
     * Return provided quayRef if valid, else throw exception.
     */
    public String getVerifiedQuayRef(String quayRef) {
        if (quayRef == null) {
            return null;
        }

        // TODO
        return quayRef;
    }
}
