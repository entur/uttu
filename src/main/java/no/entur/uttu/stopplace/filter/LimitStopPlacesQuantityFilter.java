package no.entur.uttu.stopplace.filter;

/**
 * Limit the number of returned stop places
 * @param limit
 */
public record LimitStopPlacesQuantityFilter(int limit) implements StopPlaceFilter {
  @Override
  public boolean isAppliedCompositely() {
    return false;
  }
}
