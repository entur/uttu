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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import no.entur.uttu.config.NetexHttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.jdo.annotations.Cacheable;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Integrates with https://github.com/entur/mummu stop place registry API
 * to retrieve a stop place given the ID of one of its quays.
 */
@Component
public class DefaultStopPlaceRegistry implements StopPlaceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultStopPlaceRegistry.class);

    private RestTemplate restTemplate = new RestTemplate();

    private static final String ET_CLIENT_ID_HEADER = "ET-Client-ID";
    private static final String ET_CLIENT_NAME_HEADER = "ET-Client-Name";

    @Value("${http.client.name:uttu}")
    private String clientName;

    @Value("${http.client.id:uttu}")
    private String clientId;

    @Value("${stopplace.registry.url:https://api.dev.entur.io/stop-places/v1/read}")
    private String stopPlaceRegistryUrl;

    private final LoadingCache<String, org.rutebanken.netex.model.StopPlace> stopPlaceByQuayRefCache = CacheBuilder.newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public org.rutebanken.netex.model.StopPlace load(String quayRef) {
                    return lookupStopPlaceByQuayRef(quayRef);
                }
            });

    @PostConstruct
    private void setup() {
        restTemplate.getMessageConverters().clear();
        restTemplate.getMessageConverters().add(new NetexHttpMessageConverter());
    }

    @Override
    @Cacheable("stopPlacesByQuayRef")
    public Optional<org.rutebanken.netex.model.StopPlace> getStopPlaceByQuayRef(String quayRef) {
        try {
            return Optional.of(stopPlaceByQuayRefCache.get(quayRef));
        } catch (ExecutionException e) {
            logger.warn("Failed to get stop place by quay ref ${}", quayRef);
            return Optional.empty();
        }

    }

    private org.rutebanken.netex.model.StopPlace lookupStopPlaceByQuayRef(String quayRef) {
        logger.info("cache miss");
        try {
            return restTemplate.exchange(
                    stopPlaceRegistryUrl + "/quays/" + quayRef + "/stop-place",
                    HttpMethod.GET,
                    createHttpEntity(),
                    org.rutebanken.netex.model.StopPlace.class
            ).getBody();
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    private HttpEntity<Void> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        headers.set(ET_CLIENT_NAME_HEADER, clientName);
        headers.set(ET_CLIENT_ID_HEADER, clientId);
        return new HttpEntity<>(headers);
    }
}
