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

package no.entur.uttu.stopplace.spatial;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import no.entur.uttu.stopplace.filter.params.BoundingBoxFilterParams;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.strtree.STRtree;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.StopPlace;
import org.rutebanken.netex.model.Zone_VersionStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for spatial operations on stop places.
 * Manages spatial indexing and provides efficient spatial queries.
 */
@Service
public class StopPlaceSpatialService {

  private static final Logger logger = LoggerFactory.getLogger(
    StopPlaceSpatialService.class
  );

  private final GeometryFactory geometryFactory = new GeometryFactory();
  private final ReadWriteLock spatialIndexLock = new ReentrantReadWriteLock();

  private STRtree spatialIndex = new STRtree();
  private boolean indexBuilt = false;

  /**
   * Build or rebuild the spatial index with the provided stop places
   */
  public void buildSpatialIndex(List<StopPlace> stopPlaces) {
    spatialIndexLock.writeLock().lock();
    try {
      logger.debug("Building spatial index with {} stop places", stopPlaces.size());

      spatialIndex = new STRtree();
      indexBuilt = false;

      int indexed = 0;
      for (StopPlace stopPlace : stopPlaces) {
        if (
          stopPlace.getQuays() != null &&
          !stopPlace.getQuays().getQuayRefOrQuay().isEmpty()
        ) {
          indexed++;
          stopPlace
            .getQuays()
            .getQuayRefOrQuay()
            .forEach(quayRefOrQuay -> {
              Point quayPoint = createPointFromEntityWithCentroid(
                (Quay) quayRefOrQuay.getValue()
              );
              if (quayPoint != null) {
                spatialIndex.insert(quayPoint.getEnvelopeInternal(), stopPlace);
              }
            });
        }
      }

      spatialIndex.build();
      indexBuilt = true;

      logger.debug("Spatial index built successfully with {} stop places", indexed);
    } finally {
      spatialIndexLock.writeLock().unlock();
    }
  }

  /**
   * Find stop places within a polygon
   */
  public List<StopPlace> getStopPlacesWithinPolygon(Polygon polygon) {
    if (polygon == null) {
      return new ArrayList<>();
    }

    spatialIndexLock.readLock().lock();
    try {
      if (!indexBuilt) {
        logger.warn("Spatial index not built, returning empty list");
        return new ArrayList<>();
      }

      @SuppressWarnings("unchecked")
      List<StopPlace> candidates = spatialIndex.query(polygon.getEnvelopeInternal());

      return candidates
        .stream()
        .distinct() // Remove duplicates since same StopPlace can be indexed multiple times (once per quay)
        .filter(stopPlace -> {
          if (stopPlace.getQuays() != null) {
            return stopPlace
              .getQuays()
              .getQuayRefOrQuay()
              .stream()
              .anyMatch(quayRefOrQuay -> {
                Point quayPoint = createPointFromEntityWithCentroid(
                  (Quay) quayRefOrQuay.getValue()
                );
                return quayPoint != null && polygon.contains(quayPoint);
              });
          }
          return false;
        })
        .toList();
    } finally {
      spatialIndexLock.readLock().unlock();
    }
  }

  /**
   * Pre-filter stop places by bounding box for optimization
   */
  public List<StopPlace> preFilterByBoundingBox(
    List<StopPlace> stopPlaces,
    BoundingBoxFilterParams boundingBox
  ) {
    if (boundingBox == null) {
      return stopPlaces;
    }

    Polygon polygon = createPolygonFromBoundingBox(boundingBox);
    List<StopPlace> filtered = getStopPlacesWithinPolygon(polygon);

    logger.debug(
      "Spatial pre-filtering reduced stop places from {} to {}",
      stopPlaces.size(),
      filtered.size()
    );

    return filtered;
  }

  /**
   * Convert BoundingBoxFilterParams to JTS Polygon
   */
  public Polygon createPolygonFromBoundingBox(BoundingBoxFilterParams boundingBox) {
    if (boundingBox == null) {
      throw new IllegalArgumentException("BoundingBox cannot be null");
    }

    double swLat = boundingBox.southWestLat().doubleValue();
    double swLng = boundingBox.southWestLng().doubleValue();
    double neLat = boundingBox.northEastLat().doubleValue();
    double neLng = boundingBox.northEastLng().doubleValue();

    Coordinate[] coords = {
      new Coordinate(swLng, swLat), // SW
      new Coordinate(neLng, swLat), // SE
      new Coordinate(neLng, neLat), // NE
      new Coordinate(swLng, neLat), // NW
      new Coordinate(swLng, swLat), // Close the ring
    };

    return geometryFactory.createPolygon(coords);
  }

  /**
   * Create a JTS Point from a StopPlace's centroid
   */
  private Point createPointFromEntityWithCentroid(
    Zone_VersionStructure entityWithCentroid
  ) {
    if (
      entityWithCentroid == null ||
      entityWithCentroid.getCentroid() == null ||
      entityWithCentroid.getCentroid().getLocation() == null
    ) {
      return null;
    }

    var location = entityWithCentroid.getCentroid().getLocation();
    if (location.getLongitude() == null || location.getLatitude() == null) {
      return null;
    }

    double longitude = location.getLongitude().doubleValue();
    double latitude = location.getLatitude().doubleValue();

    return geometryFactory.createPoint(new Coordinate(longitude, latitude));
  }
}
