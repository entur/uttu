package no.entur.uttu.stopplace.filter;

import java.math.BigDecimal;

public record BoundingBoxFilter(
  BigDecimal northEastLat,
  BigDecimal northEastLng,
  BigDecimal southWestLat,
  BigDecimal southWestLng
)
  implements StopPlaceFilter {}
