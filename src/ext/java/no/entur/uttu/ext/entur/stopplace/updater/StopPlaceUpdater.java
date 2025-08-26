package no.entur.uttu.ext.entur.stopplace.updater;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.stopplace.NetexPublicationDeliveryFileStopPlaceRegistry;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.helper.stopplace.changelog.StopPlaceChangelog;
import org.rutebanken.helper.stopplace.changelog.StopPlaceChangelogListener;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Profile("entur-stopplace-updater")
@Component
public class StopPlaceUpdater implements StopPlaceChangelogListener {

  private static final Logger logger = LoggerFactory.getLogger(StopPlaceUpdater.class);

  private final NetexPublicationDeliveryFileStopPlaceRegistry registry;
  private final StopPlaceChangelog stopPlaceChangelog;
  private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
    PublicationDeliveryStructure.class
  );

  public StopPlaceUpdater(NetexPublicationDeliveryFileStopPlaceRegistry registry, StopPlaceChangelog stopPlaceChangelog) {
    this.registry = registry;
    this.stopPlaceChangelog = stopPlaceChangelog;
  }

  @PostConstruct
  public void init() {
    stopPlaceChangelog.registerStopPlaceChangelogListener(this);
  }

  @PreDestroy
  public void preDestroy() {
    stopPlaceChangelog.unregisterStopPlaceChangelogListener(this);
  }

  @Override
  public void onStopPlaceUpdated(String id, InputStream publicationDelivery) {
    logger.info("Received update for stop place with id: {}", id);
    try {
      List<StopPlace> stopPlaces = extractStopPlacesFromStream(publicationDelivery);
      if (!stopPlaces.isEmpty()) {
        int updatedCount = 0;
        for (StopPlace stopPlace : stopPlaces) {
          registry.updateStopPlace(stopPlace.getId(), stopPlace);
          updatedCount++;
        }
        logger.info("Successfully updated {} stop place(s) from publication delivery triggered by id: {} - Applied changes to registry", updatedCount, id);
      } else {
        logger.warn("No stop places found in publication delivery for update with id: {} - Update skipped", id);
      }
    } catch (Exception e) {
      logger.error("Failed to apply update for stop place with id: {} - Error during processing", id, e);
    }
  }

  @Override
  public void onStopPlaceCreated(String id, InputStream publicationDelivery) {
    logger.info("Received creation event for stop place with id: {}", id);
    try {
      List<StopPlace> stopPlaces = extractStopPlacesFromStream(publicationDelivery);
      if (!stopPlaces.isEmpty()) {
        int createdCount = 0;
        for (StopPlace stopPlace : stopPlaces) {
          registry.createStopPlace(stopPlace.getId(), stopPlace);
          createdCount++;
        }
        logger.info("Successfully created {} stop place(s) from publication delivery triggered by id: {} - Added to registry", createdCount, id);
      } else {
        logger.warn("No stop places found in publication delivery for creation with id: {} - Creation skipped", id);
      }
    } catch (Exception e) {
      logger.error("Failed to apply creation for stop place with id: {} - Error during processing", id, e);
    }
  }

  @Override
  public void onStopPlaceDeactivated(String id, InputStream publicationDelivery) {
    logger.info("Received deactivation event for stop place with id: {}", id);
    try {
      List<StopPlace> stopPlaces = extractStopPlacesFromStream(publicationDelivery);
      if (!stopPlaces.isEmpty()) {
        int deactivatedCount = 0;
        for (StopPlace stopPlace : stopPlaces) {
          // For deactivation, we update the stop place (it should have a deactivation timestamp)
          registry.updateStopPlace(stopPlace.getId(), stopPlace);
          deactivatedCount++;
        }
        logger.info("Successfully deactivated {} stop place(s) from publication delivery triggered by id: {} - Applied deactivation to registry", deactivatedCount, id);
      } else {
        logger.warn("No stop places found in publication delivery for deactivation with id: {} - Deactivation skipped", id);
      }
    } catch (Exception e) {
      logger.error("Failed to apply deactivation for stop place with id: {} - Error during processing", id, e);
    }
  }

  @Override
  public void onStopPlaceDeleted(String id) {
    logger.info("Received deletion event for stop place with id: {}", id);
    try {
      registry.deleteStopPlace(id);
      logger.info("Successfully deleted stop place with id: {} - Removed from registry", id);
    } catch (Exception e) {
      logger.error("Failed to apply deletion for stop place with id: {} - Error during processing", id, e);
    }
  }

  private List<StopPlace> extractStopPlacesFromStream(InputStream publicationDelivery) {
    logger.debug("Starting to extract stop places from publication delivery stream");
    try {
      PublicationDeliveryStructure publicationDeliveryStructure =
        netexUnmarshaller.unmarshalFromSource(new StreamSource(publicationDelivery));

      logger.debug("Successfully unmarshalled publication delivery structure");
      List<StopPlace> result = extractStopPlacesFromPublicationDelivery(publicationDeliveryStructure);

      if (!result.isEmpty()) {
        logger.debug("Successfully extracted {} stop place(s) from publication delivery", result.size());
      } else {
        logger.debug("No stop places found in publication delivery structure");
      }

      return result;
    } catch (Exception e) {
      logger.error("Failed to unmarshal publication delivery - Invalid XML or parsing error", e);
      return List.of();
    }
  }

  private List<StopPlace> extractStopPlacesFromPublicationDelivery(
    PublicationDeliveryStructure publicationDeliveryStructure
  ) {
    if (publicationDeliveryStructure == null ||
        publicationDeliveryStructure.getDataObjects() == null) {
      return List.of();
    }

    return publicationDeliveryStructure
      .getDataObjects()
      .getCompositeFrameOrCommonFrame()
      .stream()
      .map(frame -> frame.getValue())
      .filter(frameValue -> frameValue instanceof SiteFrame)
      .map(frameValue -> (SiteFrame) frameValue)
      .filter(siteFrame -> siteFrame.getStopPlaces() != null)
      .flatMap(siteFrame -> siteFrame.getStopPlaces().getStopPlace_().stream())
      .map(stopPlaceJaxb -> (StopPlace) stopPlaceJaxb.getValue())
      .toList();
  }
}
