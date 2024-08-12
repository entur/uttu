package no.entur.uttu.stopplace.filter;

import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;

public record TransportModeStopPlaceFilter(
  AllVehicleModesOfTransportEnumeration transportMode
)
  implements StopPlaceFilter {}
