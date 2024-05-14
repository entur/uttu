package no.entur.uttu.ext.entur.stopplace;

import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.netex.NetexUnmarshallerUnmarshalFromSourceException;
import org.rutebanken.netex.model.StopPlace;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.AbstractXmlHttpMessageConverter;

public class NetexHttpMessageConverter extends AbstractXmlHttpMessageConverter<Object> {

  private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
    StopPlace.class
  );

  @Override
  protected Object readFromSource(Class<?> clazz, HttpHeaders headers, Source source)
    throws NetexUnmarshallerUnmarshalFromSourceException {
    return netexUnmarshaller.unmarshalFromSource(source);
  }

  @Override
  protected void writeToResult(Object o, HttpHeaders headers, Result result) {
    // This converter is intended for unmarshalling only
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(StopPlace.class);
  }

  @Override
  public List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
    return List.of(MediaType.APPLICATION_XML);
  }
}
