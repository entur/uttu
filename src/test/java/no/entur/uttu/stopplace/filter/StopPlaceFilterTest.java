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
import no.entur.uttu.stopplace.filter.params.BoundingBoxFilterParams;
import no.entur.uttu.stopplace.filter.params.LimitStopPlacesQuantityFilterParams;
import no.entur.uttu.stopplace.filter.params.QuayIdFilterParams;
import no.entur.uttu.stopplace.filter.params.SearchTextStopPlaceFilterParams;
import no.entur.uttu.stopplace.filter.params.StopPlaceFilterParams;
import no.entur.uttu.stopplace.filter.params.TransportModeStopPlaceFilterParams;
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

public class StopPlaceFilterTest {

  private final Logger logger = LoggerFactory.getLogger(StopPlaceFilterTest.class);
  private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
    PublicationDeliveryStructure.class
  );
  private final List<StopPlace> allStopPlacesIndex = new ArrayList<>();
  private final Map<String, StopPlace> stopPlaceByQuayRefIndex =
    new ConcurrentHashMap<>();
  private StopPlacesFilter stopPlacesFilter;

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
    stopPlacesFilter = new StopPlacesFilter();
    Assert.assertEquals(6, allStopPlacesIndex.size());
  }

  @Test
  public void testLimitStopsQuantityFilter() {
    StopPlaceFilterParams limitFilter = new LimitStopPlacesQuantityFilterParams(1);
    List<StopPlace> limitedStopPlaces = stopPlacesFilter.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(limitFilter)
    );
    Assert.assertEquals(1, limitedStopPlaces.size());
  }

  @Test
  public void testSearchTextFilter() {
    StopPlaceFilterParams stopNameFilter = new SearchTextStopPlaceFilterParams("Meri");
    List<StopPlace> filteredStopPlaces = stopPlacesFilter.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(stopNameFilter)
    );
    Assert.assertEquals(2, filteredStopPlaces.size());

    String stopId = "FIN:StopPlace:HKI";
    StopPlaceFilterParams stopIdFilter = new SearchTextStopPlaceFilterParams(stopId);
    filteredStopPlaces =
      stopPlacesFilter.filter(
        allStopPlacesIndex,
        stopPlaceByQuayRefIndex,
        List.of(stopIdFilter)
      );
    Assert.assertEquals(1, filteredStopPlaces.size());
    Assert.assertEquals(stopId, filteredStopPlaces.get(0).getId());

    String quayId = "FIN:Quay:HKI_1";
    StopPlaceFilterParams quayIdFilter = new SearchTextStopPlaceFilterParams(quayId);
    filteredStopPlaces =
      stopPlacesFilter.filter(
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
    StopPlaceFilterParams railFilter = new TransportModeStopPlaceFilterParams(
      AllVehicleModesOfTransportEnumeration.RAIL
    );
    List<StopPlace> filteredStopPlaces = stopPlacesFilter.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(railFilter)
    );
    Assert.assertEquals(1, filteredStopPlaces.size());

    StopPlaceFilterParams busFilter = new TransportModeStopPlaceFilterParams(
      AllVehicleModesOfTransportEnumeration.BUS
    );
    filteredStopPlaces =
      stopPlacesFilter.filter(
        allStopPlacesIndex,
        stopPlaceByQuayRefIndex,
        List.of(busFilter)
      );
    Assert.assertEquals(5, filteredStopPlaces.size());
  }

  @Test
  public void testBoundingBoxFilter() {
    // BoundingBox filtering has been moved to the registry level for spatial optimization
    // This test verifies that BoundingBoxFilterParams are now handled transparently
    // (i.e., they don't cause errors but also don't filter at the StopPlacesFilter level)
    StopPlaceFilterParams helsinkiAreaFilter = new BoundingBoxFilterParams(
      BigDecimal.valueOf(62),
      BigDecimal.valueOf(25.5),
      BigDecimal.valueOf(60),
      BigDecimal.valueOf(24)
    );
    List<StopPlace> filteredStopPlaces = stopPlacesFilter.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(helsinkiAreaFilter)
    );
    // BoundingBox filtering is no longer applied at the filter level - should return all stops
    Assert.assertEquals(6, filteredStopPlaces.size());

    StopPlaceFilterParams ouluAreaFilter = new BoundingBoxFilterParams(
      BigDecimal.valueOf(66),
      BigDecimal.valueOf(26),
      BigDecimal.valueOf(64),
      BigDecimal.valueOf(24)
    );
    filteredStopPlaces =
      stopPlacesFilter.filter(
        allStopPlacesIndex,
        stopPlaceByQuayRefIndex,
        List.of(ouluAreaFilter)
      );
    // BoundingBox filtering is no longer applied at the filter level - should return all stops
    Assert.assertEquals(6, filteredStopPlaces.size());
  }

  @Test
  public void testQuaysIdFilter() {
    List<String> quayIdList = List.of("FSR:Quay:330127", "FSR:Quay:330128");
    StopPlaceFilterParams quaysIdFilter = new QuayIdFilterParams(quayIdList);
    List<StopPlace> filteredStopPlaces = stopPlacesFilter.filter(
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
    StopPlaceFilterParams quaysIdFilter = new QuayIdFilterParams(quayIdList);
    StopPlaceFilterParams limitFilter = new LimitStopPlacesQuantityFilterParams(1);
    StopPlaceFilterParams railFilter = new TransportModeStopPlaceFilterParams(
      AllVehicleModesOfTransportEnumeration.RAIL
    );
    List<StopPlace> filteredStopPlaces = stopPlacesFilter.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(railFilter, quaysIdFilter, limitFilter)
    );
    Assert.assertEquals(2, filteredStopPlaces.size());
  }

  @Test
  public void testMultipleFiltersComposition() {
    StopPlaceFilterParams busFilter = new TransportModeStopPlaceFilterParams(
      AllVehicleModesOfTransportEnumeration.BUS
    );
    StopPlaceFilterParams meriToppilaAreaFilter = new BoundingBoxFilterParams(
      BigDecimal.valueOf(66),
      BigDecimal.valueOf(25.44),
      BigDecimal.valueOf(65.044),
      BigDecimal.valueOf(24)
    );
    List<StopPlace> filteredStopPlaces = stopPlacesFilter.filter(
      allStopPlacesIndex,
      stopPlaceByQuayRefIndex,
      List.of(busFilter, meriToppilaAreaFilter)
    );
    // BoundingBox filtering is no longer applied at the filter level
    // Only transport mode filtering is applied, so we should get all bus stops (5)
    Assert.assertEquals(5, filteredStopPlaces.size());
  }
}
