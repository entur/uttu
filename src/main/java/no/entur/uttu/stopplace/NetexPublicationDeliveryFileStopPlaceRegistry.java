package no.entur.uttu.stopplace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.netex.NetexUnmarshallerUnmarshalFromSourceException;
import no.entur.uttu.stopplace.filter.StopPlacesFilter;
import no.entur.uttu.stopplace.filter.params.StopPlaceFilterParams;
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

  private final Logger logger = LoggerFactory.getLogger(
    NetexPublicationDeliveryFileStopPlaceRegistry.class
  );

  private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
    PublicationDeliveryStructure.class
  );

  private final Map<String, StopPlace> stopPlaceByQuayRefIndex =
    new ConcurrentHashMap<>();

  private final Map<String, Quay> quayByQuayRefIndex = new ConcurrentHashMap<>();

  private final List<StopPlace> allStopPlacesIndex = new ArrayList<>();

  @Value("${uttu.stopplace.netex-file-uri}")
  String netexFileUri;

  private StopPlacesFilter stopPlacesFilter = new StopPlacesFilter();

  @PostConstruct
  public void init() {
    try (ZipFile zipFile = new ZipFile(netexFileUri)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      ZipEntry entry = entries.nextElement();
      logger.info("Found a zip file with entry {}", entry.getName());
      InputStream netexFileInputStream = zipFile.getInputStream(entry);
      logger.info("Creating StreamSource from netexFileInputStream");
      PublicationDeliveryStructure publicationDeliveryStructure =
        netexUnmarshaller.unmarshalFromSource(new StreamSource(netexFileInputStream));
      extractStopPlaceData(publicationDeliveryStructure);
    } catch (IOException ioException) {
      // probably not a zip file
      logger.info("Not a zip file", ioException);

      try {
        PublicationDeliveryStructure publicationDeliveryStructure =
          netexUnmarshaller.unmarshalFromSource(new StreamSource(new File(netexFileUri)));
        extractStopPlaceData(publicationDeliveryStructure);
      } catch (
        NetexUnmarshallerUnmarshalFromSourceException unmarshalFromSourceException
      ) {
        logger.warn(
          "Unable to unmarshal stop places xml, stop place registry will be an empty list",
          unmarshalFromSourceException
        );
      }
    } catch (NetexUnmarshallerUnmarshalFromSourceException unmarshalFromSourceException) {
      logger.warn(
        "Unable to unmarshal stop places xml, stop place registry will be an empty list",
        unmarshalFromSourceException
      );
    }
  }

  private void extractStopPlaceData(
    PublicationDeliveryStructure publicationDeliveryStructure
  ) {
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

          stopPlaces.forEach(stopPlace -> {
            Optional
              .ofNullable(stopPlace.getQuays())
              .ifPresent(quays ->
                quays
                  .getQuayRefOrQuay()
                  .forEach(quayRefOrQuay -> {
                    Quay quay = (Quay) quayRefOrQuay.getValue();
                    stopPlaceByQuayRefIndex.put(quay.getId(), stopPlace);
                    quayByQuayRefIndex.put(quay.getId(), quay);
                  })
              );
            allStopPlacesIndex.add(stopPlace);
          });
        }
      });
  }

  @Override
  public Optional<StopPlace> getStopPlaceByQuayRef(String quayRef) {
    return Optional.ofNullable(stopPlaceByQuayRefIndex.get(quayRef));
  }

  @Override
  public List<StopPlace> getStopPlaces(List<StopPlaceFilterParams> filters) {
    return filters.isEmpty()
      ? allStopPlacesIndex
      : stopPlacesFilter.filter(allStopPlacesIndex, stopPlaceByQuayRefIndex, filters);
  }

  @Override
  public Optional<Quay> getQuayById(String id) {
    return Optional.ofNullable(quayByQuayRefIndex.get(id));
  }
}
