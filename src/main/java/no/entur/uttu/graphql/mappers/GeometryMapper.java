/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.graphql.mappers;

import static no.entur.uttu.error.codes.ErrorCodeEnumeration.INVALID_POLYGON;

import java.util.Map;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

@Component
public class GeometryMapper {

  private final GeometryFactory geometryFactory;

  public GeometryMapper(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public Polygon createJTSPolygon(Map map) {
    if (map.get("type") != null && map.get("coordinates") != null) {
      if ("Polygon".equals(map.get("type"))) {
        Coordinate[] coordinates = (Coordinate[]) map.get("coordinates");
        Polygon polygon = geometryFactory.createPolygon(coordinates);
        if (!polygon.isValid()) {
          throw new CodedIllegalArgumentException(
            "Invalid polygon",
            CodedError.fromErrorCode(INVALID_POLYGON)
          );
        }
        return polygon;
      }
    }
    return null;
  }
}
