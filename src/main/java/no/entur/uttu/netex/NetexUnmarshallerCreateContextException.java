package no.entur.uttu.netex;

import java.util.Arrays;

public class NetexUnmarshallerCreateContextException extends RuntimeException {

  public NetexUnmarshallerCreateContextException(Throwable cause, Class... clazz) {
    super(
      "Could not create instance of jaxb context for class " + Arrays.toString(clazz),
      cause
    );
  }
}
