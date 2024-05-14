package no.entur.uttu.netex;

import static jakarta.xml.bind.JAXBContext.newInstance;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetexUnmarshaller {

  private static final Logger logger = LoggerFactory.getLogger(NetexUnmarshaller.class);

  // JAXBContext is thread safe so can be shared
  private final JAXBContext publicationDeliveryContext;

  public NetexUnmarshaller(Class... clazz) {
    publicationDeliveryContext = createContext(clazz);
  }

  public <T> T unmarshalFromSource(Source source)
    throws NetexUnmarshallerReadFromSourceException {
    try {
      // the Unmarshaller is not thread safe so must be created on every call
      JAXBElement<T> element = (JAXBElement<T>) getUnmarshaller().unmarshal(source);
      return element.getValue();
    } catch (JAXBException e) {
      throw new NetexUnmarshallerReadFromSourceException(source, e);
    }
  }

  private Unmarshaller getUnmarshaller() throws JAXBException {
    return publicationDeliveryContext.createUnmarshaller();
  }

  private static JAXBContext createContext(Class... clazz) {
    try {
      JAXBContext jaxbContext = newInstance(clazz);
      logger.trace("Created context {}", jaxbContext.getClass());
      return jaxbContext;
    } catch (JAXBException e) {
      throw new NetexUnmarshallerCreateContextException(e, clazz);
    }
  }
}
