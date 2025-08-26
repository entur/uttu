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
import org.rutebanken.netex.model.StopPlace;

/**
 * Extends StopPlaceRegistry with mutating operations for managing stop places.
 * This interface is for components that need to modify the registry.
 */
public interface MutableStopPlaceRegistry extends StopPlaceRegistry {
  /**
   * Create a new stop place in the registry
   * @param id The ID of the stop place
   * @param stopPlace The stop place to create
   */
  void createStopPlace(String id, StopPlace stopPlace);

  /**
   * Update an existing stop place in the registry
   * @param id The ID of the stop place
   * @param stopPlace The updated stop place
   */
  void updateStopPlace(String id, StopPlace stopPlace);

  /**
   * Delete a stop place from the registry
   * @param id The ID of the stop place to delete
   */
  void deleteStopPlace(String id);

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
