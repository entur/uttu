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

package no.entur.uttu.stopplace.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all indexes for stop places, providing thread-safe operations
 * for adding, updating, and removing stop places and their associated quays.
 */
public class StopPlaceIndexManager {

  private static final Logger logger = LoggerFactory.getLogger(
    StopPlaceIndexManager.class
  );

  // Use CopyOnWriteArrayList for thread-safe reads with infrequent writes
  private final CopyOnWriteArrayList<StopPlace> allStopPlaces =
    new CopyOnWriteArrayList<>();

  // Thread-safe maps for quay lookups
  private final Map<String, StopPlace> stopPlaceByQuayRef = new ConcurrentHashMap<>();
  private final Map<String, Quay> quayById = new ConcurrentHashMap<>();
  private final Map<String, StopPlace> stopPlaceById = new ConcurrentHashMap<>();

  /**
   * Add a stop place to all indexes
   */
  public void addStopPlace(StopPlace stopPlace) {
    if (stopPlace == null || stopPlace.getId() == null) {
      throw new IllegalArgumentException("StopPlace and its ID cannot be null");
    }

    // Add to main collections
    allStopPlaces.add(stopPlace);
    stopPlaceById.put(stopPlace.getId(), stopPlace);

    // Index quays
    indexQuays(stopPlace);

    logger.debug("Added stop place {} to indexes", stopPlace.getId());
  }

  /**
   * Update a stop place in all indexes
   */
  public void updateStopPlace(String id, StopPlace newStopPlace) {
    if (id == null || newStopPlace == null) {
      throw new IllegalArgumentException("ID and StopPlace cannot be null");
    }

    // Remove old version
    removeStopPlace(id);

    // Add new version
    newStopPlace.setId(id);
    addStopPlace(newStopPlace);

    logger.debug("Updated stop place {} in indexes", id);
  }

  /**
   * Remove a stop place from all indexes
   */
  public void removeStopPlace(String id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }

    StopPlace stopPlace = stopPlaceById.remove(id);
    if (stopPlace != null) {
      // Remove from main list
      allStopPlaces.remove(stopPlace);

      // Remove quay references
      removeQuayReferences(stopPlace);

      logger.debug("Removed stop place {} from indexes", id);
    }
  }

  /**
   * Get all stop places
   */
  public List<StopPlace> getAllStopPlaces() {
    // Return a defensive copy
    return new ArrayList<>(allStopPlaces);
  }

  /**
   * Find a stop place by its ID
   */
  public Optional<StopPlace> getStopPlaceById(String id) {
    return Optional.ofNullable(stopPlaceById.get(id));
  }

  /**
   * Find a stop place by quay reference
   */
  public Optional<StopPlace> getStopPlaceByQuayRef(String quayRef) {
    return Optional.ofNullable(stopPlaceByQuayRef.get(quayRef));
  }

  /**
   * Find a quay by its ID
   */
  public Optional<Quay> getQuayById(String id) {
    return Optional.ofNullable(quayById.get(id));
  }

  /**
   * Get the quay index for filtering operations
   */
  public Map<String, StopPlace> getQuayIndex() {
    return new ConcurrentHashMap<>(stopPlaceByQuayRef);
  }

  /**
   * Load initial data in bulk
   */
  public void loadBulkData(List<StopPlace> stopPlaces) {
    logger.info("Loading {} stop places in bulk", stopPlaces.size());

    // Clear existing data
    clear();

    // Add all stop places
    for (StopPlace stopPlace : stopPlaces) {
      if (stopPlace != null && stopPlace.getId() != null) {
        allStopPlaces.add(stopPlace);
        stopPlaceById.put(stopPlace.getId(), stopPlace);
        indexQuays(stopPlace);
      }
    }

    logger.info("Bulk loading completed");
  }

  /**
   * Clear all indexes
   */
  public void clear() {
    allStopPlaces.clear();
    stopPlaceByQuayRef.clear();
    quayById.clear();
    stopPlaceById.clear();
  }

  private void indexQuays(StopPlace stopPlace) {
    if (stopPlace.getQuays() != null && stopPlace.getQuays().getQuayRefOrQuay() != null) {
      stopPlace
        .getQuays()
        .getQuayRefOrQuay()
        .forEach(quayRefOrQuay -> {
          if (quayRefOrQuay.getValue() instanceof Quay quay) {
            stopPlaceByQuayRef.put(quay.getId(), stopPlace);
            quayById.put(quay.getId(), quay);
          }
        });
    }
  }

  private void removeQuayReferences(StopPlace stopPlace) {
    if (stopPlace.getQuays() != null && stopPlace.getQuays().getQuayRefOrQuay() != null) {
      stopPlace
        .getQuays()
        .getQuayRefOrQuay()
        .forEach(quayRefOrQuay -> {
          if (quayRefOrQuay.getValue() instanceof Quay quay) {
            stopPlaceByQuayRef.remove(quay.getId());
            quayById.remove(quay.getId());
          }
        });
    }
  }
}
