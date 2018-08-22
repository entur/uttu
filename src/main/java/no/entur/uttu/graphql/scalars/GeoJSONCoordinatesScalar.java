package no.entur.uttu.graphql.scalars;

import com.vividsolutions.jts.geom.Coordinate;
import graphql.language.ArrayValue;
import graphql.language.FloatValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONCoordinatesScalar {

    public static GraphQLScalarType getGraphQGeoJSONCoordinatesScalar() {
        return GraphQLGeoJSONCoordinates;
    }

    private static GraphQLScalarType GraphQLGeoJSONCoordinates = new GraphQLScalarType("Coordinates", null, new Coercing() {
        @Override
        public List<List<Double>> serialize(Object input) {
            if (input instanceof Coordinate[]) {
                Coordinate[] coordinates = ((Coordinate[]) input);
                List<List<Double>> coordinateList = new ArrayList<>();
                for (Coordinate coordinate : coordinates) {
                    List<Double> coordinatePair = new ArrayList<>();
                    coordinatePair.add(coordinate.x);
                    coordinatePair.add(coordinate.y);

                    coordinateList.add(coordinatePair);
                }
                return coordinateList;
            }
            return null;
        }

        @Override
        public Coordinate[] parseValue(Object input) {
            List<List<? extends Number>> coordinateList = (List<List<? extends Number>>) input;

            Coordinate[] coordinates = new Coordinate[coordinateList.size()];

            for (int i = 0; i < coordinateList.size(); i++) {
                coordinates[i] = new Coordinate(coordinateList.get(i).get(0).doubleValue(), coordinateList.get(i).get(1).doubleValue());
            }

            return coordinates;
        }

        @Override
        public Object parseLiteral(Object input) {
            if (input instanceof ArrayValue) {
                ArrayList<ArrayValue> coordinateList = (ArrayList) ((ArrayValue) input).getValues();
                Coordinate[] coordinates = new Coordinate[coordinateList.size()];

                for (int i = 0; i < coordinateList.size(); i++) {
                    ArrayValue v = coordinateList.get(i);

                    FloatValue longitude = (FloatValue) v.getValues().get(0);
                    FloatValue latitude = (FloatValue) v.getValues().get(1);
                    coordinates[i] = new Coordinate(longitude.getValue().doubleValue(), latitude.getValue().doubleValue());

                }
                return coordinates;
            }
            return null;
        }
    });
}
