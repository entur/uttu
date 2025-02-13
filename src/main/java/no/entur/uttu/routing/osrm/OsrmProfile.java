package no.entur.uttu.routing.osrm;

import java.util.List;
import no.entur.uttu.model.VehicleModeEnumeration;

public class OsrmProfile {

  List<VehicleModeEnumeration> modes;
  String endpoint;

  public List<VehicleModeEnumeration> getModes() {
    return modes;
  }

  public void setModes(List<VehicleModeEnumeration> modes) {
    this.modes = modes;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public String toString() {
    return "OsrmProfile [modes=" + modes + ", endpoint=" + endpoint + "]";
  }
}
