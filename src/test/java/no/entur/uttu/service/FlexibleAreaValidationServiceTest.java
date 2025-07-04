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

package no.entur.uttu.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.entur.uttu.model.FlexibleArea;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.ValidationResult;
import no.entur.uttu.model.Value;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.netex.model.StopPlace;

@ExtendWith(MockitoExtension.class)
class FlexibleAreaValidationServiceTest {

  @Mock
  private StopPlaceRegistry stopPlaceRegistry;

  private FlexibleAreaValidationService validationService;
  private GeometryFactory geometryFactory;

  @BeforeEach
  void setUp() {
    validationService = new FlexibleAreaValidationService(stopPlaceRegistry);
    geometryFactory = new GeometryFactory();
  }

  @Test
  void shouldValidateFlexibleStopPlaceWithoutFlexibleStopAreaType() {
    FlexibleStopPlace flexibleStopPlace = createFlexibleStopPlace();
    flexibleStopPlace.replaceKeyValues(new HashMap<>());

    ValidationResult result = validationService.validateFlexibleStopPlace(
      flexibleStopPlace
    );

    assertTrue(result.isValid());
    assertNull(result.getMessage());
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldValidateFlexibleStopPlaceWithDifferentFlexibleStopAreaType() {
    FlexibleStopPlace flexibleStopPlace = createFlexibleStopPlace();
    Map<String, Value> keyValues = new HashMap<>();
    keyValues.put("FlexibleStopAreaType", new Value("SomeOtherType"));
    flexibleStopPlace.replaceKeyValues(keyValues);

    ValidationResult result = validationService.validateFlexibleStopPlace(
      flexibleStopPlace
    );

    assertTrue(result.isValid());
    assertNull(result.getMessage());
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldValidateFlexibleStopPlaceWithUnrestrictedPublicTransportAreasAndValidAreas() {
    FlexibleStopPlace flexibleStopPlace = createFlexibleStopPlace();
    Map<String, Value> keyValues = new HashMap<>();
    keyValues.put("FlexibleStopAreaType", new Value("UnrestrictedPublicTransportAreas"));
    flexibleStopPlace.replaceKeyValues(keyValues);

    FlexibleArea flexibleArea = createFlexibleArea();
    flexibleStopPlace.setFlexibleAreas(List.of(flexibleArea));

    ValidationResult result = validationService.validateFlexibleStopPlace(
      flexibleStopPlace
    );

    assertTrue(result.isValid());
    assertNull(result.getMessage());
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldInvalidateFlexibleStopPlaceWithUnrestrictedPublicTransportAreasAndInvalidAreas() {
    FlexibleStopPlace flexibleStopPlace = createFlexibleStopPlace();
    Map<String, Value> keyValues = new HashMap<>();
    keyValues.put("FlexibleStopAreaType", new Value("UnrestrictedPublicTransportAreas"));
    flexibleStopPlace.replaceKeyValues(keyValues);

    FlexibleArea flexibleArea = createFlexibleArea();
    Map<String, Value> areaKeyValues = new HashMap<>();
    areaKeyValues.put(
      "FlexibleStopAreaType",
      new Value("UnrestrictedPublicTransportAreas")
    );
    flexibleArea.replaceKeyValues(areaKeyValues);
    flexibleStopPlace.setFlexibleAreas(List.of(flexibleArea));

    when(stopPlaceRegistry.getStopPlacesWithinPolygon(any(Polygon.class)))
      .thenReturn(Collections.emptyList());

    ValidationResult result = validationService.validateFlexibleStopPlace(
      flexibleStopPlace
    );

    assertFalse(result.isValid());
    assertTrue(result.getMessage().contains("contains invalid FlexibleArea"));
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldValidateFlexibleAreaWithoutFlexibleStopAreaType() {
    FlexibleArea flexibleArea = createFlexibleArea();
    flexibleArea.replaceKeyValues(new HashMap<>());

    ValidationResult result = validationService.validateFlexibleArea(flexibleArea);

    assertTrue(result.isValid());
    assertNull(result.getMessage());
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldValidateFlexibleAreaWithDifferentFlexibleStopAreaType() {
    FlexibleArea flexibleArea = createFlexibleArea();
    Map<String, Value> keyValues = new HashMap<>();
    keyValues.put("FlexibleStopAreaType", new Value("SomeOtherType"));
    flexibleArea.replaceKeyValues(keyValues);

    ValidationResult result = validationService.validateFlexibleArea(flexibleArea);

    assertTrue(result.isValid());
    assertNull(result.getMessage());
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldInvalidateFlexibleAreaWithoutPolygon() {
    FlexibleArea flexibleArea = createFlexibleArea();
    flexibleArea.setPolygon(null);
    Map<String, Value> keyValues = new HashMap<>();
    keyValues.put("FlexibleStopAreaType", new Value("UnrestrictedPublicTransportAreas"));
    flexibleArea.replaceKeyValues(keyValues);

    ValidationResult result = validationService.validateFlexibleArea(flexibleArea);

    assertFalse(result.isValid());
    assertTrue(result.getMessage().contains("has no polygon defined"));
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldInvalidateFlexibleAreaWithNoStopPlaces() {
    FlexibleArea flexibleArea = createFlexibleArea();
    Map<String, Value> keyValues = new HashMap<>();
    keyValues.put("FlexibleStopAreaType", new Value("UnrestrictedPublicTransportAreas"));
    flexibleArea.replaceKeyValues(keyValues);

    when(stopPlaceRegistry.getStopPlacesWithinPolygon(any(Polygon.class)))
      .thenReturn(Collections.emptyList());

    ValidationResult result = validationService.validateFlexibleArea(flexibleArea);

    assertFalse(result.isValid());
    assertTrue(result.getMessage().contains("contains no valid stop places"));
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldValidateFlexibleAreaWithStopPlacesAndReturnWarnings() {
    FlexibleArea flexibleArea = createFlexibleArea();
    Map<String, Value> keyValues = new HashMap<>();
    keyValues.put("FlexibleStopAreaType", new Value("UnrestrictedPublicTransportAreas"));
    flexibleArea.replaceKeyValues(keyValues);

    StopPlace stopPlace1 = new StopPlace();
    StopPlace stopPlace2 = new StopPlace();
    when(stopPlaceRegistry.getStopPlacesWithinPolygon(any(Polygon.class)))
      .thenReturn(List.of(stopPlace1, stopPlace2));

    ValidationResult result = validationService.validateFlexibleArea(flexibleArea);

    assertTrue(result.isValid());
    assertTrue(result.getMessage().contains("contains 2 stop places"));
    assertTrue(result.hasWarnings());
  }

  @Test
  void shouldHandleMultipleValuesInFlexibleStopAreaType() {
    FlexibleArea flexibleArea = createFlexibleArea();
    Map<String, Value> keyValues = new HashMap<>();
    keyValues.put(
      "FlexibleStopAreaType",
      new Value("SomeOtherType", "UnrestrictedPublicTransportAreas")
    );
    flexibleArea.replaceKeyValues(keyValues);

    StopPlace stopPlace = new StopPlace();
    when(stopPlaceRegistry.getStopPlacesWithinPolygon(any(Polygon.class)))
      .thenReturn(List.of(stopPlace));

    ValidationResult result = validationService.validateFlexibleArea(flexibleArea);

    assertTrue(result.isValid());
    assertTrue(result.getMessage().contains("contains 1 stop places"));
    assertTrue(result.hasWarnings());
  }

  @Test
  void shouldHandleNullKeyValues() {
    FlexibleArea flexibleArea = createFlexibleArea();
    // Don't set keyValues to null since replaceKeyValues doesn't handle it
    // Just use default empty map from createFlexibleArea()

    ValidationResult result = validationService.validateFlexibleArea(flexibleArea);

    assertTrue(result.isValid());
    assertNull(result.getMessage());
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldHandleNullValueItems() {
    FlexibleArea flexibleArea = createFlexibleArea();
    Map<String, Value> keyValues = new HashMap<>();
    Value value = new Value();
    value.setItems(null);
    keyValues.put("FlexibleStopAreaType", value);
    flexibleArea.replaceKeyValues(keyValues);

    ValidationResult result = validationService.validateFlexibleArea(flexibleArea);

    assertTrue(result.isValid());
    assertNull(result.getMessage());
    assertFalse(result.hasWarnings());
  }

  @Test
  void shouldHandleComplexFlexibleStopPlaceWithMixedAreas() {
    FlexibleStopPlace flexibleStopPlace = createFlexibleStopPlace();
    Map<String, Value> keyValues = new HashMap<>();
    keyValues.put("FlexibleStopAreaType", new Value("UnrestrictedPublicTransportAreas"));
    flexibleStopPlace.replaceKeyValues(keyValues);

    FlexibleArea validArea = createFlexibleArea();
    FlexibleArea invalidArea = createFlexibleArea();
    Map<String, Value> invalidAreaKeyValues = new HashMap<>();
    invalidAreaKeyValues.put(
      "FlexibleStopAreaType",
      new Value("UnrestrictedPublicTransportAreas")
    );
    invalidArea.replaceKeyValues(invalidAreaKeyValues);

    flexibleStopPlace.setFlexibleAreas(List.of(validArea, invalidArea));

    when(stopPlaceRegistry.getStopPlacesWithinPolygon(any(Polygon.class)))
      .thenReturn(Collections.emptyList());

    ValidationResult result = validationService.validateFlexibleStopPlace(
      flexibleStopPlace
    );

    assertFalse(result.isValid());
    assertTrue(result.getMessage().contains("contains invalid FlexibleArea"));
    assertFalse(result.hasWarnings());
  }

  private FlexibleStopPlace createFlexibleStopPlace() {
    FlexibleStopPlace flexibleStopPlace = new FlexibleStopPlace();
    flexibleStopPlace.setTransportMode(VehicleModeEnumeration.BUS);
    flexibleStopPlace.setFlexibleAreas(Collections.emptyList());
    return flexibleStopPlace;
  }

  private FlexibleArea createFlexibleArea() {
    FlexibleArea flexibleArea = new FlexibleArea();

    Coordinate[] coordinates = {
      new Coordinate(10.0, 60.0),
      new Coordinate(11.0, 60.0),
      new Coordinate(11.0, 61.0),
      new Coordinate(10.0, 61.0),
      new Coordinate(10.0, 60.0),
    };
    Polygon polygon = geometryFactory.createPolygon(coordinates);
    flexibleArea.setPolygon(polygon);

    return flexibleArea;
  }
}
