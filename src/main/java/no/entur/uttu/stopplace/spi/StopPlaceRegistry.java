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

package no.entur.uttu.stopplace.spi;

import java.util.List;
import java.util.Optional;
import no.entur.uttu.stopplace.filter.StopPlaceFilter;

/**
 * Represents a stop place registry used to lookup stop places from quay refs
 */
public interface StopPlaceRegistry {
  /**
   * Lookup a stop place entity from a quay ref
   * @param quayRef The id of a quay
   * @return The stop place that the quay belongs to
   */
  Optional<org.rutebanken.netex.model.StopPlace> getStopPlaceByQuayRef(String quayRef);

  /**
   * Serve all stop places and quays, used e.g. in stop places route pattern editing map.
   * Incoming filters must derive from StopPlaceFilter, but otherwise are simply a record with some custom fields.
   * @param filters Various filters to limit the set of stop places, e.g. by transport mode; normally, there is only one occurrence of a certain kind of filter in the filters list.
   * @return A list of all stop places satisfying the incoming filters
   */
  List<org.rutebanken.netex.model.StopPlace> getStopPlaces(List<StopPlaceFilter> filters);

  /**
   * Lookup a quay entity from its id
   * @param id The id of the quay
   * @return The quay entity
   */
  Optional<org.rutebanken.netex.model.Quay> getQuayById(String id);
}
