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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateScalar {

    public static final String EXAMPLE = "2017-04-23";


    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private static String DESCRIPTION = "Date time using the format: " + DATE_FORMAT + ". Example: " + EXAMPLE;

    private static GraphQLScalarType graphQLDateScalar;

    public static GraphQLScalarType getGraphQLDateScalar() {
        if (graphQLDateScalar == null) {
            graphQLDateScalar = createGraphQLDateScalar();
        }
        return graphQLDateScalar;
    }

    private static GraphQLScalarType createGraphQLDateScalar() {
        return new GraphQLScalarType("Date", DESCRIPTION, new Coercing() {
            @Override
            public String serialize(Object input) {
                if (input instanceof LocalDate) {
                    return (((LocalDate) input)).format(FORMATTER);
                }
                return null;
            }

            @Override
            public LocalDate parseValue(Object input) {
                return LocalDate.parse((CharSequence) input);
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
