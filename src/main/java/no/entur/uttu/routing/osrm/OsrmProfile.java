package no.entur.uttu.routing.osrm;

import java.util.List;
import no.entur.uttu.model.VehicleModeEnumeration;

public class OsrmProfile {

  private List<VehicleModeEnumeration> modes;
  private String endpoint;

  public OsrmProfile(List<VehicleModeEnumeration> modes, String endpoint) {
    this.modes = modes;
    this.endpoint = endpoint;
  }

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
