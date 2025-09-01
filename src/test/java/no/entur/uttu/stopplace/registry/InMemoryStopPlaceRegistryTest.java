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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import no.entur.uttu.stopplace.filter.StopPlacesFilter;
import no.entur.uttu.stopplace.index.StopPlaceIndexManager;
import no.entur.uttu.stopplace.spatial.StopPlaceSpatialService;
import no.entur.uttu.stopplace.spi.StopPlaceDataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.SiteRefStructure;
import org.rutebanken.netex.model.StopPlace;

@ExtendWith(MockitoExtension.class)
class InMemoryStopPlaceRegistryTest {

  @Mock
  private StopPlaceIndexManager indexManager;

  @Mock
  private StopPlaceSpatialService spatialService;

  @Mock
  private StopPlacesFilter stopPlacesFilter;

  @Mock
  private StopPlaceDataLoader dataLoader;

  private InMemoryStopPlaceRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new InMemoryStopPlaceRegistry(
      indexManager,
      spatialService,
      stopPlacesFilter,
      Optional.empty()
    );
  }

  @Test
  void testCreateOrUpdateStopPlaces_withNewStops_createsAll() {
    List<StopPlace> stopPlaces = List.of(
      createStopPlace("NSR:StopPlace:1", "Stop 1"),
      createStopPlace("NSR:StopPlace:2", "Stop 2"),
      createStopPlace("NSR:StopPlace:3", "Stop 3")
    );

    // All stops are new
    when(indexManager.getStopPlaceById(anyString())).thenReturn(Optional.empty());

    registry.createOrUpdateStopPlaces(stopPlaces);

    // Verify all stops were added
    verify(indexManager, times(3)).addStopPlace(any(StopPlace.class));
    verify(indexManager, never()).updateStopPlace(anyString(), any(StopPlace.class));

    // Verify spatial index was rebuilt once
    verify(spatialService, times(1)).buildSpatialIndex(anyList());
  }

  @Test
  void testCreateOrUpdateStopPlaces_withExistingStops_updatesAll() {
    List<StopPlace> stopPlaces = List.of(
      createStopPlace("NSR:StopPlace:1", "Stop 1"),
      createStopPlace("NSR:StopPlace:2", "Stop 2")
    );

    // All stops exist
    when(indexManager.getStopPlaceById(anyString())).thenReturn(
      Optional.of(new StopPlace())
    );

    registry.createOrUpdateStopPlaces(stopPlaces);

    // Verify all stops were updated
    verify(indexManager, never()).addStopPlace(any(StopPlace.class));
    verify(indexManager, times(2)).updateStopPlace(anyString(), any(StopPlace.class));

    // Verify spatial index was rebuilt once
    verify(spatialService, times(1)).buildSpatialIndex(anyList());
  }

  @Test
  void testCreateOrUpdateStopPlaces_withMixedStops_createsAndUpdates() {
    // Create parent (new) and children (existing)
    StopPlace parent = createStopPlace("NSR:StopPlace:1", "Parent Station");

    StopPlace child1 = createStopPlace("NSR:StopPlace:2", "Child 1");
    child1.setParentSiteRef(createParentRef("NSR:StopPlace:1"));

    StopPlace child2 = createStopPlace("NSR:StopPlace:3", "Child 2");
    child2.setParentSiteRef(createParentRef("NSR:StopPlace:1"));

    List<StopPlace> stopPlaces = List.of(parent, child1, child2);

    // Parent is new, children exist
    when(indexManager.getStopPlaceById("NSR:StopPlace:1")).thenReturn(Optional.empty());
    when(indexManager.getStopPlaceById("NSR:StopPlace:2")).thenReturn(
      Optional.of(createStopPlace("NSR:StopPlace:2", "Old Child 1"))
    );
    when(indexManager.getStopPlaceById("NSR:StopPlace:3")).thenReturn(
      Optional.of(createStopPlace("NSR:StopPlace:3", "Old Child 2"))
    );

    registry.createOrUpdateStopPlaces(stopPlaces);

    // Verify parent was created
    verify(indexManager, times(1)).addStopPlace(
      argThat(sp -> "NSR:StopPlace:1".equals(sp.getId()))
    );

    // Verify children were updated
    verify(indexManager, times(1)).updateStopPlace(
      eq("NSR:StopPlace:2"),
      argThat(sp -> "Child 1".equals(sp.getName().getValue()))
    );
    verify(indexManager, times(1)).updateStopPlace(
      eq("NSR:StopPlace:3"),
      argThat(sp -> "Child 2".equals(sp.getName().getValue()))
    );

    // Verify spatial index was rebuilt once
    verify(spatialService, times(1)).buildSpatialIndex(anyList());
  }

  @Test
  void testCreateOrUpdateStopPlaces_withEmptyList_doesNothing() {
    registry.createOrUpdateStopPlaces(List.of());

    verify(indexManager, never()).addStopPlace(any());
    verify(indexManager, never()).updateStopPlace(anyString(), any());
    verify(spatialService, never()).buildSpatialIndex(anyList());
  }

  @Test
  void testCreateOrUpdateStopPlaces_withNullList_doesNothing() {
    registry.createOrUpdateStopPlaces(null);

    verify(indexManager, never()).addStopPlace(any());
    verify(indexManager, never()).updateStopPlace(anyString(), any());
    verify(spatialService, never()).buildSpatialIndex(anyList());
  }

  @Test
  void testCreateOrUpdateStopPlaces_withNullStopsInList_skipsNulls() {
    List<StopPlace> stopPlaces = List.of(
      createStopPlace("NSR:StopPlace:1", "Stop 1")
      // Can't actually add null to List.of(), but we can test with empty ID
    );

    StopPlace stopWithoutId = new StopPlace();
    stopWithoutId.setName(new MultilingualString().withValue("No ID"));

    List<StopPlace> mixedList = List.of(stopPlaces.get(0), stopWithoutId);

    when(indexManager.getStopPlaceById("NSR:StopPlace:1")).thenReturn(Optional.empty());

    registry.createOrUpdateStopPlaces(mixedList);

    // Only the valid stop should be processed
    verify(indexManager, times(1)).addStopPlace(any());
    verify(spatialService, times(1)).buildSpatialIndex(anyList());
  }

  @Test
  void testDeleteStopPlaceAndRelated_callsIndexManager() {
    when(indexManager.removeStopPlaceAndRelated("NSR:StopPlace:1")).thenReturn(
      List.of("NSR:StopPlace:1", "NSR:StopPlace:2", "NSR:StopPlace:3")
    );
    when(indexManager.getAllStopPlaces()).thenReturn(List.of());

    registry.deleteStopPlaceAndRelated("NSR:StopPlace:1");

    verify(indexManager).removeStopPlaceAndRelated("NSR:StopPlace:1");
    verify(spatialService).buildSpatialIndex(anyList());
  }

  @Test
  void testDeleteStopPlaceAndRelated_withNullId_throwsException() {
    assertThrows(
      IllegalArgumentException.class,
      () -> registry.deleteStopPlaceAndRelated(null)
    );
  }

  @Test
  void testInit_withDataLoader_loadsData() {
    StopPlaceDataLoader.LoadResult loadResult = new StopPlaceDataLoader.LoadResult(
      List.of(
        createStopPlace("NSR:StopPlace:1", "Stop 1"),
        createStopPlace("NSR:StopPlace:2", "Stop 2")
      ),
      Instant.now()
    );

    when(dataLoader.loadStopPlaces()).thenReturn(loadResult);

    InMemoryStopPlaceRegistry registryWithLoader = new InMemoryStopPlaceRegistry(
      indexManager,
      spatialService,
      stopPlacesFilter,
      Optional.of(dataLoader)
    );

    registryWithLoader.init();

    verify(dataLoader).loadStopPlaces();
    verify(indexManager).loadBulkData(loadResult.stopPlaces());
    verify(spatialService).buildSpatialIndex(loadResult.stopPlaces());
    assertEquals(loadResult.publicationTime(), registryWithLoader.getPublicationTime());
  }

  @Test
  void testInit_withDataLoaderException_logsError() {
    when(dataLoader.loadStopPlaces()).thenThrow(new RuntimeException("Load failed"));

    InMemoryStopPlaceRegistry registryWithLoader = new InMemoryStopPlaceRegistry(
      indexManager,
      spatialService,
      stopPlacesFilter,
      Optional.of(dataLoader)
    );

    // Should not throw, just log
    assertDoesNotThrow(() -> registryWithLoader.init());

    verify(dataLoader).loadStopPlaces();
    verify(indexManager, never()).loadBulkData(any());
    verify(spatialService, never()).buildSpatialIndex(any());
  }

  @Test
  void testGetStopPlaceByQuayRef_delegatesToIndexManager() {
    StopPlace expected = createStopPlace("NSR:StopPlace:1", "Stop 1");
    when(indexManager.getStopPlaceByQuayRef("NSR:Quay:1")).thenReturn(
      Optional.of(expected)
    );

    Optional<StopPlace> result = registry.getStopPlaceByQuayRef("NSR:Quay:1");

    assertTrue(result.isPresent());
    assertEquals(expected, result.get());
    verify(indexManager).getStopPlaceByQuayRef("NSR:Quay:1");
  }

  @Test
  void testPublicationTime_canBeSetAndRetrieved() {
    Instant now = Instant.now();

    registry.setPublicationTime(now);

    assertEquals(now, registry.getPublicationTime());
  }

  @Test
  void testCreateOrUpdateStopPlaces_batchPerformance() {
    // Create a large batch of stops
    List<StopPlace> largeBatch = new java.util.ArrayList<>();
    for (int i = 1; i <= 100; i++) {
      largeBatch.add(createStopPlace("NSR:StopPlace:" + i, "Stop " + i));
    }

    // Half exist, half are new
    when(
      indexManager.getStopPlaceById(
        argThat(id -> {
          if (id == null) return false;
          int num = Integer.parseInt(id.split(":")[2]);
          return num <= 50;
        })
      )
    ).thenReturn(Optional.of(new StopPlace()));

    when(
      indexManager.getStopPlaceById(
        argThat(id -> {
          if (id == null) return false;
          int num = Integer.parseInt(id.split(":")[2]);
          return num > 50;
        })
      )
    ).thenReturn(Optional.empty());

    registry.createOrUpdateStopPlaces(largeBatch);

    // Verify correct number of creates and updates
    verify(indexManager, times(50)).addStopPlace(any());
    verify(indexManager, times(50)).updateStopPlace(anyString(), any());

    // Verify spatial index was only rebuilt once (not 100 times!)
    verify(spatialService, times(1)).buildSpatialIndex(anyList());
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
}
