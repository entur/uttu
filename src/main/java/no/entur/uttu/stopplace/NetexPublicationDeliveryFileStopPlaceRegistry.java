package no.entur.uttu.stopplace;

import static no.entur.uttu.error.codes.ErrorCodeEnumeration.INVALID_STOP_PLACE_FILTER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import no.entur.uttu.graphql.fetchers.StopPlacesFetcher;
import no.entur.uttu.model.VehicleModeEnumeration;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.netex.NetexUnmarshallerUnmarshalFromSourceException;
import no.entur.uttu.stopplace.filter.SearchTextStopPlaceFilter;
import no.entur.uttu.stopplace.filter.StopPlaceFilter;
import no.entur.uttu.stopplace.filter.TransportModeStopPlaceFilter;
import no.entur.uttu.stopplace.spi.StopPlaceRegistry;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
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

  private final Map<AllVehicleModesOfTransportEnumeration, List<no.entur.uttu.graphql.model.StopPlace>> stopPlaceByTransportModeIndex =
          new ConcurrentHashMap<>();

  private final Map<String, no.entur.uttu.graphql.model.StopPlace> stopPlaceByQuayRefIndex2 =
          new ConcurrentHashMap<>();

  private final List<StopPlace> allStops = new ArrayList<>();

  private final List<no.entur.uttu.graphql.model.StopPlace> allStops2 = new ArrayList<>();

  @Value("${uttu.stopplace.netex-file-uri}")
  String netexFileUri;

  @PostConstruct
  public void init() {
    try {
      PublicationDeliveryStructure publicationDeliveryStructure =
        netexUnmarshaller.unmarshalFromSource(new StreamSource(new File(netexFileUri)));
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

                                                stopPlaceByQuayRefIndex2.put(quay.getId(), StopPlacesFetcher.mapStopPlace(stopPlace));
                                              })
                              );

                      allStops2.add(StopPlacesFetcher.mapStopPlace(stopPlace));
                      allStops.add(stopPlace);

                      no.entur.uttu.graphql.model.StopPlace mappedStopPlace = StopPlacesFetcher.mapStopPlace(stopPlace);
                      if (stopPlace.getTransportMode() != null) {
                        if (stopPlaceByTransportModeIndex.containsKey(stopPlace.getTransportMode())) {
                          stopPlaceByTransportModeIndex.get(stopPlace.getTransportMode()).add(mappedStopPlace);
                        } else {
                          stopPlaceByTransportModeIndex.put(stopPlace.getTransportMode(), new ArrayList<>(List.of(mappedStopPlace)));
                        }
                      } else {
                        //System.out.println(stopPlace.getName().getValue() + " " + stopPlace.getId());
                      }
              }
            );
          }
        });
    } catch (NetexUnmarshallerUnmarshalFromSourceException e) {
      logger.warn(
        "Unable to unmarshal stop places xml, stop place registry will be an empty list"
      );
    }
  }

  @Override
  public Optional<StopPlace> getStopPlaceByQuayRef(String quayRef) {
    return Optional.ofNullable(stopPlaceByQuayRefIndex.get(quayRef));
  }

  @Override
  public List<StopPlace> getStopPlaces(List<StopPlaceFilter> filters) {
    /*List<StopPlace> allStopPlaces = stopPlaceByQuayRefIndex
            .values()
            .stream()
            .distinct()
            .toList();*/

    if (filters.isEmpty()) {
      return allStops;
    }

    return allStops
            .stream()
            .filter(s -> isStopPlaceToBeIncluded1(s, filters))
            .toList();
  }

  @Override
  public List<no.entur.uttu.graphql.model.StopPlace> getStopPlaces2(List<StopPlaceFilter> filters) {

    if (filters.isEmpty()) {
      return allStops2;
    }

    AllVehicleModesOfTransportEnumeration transportMode = null;
    for (StopPlaceFilter f : filters) {
      if (f instanceof TransportModeStopPlaceFilter transportModeStopPlaceFilter) {
        transportMode = transportModeStopPlaceFilter.transportMode();
        break;
      }
    }

    if (transportMode != null && filters.size() == 1) {
      return stopPlaceByTransportModeIndex.get(transportMode);
    }

    List<no.entur.uttu.graphql.model.StopPlace> stopsData = transportMode != null ? stopPlaceByTransportModeIndex.get(transportMode) : allStops2;

    return stopsData
            .stream()
            .filter(s -> isStopPlaceToBeIncluded(s, filters))
            .toList();
  }

  private boolean isStopPlaceToBeIncluded1(
          StopPlace stopPlace,
          List<StopPlaceFilter> filters
  ) {
    for (StopPlaceFilter f : filters) {
      if (f instanceof TransportModeStopPlaceFilter transportModeStopPlaceFilter) {
        boolean isOfTransportMode =
                stopPlace.getTransportMode() == transportModeStopPlaceFilter.transportMode();
        if (!isOfTransportMode) {
          return false;
        }
      } else if (f instanceof SearchTextStopPlaceFilter searchTextStopPlaceFilter) {
        String searchText = searchTextStopPlaceFilter.searchText().toLowerCase();
        List<Quay> quays = stopPlace
          .getQuays()
          .getQuayRefOrQuay()
          .stream()
          .map(jaxbElement -> (org.rutebanken.netex.model.Quay) jaxbElement.getValue())
          .toList();
        boolean includesSearchText =
                stopPlace.getId().toLowerCase().contains(searchText) ||
                        stopPlace.getName().getValue().toLowerCase().contains(searchText) ||
                        quays
                                .stream()
                                .anyMatch(quay -> quay.getId().toLowerCase().contains(searchText));
        if (!includesSearchText) {
          return false;
        }
      } else {
        throw new CodedIllegalArgumentException(
                "Unsupported kind of filter encountered ",
                CodedError.fromErrorCode(INVALID_STOP_PLACE_FILTER)
        );
      }
    }
    return true;
  }

  @Override
  public List<no.entur.uttu.graphql.model.StopPlace> getStopPlaces3(List<StopPlaceFilter> filters) {
    return allStops2;
  }

  private boolean isStopPlaceToBeIncluded(
    no.entur.uttu.graphql.model.StopPlace stopPlace,
    List<StopPlaceFilter> filters
  ) {
    for (StopPlaceFilter f : filters) {
      if (f instanceof TransportModeStopPlaceFilter transportModeStopPlaceFilter) {
        continue;
      } else if (f instanceof SearchTextStopPlaceFilter searchTextStopPlaceFilter) {
        String searchText = searchTextStopPlaceFilter.searchText().toLowerCase();
        List<no.entur.uttu.graphql.model.Quay> quays = stopPlace.quays(); /*stopPlace
          .getQuays()
          .getQuayRefOrQuay()
          .stream()
          .map(jaxbElement -> (org.rutebanken.netex.model.Quay) jaxbElement.getValue())
          .toList();*/
        boolean includesSearchText =
          stopPlace.id().toLowerCase().contains(searchText) ||
          stopPlace.name().getValue().toLowerCase().contains(searchText) ||
          quays
            .stream()
            .anyMatch(quay -> quay.id().toLowerCase().contains(searchText));
        if (!includesSearchText) {
          return false;
        }
      } else {
        throw new CodedIllegalArgumentException(
          "Unsupported kind of filter encountered ",
          CodedError.fromErrorCode(INVALID_STOP_PLACE_FILTER)
        );
      }
    }
    return true;
  }
}
