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

import java.util.List;
import java.util.Map;
import no.entur.uttu.model.FlexibleArea;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.ValidationResult;
import no.entur.uttu.model.Value;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.locationtech.jts.geom.Polygon;
import org.rutebanken.netex.model.StopPlace;
import org.springframework.stereotype.Service;

@Service
public class FlexibleAreaValidationService {

  private static final String FLEXIBLE_STOP_AREA_TYPE_KEY = "FlexibleStopAreaType";
  private static final String UNRESTRICTED_PUBLIC_TRANSPORT_AREAS_VALUE =
    "UnrestrictedPublicTransportAreas";

  private final StopPlaceRegistry stopPlaceRegistry;

  public FlexibleAreaValidationService(StopPlaceRegistry stopPlaceRegistry) {
    this.stopPlaceRegistry = stopPlaceRegistry;
  }

  public ValidationResult validateFlexibleStopPlace(FlexibleStopPlace flexibleStopPlace) {
    if (!requiresValidation(flexibleStopPlace.getKeyValues())) {
      return ValidationResult.valid();
    }

    for (FlexibleArea flexibleArea : flexibleStopPlace.getFlexibleAreas()) {
      ValidationResult areaResult = validateFlexibleArea(flexibleArea);
      if (!areaResult.isValid()) {
        return ValidationResult.invalid(
          "FlexibleStopPlace " +
          flexibleStopPlace.getPk() +
          " contains invalid FlexibleArea " +
          flexibleArea.getPk() +
          ": " +
          areaResult.getMessage()
        );
      }
    }

    return ValidationResult.valid();
  }

  public ValidationResult validateFlexibleArea(FlexibleArea flexibleArea) {
    if (!requiresValidation(flexibleArea.getKeyValues())) {
      return ValidationResult.valid();
    }

    Polygon polygon = flexibleArea.getPolygon();
    if (polygon == null) {
      return ValidationResult.invalid(
        "FlexibleArea " + flexibleArea.getPk() + " has no polygon defined"
      );
    }

    List<StopPlace> stopPlacesInArea = stopPlaceRegistry.getStopPlacesWithinPolygon(
      polygon
    );

    if (stopPlacesInArea.isEmpty()) {
      return ValidationResult.invalid(
        "FlexibleArea " +
        flexibleArea.getPk() +
        " with FlexibleStopAreaType=UnrestrictedPublicTransportAreas contains no valid stop places"
      );
    }

    return ValidationResult.withWarnings(
      "FlexibleArea " +
      flexibleArea.getPk() +
      " contains " +
      stopPlacesInArea.size() +
      " stop places"
    );
  }

  private boolean requiresValidation(Map<String, Value> keyValues) {
    if (keyValues == null || keyValues.isEmpty()) {
      return false;
    }

    Value flexibleStopAreaType = keyValues.get(FLEXIBLE_STOP_AREA_TYPE_KEY);
    if (flexibleStopAreaType == null || flexibleStopAreaType.getItems() == null) {
      return false;
    }

    return flexibleStopAreaType
      .getItems()
      .contains(UNRESTRICTED_PUBLIC_TRANSPORT_AREAS_VALUE);
  }
}
