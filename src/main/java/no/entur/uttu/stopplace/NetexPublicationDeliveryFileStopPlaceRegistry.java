package no.entur.uttu.stopplace;

import static jakarta.xml.bind.JAXBContext.newInstance;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(
  value = StopPlaceRegistry.class,
  ignored = NetexPublicationDeliveryFileStopPlaceRegistry.class
)
public class NetexPublicationDeliveryFileStopPlaceRegistry implements StopPlaceRegistry {

  private static final Logger logger = LoggerFactory.getLogger(
    NetexPublicationDeliveryFileStopPlaceRegistry.class
  );

  private static final JAXBContext publicationDeliveryContext = createContext(
    PublicationDeliveryStructure.class,
    StopPlace.class
  );

  private final Map<String, StopPlace> stopPlaceByQuayRefIndex =
    new ConcurrentHashMap<>();

  @Value("${uttu.stopplace.netex-file-uri}")
  String netexFileUri;

  @PostConstruct
  public void init() {
    try {
      PublicationDeliveryStructure publicationDeliveryStructure = readFromSource(
        new StreamSource(new File(netexFileUri))
      );
      publicationDeliveryStructure
        .getDataObjects()
        .getCompositeFrameOrCommonFrame()
        .forEach(frame -> {
          var frameValue = frame.getValue();
          if (frameValue instanceof SiteFrame siteFrame) {
            List<StopPlace> stopPlaces = siteFrame
              .getStopPlaces()
              .getStopPlace_()
              .stream()
              .map(stopPlace -> (StopPlace) stopPlace.getValue())
              .toList();

            stopPlaces.forEach(stopPlace ->
              Optional
                .ofNullable(stopPlace.getQuays())
                .ifPresent(quays ->
                  quays
                    .getQuayRefOrQuay()
                    .forEach(quayRefOrQuay -> {
                      Quay quay = (Quay) quayRefOrQuay.getValue();
                      stopPlaceByQuayRefIndex.put(quay.getId(), stopPlace);
                    })
                )
            );
          }
        });
    } catch (JAXBException e) {
      logger.warn(
        "Unable to unmarshal stop places xml, stop place registry will be an empty list"
      );
    }
  }

  @Override
  public Optional<StopPlace> getStopPlaceByQuayRef(String quayRef) {
    return Optional.ofNullable(stopPlaceByQuayRefIndex.get(quayRef));
  }

  private <T> T readFromSource(Source source) throws JAXBException {
    JAXBElement<T> element = (JAXBElement<T>) getUnmarshaller().unmarshal(source);
    return element.getValue();
  }

  private Unmarshaller getUnmarshaller() throws JAXBException {
    return publicationDeliveryContext.createUnmarshaller();
  }

  private static JAXBContext createContext(Class... clazz) {
    try {
      JAXBContext jaxbContext = newInstance(clazz);
      logger.info("Created context {}", jaxbContext.getClass());
      return jaxbContext;
    } catch (JAXBException e) {
      String message = "Could not create instance of jaxb context for class " + clazz;
      logger.warn(message, e);
      throw new RuntimeException(
        "Could not create instance of jaxb context for class " + clazz,
        e
      );
    }
  }
}
