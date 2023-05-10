package no.entur.uttu.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

public class ProviderCodeScalar {

  public static final GraphQLScalarType PROVIDER_CODE = new GraphQLScalarType.Builder()
    .name("ProviderCode")
    .description("Provider codes must be lower-case strings")
    .coercing(
      new Coercing() {
        @Override
        public Object serialize(Object dataFetcherResult)
          throws CoercingSerializeException {
          return serializeProviderCode(dataFetcherResult);
        }

        @Override
        public Object parseValue(Object input) throws CoercingParseValueException {
          return parseProviderCodeFromValue(input);
        }

        @Override
        public Object parseLiteral(Object input) throws CoercingParseLiteralException {
          return parseProviderCodeAsLiteral(input);
        }
      }
    )
    .build();

  private static boolean isValidProviderCode(String code) {
    return code.toLowerCase().equals(code);
  }

  private static Object serializeProviderCode(Object dataFetcherResult) {
    String providerCode = String.valueOf(dataFetcherResult);
    if (isValidProviderCode(providerCode)) {
      return providerCode;
    } else {
      throw new CoercingSerializeException(
        "Unable to serialize " + providerCode + " as a provider code"
      );
    }
  }

  private static Object parseProviderCodeFromValue(Object input) {
    if (input instanceof String) {
      String providerCode = input.toString();
      if (isValidProviderCode(providerCode)) {
        return providerCode;
      }
    }
    throw new CoercingParseValueException(
      "Unable to parse variable value " + input + " as a provider code"
    );
  }

  private static Object parseProviderCodeAsLiteral(Object input) {
    if (input instanceof StringValue) {
      String providerCode = ((StringValue) input).getValue();
      if (isValidProviderCode(providerCode)) {
        return providerCode;
      }
    }
    throw new CoercingParseLiteralException(
      "Value is not a provider code: '" + String.valueOf(input) + "'"
    );
  }
}
