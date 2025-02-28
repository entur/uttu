package no.entur.uttu.stopplace.filter.params;

/**
 * Limit the number of returned stop places
 * @param limit
 */
public record LimitStopPlacesQuantityFilterParams(int limit)
  implements StopPlaceFilterParams {
  @Override
  public boolean isFilterAppliedCompositely() {
    return false;
  }
}
