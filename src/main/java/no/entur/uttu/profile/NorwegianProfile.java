package no.entur.uttu.profile;

import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.model.VehicleSubmodeEnumeration;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static no.entur.uttu.model.VehicleSubmodeEnumeration.*;


@Component
public class NorwegianProfile implements Profile {

    private static final List<VehicleSubmodeEnumeration> LEGAL_VEHICLE_SUBMODES = Arrays.asList(AIRPORT_LINK_BUS, EXPRESS_BUS, LOCAL_BUS, NIGHT_BUS,
            RAIL_REPLACEMENT_BUS, REGIONAL_BUS, SCHOOL_BUS, SHUTTLE_BUS, SIGHTSEEING_BUS, LOCAL_PASSENGER_FERRY, SIGHTSEEING_SERVICE);

    @Override
    public List<VehicleModeEnumeration> getLegalVehicleModes() {
        return getLegalVehicleSubmodes().stream().map(VehicleSubmodeEnumeration::getVehicleMode).distinct().collect(Collectors.toList());
    }

    @Override
    public List<VehicleSubmodeEnumeration> getLegalVehicleSubmodes() {
        return LEGAL_VEHICLE_SUBMODES;
    }
}
