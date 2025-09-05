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

import java.time.Instant;
import java.util.List;
import org.rutebanken.netex.model.StopPlace;

/**
 * Extends StopPlaceRegistry with mutating operations for managing stop places.
 * This interface is for components that need to modify the registry.
 */
public interface MutableStopPlaceRegistry extends StopPlaceRegistry {
  /**
   * Create or update multiple stop places in a batch operation.
   * This method intelligently handles mixed operations - creating new stops
   * and updating existing ones. This is particularly important for multimodal
   * structures where a "create" event may include both a new parent stop
   * and updates to existing child stops.
   * @param stopPlaces List of stop places to create or update
   */
  void createOrUpdateStopPlaces(List<StopPlace> stopPlaces);

  /**
   * Delete a stop place and all related stops (for multimodal structures)
   * This will remove the stop with the given ID and any child stops that reference it
   * @param id The ID of the stop place to delete
   */
  void deleteStopPlaceAndRelated(String id);

  /**
   * Get the publication time of the stop place data
   * @return The publication time, or null if not available
   */
  Instant getPublicationTime();

  /**
   * Set the publication time for the stop place data
   * @param publicationTime The publication time to set
   */
  void setPublicationTime(Instant publicationTime);
}
