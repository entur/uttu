package no.entur.uttu.stopplace.filter.params;

import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;

/**
 * Allows to get stop places of a certain transport mode
 * @param transportMode
 */
public record TransportModeStopPlaceFilterParams(
  AllVehicleModesOfTransportEnumeration transportMode
)
  implements StopPlaceFilterParams {}
