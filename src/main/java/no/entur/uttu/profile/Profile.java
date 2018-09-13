package no.entur.uttu.profile;

import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.model.VehicleSubmodeEnumeration;

import java.util.List;

public interface Profile {


    List<VehicleModeEnumeration> getLegalVehicleModes();

    List<VehicleSubmodeEnumeration> getLegalVehicleSubmodes();
}
