package no.entur.uttu.stopplace.filter;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.netex.NetexUnmarshallerUnmarshalFromSourceException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopPlaceFiltererTest {

  private final Logger logger = LoggerFactory.getLogger(StopPlaceFiltererTest.class);
  private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
    PublicationDeliveryStructure.class
  );
  private final List<StopPlace> allStopPlacesIndex = new ArrayList<>();
  private final Map<String, StopPlace> stopPlaceByQuayRefIndex =
    new ConcurrentHashMap<>();
  private StopPlacesFilterer stopPlacesFilterer;

  @Before
  public void init() {
    try {
      String testFilePath = "./src/test/resources/fixtures";
      String testFileName = "stopsfiltering.xml";
      File file = new File(testFilePath, testFileName);
      PublicationDeliveryStructure publicationDeliveryStructure =
        netexUnmarshaller.unmarshalFromSource(new StreamSource(file));
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
                    })
                );
              allStopPlacesIndex.add(stopPlace);
            });
          }
        });
    } catch (NetexUnmarshallerUnmarshalFromSourceException e) {
      logger.warn(
        "Unable to unmarshal stop places xml, stop place registry will be an empty list"
      );
    }
    stopPlacesFilterer = new StopPlacesFilterer();
    Assert.assertEquals(4, allStopPlacesIndex.size());
  }

  @Test
  public void testLimitStopsQuantityFilter() {
    StopPlaceFilter limitFilter = new LimitStopPlacesQuantityFilter(1);
    List<StopPlace> limitedStopPlaces = stopPlacesFilterer.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(limitFilter)
    );
    Assert.assertEquals(1, limitedStopPlaces.size());
  }

  @Test
  public void testSearchTextFilter() {
    StopPlaceFilter stopNameFilter = new SearchTextStopPlaceFilter("Meri");
    List<StopPlace> filteredStopPlaces = stopPlacesFilterer.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(stopNameFilter)
    );
    Assert.assertEquals(2, filteredStopPlaces.size());

    String stopId = "FIN:StopPlace:HKI";
    StopPlaceFilter stopIdFilter = new SearchTextStopPlaceFilter(stopId);
    filteredStopPlaces =
      stopPlacesFilterer.filter(
        allStopPlacesIndex,
        stopPlaceByQuayRefIndex,
        List.of(stopIdFilter)
      );
    Assert.assertEquals(1, filteredStopPlaces.size());
    Assert.assertEquals(stopId, filteredStopPlaces.get(0).getId());

    String quayId = "FIN:Quay:HKI_1";
    StopPlaceFilter quayIdFilter = new SearchTextStopPlaceFilter(quayId);
    filteredStopPlaces =
      stopPlacesFilterer.filter(
        allStopPlacesIndex,
        stopPlaceByQuayRefIndex,
        List.of(quayIdFilter)
      );
    Assert.assertEquals(1, filteredStopPlaces.size());
    Assert.assertTrue(
      filteredStopPlaces
        .get(0)
        .getQuays()
        .getQuayRefOrQuay()
        .get(0)
        .getValue()
        .toString()
        .contains(quayId)
    );
  }

  @Test
  public void testTransportModeFilter() {
    StopPlaceFilter railFilter = new TransportModeStopPlaceFilter(
      AllVehicleModesOfTransportEnumeration.RAIL
    );
    List<StopPlace> filteredStopPlaces = stopPlacesFilterer.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(railFilter)
    );
    Assert.assertEquals(1, filteredStopPlaces.size());

    StopPlaceFilter busFilter = new TransportModeStopPlaceFilter(
      AllVehicleModesOfTransportEnumeration.BUS
    );
    filteredStopPlaces =
      stopPlacesFilterer.filter(
        allStopPlacesIndex,
        stopPlaceByQuayRefIndex,
        List.of(busFilter)
      );
    Assert.assertEquals(3, filteredStopPlaces.size());
  }

  @Test
  public void testBoundingBoxFilter() {
    StopPlaceFilter helsinkiAreaFilter = new BoundingBoxFilter(
      BigDecimal.valueOf(62),
      BigDecimal.valueOf(25.5),
      BigDecimal.valueOf(60),
      BigDecimal.valueOf(24)
    );
    List<StopPlace> filteredStopPlaces = stopPlacesFilterer.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(helsinkiAreaFilter)
    );
    Assert.assertEquals(1, filteredStopPlaces.size());
    Assert.assertEquals("Helsinki", filteredStopPlaces.get(0).getName().getValue());

    StopPlaceFilter ouluAreaFilter = new BoundingBoxFilter(
      BigDecimal.valueOf(66),
      BigDecimal.valueOf(26),
      BigDecimal.valueOf(64),
      BigDecimal.valueOf(24)
    );
    filteredStopPlaces =
      stopPlacesFilterer.filter(
        allStopPlacesIndex,
        stopPlaceByQuayRefIndex,
        List.of(ouluAreaFilter)
      );
    Assert.assertEquals(3, filteredStopPlaces.size());
  }

  @Test
  public void testQuaysIdFilter() {
    List<String> quayIdList = List.of("FSR:Quay:330127", "FSR:Quay:330128");
    StopPlaceFilter quaysIdFilter = new QuayIdFilter(quayIdList);
    List<StopPlace> filteredStopPlaces = stopPlacesFilterer.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(quaysIdFilter)
    );
    Assert.assertEquals(2, filteredStopPlaces.size());
    Assert.assertTrue(
      filteredStopPlaces.get(0).getName().getValue().contains("Meri-Toppila")
    );
  }

  @Test
  public void testQuayIdFilterOverruling() {
    List<String> quayIdList = List.of("FSR:Quay:330127", "FSR:Quay:330128");
    StopPlaceFilter quaysIdFilter = new QuayIdFilter(quayIdList);
    StopPlaceFilter limitFilter = new LimitStopPlacesQuantityFilter(1);
    StopPlaceFilter railFilter = new TransportModeStopPlaceFilter(
      AllVehicleModesOfTransportEnumeration.RAIL
    );
    List<StopPlace> filteredStopPlaces = stopPlacesFilterer.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(railFilter, quaysIdFilter, limitFilter)
    );
    Assert.assertEquals(2, filteredStopPlaces.size());
  }

  @Test
  public void testMultipleFiltersComposition() {
    StopPlaceFilter busFilter = new TransportModeStopPlaceFilter(
      AllVehicleModesOfTransportEnumeration.BUS
    );
    StopPlaceFilter meriToppilaAreaFilter = new BoundingBoxFilter(
      BigDecimal.valueOf(66),
      BigDecimal.valueOf(26),
      BigDecimal.valueOf(65.044),
      BigDecimal.valueOf(24)
    );
    List<StopPlace> filteredStopPlaces = stopPlacesFilterer.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(busFilter, meriToppilaAreaFilter)
    );
    Assert.assertEquals(2, filteredStopPlaces.size());
  }
}
