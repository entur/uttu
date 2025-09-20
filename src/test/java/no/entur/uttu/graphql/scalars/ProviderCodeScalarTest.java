package no.entur.uttu.graphql.scalars;

import static org.junit.Assert.assertEquals;

import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.junit.Test;

public class ProviderCodeScalarTest {

  GraphQLScalarType scalar = ProviderCodeScalar.PROVIDER_CODE;

  @Test
  public void serialize() {
    assertEquals("tst", scalar.getCoercing().serialize("tst"));
  }

  @Test
  public void parseLiteral() {
    assertEquals("tst", scalar.getCoercing().parseLiteral(new StringValue("tst")));
  }

  @Test
  public void parseValue() {
    assertEquals("tst", scalar.getCoercing().parseValue("tst"));
  }

  @Test(expected = CoercingSerializeException.class)
  public void invalidSerializeThrows() {
    scalar.getCoercing().serialize("TST");
  }

  @Test(expected = CoercingParseLiteralException.class)
  public void invalidParseLiteralThrows() {
    scalar.getCoercing().parseLiteral(new StringValue("TST"));
  }

  @Test(expected = CoercingParseValueException.class)
  public void invalidParseValueThrows() {
    scalar.getCoercing().parseValue("TST");
  }
}
