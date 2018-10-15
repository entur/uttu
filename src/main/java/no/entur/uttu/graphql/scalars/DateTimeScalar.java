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

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeScalar {

    private static final ZoneId UTC_TIME_ZONE = ZoneId.of("UTC");

    public static final String EXAMPLE_DATE_TIME = "2017-04-23T18:25:43.511+0000";

    /**
     * Milliseconds and time zone offset is _REQUIRED_ in this scalar.
     * Milliseconds will handle most cases for date and time.
     * Enforcing time zone will avoid issues with making assumptions about time zones.
     * ISO 8601 alone does not require time zone.
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXXX";

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    private static String DESCRIPTION = "Date time using the format: " + DATE_TIME_PATTERN + ". Example: " + EXAMPLE_DATE_TIME;

    private GraphQLScalarType graphQLDateScalar;

    public GraphQLScalarType getDateTimeScalar() {
        if (graphQLDateScalar == null) {
            graphQLDateScalar = createGraphQLDateScalar();
        }
        return graphQLDateScalar;
    }

    private GraphQLScalarType createGraphQLDateScalar() {
        return new GraphQLScalarType("DateTime", DESCRIPTION, new Coercing() {
            @Override
            public String serialize(Object input) {
                if (input instanceof Instant) {
                    return (((Instant) input)).atZone(UTC_TIME_ZONE).format(FORMATTER);
                }
                return null;
            }

            @Override
            public Instant parseValue(Object input) {
                return Instant.from(FORMATTER.parse((CharSequence) input));
            }

            @Override
            public Object parseLiteral(Object input) {
                if (input instanceof StringValue) {
                    return parseValue(((StringValue) input).getValue());
                }
                return null;
            }
        });
    }

}
