package no.entur.uttu.routing;

import java.math.BigDecimal;
import java.util.List;

/**
 * @param coordinates List of coordinates in [[longitude, latitude]] format
 * @param distance Distance in meters
 */
public record RouteGeometry(List<List<BigDecimal>> coordinates, BigDecimal distance) {}
