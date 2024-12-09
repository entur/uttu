package no.entur.uttu.routing;

import java.math.BigDecimal;
import java.util.List;

public record RouteGeometry(List<List<BigDecimal>> coordinates, BigDecimal distance) {}
