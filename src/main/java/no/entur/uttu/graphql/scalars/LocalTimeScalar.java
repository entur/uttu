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

package no.entur.uttu.graphql.scalars;

import com.google.protobuf.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.GraphQLScalarType;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalTimeScalar {

  public static final String EXAMPLE_TIME = "18:25:SS";

  public static final String TIME_PATTERN = "HH:mm:SS";

  public static final String DESCRIPTION =
    "Time using the format: " + TIME_PATTERN + ". Example: " + EXAMPLE_TIME;

  private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

  public static GraphQLScalarType getLocalTimeScalar() {
    return LocalTimeScalar;
  }

  private static GraphQLScalarType LocalTimeScalar = new GraphQLScalarType(
    "LocalTime",
    DESCRIPTION,
    new Coercing() {
      @Override
      public String serialize(Object input) {
        if (input instanceof LocalTime) {
          return FORMATTER.format((LocalTime) input);
        }
        return null;
      }

      @Override
      public LocalTime parseValue(Object input) {
        try {
          return LocalTime.parse((CharSequence) input);
        } catch (DateTimeParseException dtpe) {
          throw new CoercingParseValueException(
            "Expected type 'LocalTime' but was '" + input + "'."
          );
        }
      }

      @Override
      public LocalTime parseLiteral(Object input) {
        if (input instanceof StringValue) {
          return parseValue(((StringValue) input).getValue());
        }
        return null;
      }
    }
  );
}
