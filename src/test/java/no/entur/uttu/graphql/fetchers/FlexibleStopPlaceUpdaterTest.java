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

package no.entur.uttu.graphql.fetchers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import no.entur.uttu.graphql.mappers.FlexibleStopPlaceMapper;
import no.entur.uttu.model.FlexibleArea;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.ValidationResult;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.repository.FlexibleStopPlaceRepository;
import no.entur.uttu.repository.StopPointInJourneyPatternRepository;
import no.entur.uttu.service.FlexibleAreaValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlexibleStopPlaceUpdaterTest {

  @Mock
  private FlexibleStopPlaceMapper mapper;

  @Mock
  private FlexibleStopPlaceRepository repository;

  @Mock
  private StopPointInJourneyPatternRepository stopPointInJourneyPatternRepository;

  @Mock
  private FlexibleAreaValidationService flexibleAreaValidationService;

  @Mock
  private DataFetchingEnvironment environment;

  private FlexibleStopPlaceUpdater updater;

  @BeforeEach
  void setUp() throws Exception {
    updater =
      new FlexibleStopPlaceUpdater(
        mapper,
        repository,
        stopPointInJourneyPatternRepository,
        flexibleAreaValidationService
      );

    // Use reflection to set the private field
    Field field =
      FlexibleStopPlaceUpdater.class.getDeclaredField("flexibleAreaValidationService");
    field.setAccessible(true);
    field.set(updater, flexibleAreaValidationService);
  }

  @Test
  void shouldSaveValidFlexibleStopPlace() {
    // Arrange
    Map<String, Object> input = new HashMap<>();
    FlexibleStopPlace flexibleStopPlace = createValidFlexibleStopPlace();

    when(environment.getArgument("input")).thenReturn(input);
    when(mapper.map(input)).thenReturn(flexibleStopPlace);
    when(flexibleAreaValidationService.validateFlexibleStopPlace(flexibleStopPlace))
      .thenReturn(ValidationResult.valid());
    when(repository.save(flexibleStopPlace)).thenReturn(flexibleStopPlace);

    // Act
    FlexibleStopPlace result = updater.saveEntity(environment);

    // Assert
    assertNotNull(result);
    verify(flexibleAreaValidationService).validateFlexibleStopPlace(flexibleStopPlace);
    verify(repository).save(flexibleStopPlace);
  }

  @Test
  void shouldThrowExceptionForInvalidFlexibleStopPlace() {
    // Arrange
    Map<String, Object> input = new HashMap<>();
    FlexibleStopPlace flexibleStopPlace = createValidFlexibleStopPlace();

    when(environment.getArgument("input")).thenReturn(input);
    when(mapper.map(input)).thenReturn(flexibleStopPlace);
    when(flexibleAreaValidationService.validateFlexibleStopPlace(flexibleStopPlace))
      .thenReturn(ValidationResult.invalid("Test validation error"));

    // Act & Assert
    CodedIllegalArgumentException exception = assertThrows(
      CodedIllegalArgumentException.class,
      () -> updater.saveEntity(environment)
    );

    assertTrue(exception.getMessage().contains("Flexible stop place validation failed"));
    assertTrue(exception.getMessage().contains("Test validation error"));
    verify(flexibleAreaValidationService).validateFlexibleStopPlace(flexibleStopPlace);
    verify(repository, never()).save(any());
  }

  @Test
  void shouldSaveFlexibleStopPlaceWithWarnings() {
    // Arrange
    Map<String, Object> input = new HashMap<>();
    FlexibleStopPlace flexibleStopPlace = createValidFlexibleStopPlace();

    when(environment.getArgument("input")).thenReturn(input);
    when(mapper.map(input)).thenReturn(flexibleStopPlace);
    when(flexibleAreaValidationService.validateFlexibleStopPlace(flexibleStopPlace))
      .thenReturn(ValidationResult.withWarnings("Test warning"));
    when(repository.save(flexibleStopPlace)).thenReturn(flexibleStopPlace);

    // Act
    FlexibleStopPlace result = updater.saveEntity(environment);

    // Assert
    assertNotNull(result);
    verify(flexibleAreaValidationService).validateFlexibleStopPlace(flexibleStopPlace);
    verify(repository).save(flexibleStopPlace);
  }

  private FlexibleStopPlace createValidFlexibleStopPlace() {
    FlexibleStopPlace flexibleStopPlace = new FlexibleStopPlace();
    flexibleStopPlace.setTransportMode(VehicleModeEnumeration.BUS);

    // Add a FlexibleArea to satisfy checkPersistable
    FlexibleArea flexibleArea = new FlexibleArea();
    GeometryFactory geometryFactory = new GeometryFactory();
    Coordinate[] coordinates = {
      new Coordinate(10.0, 60.0),
      new Coordinate(11.0, 60.0),
      new Coordinate(11.0, 61.0),
      new Coordinate(10.0, 61.0),
      new Coordinate(10.0, 60.0),
    };
    Polygon polygon = geometryFactory.createPolygon(coordinates);
    flexibleArea.setPolygon(polygon);
    flexibleArea.setFlexibleStopPlace(flexibleStopPlace);

    flexibleStopPlace.setFlexibleAreas(List.of(flexibleArea));
    return flexibleStopPlace;
  }
}
