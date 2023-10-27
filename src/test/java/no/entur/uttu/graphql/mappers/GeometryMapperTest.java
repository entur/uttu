package no.entur.uttu.graphql.mappers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

public class GeometryMapperTest {

  @Test
  public void testCreateValidPolygonDoesNotThrow() {
    GeometryMapper geometryMapper = new GeometryMapper(new GeometryFactory());
    Coordinate[] coordinates = new Coordinate[] {
      new Coordinate(10.745830535888672, 59.922506218598684),
      new Coordinate(10.765056610107422, 59.92500104352794),
      new Coordinate(10.764627456665039, 59.91850550432952),
      new Coordinate(10.745830535888672, 59.922506218598684),
    };

    var arguments = Map.of("type", "Polygon", "coordinates", coordinates);

    assertDoesNotThrow(() -> geometryMapper.createJTSPolygon(arguments));
  }

  @Test
  public void testCreateSelfIntersectingPolygonThrows() {
    GeometryMapper geometryMapper = new GeometryMapper(new GeometryFactory());
    Coordinate[] coordinates = new Coordinate[] {
      new Coordinate(10.750551223754885, 59.92366762595335),
      new Coordinate(10.769348144531252, 59.92267828151254),
      new Coordinate(10.759305953979492, 59.916741595387904),
      new Coordinate(10.759477615356445, 59.925216105860315),
      new Coordinate(10.750551223754885, 59.92366762595335),
    };

    var arguments = Map.of("type", "Polygon", "coordinates", coordinates);

    assertThrows(
      CodedIllegalArgumentException.class,
      () -> geometryMapper.createJTSPolygon(arguments)
    );
  }
}
