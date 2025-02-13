package no.entur.uttu.routing;

import java.math.BigDecimal;
import no.entur.uttu.model.VehicleModeEnumeration;

public record RoutingServiceRequestParams(
  BigDecimal longitudeFrom,
  BigDecimal latitudeFrom,
  BigDecimal longitudeTo,
  BigDecimal latitudeTo,
  VehicleModeEnumeration mode
) {}
