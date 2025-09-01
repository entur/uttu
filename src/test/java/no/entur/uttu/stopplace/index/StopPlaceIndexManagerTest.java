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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.Quays_RelStructure;
import org.rutebanken.netex.model.SiteRefStructure;
import org.rutebanken.netex.model.StopPlace;

class StopPlaceIndexManagerTest {

  private StopPlaceIndexManager indexManager;

  @BeforeEach
  void setUp() {
    indexManager = new StopPlaceIndexManager();
  }

  @Test
  void testAddStopPlace_withValidStopPlace_addsToAllIndexes() {
    StopPlace stopPlace = createStopPlace("NSR:StopPlace:1", "Oslo S");

    indexManager.addStopPlace(stopPlace);

    assertEquals(1, indexManager.getAllStopPlaces().size());
    assertTrue(indexManager.getStopPlaceById("NSR:StopPlace:1").isPresent());
    assertEquals(
      "Oslo S",
      indexManager.getStopPlaceById("NSR:StopPlace:1").get().getName().getValue()
    );
  }

  @Test
  void testAddStopPlace_withNullStopPlace_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> indexManager.addStopPlace(null));
  }

  @Test
  void testAddStopPlace_withNullId_throwsException() {
    StopPlace stopPlace = new StopPlace();
    assertThrows(
      IllegalArgumentException.class,
      () -> indexManager.addStopPlace(stopPlace)
    );
  }

  @Test
  void testUpdateStopPlace_updatesExistingStop() {
    StopPlace original = createStopPlace("NSR:StopPlace:1", "Oslo S");
    indexManager.addStopPlace(original);

    StopPlace updated = createStopPlace("NSR:StopPlace:1", "Oslo Sentralstasjon");
    indexManager.updateStopPlace("NSR:StopPlace:1", updated);

    assertEquals(1, indexManager.getAllStopPlaces().size());
    assertEquals(
      "Oslo Sentralstasjon",
      indexManager.getStopPlaceById("NSR:StopPlace:1").get().getName().getValue()
    );
  }

  @Test
  void testRemoveStopPlaceAndRelated_withParentAndChildren_removesAll() {
    // Create parent stop
    StopPlace parent = createStopPlace("NSR:StopPlace:1", "Oslo S");

    // Create child stops with parent reference
    StopPlace child1 = createStopPlace("NSR:StopPlace:2", "Oslo S - Platform 1");
    child1.setParentSiteRef(createParentRef("NSR:StopPlace:1"));

    StopPlace child2 = createStopPlace("NSR:StopPlace:3", "Oslo S - Platform 2");
    child2.setParentSiteRef(createParentRef("NSR:StopPlace:1"));

    // Add all stops
    indexManager.addStopPlace(parent);
    indexManager.addStopPlace(child1);
    indexManager.addStopPlace(child2);

    assertEquals(3, indexManager.getAllStopPlaces().size());

    // Remove parent should remove all
    List<String> removedIds = indexManager.removeStopPlaceAndRelated("NSR:StopPlace:1");

    assertEquals(3, removedIds.size());
    assertTrue(removedIds.contains("NSR:StopPlace:1"));
    assertTrue(removedIds.contains("NSR:StopPlace:2"));
    assertTrue(removedIds.contains("NSR:StopPlace:3"));
    assertEquals(0, indexManager.getAllStopPlaces().size());
  }

  @Test
  void testRemoveStopPlaceAndRelated_withOnlyParent_removesParent() {
    StopPlace parent = createStopPlace("NSR:StopPlace:1", "Oslo S");
    indexManager.addStopPlace(parent);

    List<String> removedIds = indexManager.removeStopPlaceAndRelated("NSR:StopPlace:1");

    assertEquals(1, removedIds.size());
    assertEquals("NSR:StopPlace:1", removedIds.get(0));
    assertEquals(0, indexManager.getAllStopPlaces().size());
  }

  @Test
  void testRemoveStopPlaceAndRelated_withNonExistentId_returnsEmptyList() {
    List<String> removedIds = indexManager.removeStopPlaceAndRelated("NSR:StopPlace:999");

    assertTrue(removedIds.isEmpty());
  }

  @Test
  void testParentChildRelationship_whenChildMovesToNewParent() {
    // Create two parents
    StopPlace parent1 = createStopPlace("NSR:StopPlace:1", "Parent 1");
    StopPlace parent2 = createStopPlace("NSR:StopPlace:2", "Parent 2");

    // Create child initially with parent1
    StopPlace child = createStopPlace("NSR:StopPlace:3", "Child");
    child.setParentSiteRef(createParentRef("NSR:StopPlace:1"));

    indexManager.addStopPlace(parent1);
    indexManager.addStopPlace(parent2);
    indexManager.addStopPlace(child);

    // Verify initial relationship
    List<String> removed1 = indexManager.removeStopPlaceAndRelated("NSR:StopPlace:1");
    assertEquals(2, removed1.size()); // parent1 and child

    // Re-add for next test
    indexManager.addStopPlace(parent1);
    indexManager.addStopPlace(parent2);

    // Update child to have parent2
    child.setParentSiteRef(createParentRef("NSR:StopPlace:2"));
    indexManager.updateStopPlace("NSR:StopPlace:3", child);

    // Now removing parent1 should only remove parent1
    List<String> removed2 = indexManager.removeStopPlaceAndRelated("NSR:StopPlace:1");
    assertEquals(1, removed2.size());

    // And removing parent2 should remove parent2 and child
    List<String> removed3 = indexManager.removeStopPlaceAndRelated("NSR:StopPlace:2");
    assertEquals(2, removed3.size());
  }

  @Test
  void testQuayIndexing_withQuays_indexesCorrectly() {
    StopPlace stopPlace = createStopPlace("NSR:StopPlace:1", "Oslo S");

    // Add quays
    Quay quay1 = new Quay();
    quay1.setId("NSR:Quay:1");

    Quay quay2 = new Quay();
    quay2.setId("NSR:Quay:2");

    Quays_RelStructure quays = new Quays_RelStructure();
    quays.getQuayRefOrQuay().add(createQuayRef(quay1));
    quays.getQuayRefOrQuay().add(createQuayRef(quay2));
    stopPlace.setQuays(quays);

    indexManager.addStopPlace(stopPlace);

    // Check quay lookups
    Optional<StopPlace> foundByQuay1 = indexManager.getStopPlaceByQuayRef("NSR:Quay:1");
    Optional<StopPlace> foundByQuay2 = indexManager.getStopPlaceByQuayRef("NSR:Quay:2");

    assertTrue(foundByQuay1.isPresent());
    assertTrue(foundByQuay2.isPresent());
    assertEquals("NSR:StopPlace:1", foundByQuay1.get().getId());
    assertEquals("NSR:StopPlace:1", foundByQuay2.get().getId());

    Optional<Quay> quay1Found = indexManager.getQuayById("NSR:Quay:1");
    assertTrue(quay1Found.isPresent());
    assertEquals("NSR:Quay:1", quay1Found.get().getId());
  }

  @Test
  void testLoadBulkData_clearsAndLoadsNewData() {
    // Add initial data
    StopPlace initial = createStopPlace("NSR:StopPlace:1", "Initial");
    indexManager.addStopPlace(initial);
    assertEquals(1, indexManager.getAllStopPlaces().size());

    // Load bulk data
    List<StopPlace> bulkData = List.of(
      createStopPlace("NSR:StopPlace:2", "Bulk 1"),
      createStopPlace("NSR:StopPlace:3", "Bulk 2"),
      createStopPlace("NSR:StopPlace:4", "Bulk 3")
    );

    indexManager.loadBulkData(bulkData);

    // Should have replaced all data
    assertEquals(3, indexManager.getAllStopPlaces().size());
    assertFalse(indexManager.getStopPlaceById("NSR:StopPlace:1").isPresent());
    assertTrue(indexManager.getStopPlaceById("NSR:StopPlace:2").isPresent());
    assertTrue(indexManager.getStopPlaceById("NSR:StopPlace:3").isPresent());
    assertTrue(indexManager.getStopPlaceById("NSR:StopPlace:4").isPresent());
  }

  @Test
  void testClear_removesAllData() {
    // Add test data
    StopPlace stop1 = createStopPlace("NSR:StopPlace:1", "Stop 1");
    StopPlace stop2 = createStopPlace("NSR:StopPlace:2", "Stop 2");
    indexManager.addStopPlace(stop1);
    indexManager.addStopPlace(stop2);

    assertEquals(2, indexManager.getAllStopPlaces().size());

    indexManager.clear();

    assertEquals(0, indexManager.getAllStopPlaces().size());
    assertFalse(indexManager.getStopPlaceById("NSR:StopPlace:1").isPresent());
    assertFalse(indexManager.getStopPlaceById("NSR:StopPlace:2").isPresent());
  }

  @Test
  void testComplexMultimodalStructure() {
    // Create a complex multimodal structure
    StopPlace mainStation = createStopPlace("NSR:StopPlace:1", "Central Station");

    StopPlace busTerminal = createStopPlace("NSR:StopPlace:2", "Bus Terminal");
    busTerminal.setParentSiteRef(createParentRef("NSR:StopPlace:1"));

    StopPlace metroStation = createStopPlace("NSR:StopPlace:3", "Metro Station");
    metroStation.setParentSiteRef(createParentRef("NSR:StopPlace:1"));

    StopPlace tramStop = createStopPlace("NSR:StopPlace:4", "Tram Stop");
    tramStop.setParentSiteRef(createParentRef("NSR:StopPlace:1"));

    // Add all stops
    indexManager.addStopPlace(mainStation);
    indexManager.addStopPlace(busTerminal);
    indexManager.addStopPlace(metroStation);
    indexManager.addStopPlace(tramStop);

    assertEquals(4, indexManager.getAllStopPlaces().size());

    // Remove parent should cascade to all children
    List<String> removedIds = indexManager.removeStopPlaceAndRelated("NSR:StopPlace:1");

    assertEquals(4, removedIds.size());
    assertEquals(0, indexManager.getAllStopPlaces().size());
  }

  @Test
  void testGetQuayIndex_returnsDefensiveCopy() {
    StopPlace stopPlace = createStopPlace("NSR:StopPlace:1", "Oslo S");
    Quay quay = new Quay();
    quay.setId("NSR:Quay:1");
    Quays_RelStructure quays = new Quays_RelStructure();
    quays.getQuayRefOrQuay().add(createQuayRef(quay));
    stopPlace.setQuays(quays);

    indexManager.addStopPlace(stopPlace);

    var quayIndex1 = indexManager.getQuayIndex();
    var quayIndex2 = indexManager.getQuayIndex();

    assertNotSame(quayIndex1, quayIndex2, "Should return different instances");
    assertEquals(quayIndex1, quayIndex2, "Should have same content");
  }

  // Helper methods
  private StopPlace createStopPlace(String id, String name) {
    StopPlace stopPlace = new StopPlace();
    stopPlace.setId(id);
    stopPlace.setName(new MultilingualString().withValue(name));
    return stopPlace;
  }

  private SiteRefStructure createParentRef(String parentId) {
    SiteRefStructure ref = new SiteRefStructure();
    ref.setRef(parentId);
    return ref;
  }

  private jakarta.xml.bind.JAXBElement<Object> createQuayRef(Quay quay) {
    return new jakarta.xml.bind.JAXBElement<>(
      new javax.xml.namespace.QName("http://www.netex.org.uk/netex", "Quay"),
      Object.class,
      quay
    );
  }
}
