package no.entur.uttu.netex;

import javax.xml.transform.Source;

public class NetexUnmarshallerReadFromSourceException extends Exception {

  public NetexUnmarshallerReadFromSourceException(Source source, Throwable cause) {
    super(String.format("Unable to unmarshal from source=%s", source), cause);
  }
}
