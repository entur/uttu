package no.entur.uttu.stopplace.filter;

import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;

/**
 * Allows to get stop places of a certain transport mode
 * @param transportMode
 */
public record TransportModeStopPlaceFilter(
  AllVehicleModesOfTransportEnumeration transportMode
)
  implements StopPlaceFilter {}
