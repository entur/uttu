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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.xml.bind.JAXBElement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import javax.xml.namespace.QName;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopPlace;
import org.rutebanken.netex.model.StopPlacesInFrame_RelStructure;

class NetexStopPlaceExtractorTest {

  @Test
  void testExtractStopPlaces_withValidData_extractsAll() {
    PublicationDeliveryStructure publicationDelivery = createPublicationDeliveryWithStops(
      List.of(
        createStopPlace("NSR:StopPlace:1", "Oslo S"),
        createStopPlace("NSR:StopPlace:2", "Bergen"),
        createStopPlace("NSR:StopPlace:3", "Trondheim")
      )
    );

    List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlaces(
      publicationDelivery
    );

    assertEquals(3, result.size());
    assertEquals("NSR:StopPlace:1", result.get(0).getId());
    assertEquals("Oslo S", result.get(0).getName().getValue());
    assertEquals("NSR:StopPlace:2", result.get(1).getId());
    assertEquals("Bergen", result.get(1).getName().getValue());
    assertEquals("NSR:StopPlace:3", result.get(2).getId());
    assertEquals("Trondheim", result.get(2).getName().getValue());
  }

  @Test
  void testExtractStopPlaces_withNullDelivery_returnsEmpty() {
    List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlaces(null);

    assertTrue(result.isEmpty());
  }

  @Test
  void testExtractStopPlaces_withNullDataObjects_returnsEmpty() {
    PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure();
    // Don't set dataObjects

    List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlaces(
      publicationDelivery
    );

    assertTrue(result.isEmpty());
  }

  @Test
  void testExtractStopPlaces_withEmptyDataObjects_returnsEmpty() {
    PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure();
    publicationDelivery.setDataObjects(new PublicationDeliveryStructure.DataObjects());

    List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlaces(
      publicationDelivery
    );

    assertTrue(result.isEmpty());
  }

  @Test
  void testExtractStopPlaces_withMultipleSiteFrames_extractsFromAll() {
    PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure();
    PublicationDeliveryStructure.DataObjects dataObjects =
      new PublicationDeliveryStructure.DataObjects();

    // Create first SiteFrame with 2 stops
    SiteFrame siteFrame1 = createSiteFrameWithStops(
      List.of(
        createStopPlace("NSR:StopPlace:1", "Stop 1"),
        createStopPlace("NSR:StopPlace:2", "Stop 2")
      )
    );

    // Create second SiteFrame with 1 stop
    SiteFrame siteFrame2 = createSiteFrameWithStops(
      List.of(createStopPlace("NSR:StopPlace:3", "Stop 3"))
    );

    // Add both frames to data objects
    dataObjects.getCompositeFrameOrCommonFrame().add(createFrameElement(siteFrame1));
    dataObjects.getCompositeFrameOrCommonFrame().add(createFrameElement(siteFrame2));

    publicationDelivery.setDataObjects(dataObjects);

    List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlaces(
      publicationDelivery
    );

    assertEquals(3, result.size());
    assertEquals("NSR:StopPlace:1", result.get(0).getId());
    assertEquals("NSR:StopPlace:2", result.get(1).getId());
    assertEquals("NSR:StopPlace:3", result.get(2).getId());
  }

  @Test
  void testExtractStopPlacesFromSiteFrame_withValidFrame_extractsAll() {
    SiteFrame siteFrame = createSiteFrameWithStops(
      List.of(
        createStopPlace("NSR:StopPlace:1", "Oslo S"),
        createStopPlace("NSR:StopPlace:2", "Bergen")
      )
    );

    List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlacesFromSiteFrame(
      siteFrame
    );

    assertEquals(2, result.size());
    assertEquals("NSR:StopPlace:1", result.get(0).getId());
    assertEquals("NSR:StopPlace:2", result.get(1).getId());
  }

  @Test
  void testExtractStopPlacesFromSiteFrame_withNullFrame_returnsEmpty() {
    List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlacesFromSiteFrame(null);

    assertTrue(result.isEmpty());
  }

  @Test
  void testExtractStopPlacesFromSiteFrame_withNullStopPlaces_returnsEmpty() {
    SiteFrame siteFrame = new SiteFrame();
    // Don't set stopPlaces

    List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlacesFromSiteFrame(
      siteFrame
    );

    assertTrue(result.isEmpty());
  }

  @Test
  void testExtractStopPlacesFromSiteFrame_withEmptyStopPlaces_returnsEmpty() {
    SiteFrame siteFrame = new SiteFrame();
    siteFrame.setStopPlaces(new StopPlacesInFrame_RelStructure());

    List<StopPlace> result = NetexStopPlaceExtractor.extractStopPlacesFromSiteFrame(
      siteFrame
    );

    assertTrue(result.isEmpty());
  }

  @Test
  void testExtractFirstStopPlace_withMultipleStops_returnsFirst() {
    PublicationDeliveryStructure publicationDelivery = createPublicationDeliveryWithStops(
      List.of(
        createStopPlace("NSR:StopPlace:1", "First"),
        createStopPlace("NSR:StopPlace:2", "Second")
      )
    );

    StopPlace result = NetexStopPlaceExtractor.extractFirstStopPlace(publicationDelivery);

    assertNotNull(result);
    assertEquals("NSR:StopPlace:1", result.getId());
    assertEquals("First", result.getName().getValue());
  }

  @Test
  void testExtractFirstStopPlace_withNoStops_returnsNull() {
    PublicationDeliveryStructure publicationDelivery = createPublicationDeliveryWithStops(
      List.of()
    );

    StopPlace result = NetexStopPlaceExtractor.extractFirstStopPlace(publicationDelivery);

    assertNull(result);
  }

  @Test
  void testExtractFirstStopPlace_withNullDelivery_returnsNull() {
    StopPlace result = NetexStopPlaceExtractor.extractFirstStopPlace(null);

    assertNull(result);
  }

  @Test
  void testExtractPublicationTime_withValidTimestamp_extractsCorrectly() {
    PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure();
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 14, 30, 0);
    publicationDelivery.setPublicationTimestamp(timestamp);

    Instant result = NetexStopPlaceExtractor.extractPublicationTime(publicationDelivery);

    // Should be interpreted as Europe/Oslo time zone
    Instant expected = timestamp.atZone(ZoneId.of("Europe/Oslo")).toInstant();
    assertEquals(expected, result);
  }

  @Test
  void testExtractPublicationTime_withCustomTimeZone_extractsCorrectly() {
    PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure();
    LocalDateTime timestamp = LocalDateTime.of(2024, 6, 15, 14, 30, 0); // Summer time
    publicationDelivery.setPublicationTimestamp(timestamp);

    Instant result = NetexStopPlaceExtractor.extractPublicationTime(
      publicationDelivery,
      "UTC"
    );

    Instant expected = timestamp.atZone(ZoneId.of("UTC")).toInstant();
    assertEquals(expected, result);
  }

  @Test
  void testExtractPublicationTime_withNullDelivery_returnsCurrentTime() {
    Instant beforeCall = Instant.now();

    Instant result = NetexStopPlaceExtractor.extractPublicationTime(null);

    Instant afterCall = Instant.now();

    assertTrue(result.isAfter(beforeCall.minusSeconds(1)));
    assertTrue(result.isBefore(afterCall.plusSeconds(1)));
  }

  @Test
  void testExtractPublicationTime_withNullTimestamp_returnsCurrentTime() {
    PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure();
    // Don't set publicationTimestamp

    Instant beforeCall = Instant.now();

    Instant result = NetexStopPlaceExtractor.extractPublicationTime(publicationDelivery);

    Instant afterCall = Instant.now();

    assertTrue(result.isAfter(beforeCall.minusSeconds(1)));
    assertTrue(result.isBefore(afterCall.plusSeconds(1)));
  }

  @Test
  void testExtractAll_withValidData_returnsBothStopsAndTime() {
    PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure();
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 14, 30, 0);
    publicationDelivery.setPublicationTimestamp(timestamp);

    // Add data with stops
    PublicationDeliveryStructure.DataObjects dataObjects =
      new PublicationDeliveryStructure.DataObjects();
    SiteFrame siteFrame = createSiteFrameWithStops(
      List.of(
        createStopPlace("NSR:StopPlace:1", "Stop 1"),
        createStopPlace("NSR:StopPlace:2", "Stop 2")
      )
    );
    dataObjects.getCompositeFrameOrCommonFrame().add(createFrameElement(siteFrame));
    publicationDelivery.setDataObjects(dataObjects);

    NetexStopPlaceExtractor.ExtractResult result = NetexStopPlaceExtractor.extractAll(
      publicationDelivery
    );

    assertEquals(2, result.stopPlaces().size());
    assertEquals("NSR:StopPlace:1", result.stopPlaces().get(0).getId());
    assertEquals("NSR:StopPlace:2", result.stopPlaces().get(1).getId());

    Instant expectedTime = timestamp.atZone(ZoneId.of("Europe/Oslo")).toInstant();
    assertEquals(expectedTime, result.publicationTime());
  }

  @Test
  void testExtractAll_withNullDelivery_returnsEmptyStopsAndCurrentTime() {
    Instant beforeCall = Instant.now();

    NetexStopPlaceExtractor.ExtractResult result = NetexStopPlaceExtractor.extractAll(
      null
    );

    Instant afterCall = Instant.now();

    assertTrue(result.stopPlaces().isEmpty());
    assertTrue(result.publicationTime().isAfter(beforeCall.minusSeconds(1)));
    assertTrue(result.publicationTime().isBefore(afterCall.plusSeconds(1)));
  }

  // Helper methods
  private StopPlace createStopPlace(String id, String name) {
    StopPlace stopPlace = new StopPlace();
    stopPlace.setId(id);
    stopPlace.setName(new MultilingualString().withValue(name));
    return stopPlace;
  }

  private SiteFrame createSiteFrameWithStops(List<StopPlace> stops) {
    SiteFrame siteFrame = new SiteFrame();
    StopPlacesInFrame_RelStructure stopPlacesRel = new StopPlacesInFrame_RelStructure();

    for (StopPlace stop : stops) {
      JAXBElement<StopPlace> stopElement = new JAXBElement<>(
        new QName("http://www.netex.org.uk/netex", "StopPlace"),
        StopPlace.class,
        stop
      );
      stopPlacesRel.getStopPlace_().add(stopElement);
    }

    siteFrame.setStopPlaces(stopPlacesRel);
    return siteFrame;
  }

  private PublicationDeliveryStructure createPublicationDeliveryWithStops(
    List<StopPlace> stops
  ) {
    PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure();
    LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 14, 30, 0);
    publicationDelivery.setPublicationTimestamp(timestamp);

    PublicationDeliveryStructure.DataObjects dataObjects =
      new PublicationDeliveryStructure.DataObjects();
    SiteFrame siteFrame = createSiteFrameWithStops(stops);
    dataObjects.getCompositeFrameOrCommonFrame().add(createFrameElement(siteFrame));

    publicationDelivery.setDataObjects(dataObjects);
    return publicationDelivery;
  }

  private JAXBElement<SiteFrame> createFrameElement(SiteFrame siteFrame) {
    return new JAXBElement<>(
      new QName("http://www.netex.org.uk/netex", "SiteFrame"),
      SiteFrame.class,
      siteFrame
    );
  }
}
