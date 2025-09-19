package no.entur.uttu.graphql;

import graphql.schema.GraphQLEnumType;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public class TypeUtils {

  static <T extends Enum> GraphQLEnumType createEnum(
    String name,
    T[] values,
    Function<T, String> mapping
  ) {
    return createEnum(name, Arrays.asList(values), mapping);
  }

  static <T extends Enum> GraphQLEnumType createEnum(
    String name,
    Collection<T> values,
    Function<T, String> mapping
  ) {
    GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum().name(name);
    values.forEach(type -> enumBuilder.value(mapping.apply(type), type));
    return enumBuilder.build();
  }
}
