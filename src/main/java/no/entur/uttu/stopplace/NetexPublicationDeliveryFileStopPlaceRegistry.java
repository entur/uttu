package no.entur.uttu.stopplace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.netex.NetexUnmarshallerUnmarshalFromSourceException;
import no.entur.uttu.stopplace.filter.StopPlacesFilter;
import no.entur.uttu.stopplace.filter.params.BoundingBoxFilterParams;
import no.entur.uttu.stopplace.filter.params.StopPlaceFilterParams;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.strtree.STRtree;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(
  value = StopPlaceRegistry.class,
  ignored = NetexPublicationDeliveryFileStopPlaceRegistry.class
)
public class NetexPublicationDeliveryFileStopPlaceRegistry implements StopPlaceRegistry {

  private final Logger logger = LoggerFactory.getLogger(
    NetexPublicationDeliveryFileStopPlaceRegistry.class
  );

  private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
    PublicationDeliveryStructure.class
  );

  private final Map<String, StopPlace> stopPlaceByQuayRefIndex =
    new ConcurrentHashMap<>();

  private final Map<String, Quay> quayByQuayRefIndex = new ConcurrentHashMap<>();

  private final List<StopPlace> allStopPlacesIndex = new ArrayList<>();

  private STRtree spatialIndex = new STRtree();
  private final GeometryFactory geometryFactory = new GeometryFactory();

  @Value("${uttu.stopplace.netex-file-uri}")
  String netexFileUri;

  private StopPlacesFilter stopPlacesFilter = new StopPlacesFilter();

  private Instant publicationTime;

  @PostConstruct
  public void init() {
    try (ZipFile zipFile = new ZipFile(netexFileUri)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      ZipEntry entry = entries.nextElement();
      logger.info("Found a zip file with entry {}", entry.getName());
      InputStream netexFileInputStream = zipFile.getInputStream(entry);
      logger.info("Creating StreamSource from netexFileInputStream");
      PublicationDeliveryStructure publicationDeliveryStructure =
        netexUnmarshaller.unmarshalFromSource(new StreamSource(netexFileInputStream));
      extractPublicationTime(publicationDeliveryStructure);
      extractStopPlaceData(publicationDeliveryStructure);
      buildSpatialIndex();
    } catch (IOException ioException) {
      // probably not a zip file
      logger.info("Not a zip file", ioException);

      try {
        PublicationDeliveryStructure publicationDeliveryStructure =
          netexUnmarshaller.unmarshalFromSource(new StreamSource(new File(netexFileUri)));
        extractStopPlaceData(publicationDeliveryStructure);
        buildSpatialIndex();
      } catch (
        NetexUnmarshallerUnmarshalFromSourceException unmarshalFromSourceException
      ) {
        logger.warn(
          "Unable to unmarshal stop places xml, stop place registry will be an empty list",
          unmarshalFromSourceException
        );
      }
    } catch (NetexUnmarshallerUnmarshalFromSourceException unmarshalFromSourceException) {
      logger.warn(
        "Unable to unmarshal stop places xml, stop place registry will be an empty list",
        unmarshalFromSourceException
      );
    }
  }

  private void extractPublicationTime(PublicationDeliveryStructure publicationDeliveryStructure) {
    var localPublicationTimestamp = publicationDeliveryStructure.getPublicationTimestamp();
    var timeZone =  "Europe/Oslo";
    publicationTime = localPublicationTimestamp.atZone(ZoneId.of(timeZone)).toInstant();
  }

  private void extractStopPlaceData(
    PublicationDeliveryStructure publicationDeliveryStructure
  ) {
    publicationDeliveryStructure
      .getDataObjects()
      .getCompositeFrameOrCommonFrame()
      .forEach(frame -> {
        var frameValue = frame.getValue();
        if (frameValue instanceof SiteFrame siteFrame) {
          List<StopPlace> stopPlaces = siteFrame
            .getStopPlaces()
            .getStopPlace_()
            .stream()
            .map(stopPlace -> (StopPlace) stopPlace.getValue())
            .toList();

          stopPlaces.forEach(stopPlace -> {
            Optional.ofNullable(stopPlace.getQuays()).ifPresent(quays ->
              quays
                .getQuayRefOrQuay()
                .forEach(quayRefOrQuay -> {
                  Quay quay = (Quay) quayRefOrQuay.getValue();
                  stopPlaceByQuayRefIndex.put(quay.getId(), stopPlace);
                  quayByQuayRefIndex.put(quay.getId(), quay);
                }));
            allStopPlacesIndex.add(stopPlace);
          });
        }
      });
  }

  @Override
  public Optional<StopPlace> getStopPlaceByQuayRef(String quayRef) {
    return Optional.ofNullable(stopPlaceByQuayRefIndex.get(quayRef));
  }

  @Override
  public List<StopPlace> getStopPlaces(List<StopPlaceFilterParams> filters) {
    if (filters.isEmpty()) {
      return allStopPlacesIndex;
    }

    // Check if we can optimize with spatial pre-filtering
    Optional<BoundingBoxFilterParams> boundingBoxFilter = filters
      .stream()
      .filter(BoundingBoxFilterParams.class::isInstance)
      .map(BoundingBoxFilterParams.class::cast)
      .findFirst();

    if (boundingBoxFilter.isPresent()) {
      return getStopPlacesOptimized(filters, boundingBoxFilter.get());
    }

    return stopPlacesFilter.filter(allStopPlacesIndex, stopPlaceByQuayRefIndex, filters);
  }

  /**
   * Optimized stop place filtering that uses spatial index for bounding box pre-filtering
   */
  private List<StopPlace> getStopPlacesOptimized(
    List<StopPlaceFilterParams> filters,
    BoundingBoxFilterParams boundingBoxFilter
  ) {
    logger.debug(
      "About to apply pre-filtering to {} stop places",
      allStopPlacesIndex.size()
    );

    // Step 1: Use spatial index to pre-filter by bounding box
    Polygon boundingBoxPolygon = createPolygonFromBoundingBox(boundingBoxFilter);
    List<StopPlace> spatiallyFilteredStops = getStopPlacesWithinPolygon(
      boundingBoxPolygon
    );

    logger.debug(
      "Spatial pre-filtering reduced stop places from {} to {}",
      allStopPlacesIndex.size(),
      spatiallyFilteredStops.size()
    );

    // Step 2: Apply remaining filters to the spatially pre-filtered set
    return stopPlacesFilter.filter(
      spatiallyFilteredStops,
      stopPlaceByQuayRefIndex,
      filters
    );
  }

  /**
   * Convert BoundingBoxFilterParams to JTS Polygon for spatial querying
   */
  private Polygon createPolygonFromBoundingBox(BoundingBoxFilterParams boundingBox) {
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

  @Override
  public Optional<Quay> getQuayById(String id) {
    return Optional.ofNullable(quayByQuayRefIndex.get(id));
  }

  @Override
  public List<StopPlace> getStopPlacesWithinPolygon(Polygon polygon) {
    if (polygon == null) {
      return new ArrayList<>();
    }

    // Step 1: Fast spatial query using bounding box
    @SuppressWarnings("unchecked")
    List<StopPlace> candidates = spatialIndex.query(polygon.getEnvelopeInternal());

    // Step 2: Precise polygon containment check
    return candidates
      .stream()
      .filter(stopPlace -> {
        Point point = createPointFromStopPlace(stopPlace);
        return point != null && polygon.contains(point);
      })
      .toList();
  }

  private void buildSpatialIndex() {
    logger.info("Building spatial index with {} stop places", allStopPlacesIndex.size());

    for (StopPlace stopPlace : allStopPlacesIndex) {
      Point point = createPointFromStopPlace(stopPlace);
      if (point != null) {
        spatialIndex.insert(point.getEnvelopeInternal(), stopPlace);
      }
    }

    spatialIndex.build();
    logger.info("Spatial index built successfully");
  }

  private Point createPointFromStopPlace(StopPlace stopPlace) {
    if (
      stopPlace.getCentroid() == null || stopPlace.getCentroid().getLocation() == null
    ) {
      return null;
    }

    var centroid = stopPlace.getCentroid().getLocation();
    double longitude = centroid.getLongitude().doubleValue();
    double latitude = centroid.getLatitude().doubleValue();

    return geometryFactory.createPoint(new Coordinate(longitude, latitude));
  }

  private void rebuildSpatialIndexAfterModification() {
    // STRtree doesn't support removal after build, so we need to create a new instance
    spatialIndex = new STRtree();
    buildSpatialIndex();
  }

  public void createStopPlace(String id, StopPlace stopPlace) {
    if (id == null || stopPlace == null) {
      throw new IllegalArgumentException("ID and StopPlace cannot be null");
    }

    stopPlace.setId(id);

    // Add to all stop places index
    allStopPlacesIndex.add(stopPlace);

    // Index quays if present
    Optional.ofNullable(stopPlace.getQuays()).ifPresent(quays ->
      quays.getQuayRefOrQuay().forEach(quayRefOrQuay -> {
        Quay quay = (Quay) quayRefOrQuay.getValue();
        stopPlaceByQuayRefIndex.put(quay.getId(), stopPlace);
        quayByQuayRefIndex.put(quay.getId(), quay);
      })
    );

    // Rebuild spatial index (STRtree doesn't support insertion after build)
    rebuildSpatialIndexAfterModification();

    logger.debug("Created stop place with id: {}", id);
  }

  public void updateStopPlace(String id, StopPlace stopPlace) {
    if (id == null || stopPlace == null) {
      throw new IllegalArgumentException("ID and StopPlace cannot be null");
    }

    // Remove old stop place
    StopPlace oldStopPlace = allStopPlacesIndex.stream()
      .filter(sp -> id.equals(sp.getId()))
      .findFirst()
      .orElse(null);

    if (oldStopPlace != null) {
      // Remove from all indexes
      allStopPlacesIndex.remove(oldStopPlace);

      // Remove old quay references
      Optional.ofNullable(oldStopPlace.getQuays()).ifPresent(quays ->
        quays.getQuayRefOrQuay().forEach(quayRefOrQuay -> {
          Quay quay = (Quay) quayRefOrQuay.getValue();
          stopPlaceByQuayRefIndex.remove(quay.getId());
          quayByQuayRefIndex.remove(quay.getId());
        })
      );

      // Remove from spatial index (note: STRtree doesn't support removal after build,
      // so we'll need to rebuild the index)
      rebuildSpatialIndexAfterModification();
    }

    // Add updated stop place
    stopPlace.setId(id);
    allStopPlacesIndex.add(stopPlace);

    // Index new quays
    Optional.ofNullable(stopPlace.getQuays()).ifPresent(quays ->
      quays.getQuayRefOrQuay().forEach(quayRefOrQuay -> {
        Quay quay = (Quay) quayRefOrQuay.getValue();
        stopPlaceByQuayRefIndex.put(quay.getId(), stopPlace);
        quayByQuayRefIndex.put(quay.getId(), quay);
      })
    );

    logger.debug("Updated stop place with id: {}", id);
  }

  public void deleteStopPlace(String id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }

    // Find and remove stop place
    StopPlace stopPlaceToRemove = allStopPlacesIndex.stream()
      .filter(sp -> id.equals(sp.getId()))
      .findFirst()
      .orElse(null);

    if (stopPlaceToRemove != null) {
      // Remove from all stop places index
      allStopPlacesIndex.remove(stopPlaceToRemove);

      // Remove quay references
      Optional.ofNullable(stopPlaceToRemove.getQuays()).ifPresent(quays ->
        quays.getQuayRefOrQuay().forEach(quayRefOrQuay -> {
          Quay quay = (Quay) quayRefOrQuay.getValue();
          stopPlaceByQuayRefIndex.remove(quay.getId());
          quayByQuayRefIndex.remove(quay.getId());
        })
      );

      // Rebuild spatial index (STRtree doesn't support removal after build)
      rebuildSpatialIndexAfterModification();

      logger.debug("Deleted stop place with id: {}", id);
    } else {
      logger.warn("Attempted to delete non-existent stop place with id: {}", id);
    }
  }

  public Instant getPublicationTime() {
    return publicationTime;
  }
}
