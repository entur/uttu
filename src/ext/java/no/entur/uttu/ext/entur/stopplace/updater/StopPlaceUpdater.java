package no.entur.uttu.ext.entur.stopplace.updater;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.stopplace.loader.NetexStopPlaceExtractor;
import no.entur.uttu.stopplace.spi.MutableStopPlaceRegistry;
import org.rutebanken.helper.stopplace.changelog.StopPlaceChangelog;
import org.rutebanken.helper.stopplace.changelog.StopPlaceChangelogListener;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("entur-stopplace-updater")
@Component
public class StopPlaceUpdater implements StopPlaceChangelogListener {

  private static final Logger logger = LoggerFactory.getLogger(StopPlaceUpdater.class);

  private final MutableStopPlaceRegistry registry;
  private final StopPlaceChangelog stopPlaceChangelog;
  private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
    PublicationDeliveryStructure.class
  );

  public StopPlaceUpdater(
    MutableStopPlaceRegistry registry,
    StopPlaceChangelog stopPlaceChangelog
  ) {
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
    try (publicationDelivery) {
      List<StopPlace> stopPlaces = extractStopPlacesFromStream(publicationDelivery);
      if (!stopPlaces.isEmpty()) {
        // Use createOrUpdate to handle both updates and potential new stops in multimodal structures
        registry.createOrUpdateStopPlaces(stopPlaces);
        logger.info(
          "Successfully processed {} stop place(s) from update event triggered by id: {} - Applied changes to registry",
          stopPlaces.size(),
          id
        );
      } else {
        logger.warn(
          "No stop places found in publication delivery for update with id: {} - Update skipped",
          id
        );
      }
    } catch (Exception e) {
      logger.error(
        "Failed to apply update for stop place with id: {} - Error during processing",
        id,
        e
      );
    }
  }

  @Override
  public void onStopPlaceCreated(String id, InputStream publicationDelivery) {
    logger.info("Received creation event for stop place with id: {}", id);
    try (publicationDelivery){
      List<StopPlace> stopPlaces = extractStopPlacesFromStream(publicationDelivery);
      if (!stopPlaces.isEmpty()) {
        registry.createOrUpdateStopPlaces(stopPlaces);
        logger.info(
          "Successfully processed {} stop place(s) from creation event triggered by id: {} - Applied to registry",
          stopPlaces.size(),
          id
        );
      } else {
        logger.warn(
          "No stop places found in publication delivery for creation with id: {} - Creation skipped",
          id
        );
      }
    } catch (Exception e) {
      logger.error(
        "Failed to apply creation for stop place with id: {} - Error during processing",
        id,
        e
      );
    }
  }

  @Override
  public void onStopPlaceDeactivated(String id, InputStream publicationDelivery) {
    logger.info("Received deactivation event for stop place with id: {}", id);
    try {
      // Remove the stop place and all related stops
      registry.deleteStopPlaceAndRelated(id);
      logger.info(
        "Successfully deactivated stop place with id: {} and related stops - Removed from registry",
        id
      );
    } catch (Exception e) {
      logger.error(
        "Failed to apply deactivation for stop place with id: {} - Error during processing",
        id,
        e
      );
    }
  }

  @Override
  public void onStopPlaceDeleted(String id) {
    logger.info("Received deletion event for stop place with id: {}", id);
    try {
      // Delete the stop place and all related stops (for multimodal structures)
      registry.deleteStopPlaceAndRelated(id);
      logger.info(
        "Successfully deleted stop place with id: {} and related stops - Removed from registry",
        id
      );
    } catch (Exception e) {
      logger.error(
        "Failed to apply deletion for stop place with id: {} - Error during processing",
        id,
        e
      );
    }
  }

  private List<StopPlace> extractStopPlacesFromStream(InputStream publicationDelivery) {
    logger.debug("Starting to extract stop places from publication delivery stream");
    try {
      PublicationDeliveryStructure publicationDeliveryStructure =
        netexUnmarshaller.unmarshalFromSource(new StreamSource(publicationDelivery));

      logger.debug("Successfully unmarshalled publication delivery structure");

      // Use the centralized extractor
      List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlaces(
        publicationDeliveryStructure
      );

      if (!result.isEmpty()) {
        logger.debug(
          "Successfully extracted {} stop place(s) from publication delivery",
          result.size()
        );
      } else {
        logger.debug("No stop places found in publication delivery structure");
      }

      return result;
    } catch (Exception e) {
      logger.error(
        "Failed to unmarshal publication delivery - Invalid XML or parsing error",
        e
      );
      return List.of();
    }
  }
}
