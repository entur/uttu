package no.entur.uttu.stopplace;

import static no.entur.uttu.error.codes.ErrorCodeEnumeration.INVALID_STOP_PLACE_FILTER;

import java.io.File;
import java.math.BigDecimal;
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

            stopPlaces.forEach(stopPlace ->
              Optional
                .ofNullable(stopPlace.getQuays())
                .ifPresent(quays ->
                  quays
                    .getQuayRefOrQuay()
                    .forEach(quayRefOrQuay -> {
                      Quay quay = (Quay) quayRefOrQuay.getValue();
                      stopPlaceByQuayRefIndex.put(quay.getId(), stopPlace);
                    })
                )
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
    List<StopPlace> allStopPlaces = stopPlaceByQuayRefIndex
      .values()
      .stream()
      .distinct()
      .toList();
    if (filters.isEmpty()) {
      return allStopPlaces;
    }

    return allStopPlaces
      .stream()
      .filter(s -> isStopPlaceToBeIncluded(s, filters))
      .toList();
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
}
