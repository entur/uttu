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

import no.entur.uttu.config.NetexHttpMessageConverter;
import no.entur.uttu.stopplace.StopPlace;
import no.entur.uttu.stopplace.StopPlaceMapper;
import no.entur.uttu.stopplace.StopPlaceRegistry;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Component
public class StopPlaceRegistryStub implements StopPlaceRegistry {

    @Override
    public Optional<StopPlace> getStopPlaceByQuayRef(String quayRef) {
        NetexHttpMessageConverter converter = new NetexHttpMessageConverter();

        try {
            org.rutebanken.netex.model.StopPlace stopPlace = (org.rutebanken.netex.model.StopPlace) converter.read(
                    org.rutebanken.netex.model.StopPlace.class,
                    new HttpInputMessage() {
                        @Override
                        public InputStream getBody() throws IOException {
                            return new FileInputStream("src/test/resources/stopPlaceFixture.xml");
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            return HttpHeaders.EMPTY;
                        }
                    }
            );
            return Optional.of(StopPlaceMapper.mapStopPlace(stopPlace));
        } catch (IOException e) {
            return Optional.empty();
        }


    }
}
