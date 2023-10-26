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

package no.entur.uttu.profile;

import static no.entur.uttu.model.VehicleSubmodeEnumeration.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.model.VehicleSubmodeEnumeration;
import org.springframework.stereotype.Component;

@Component
public class NorwegianProfile implements Profile {

  private static final List<VehicleSubmodeEnumeration> LEGAL_VEHICLE_SUBMODES =
    Arrays.asList(
      DOMESTIC_FLIGHT,
      HELICOPTER_SERVICE,
      INTERNATIONAL_FLIGHT,
      AIRPORT_LINK_BUS,
      EXPRESS_BUS,
      LOCAL_BUS,
      NIGHT_BUS,
      RAIL_REPLACEMENT_BUS,
      REGIONAL_BUS,
      SCHOOL_BUS,
      SHUTTLE_BUS,
      SIGHTSEEING_BUS,
      SIGHTSEEING_SERVICE,
      TELECABIN,
      INTERNATIONAL_COACH,
      NATIONAL_COACH,
      TOURIST_COACH,
      FUNICULAR,
      METRO,
      AIRPORT_LINK_RAIL,
      INTERNATIONAL,
      INTERREGIONAL_RAIL,
      LOCAL,
      LONG_DISTANCE,
      NIGHT_RAIL,
      REGIONAL_RAIL,
      TOURIST_RAILWAY,
      CITY_TRAM,
      LOCAL_TRAM,
      HIGH_SPEED_PASSENGER_SERVICE,
      HIGH_SPEED_VEHICLE_SERVICE,
      INTERNATIONAL_CAR_FERRY,
      INTERNATIONAL_PASSENGER_FERRY,
      LOCAL_CAR_FERRY,
      LOCAL_PASSENGER_FERRY,
      NATIONAL_CAR_FERRY,
      SIGHTSEEING_SERVICE,
      CHARTER_TAXI,
      COMMUNAL_TAXI,
      WATER_TAXI
    );

  @Override
  public List<VehicleModeEnumeration> getLegalVehicleModes() {
    return getLegalVehicleSubmodes()
      .stream()
      .map(VehicleSubmodeEnumeration::getVehicleMode)
      .distinct()
      .collect(Collectors.toList());
  }

  @Override
  public List<VehicleSubmodeEnumeration> getLegalVehicleSubmodes() {
    return LEGAL_VEHICLE_SUBMODES;
  }
}
