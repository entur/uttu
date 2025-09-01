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

package no.entur.uttu.ext.entur.stopplace.updater;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import no.entur.uttu.stopplace.spi.MutableStopPlaceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.helper.stopplace.changelog.StopPlaceChangelog;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.SiteRefStructure;
import org.rutebanken.netex.model.StopPlace;

@ExtendWith(MockitoExtension.class)
class StopPlaceUpdaterTest {

  @Mock
  private MutableStopPlaceRegistry registry;

  @Mock
  private StopPlaceChangelog stopPlaceChangelog;

  private StopPlaceUpdater updater;

  @BeforeEach
  void setUp() {
    updater = new StopPlaceUpdater(registry, stopPlaceChangelog);
  }

  @Test
  void testInit_registersListener() {
    updater.init();
    verify(stopPlaceChangelog).registerStopPlaceChangelogListener(updater);
  }

  @Test
  void testPreDestroy_unregistersListener() {
    updater.preDestroy();
    verify(stopPlaceChangelog).unregisterStopPlaceChangelogListener(updater);
  }

  @Test
  void testOnStopPlaceCreated_withSingleStop_callsCreateOrUpdate() {
    String xml = createSimpleStopPlaceXml("NSR:StopPlace:1", "Oslo S");
    InputStream inputStream = new ByteArrayInputStream(
      xml.getBytes(StandardCharsets.UTF_8)
    );

    updater.onStopPlaceCreated("NSR:StopPlace:1", inputStream);

    ArgumentCaptor<List<StopPlace>> captor = ArgumentCaptor.forClass(List.class);
    verify(registry).createOrUpdateStopPlaces(captor.capture());

    List<StopPlace> stops = captor.getValue();
    assertEquals(1, stops.size());
    assertEquals("NSR:StopPlace:1", stops.get(0).getId());
    assertEquals("Oslo S", stops.get(0).getName().getValue());
  }

  @Test
  void testOnStopPlaceCreated_withMultimodalStructure_callsCreateOrUpdateWithAll() {
    String xml = createMultimodalStopPlaceXml();
    InputStream inputStream = new ByteArrayInputStream(
      xml.getBytes(StandardCharsets.UTF_8)
    );

    updater.onStopPlaceCreated("NSR:StopPlace:1", inputStream);

    ArgumentCaptor<List<StopPlace>> captor = ArgumentCaptor.forClass(List.class);
    verify(registry).createOrUpdateStopPlaces(captor.capture());

    List<StopPlace> stops = captor.getValue();
    assertEquals(3, stops.size());

    // Verify parent and children are present
    assertTrue(stops.stream().anyMatch(s -> "NSR:StopPlace:1".equals(s.getId())));
    assertTrue(stops.stream().anyMatch(s -> "NSR:StopPlace:2".equals(s.getId())));
    assertTrue(stops.stream().anyMatch(s -> "NSR:StopPlace:3".equals(s.getId())));
  }

  @Test
  void testOnStopPlaceUpdated_callsCreateOrUpdate() {
    String xml = createSimpleStopPlaceXml("NSR:StopPlace:1", "Oslo S Updated");
    InputStream inputStream = new ByteArrayInputStream(
      xml.getBytes(StandardCharsets.UTF_8)
    );

    updater.onStopPlaceUpdated("NSR:StopPlace:1", inputStream);

    ArgumentCaptor<List<StopPlace>> captor = ArgumentCaptor.forClass(List.class);
    verify(registry).createOrUpdateStopPlaces(captor.capture());

    List<StopPlace> stops = captor.getValue();
    assertEquals(1, stops.size());
    assertEquals("Oslo S Updated", stops.get(0).getName().getValue());
  }

  @Test
  void testOnStopPlaceDeactivated_withNullInputStream_callsDeleteStopPlaceAndRelated() {
    updater.onStopPlaceDeactivated("NSR:StopPlace:1", null);

    verify(registry).deleteStopPlaceAndRelated("NSR:StopPlace:1");
    verify(registry, never()).createOrUpdateStopPlaces(any());
  }

  @Test
  void testOnStopPlaceDeactivated_withInputStream_deletesStopPlaceAndRelated() {
    String xml = createMultimodalStopPlaceXml();
    InputStream inputStream = new ByteArrayInputStream(
      xml.getBytes(StandardCharsets.UTF_8)
    );

    updater.onStopPlaceDeactivated("NSR:StopPlace:1", inputStream);

    // Should only call deleteStopPlaceAndRelated once with the main ID
    // The method itself handles deletion of related stops
    verify(registry, times(1)).deleteStopPlaceAndRelated("NSR:StopPlace:1");
  }

  @Test
  void testOnStopPlaceDeleted_callsDeleteStopPlaceAndRelated() {
    updater.onStopPlaceDeleted("NSR:StopPlace:1");

    verify(registry).deleteStopPlaceAndRelated("NSR:StopPlace:1");
  }

  @Test
  void testOnStopPlaceCreated_withEmptyDelivery_doesNotCallRegistry() {
    String xml = createEmptyPublicationDeliveryXml();
    InputStream inputStream = new ByteArrayInputStream(
      xml.getBytes(StandardCharsets.UTF_8)
    );

    updater.onStopPlaceCreated("NSR:StopPlace:1", inputStream);

    verify(registry, never()).createOrUpdateStopPlaces(any());
  }

  @Test
  void testOnStopPlaceCreated_withInvalidXml_handlesGracefully() {
    String invalidXml = "not valid xml";
    InputStream inputStream = new ByteArrayInputStream(
      invalidXml.getBytes(StandardCharsets.UTF_8)
    );

    // Should not throw exception
    assertDoesNotThrow(() -> updater.onStopPlaceCreated("NSR:StopPlace:1", inputStream));

    verify(registry, never()).createOrUpdateStopPlaces(any());
  }

  @Test
  void testOnStopPlaceUpdated_withException_handlesGracefully() {
    String xml = createSimpleStopPlaceXml("NSR:StopPlace:1", "Oslo S");
    InputStream inputStream = new ByteArrayInputStream(
      xml.getBytes(StandardCharsets.UTF_8)
    );

    doThrow(new RuntimeException("Registry error"))
      .when(registry)
      .createOrUpdateStopPlaces(any());

    // Should not throw exception
    assertDoesNotThrow(() -> updater.onStopPlaceUpdated("NSR:StopPlace:1", inputStream));
  }

  @Test
  void testOnStopPlaceDeactivated_withException_handlesGracefully() {
    doThrow(new RuntimeException("Registry error"))
      .when(registry)
      .deleteStopPlaceAndRelated(anyString());

    // Should not throw exception
    assertDoesNotThrow(() -> updater.onStopPlaceDeactivated("NSR:StopPlace:1", null));
  }

  @Test
  void testOnStopPlaceDeleted_withException_handlesGracefully() {
    doThrow(new RuntimeException("Registry error"))
      .when(registry)
      .deleteStopPlaceAndRelated(anyString());

    // Should not throw exception
    assertDoesNotThrow(() -> updater.onStopPlaceDeleted("NSR:StopPlace:1"));
  }

  // Helper methods to create XML
  private String createSimpleStopPlaceXml(String id, String name) {
    return """
    <?xml version="1.0" encoding="UTF-8"?>
    <PublicationDelivery xmlns="http://www.netex.org.uk/netex" version="1.0">
      <dataObjects>
        <SiteFrame>
          <stopPlaces>
            <StopPlace id="%s" version="1">
              <Name>%s</Name>
            </StopPlace>
          </stopPlaces>
        </SiteFrame>
      </dataObjects>
    </PublicationDelivery>
    """.formatted(id, name);
  }

  private String createMultimodalStopPlaceXml() {
    return """
    <?xml version="1.0" encoding="UTF-8"?>
    <PublicationDelivery xmlns="http://www.netex.org.uk/netex" version="1.0">
      <dataObjects>
        <SiteFrame>
          <stopPlaces>
            <StopPlace id="NSR:StopPlace:1" version="1">
              <Name>Parent Station</Name>
            </StopPlace>
            <StopPlace id="NSR:StopPlace:2" version="1">
              <Name>Child Stop 1</Name>
              <ParentSiteRef ref="NSR:StopPlace:1"/>
            </StopPlace>
            <StopPlace id="NSR:StopPlace:3" version="1">
              <Name>Child Stop 2</Name>
              <ParentSiteRef ref="NSR:StopPlace:1"/>
            </StopPlace>
          </stopPlaces>
        </SiteFrame>
      </dataObjects>
    </PublicationDelivery>
    """;
  }

  private String createEmptyPublicationDeliveryXml() {
    return """
    <?xml version="1.0" encoding="UTF-8"?>
    <PublicationDelivery xmlns="http://www.netex.org.uk/netex" version="1.0">
      <dataObjects>
        <SiteFrame>
          <stopPlaces/>
        </SiteFrame>
      </dataObjects>
    </PublicationDelivery>
    """;
  }
}
