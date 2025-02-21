package no.entur.uttu.stopplace;

import static no.entur.uttu.error.codes.ErrorCodeEnumeration.INVALID_STOP_PLACE_FILTER;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import no.entur.uttu.netex.NetexUnmarshaller;
import no.entur.uttu.netex.NetexUnmarshallerUnmarshalFromSourceException;
import no.entur.uttu.stopplace.filter.BoundingBoxFilter;
import no.entur.uttu.stopplace.filter.LimitFilter;
import no.entur.uttu.stopplace.filter.QuayIdFilter;
import no.entur.uttu.stopplace.filter.SearchTextStopPlaceFilter;
import no.entur.uttu.stopplace.filter.StopPlaceFilter;
import no.entur.uttu.stopplace.filter.TransportModeStopPlaceFilter;
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
                      quayByQuayRefIndex.put(quay.getId(), quay);
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
  }

  @Override
  public Optional<StopPlace> getStopPlaceByQuayRef(String quayRef) {
    return Optional.ofNullable(stopPlaceByQuayRefIndex.get(quayRef));
  }

  @Override
  public List<StopPlace> getStopPlaces(List<StopPlaceFilter> filters) {
    if (filters.isEmpty()) {
      return allStopPlacesIndex;
    }

    Optional<StopPlaceFilter> quayIdsFilterOpt = findFilterByClass(
      filters,
      QuayIdFilter.class
    );
    if (quayIdsFilterOpt.isPresent()) {
      return getStopPlacesByQuayIds((QuayIdFilter) quayIdsFilterOpt.get());
    }

    List<StopPlace> filteredStopPlaces = allStopPlacesIndex
      .stream()
      .filter(s -> isStopPlaceToBeIncluded(s, filters))
      .toList();

    Optional<StopPlaceFilter> limitFilterOpt = findFilterByClass(
      filters,
      LimitFilter.class
    );

    return limitFilterOpt
      .map(stopPlaceFilter ->
        limitNumberOfStopPlaces(
          ((LimitFilter) stopPlaceFilter).limit(),
          filteredStopPlaces
        )
      )
      .orElse(filteredStopPlaces);
  }

  @Override
  public Optional<Quay> getQuayById(String id) {
    return Optional.ofNullable(quayByQuayRefIndex.get(id));
  }

  private boolean isStopPlaceToBeIncluded(
    StopPlace stopPlace,
    List<StopPlaceFilter> filters
  ) {
    List<Quay> quays = stopPlace
      .getQuays()
      .getQuayRefOrQuay()
      .stream()
      .map(jaxbElement -> (org.rutebanken.netex.model.Quay) jaxbElement.getValue())
      .toList();
    for (StopPlaceFilter f : filters) {
      if (f instanceof BoundingBoxFilter boundingBoxFilter) {
        boolean isInsideBoundingBox = isStopPlaceWithinBoundingBox(
          boundingBoxFilter,
          stopPlace,
          quays
        );
        if (!isInsideBoundingBox) {
          return false;
        }
      } else if (f instanceof TransportModeStopPlaceFilter transportModeStopPlaceFilter) {
        boolean isOfTransportMode =
          stopPlace.getTransportMode() == transportModeStopPlaceFilter.transportMode();
        if (!isOfTransportMode) {
          return false;
        }
      } else if (f instanceof SearchTextStopPlaceFilter searchTextStopPlaceFilter) {
        boolean includesSearchText = includesSearchText(
          searchTextStopPlaceFilter,
          stopPlace,
          quays
        );
        if (!includesSearchText) {
          return false;
        }
      } else if (f instanceof LimitFilter) {
        // This filter gets in action further on when the whole filtering is completed
      } else {
        throw new CodedIllegalArgumentException(
          "Unsupported kind of filter encountered ",
          CodedError.fromErrorCode(INVALID_STOP_PLACE_FILTER)
        );
      }
    }
    return true;
  }

  private boolean includesSearchText(
    SearchTextStopPlaceFilter searchTextStopPlaceFilter,
    StopPlace stopPlace,
    List<Quay> quays
  ) {
    String searchText = searchTextStopPlaceFilter.searchText().toLowerCase();
    return (
      stopPlace.getId().toLowerCase().contains(searchText) ||
      stopPlace.getName().getValue().toLowerCase().contains(searchText) ||
      quays.stream().anyMatch(quay -> quay.getId().toLowerCase().contains(searchText))
    );
  }

  private boolean isStopPlaceWithinBoundingBox(
    BoundingBoxFilter boundingBoxFilter,
    StopPlace stopPlace,
    List<Quay> quays
  ) {
    BigDecimal lat = Optional
      .ofNullable(stopPlace.getCentroid())
      .map(centroid -> centroid.getLocation().getLatitude())
      .orElse(null);
    BigDecimal lng = Optional
      .ofNullable(stopPlace.getCentroid())
      .map(centroid -> centroid.getLocation().getLongitude())
      .orElse(null);

    if (lat == null || lng == null) {
      Quay firstQuay = quays.get(0);
      lat = firstQuay.getCentroid().getLocation().getLatitude();
      lng = firstQuay.getCentroid().getLocation().getLongitude();
    }
    if (lat == null || lng == null) {
      // oh well, we tried
      return false;
    }

    return (
      lat.compareTo(boundingBoxFilter.northEastLat()) < 0 &&
      lng.compareTo(boundingBoxFilter.northEastLng()) < 0 &&
      lat.compareTo(boundingBoxFilter.southWestLat()) > 0 &&
      lng.compareTo(boundingBoxFilter.southWestLng()) > 0
    );
  }

  private Optional<StopPlaceFilter> findFilterByClass(
    List<StopPlaceFilter> filters,
    Class filteringClass
  ) {
    return filters.stream().filter(filteringClass::isInstance).findFirst();
  }

  private List<StopPlace> getStopPlacesByQuayIds(QuayIdFilter quayIdFilter) {
    List<String> quayIds = quayIdFilter.quayIds();

    List<StopPlace> stopPlacesbyQuayIds = new ArrayList<>();
    quayIds.forEach(quayId -> {
      StopPlace stopPlace = stopPlaceByQuayRefIndex.get(quayId);
      if (stopPlace != null) {
        stopPlacesbyQuayIds.add(stopPlace);
      }
    });

    return stopPlacesbyQuayIds.stream().distinct().toList();
  }

  private List<StopPlace> limitNumberOfStopPlaces(int limit, List<StopPlace> stopPlaces) {
    if (stopPlaces.size() <= limit) {
      return stopPlaces;
    }
    List<StopPlace> shuffledStopPlaces = new ArrayList<>(stopPlaces);
    Collections.shuffle(shuffledStopPlaces);
    return shuffledStopPlaces.subList(0, limit);
  }
}
