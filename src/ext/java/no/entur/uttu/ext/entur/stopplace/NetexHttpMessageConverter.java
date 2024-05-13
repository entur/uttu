package no.entur.uttu.ext.entur.stopplace;

import static jakarta.xml.bind.JAXBContext.newInstance;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.AbstractXmlHttpMessageConverter;

public class NetexHttpMessageConverter extends AbstractXmlHttpMessageConverter<Object> {

  private static final Logger log = LoggerFactory.getLogger(
    NetexHttpMessageConverter.class
  );
  private static final JAXBContext publicationDeliveryContext = createContext(
    PublicationDeliveryStructure.class,
    StopPlace.class
  );

  @Override
  protected Object readFromSource(Class<?> clazz, HttpHeaders headers, Source source)
    throws JAXBException {
    JAXBElement<?> element = (JAXBElement<?>) getUnmarshaller().unmarshal(source);
    return element.getValue();
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

  private Unmarshaller getUnmarshaller() throws JAXBException {
    return publicationDeliveryContext.createUnmarshaller();
  }

  private static JAXBContext createContext(Class... clazz) {
    try {
      JAXBContext jaxbContext = newInstance(clazz);
      log.info("Created context {}", jaxbContext.getClass());
      return jaxbContext;
    } catch (JAXBException e) {
      String message = "Could not create instance of jaxb context for class " + clazz;
      log.warn(message, e);
      throw new RuntimeException(
        "Could not create instance of jaxb context for class " + clazz,
        e
      );
    }
  }
}
