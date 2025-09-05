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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.namespace.QName;
import no.entur.uttu.stopplace.filter.params.BoundingBoxFilterParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.rutebanken.netex.model.LocationStructure;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.Quays_RelStructure;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;
import org.rutebanken.netex.model.StopPlace;

class StopPlaceSpatialServiceTest {

  private StopPlaceSpatialService spatialService;
  private GeometryFactory geometryFactory;

  @BeforeEach
  void setUp() {
    spatialService = new StopPlaceSpatialService();
    geometryFactory = new GeometryFactory();
  }

  @Test
  void testBuildSpatialIndex_withValidStopPlaces_buildsIndex() {
    List<StopPlace> stopPlaces = List.of(
      createStopPlaceWithLocation("NSR:StopPlace:1", "Oslo S", 59.911491, 10.750375),
      createStopPlaceWithLocation("NSR:StopPlace:2", "Bergen", 60.391263, 5.322054),
      createStopPlaceWithLocation("NSR:StopPlace:3", "Trondheim", 63.436188, 10.398583)
    );

    spatialService.buildSpatialIndex(stopPlaces);

    // Test that index was built by querying it
    Polygon largePolygon = createPolygonAroundNorway();
    List<StopPlace> result = spatialService.getStopPlacesWithinPolygon(largePolygon);

    assertEquals(3, result.size());
  }

  @Test
  void testBuildSpatialIndex_withEmptyList_buildsEmptyIndex() {
    spatialService.buildSpatialIndex(List.of());

    Polygon anyPolygon = createSmallPolygonAroundOslo();
    List<StopPlace> result = spatialService.getStopPlacesWithinPolygon(anyPolygon);

    assertTrue(result.isEmpty());
  }

  @Test
  void testBuildSpatialIndex_withStopsWithoutQuays_doesNotIndexStopsWithoutQuays() {
    // Stop without quays (won't be indexed)
    StopPlace stopWithoutQuays = createStopPlace("NSR:StopPlace:1", "No Quays");

    List<StopPlace> stopPlaces = List.of(
      stopWithoutQuays,
      createStopPlaceWithLocation("NSR:StopPlace:2", "Oslo S", 59.911491, 10.750375)
    );

    spatialService.buildSpatialIndex(stopPlaces);

    Polygon largePolygon = createPolygonAroundNorway();
    List<StopPlace> result = spatialService.getStopPlacesWithinPolygon(largePolygon);

    // Only the stop with quays should be found
    assertEquals(1, result.size());
    assertEquals("NSR:StopPlace:2", result.get(0).getId());
  }

  @Test
  void testGetStopPlacesWithinPolygon_withNullPolygon_returnsEmptyList() {
    spatialService.buildSpatialIndex(
      List.of(
        createStopPlaceWithLocation("NSR:StopPlace:1", "Oslo S", 59.911491, 10.750375)
      )
    );

    List<StopPlace> result = spatialService.getStopPlacesWithinPolygon(null);

    assertTrue(result.isEmpty());
  }

  @Test
  void testGetStopPlacesWithinPolygon_withSmallPolygonAroundOslo_findsOsloStop() {
    List<StopPlace> stopPlaces = List.of(
      createStopPlaceWithLocation("NSR:StopPlace:1", "Oslo S", 59.911491, 10.750375),
      createStopPlaceWithLocation("NSR:StopPlace:2", "Bergen", 60.391263, 5.322054)
    );

    spatialService.buildSpatialIndex(stopPlaces);

    Polygon osloPolygon = createSmallPolygonAroundOslo();
    List<StopPlace> result = spatialService.getStopPlacesWithinPolygon(osloPolygon);

    assertEquals(1, result.size());
    assertEquals("NSR:StopPlace:1", result.get(0).getId());
    assertEquals("Oslo S", result.get(0).getName().getValue());
  }

  @Test
  void testGetStopPlacesWithinPolygon_withLargePolygon_findsAllStops() {
    // Test with more stop places to verify large polygon correctly includes multiple regions
    List<StopPlace> stopPlaces = List.of(
      createStopPlaceWithLocation("NSR:StopPlace:1", "Oslo S", 59.911491, 10.750375),
      createStopPlaceWithLocation("NSR:StopPlace:2", "Bergen", 60.391263, 5.322054),
      createStopPlaceWithLocation("NSR:StopPlace:3", "Trondheim", 63.436188, 10.398583),
      createStopPlaceWithLocation("NSR:StopPlace:4", "Stavanger", 58.969975, 5.733107),
      createStopPlaceWithLocation("NSR:StopPlace:5", "Tromsø", 69.649208, 18.955324)
    );

    spatialService.buildSpatialIndex(stopPlaces);

    Polygon largePolygon = createPolygonAroundNorway();
    List<StopPlace> result = spatialService.getStopPlacesWithinPolygon(largePolygon);

    // Verify all stops are found and check they're ordered correctly
    assertEquals(5, result.size());

    // Verify specific stops are included to ensure polygon boundaries work correctly
    assertTrue(
      result.stream().anyMatch(sp -> "NSR:StopPlace:1".equals(sp.getId())),
      "Oslo should be included"
    );
    assertTrue(
      result.stream().anyMatch(sp -> "NSR:StopPlace:2".equals(sp.getId())),
      "Bergen should be included"
    );
    assertTrue(
      result.stream().anyMatch(sp -> "NSR:StopPlace:3".equals(sp.getId())),
      "Trondheim should be included"
    );
    assertTrue(
      result.stream().anyMatch(sp -> "NSR:StopPlace:4".equals(sp.getId())),
      "Stavanger should be included"
    );
    assertTrue(
      result.stream().anyMatch(sp -> "NSR:StopPlace:5".equals(sp.getId())),
      "Tromsø should be included"
    );
  }

  @Test
  void testGetStopPlacesWithinPolygon_withPolygonNotContainingAnyStops_returnsEmpty() {
    List<StopPlace> stopPlaces = List.of(
      createStopPlaceWithLocation("NSR:StopPlace:1", "Oslo S", 59.911491, 10.750375)
    );

    spatialService.buildSpatialIndex(stopPlaces);

    // Create polygon in the middle of the ocean
    Polygon oceanPolygon = geometryFactory.createPolygon(
      new Coordinate[] {
        new Coordinate(0.0, 0.0),
        new Coordinate(1.0, 0.0),
        new Coordinate(1.0, 1.0),
        new Coordinate(0.0, 1.0),
        new Coordinate(0.0, 0.0),
      }
    );

    List<StopPlace> result = spatialService.getStopPlacesWithinPolygon(oceanPolygon);

    assertTrue(result.isEmpty());
  }

  @Test
  void testGetStopPlacesWithinPolygon_beforeIndexBuilt_returnsEmpty() {
    // Don't build index
    Polygon anyPolygon = createSmallPolygonAroundOslo();
    List<StopPlace> result = spatialService.getStopPlacesWithinPolygon(anyPolygon);

    assertTrue(result.isEmpty());
  }

  @Test
  void testPreFilterByBoundingBox_withValidBoundingBox_filtersCorrectly() {
    List<StopPlace> allStopPlaces = List.of(
      createStopPlaceWithLocation("NSR:StopPlace:1", "Oslo S", 59.911491, 10.750375),
      createStopPlaceWithLocation("NSR:StopPlace:2", "Bergen", 60.391263, 5.322054),
      createStopPlaceWithLocation("NSR:StopPlace:3", "Trondheim", 63.436188, 10.398583)
    );

    spatialService.buildSpatialIndex(allStopPlaces);

    // Create bounding box around Oslo area
    BoundingBoxFilterParams osloBox = new BoundingBoxFilterParams(
      BigDecimal.valueOf(59.920), // NE Lat
      BigDecimal.valueOf(10.760), // NE Lng
      BigDecimal.valueOf(59.900), // SW Lat
      BigDecimal.valueOf(10.740) // SW Lng
    );

    List<StopPlace> result = spatialService.preFilterByBoundingBox(
      allStopPlaces,
      osloBox
    );

    assertEquals(1, result.size());
    assertEquals("NSR:StopPlace:1", result.get(0).getId());
  }

  @Test
  void testPreFilterByBoundingBox_withNullBoundingBox_returnsOriginalList() {
    List<StopPlace> allStopPlaces = List.of(
      createStopPlaceWithLocation("NSR:StopPlace:1", "Oslo S", 59.911491, 10.750375)
    );

    List<StopPlace> result = spatialService.preFilterByBoundingBox(allStopPlaces, null);

    assertEquals(allStopPlaces, result);
  }

  @Test
  void testCreatePolygonFromBoundingBox_withValidBoundingBox_createsCorrectPolygon() {
    BoundingBoxFilterParams boundingBox = new BoundingBoxFilterParams(
      BigDecimal.valueOf(60.0), // NE Lat
      BigDecimal.valueOf(11.0), // NE Lng
      BigDecimal.valueOf(59.0), // SW Lat
      BigDecimal.valueOf(10.0) // SW Lng
    );

    Polygon result = spatialService.createPolygonFromBoundingBox(boundingBox);

    assertNotNull(result);
    assertEquals(5, result.getCoordinates().length); // 4 corners + closing coordinate

    // Check corners (note: coordinates are in lng, lat order)
    Coordinate[] coords = result.getCoordinates();
    assertEquals(new Coordinate(10.0, 59.0), coords[0]); // SW
    assertEquals(new Coordinate(11.0, 59.0), coords[1]); // SE
    assertEquals(new Coordinate(11.0, 60.0), coords[2]); // NE
    assertEquals(new Coordinate(10.0, 60.0), coords[3]); // NW
    assertEquals(new Coordinate(10.0, 59.0), coords[4]); // Close ring
  }

  @Test
  void testCreatePolygonFromBoundingBox_withNullBoundingBox_throwsException() {
    assertThrows(
      IllegalArgumentException.class,
      () -> spatialService.createPolygonFromBoundingBox(null)
    );
  }

  @Test
  void testConcurrentAccess_readWhileBuilding_isThreadSafe() throws Exception {
    List<StopPlace> stopPlaces = List.of(
      createStopPlaceWithLocation("NSR:StopPlace:1", "Oslo S", 59.911491, 10.750375)
    );

    ExecutorService executor = Executors.newFixedThreadPool(10);

    try {
      // Start building index
      CompletableFuture<Void> buildFuture = CompletableFuture.runAsync(
        () -> spatialService.buildSpatialIndex(stopPlaces),
        executor
      );

      // Simultaneously try to read from index
      CompletableFuture<List<StopPlace>>[] readFutures = new CompletableFuture[50];
      for (int i = 0; i < readFutures.length; i++) {
        readFutures[i] = CompletableFuture.supplyAsync(
          () -> {
            Polygon polygon = createSmallPolygonAroundOslo();
            return spatialService.getStopPlacesWithinPolygon(polygon);
          },
          executor
        );
      }

      // Wait for all operations to complete
      buildFuture.get();
      CompletableFuture.allOf(readFutures).get();

      // Verify all read operations completed without exception
      for (CompletableFuture<List<StopPlace>> future : readFutures) {
        List<StopPlace> result = future.get();
        // Result should be either empty (if read before build) or contain 1 stop (if read after build)
        assertTrue(result.size() <= 1);
      }
    } finally {
      executor.shutdown();
    }
  }

  @Test
  void testBuildSpatialIndex_withMultipleQuaysPerStop_indexesAllQuays() {
    // Create a stop place with multiple quays at slightly different locations
    StopPlace stopWithMultipleQuays = createStopPlace(
      "NSR:StopPlace:1",
      "Central Station"
    );

    Quays_RelStructure quays = new Quays_RelStructure();

    // First quay
    Quay quay1 = new Quay();
    quay1.setId("NSR:StopPlace:1:Quay:1");
    SimplePoint_VersionStructure centroid1 = new SimplePoint_VersionStructure();
    LocationStructure location1 = new LocationStructure();
    location1.setLatitude(BigDecimal.valueOf(59.9114));
    location1.setLongitude(BigDecimal.valueOf(10.7503));
    centroid1.setLocation(location1);
    quay1.setCentroid(centroid1);

    // Second quay (slightly offset)
    Quay quay2 = new Quay();
    quay2.setId("NSR:StopPlace:1:Quay:2");
    SimplePoint_VersionStructure centroid2 = new SimplePoint_VersionStructure();
    LocationStructure location2 = new LocationStructure();
    location2.setLatitude(BigDecimal.valueOf(59.9115));
    location2.setLongitude(BigDecimal.valueOf(10.7504));
    centroid2.setLocation(location2);
    quay2.setCentroid(centroid2);

    quays
      .getQuayRefOrQuay()
      .add(
        new JAXBElement<>(
          new QName("http://www.netex.org.uk/netex", "Quay"),
          Quay.class,
          quay1
        )
      );
    quays
      .getQuayRefOrQuay()
      .add(
        new JAXBElement<>(
          new QName("http://www.netex.org.uk/netex", "Quay"),
          Quay.class,
          quay2
        )
      );

    stopWithMultipleQuays.setQuays(quays);

    spatialService.buildSpatialIndex(List.of(stopWithMultipleQuays));

    // Query with polygon that includes both quays
    Polygon polygon = createSmallPolygonAroundOslo();
    List<StopPlace> result = spatialService.getStopPlacesWithinPolygon(polygon);

    // Should find the stop place (once, even though it has multiple quays)
    assertEquals(1, result.size());
    assertEquals("NSR:StopPlace:1", result.get(0).getId());
  }

  @Test
  void testMultipleIndexRebuilds_maintainsCorrectState() {
    // Initial build
    List<StopPlace> firstSet = List.of(
      createStopPlaceWithLocation("NSR:StopPlace:1", "Oslo S", 59.911491, 10.750375)
    );
    spatialService.buildSpatialIndex(firstSet);

    Polygon osloPolygon = createSmallPolygonAroundOslo();
    List<StopPlace> firstResult = spatialService.getStopPlacesWithinPolygon(osloPolygon);
    assertEquals(1, firstResult.size());

    // Rebuild with different data
    List<StopPlace> secondSet = List.of(
      createStopPlaceWithLocation("NSR:StopPlace:2", "Bergen", 60.391263, 5.322054),
      createStopPlaceWithLocation("NSR:StopPlace:3", "Trondheim", 63.436188, 10.398583)
    );
    spatialService.buildSpatialIndex(secondSet);

    // Oslo polygon should now return empty (no Oslo stop in new data)
    List<StopPlace> secondResult = spatialService.getStopPlacesWithinPolygon(osloPolygon);
    assertTrue(secondResult.isEmpty());

    // But large polygon should find the new stops
    Polygon largePolygon = createPolygonAroundNorway();
    List<StopPlace> allResult = spatialService.getStopPlacesWithinPolygon(largePolygon);
    assertEquals(2, allResult.size());
  }

  // Helper methods
  private StopPlace createStopPlace(String id, String name) {
    StopPlace stopPlace = new StopPlace();
    stopPlace.setId(id);
    stopPlace.setName(new MultilingualString().withValue(name));
    return stopPlace;
  }

  private StopPlace createStopPlaceWithLocation(
    String id,
    String name,
    double lat,
    double lng
  ) {
    StopPlace stopPlace = createStopPlace(id, name);

    // Create a quay with the location
    Quay quay = new Quay();
    quay.setId(id + ":Quay:1");
    quay.setName(new MultilingualString().withValue(name + " Quay"));

    // Set centroid on the quay
    SimplePoint_VersionStructure quayCentroid = new SimplePoint_VersionStructure();
    LocationStructure quayLocation = new LocationStructure();
    quayLocation.setLatitude(BigDecimal.valueOf(lat));
    quayLocation.setLongitude(BigDecimal.valueOf(lng));
    quayCentroid.setLocation(quayLocation);
    quay.setCentroid(quayCentroid);

    // Add quay to stop place
    Quays_RelStructure quays = new Quays_RelStructure();
    JAXBElement<Quay> quayElement = new JAXBElement<>(
      new QName("http://www.netex.org.uk/netex", "Quay"),
      Quay.class,
      quay
    );
    quays.getQuayRefOrQuay().add(quayElement);
    stopPlace.setQuays(quays);

    return stopPlace;
  }

  private Polygon createSmallPolygonAroundOslo() {
    // Small polygon around Oslo S coordinates (59.911491, 10.750375)
    return geometryFactory.createPolygon(
      new Coordinate[] {
        new Coordinate(10.745, 59.905), // SW
        new Coordinate(10.755, 59.905), // SE
        new Coordinate(10.755, 59.915), // NE
        new Coordinate(10.745, 59.915), // NW
        new Coordinate(10.745, 59.905), // Close ring
      }
    );
  }

  private Polygon createPolygonAroundNorway() {
    // Large polygon covering most of Norway
    return geometryFactory.createPolygon(
      new Coordinate[] {
        new Coordinate(4.0, 58.0), // SW
        new Coordinate(32.0, 58.0), // SE
        new Coordinate(32.0, 72.0), // NE
        new Coordinate(4.0, 72.0), // NW
        new Coordinate(4.0, 58.0), // Close ring
      }
    );
  }
}
