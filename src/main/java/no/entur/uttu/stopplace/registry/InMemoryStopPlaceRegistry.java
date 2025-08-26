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

package no.entur.uttu.stopplace.registry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import no.entur.uttu.stopplace.filter.StopPlacesFilter;
import no.entur.uttu.stopplace.filter.params.BoundingBoxFilterParams;
import no.entur.uttu.stopplace.filter.params.StopPlaceFilterParams;
import no.entur.uttu.stopplace.index.StopPlaceIndexManager;
import no.entur.uttu.stopplace.spatial.StopPlaceSpatialService;
import no.entur.uttu.stopplace.spi.MutableStopPlaceRegistry;
import no.entur.uttu.stopplace.spi.StopPlaceDataLoader;
import org.locationtech.jts.geom.Polygon;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of the StopPlaceRegistry.
 * This implementation separates data loading from registry logic.
 */
@Component
@ConditionalOnMissingBean(
  value = MutableStopPlaceRegistry.class,
  ignored = InMemoryStopPlaceRegistry.class
)
public class InMemoryStopPlaceRegistry implements MutableStopPlaceRegistry {

  private static final Logger logger = LoggerFactory.getLogger(
    InMemoryStopPlaceRegistry.class
  );

  private final StopPlaceIndexManager indexManager;
  private final StopPlaceSpatialService spatialService;
  private final StopPlacesFilter stopPlacesFilter;
  private final Optional<StopPlaceDataLoader> dataLoader;

  private final AtomicReference<Instant> publicationTime = new AtomicReference<>();

  @Autowired
  public InMemoryStopPlaceRegistry(
    StopPlaceIndexManager indexManager,
    StopPlaceSpatialService spatialService,
    StopPlacesFilter stopPlacesFilter,
    Optional<StopPlaceDataLoader> dataLoader
  ) {
    this.indexManager = indexManager;
    this.spatialService = spatialService;
    this.stopPlacesFilter = stopPlacesFilter;
    this.dataLoader = dataLoader;
  }

  @PostConstruct
  public void init() {
    if (dataLoader.isPresent()) {
      logger.info("Loading initial stop place data");
      try {
        StopPlaceDataLoader.LoadResult result = dataLoader.get().loadStopPlaces();

        // Load data into indexes
        indexManager.loadBulkData(result.stopPlaces());

        // Build spatial index
        spatialService.buildSpatialIndex(result.stopPlaces());

        // Set publication time
        publicationTime.set(result.publicationTime());

        logger.info("Successfully loaded {} stop places", result.stopPlaces().size());
      } catch (Exception e) {
        logger.error("Failed to load initial stop place data", e);
      }
    } else {
      logger.info("No data loader configured, starting with empty registry");
    }
  }

  @Override
  public Optional<StopPlace> getStopPlaceByQuayRef(String quayRef) {
    return indexManager.getStopPlaceByQuayRef(quayRef);
  }

  @Override
  public List<StopPlace> getStopPlaces(List<StopPlaceFilterParams> filters) {
    List<StopPlace> stopPlaces = indexManager.getAllStopPlaces();

    if (filters.isEmpty()) {
      return stopPlaces;
    }

    // Check for bounding box filter for optimization
    Optional<BoundingBoxFilterParams> boundingBoxFilter = filters
      .stream()
      .filter(BoundingBoxFilterParams.class::isInstance)
      .map(BoundingBoxFilterParams.class::cast)
      .findFirst();

    if (boundingBoxFilter.isPresent()) {
      // Pre-filter with spatial index
      stopPlaces = spatialService.preFilterByBoundingBox(
        stopPlaces,
        boundingBoxFilter.get()
      );
    }

    // Apply remaining filters
    return stopPlacesFilter.filter(stopPlaces, indexManager.getQuayIndex(), filters);
  }

  @Override
  public Optional<Quay> getQuayById(String id) {
    return indexManager.getQuayById(id);
  }

  @Override
  public List<StopPlace> getStopPlacesWithinPolygon(Polygon polygon) {
    return spatialService.getStopPlacesWithinPolygon(polygon);
  }

  @Override
  public void createStopPlace(String id, StopPlace stopPlace) {
    if (id == null || stopPlace == null) {
      throw new IllegalArgumentException("ID and StopPlace cannot be null");
    }

    stopPlace.setId(id);
    indexManager.addStopPlace(stopPlace);

    // Rebuild spatial index
    rebuildSpatialIndex();

    logger.info("Created stop place with id: {}", id);
  }

  @Override
  public void updateStopPlace(String id, StopPlace stopPlace) {
    if (id == null || stopPlace == null) {
      throw new IllegalArgumentException("ID and StopPlace cannot be null");
    }

    indexManager.updateStopPlace(id, stopPlace);

    // Rebuild spatial index
    rebuildSpatialIndex();

    logger.info("Updated stop place with id: {}", id);
  }

  @Override
  public void deleteStopPlace(String id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }

    indexManager.removeStopPlace(id);

    // Rebuild spatial index
    rebuildSpatialIndex();

    logger.info("Deleted stop place with id: {}", id);
  }

  @Override
  public Instant getPublicationTime() {
    return publicationTime.get();
  }

  @Override
  public void setPublicationTime(Instant publicationTime) {
    this.publicationTime.set(publicationTime);
  }

  private void rebuildSpatialIndex() {
    List<StopPlace> allStopPlaces = indexManager.getAllStopPlaces();
    spatialService.buildSpatialIndex(allStopPlaces);
  }
}
