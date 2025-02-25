package no.entur.uttu.stopplace.filter;

import java.math.BigDecimal;

/**
 * Filter stop places by location - being withing the bounding box or not
 * @param northEastLat
 * @param northEastLng
 * @param southWestLat
 * @param southWestLng
 */
public record BoundingBoxFilter(
  BigDecimal northEastLat,
  BigDecimal northEastLng,
  BigDecimal southWestLat,
  BigDecimal southWestLng
)
  implements StopPlaceFilter {}
