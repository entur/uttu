package no.entur.uttu.netex;

import javax.xml.transform.Source;

public class NetexUnmarshallerUnmarshalFromSourceException extends Exception {

  public NetexUnmarshallerUnmarshalFromSourceException(Source source, Throwable cause) {
    super(String.format("Unable to unmarshal from source=%s", source), cause);
  }
}
