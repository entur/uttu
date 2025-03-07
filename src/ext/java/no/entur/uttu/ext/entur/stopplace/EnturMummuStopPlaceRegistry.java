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

package no.entur.uttu.ext.entur.stopplace;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.xml.bind.JAXBElement;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import no.entur.uttu.stopplace.filter.params.StopPlaceFilterParams;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.netex.model.EntityInVersionStructure;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Integrates with https://github.com/entur/mummu stop place registry API
 * to retrieve a stop place given the ID of one of its quays.
 */
@Component
@Profile("entur-mummu-stop-place-registry")
public class EnturMummuStopPlaceRegistry implements StopPlaceRegistry {

  private static final Logger logger = LoggerFactory.getLogger(
    EnturMummuStopPlaceRegistry.class
  );

  private RestTemplate restTemplate = new RestTemplate();

  private static final String ET_CLIENT_ID_HEADER = "ET-Client-ID";
  private static final String ET_CLIENT_NAME_HEADER = "ET-Client-Name";

  @Value("${http.client.name:uttu}")
  private String clientName;

  @Value("${http.client.id:uttu}")
  private String clientId;

  @Value("${stopplace.registry.url:https://api.dev.entur.io/stop-places/v1/read}")
  private String stopPlaceRegistryUrl;

  private final LoadingCache<String, org.rutebanken.netex.model.StopPlace> stopPlaceByQuayRefCache =
    CacheBuilder
      .newBuilder()
      .expireAfterWrite(6, TimeUnit.HOURS)
      .build(
        new CacheLoader<>() {
          @Override
          public org.rutebanken.netex.model.StopPlace load(String quayRef) {
            return lookupStopPlaceByQuayRef(quayRef);
          }

          private org.rutebanken.netex.model.StopPlace lookupStopPlaceByQuayRef(
            String quayRef
          ) {
            try {
              return restTemplate
                .exchange(
                  stopPlaceRegistryUrl + "/quays/" + quayRef + "/stop-place",
                  HttpMethod.GET,
                  createHttpEntity(),
                  org.rutebanken.netex.model.StopPlace.class
                )
                .getBody();
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
      );

  @PostConstruct
  private void setup() {
    restTemplate.getMessageConverters().clear();
    restTemplate.getMessageConverters().add(new NetexHttpMessageConverter());
  }

  @Override
  public Optional<org.rutebanken.netex.model.StopPlace> getStopPlaceByQuayRef(
    String quayRef
  ) {
    try {
      return Optional
        .of(stopPlaceByQuayRefCache.get(quayRef))
        .filter(this::currentValidityFilter);
    } catch (ExecutionException e) {
      logger.warn("Failed to get stop place by quay ref ${}", quayRef);
      return Optional.empty();
    }
  }

  @Override
  public Optional<Quay> getQuayById(String id) {
    return getStopPlaceByQuayRef(id)
      .flatMap(stopPlace ->
        stopPlace
          .getQuays()
          .getQuayRefOrQuay()
          .stream()
          .map(JAXBElement::getValue)
          .map(Quay.class::cast)
          .filter(quay -> id.equals(quay.getId()))
          .findFirst()
      );
  }

  public boolean currentValidityFilter(EntityInVersionStructure entity) {
    return entity
      .getValidBetween()
      .stream()
      .filter(validBetween -> validBetween.getToDate() != null)
      .noneMatch(validBetween ->
        Instant
          .now()
          .isAfter(validBetween.getToDate().atZone(ZoneId.systemDefault()).toInstant())
      );
  }

  @Override
  public List<StopPlace> getStopPlaces(List<StopPlaceFilterParams> filters) {
    return List.of();
  }
}
