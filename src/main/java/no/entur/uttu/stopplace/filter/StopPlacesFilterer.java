package no.entur.uttu.stopplace.filter;

import static no.entur.uttu.error.codes.ErrorCodeEnumeration.INVALID_STOP_PLACE_FILTER;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.StopPlace;

public class StopPlacesFilterer {

  /**
   * Filter the set of all stop places by various criteria;
   * Filtering by quay id-s doesn't take into account any other filters that may be by chance provided along;
   * Filtering by limiting the final number of stop places if applied at the very last step;
   * All the other types of filtering one-by-one, in iteration, narrow down the stop places set;
   * @param allStopPlaces
   * @param stopPlaceByQuayRefIndex
   * @param filters
   * @return
   */
  public List<StopPlace> filter(
    List<StopPlace> allStopPlaces,
    Map<String, StopPlace> stopPlaceByQuayRefIndex,
    List<StopPlaceFilter> filters
  ) {
    Optional<StopPlaceFilter> quayIdsFilterOpt = findFilterByClass(
      filters,
      QuayIdFilter.class
    );

    if (quayIdsFilterOpt.isPresent()) {
      return getStopPlacesByQuayIds(
        (QuayIdFilter) quayIdsFilterOpt.get(),
        stopPlaceByQuayRefIndex
      );
    }

    List<StopPlaceFilter> filtersToIterateThrough = filters
      .stream()
      .filter(StopPlaceFilter::isAppliedCompositely)
      .toList();
    List<StopPlace> filteredStopPlaces = allStopPlaces
      .stream()
      .filter(s -> isStopPlaceToBeIncluded(s, filtersToIterateThrough))
      .toList();

    Optional<StopPlaceFilter> limitFilterOpt = findFilterByClass(
      filters,
      LimitStopPlacesQuantityFilter.class
    );

    return limitFilterOpt
      .map(stopPlaceFilter ->
        limitNumberOfStopPlaces(
          ((LimitStopPlacesQuantityFilter) stopPlaceFilter).limit(),
          filteredStopPlaces
        )
      )
      .orElse(filteredStopPlaces);
  }

  /**
   * Find an instance of a certain kind of filter;
   * Normally, there is only one occurrence of a certain kind of filter in the filters list
   * @param filters
   * @param filteringClass
   * @return
   */
  private Optional<StopPlaceFilter> findFilterByClass(
    List<StopPlaceFilter> filters,
    Class filteringClass
  ) {
    return filters.stream().filter(filteringClass::isInstance).findFirst();
  }

  /**
   * Iterating through supplied filters and checking if the stop place fits the filters' requirements
   * @param stopPlace
   * @param filters
   * @return
   */
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
          "Unsupported kind of filter encountered " + f.toString(),
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

  private List<StopPlace> getStopPlacesByQuayIds(
    QuayIdFilter quayIdFilter,
    Map<String, StopPlace> stopPlaceByQuayRefIndex
  ) {
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
