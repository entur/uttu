package no.entur.uttu.stopplace.filter;

import static no.entur.uttu.error.codes.ErrorCodeEnumeration.INVALID_STOP_PLACE_FILTER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import no.entur.uttu.stopplace.filter.params.BoundingBoxFilterParams;
import no.entur.uttu.stopplace.filter.params.LimitStopPlacesQuantityFilterParams;
import no.entur.uttu.stopplace.filter.params.QuayIdFilterParams;
import no.entur.uttu.stopplace.filter.params.SearchTextStopPlaceFilterParams;
import no.entur.uttu.stopplace.filter.params.StopPlaceFilterParams;
import no.entur.uttu.stopplace.filter.params.TransportModeStopPlaceFilterParams;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.StopPlace;

public class StopPlacesFilter {

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
    List<StopPlaceFilterParams> filters
  ) {
    Optional<StopPlaceFilterParams> quayIdsFilterOpt = findFilterByClass(
      filters,
      QuayIdFilterParams.class
    );

    if (quayIdsFilterOpt.isPresent()) {
      return getStopPlacesByQuayIds(
        (QuayIdFilterParams) quayIdsFilterOpt.get(),
        stopPlaceByQuayRefIndex
      );
    }

    List<StopPlaceFilterParams> filtersToIterateThrough = filters
      .stream()
      .filter(StopPlaceFilterParams::isFilterAppliedCompositely)
      .toList();
    List<StopPlace> filteredStopPlaces = allStopPlaces
      .stream()
      .filter(s -> includeStopPlace(s, filtersToIterateThrough))
      .toList();

    Optional<StopPlaceFilterParams> limitFilterOpt = findFilterByClass(
      filters,
      LimitStopPlacesQuantityFilterParams.class
    );

    return limitFilterOpt
      .map(stopPlaceFilter ->
        limitNumberOfStopPlaces(
          ((LimitStopPlacesQuantityFilterParams) stopPlaceFilter).limit(),
          filteredStopPlaces
        )
      )
      .orElse(filteredStopPlaces);
  }

  /**
   * Find an instance of a certain kind of filter;
   * There should be only one occurrence of a certain kind of filter in the filters list
   * @param filters
   * @param filteringClass
   * @return
   */
  private Optional<StopPlaceFilterParams> findFilterByClass(
    List<StopPlaceFilterParams> filters,
    Class<? extends StopPlaceFilterParams> filteringClass
  ) {
    return filters.stream().filter(filteringClass::isInstance).findFirst();
  }

  /**
   * Iterating through supplied filters and checking if the stop place fits the filters' requirements
   * @param stopPlace
   * @param filters
   * @return
   */
  private boolean includeStopPlace(
    StopPlace stopPlace,
    List<StopPlaceFilterParams> filters
  ) {
    List<Quay> quays = Optional
      .ofNullable(stopPlace.getQuays())
      .map(quaysRelStructure ->
        quaysRelStructure
          .getQuayRefOrQuay()
          .stream()
          .map(jaxbElement -> (org.rutebanken.netex.model.Quay) jaxbElement.getValue())
          .toList()
      )
      .orElse(Collections.emptyList());

    for (StopPlaceFilterParams f : filters) {
      switch (f) {
        case BoundingBoxFilterParams boundingBoxFilterParams -> {
          // BoundingBox filtering is now handled by spatial pre-filtering in the registry
          // Skip this filter as it's already been applied
        }
        case TransportModeStopPlaceFilterParams transportModeFilterParams -> {
          boolean isOfTransportMode =
            stopPlace.getTransportMode() == transportModeFilterParams.transportMode();
          if (!isOfTransportMode) {
            return false;
          }
        }
        case SearchTextStopPlaceFilterParams searchTextStopPlaceFilterParams -> {
          boolean includesSearchText = foundMatchForSearchText(
            searchTextStopPlaceFilterParams,
            stopPlace,
            quays
          );
          if (!includesSearchText) {
            return false;
          }
        }
        default -> throw new CodedIllegalArgumentException(
          "Unsupported kind of filter encountered " + f.toString(),
          CodedError.fromErrorCode(INVALID_STOP_PLACE_FILTER)
        );
      }
    }
    return true;
  }

  private boolean foundMatchForSearchText(
    SearchTextStopPlaceFilterParams searchTextStopPlaceFilterParams,
    StopPlace stopPlace,
    List<Quay> quays
  ) {
    String searchText = searchTextStopPlaceFilterParams.searchText().toLowerCase();
    return (
      stopPlace.getId().toLowerCase().contains(searchText) ||
      stopPlace.getName().getValue().toLowerCase().contains(searchText) ||
      quays.stream().anyMatch(quay -> quay.getId().toLowerCase().contains(searchText))
    );
  }

  private List<StopPlace> getStopPlacesByQuayIds(
    QuayIdFilterParams quayIdFilterParams,
    Map<String, StopPlace> stopPlaceByQuayRefIndex
  ) {
    List<String> quayIds = quayIdFilterParams.quayIds();

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
