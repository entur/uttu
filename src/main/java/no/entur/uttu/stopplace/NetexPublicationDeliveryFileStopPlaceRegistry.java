package no.entur.uttu.stopplace;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.netex.NetexUnmarshallerReadFromSourceException;
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

  private static final NetexUnmarshaller publicationDeliveryUnmarshaller =
    new NetexUnmarshaller(PublicationDeliveryStructure.class);

  private final Map<String, StopPlace> stopPlaceByQuayRefIndex =
    new ConcurrentHashMap<>();

  @Value("${uttu.stopplace.netex-file-uri}")
  String netexFileUri;

  @PostConstruct
  public void init() {
    try {
      PublicationDeliveryStructure publicationDeliveryStructure =
        publicationDeliveryUnmarshaller.unmarshalFromSource(
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
    } catch (NetexUnmarshallerReadFromSourceException e) {
      logger.warn(
        "Unable to unmarshal stop places xml, stop place registry will be an empty list"
      );
    }
  }

  @Override
  public Optional<StopPlace> getStopPlaceByQuayRef(String quayRef) {
    return Optional.ofNullable(stopPlaceByQuayRefIndex.get(quayRef));
  }
}
