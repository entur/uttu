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

import no.entur.uttu.config.NetexHttpMessageConverter;
import no.entur.uttu.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class StopPlaceRegistryImpl implements StopPlaceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(StopPlaceRegistryImpl.class);

    private RestTemplate restTemplate = new RestTemplate();

    private static final String ET_CLIENT_ID_HEADER = "ET-Client-ID";
    private static final String ET_CLIENT_NAME_HEADER = "ET-Client-Name";

    private static final String SUCCESS_MATCHER = "\"id\"";
    @Value("${http.client.name:uttu}")
    private String clientName;

    @Value("${http.client.id:uttu}")
    private String clientId;

    @Value("${stopplace.registry.url:https://api.entur.io/stop-places/v1/read}")
    private String stopPlaceRegistryUrl;

    @PostConstruct
    private void setup() {
        restTemplate.getMessageConverters().clear();
        restTemplate.getMessageConverters().add(new NetexHttpMessageConverter());
    }

    public boolean isValidQuayRef(String quayRef) {
        if (quayRef == null) {
            return false;
        }
        if (!quayRef.contains(":Quay:")) {
            return false;
        }

        try {
            StopPlace stopPlace = getStopPlaceByQuayRef(quayRef);
            return stopPlace != null && stopPlace.getId() != null;
        } catch (RestClientException e) {
            logger.warn("Error checking quay ref {}: {}", quayRef, e.getMessage());
            return false;
        }
    }

    /**
     * Return provided quayRef if valid, else throw exception.
     */
    public String getVerifiedQuayRef(String quayRef) {
        if (quayRef == null) {
            return null;
        }

        // Check that quayRef is a valid Quay id. To avoid getting hits on stop place / street / municipality whatever, as stop place registry query matches anything
        Preconditions.checkArgument(isValidQuayRef(quayRef), "%s is not a valid quayRef", quayRef);

        return quayRef;
    }

    @Override
    public StopPlace getStopPlaceByQuayRef(String quayRef) {
        try {
            org.rutebanken.netex.model.StopPlace stopPlace = restTemplate.exchange(stopPlaceRegistryUrl + "/quays/" + quayRef + "/stop-place", HttpMethod.GET, createHttpEntity(), org.rutebanken.netex.model.StopPlace.class).getBody();
            assert stopPlace != null;
            return map(stopPlace);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            throw e;
        }
    }

    private StopPlace map(org.rutebanken.netex.model.StopPlace stopPlace) {
        StopPlace mapped = new StopPlace();
        mapped.setId(stopPlace.getId());
        mapped.setName(stopPlace.getName().getValue());
        mapped.setQuays(stopPlace.getQuays().getQuayRefOrQuay().stream().map(v -> (org.rutebanken.netex.model.Quay)v).map(this::mapQuay).collect(Collectors.toList()));
        return mapped;
    }

    private Quay mapQuay(org.rutebanken.netex.model.Quay quay) {
        Quay mapped = new Quay();
        mapped.setId(quay.getId());
        mapped.setPublicCode(quay.getPublicCode());
        return mapped;
    }

    private HttpEntity<Void> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        headers.set(ET_CLIENT_NAME_HEADER, clientName);
        headers.set(ET_CLIENT_ID_HEADER, clientId);
        return new HttpEntity<>(headers);
    }
}
