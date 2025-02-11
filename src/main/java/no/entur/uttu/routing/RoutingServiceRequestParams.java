package no.entur.uttu.routing;

import java.math.BigDecimal;

public record RoutingServiceRequestParams(
  BigDecimal longitudeFrom,
  BigDecimal latitudeFrom,
  BigDecimal longitudeTo,
  BigDecimal latitudeTo,
  RoutingProfile routingProfile
) {}
