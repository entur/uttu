/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.stopplace.loader;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for extracting stop places from NeTEx publication deliveries.
 * This centralizes the logic for navigating the NeTEx structure.
 */
public class NetexStopPlaceExtractor {

  private static final Logger logger = LoggerFactory.getLogger(
    NetexStopPlaceExtractor.class
  );
  private static final String DEFAULT_TIME_ZONE = "Europe/Oslo";

  /**
   * Extract all stop places from a publication delivery structure
   */
  public static List<StopPlace> extractStopPlaces(
    PublicationDeliveryStructure publicationDelivery
  ) {
    List<StopPlace> stopPlaces = new ArrayList<>();

    if (publicationDelivery == null || publicationDelivery.getDataObjects() == null) {
      logger.debug("No data objects in publication delivery");
      return stopPlaces;
    }

    publicationDelivery
      .getDataObjects()
      .getCompositeFrameOrCommonFrame()
      .forEach(frame -> {
        var frameValue = frame.getValue();
        if (frameValue instanceof SiteFrame siteFrame) {
          stopPlaces.addAll(extractStopPlacesFromSiteFrame(siteFrame));
        }
      });

    logger.debug("Extracted {} stop places from publication delivery", stopPlaces.size());
    return stopPlaces;
  }

  /**
   * Extract stop places from a site frame
   */
  public static List<StopPlace> extractStopPlacesFromSiteFrame(SiteFrame siteFrame) {
    List<StopPlace> stopPlaces = new ArrayList<>();

    if (siteFrame == null || siteFrame.getStopPlaces() == null) {
      return stopPlaces;
    }

    siteFrame
      .getStopPlaces()
      .getStopPlace_()
      .forEach(stopPlaceJaxb -> {
        if (stopPlaceJaxb.getValue() instanceof StopPlace stopPlace) {
          stopPlaces.add(stopPlace);
        }
      });

    return stopPlaces;
  }

  /**
   * Extract the first stop place from a publication delivery
   * Useful for single stop place updates
   */
  public static StopPlace extractFirstStopPlace(
    PublicationDeliveryStructure publicationDelivery
  ) {
    List<StopPlace> stopPlaces = extractStopPlaces(publicationDelivery);
    return stopPlaces.isEmpty() ? null : stopPlaces.get(0);
  }

  /**
   * Extract publication time from a publication delivery
   */
  public static Instant extractPublicationTime(
    PublicationDeliveryStructure publicationDelivery
  ) {
    return extractPublicationTime(publicationDelivery, DEFAULT_TIME_ZONE);
  }

  /**
   * Extract publication time with specified time zone
   */
  public static Instant extractPublicationTime(
    PublicationDeliveryStructure publicationDelivery,
    String timeZone
  ) {
    if (
      publicationDelivery == null || publicationDelivery.getPublicationTimestamp() == null
    ) {
      logger.warn("No publication timestamp in delivery, using current time");
      return Instant.now();
    }

    var localPublicationTimestamp = publicationDelivery.getPublicationTimestamp();
    return localPublicationTimestamp.atZone(ZoneId.of(timeZone)).toInstant();
  }

  /**
   * Extract result containing both stop places and publication time
   */
  public static ExtractResult extractAll(
    PublicationDeliveryStructure publicationDelivery
  ) {
    List<StopPlace> stopPlaces = extractStopPlaces(publicationDelivery);
    Instant publicationTime = extractPublicationTime(publicationDelivery);
    return new ExtractResult(stopPlaces, publicationTime);
  }

  /**
   * Result of extraction containing stop places and metadata
   */
  public record ExtractResult(List<StopPlace> stopPlaces, Instant publicationTime) {}
}
