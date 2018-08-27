package no.entur.uttu.graphql.scalars;

import com.google.protobuf.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import graphql.schema.GraphQLScalarType;

import java.time.Duration;
import java.time.format.DateTimeParseException;

public class DurationScalar {
    public static final String EXAMPLE_TIME = "PT2H";

    public static final String DESCRIPTION = "Duration. A time-based amount of time. Example (two hours): " + EXAMPLE_TIME;

    public static GraphQLScalarType getDurationScalar() {
        return DurationScalar;
    }

    private static GraphQLScalarType DurationScalar = new GraphQLScalarType("Duration", DESCRIPTION, new Coercing() {
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
                throw new CoercingParseValueException("Expected type 'Duration' but was '" + input + "'.");
            }
        }

        @Override
        public Duration parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return parseValue(((StringValue) input).getValue());
            }
            return null;
        }
    });
}
