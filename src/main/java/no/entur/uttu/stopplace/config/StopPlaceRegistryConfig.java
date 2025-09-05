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

package no.entur.uttu.stopplace.config;

import no.entur.uttu.stopplace.filter.StopPlacesFilter;
import no.entur.uttu.stopplace.index.StopPlaceIndexManager;
import no.entur.uttu.stopplace.spatial.StopPlaceSpatialService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the stop place registry components.
 */
@Configuration
public class StopPlaceRegistryConfig {

  @Bean
  public StopPlaceIndexManager stopPlaceIndexManager() {
    return new StopPlaceIndexManager();
  }

  @Bean
  public StopPlaceSpatialService stopPlaceSpatialService() {
    return new StopPlaceSpatialService();
  }

  @Bean
  public StopPlacesFilter stopPlacesFilter() {
    return new StopPlacesFilter();
  }
}
