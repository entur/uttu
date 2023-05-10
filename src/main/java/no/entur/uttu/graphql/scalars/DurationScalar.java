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
import java.time.Duration;
import java.time.format.DateTimeParseException;

public class DurationScalar {

  public static final String EXAMPLE_TIME = "PT2H";

  public static final String DESCRIPTION =
    "Duration. A time-based amount of time. Example (two hours): " + EXAMPLE_TIME;

  public static GraphQLScalarType getDurationScalar() {
    return DurationScalar;
  }

  private static GraphQLScalarType DurationScalar = new GraphQLScalarType.Builder()
    .name("Duration")
    .description(DESCRIPTION)
    .coercing(
      new Coercing() {
        @Override
        public String serialize(Object input) {
          if (input instanceof Duration) {
            return input.toString();
          }
          return null;
        }

        @Override
        public Duration parseValue(Object input) {
          try {
            return Duration.parse((CharSequence) input);
          } catch (DateTimeParseException dtpe) {
            throw new CoercingParseValueException(
              "Expected type 'Duration' but was '" + input + "'."
            );
          }
        }

        @Override
        public Duration parseLiteral(Object input) {
          if (input instanceof StringValue) {
            return parseValue(((StringValue) input).getValue());
          }
          return null;
        }
      }
    )
    .build();
}
