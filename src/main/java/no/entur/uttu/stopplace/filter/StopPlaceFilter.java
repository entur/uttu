package no.entur.uttu.stopplace.filter;

public interface StopPlaceFilter {
  /**
   * Whether a certain kind of filter will be applied iteratively together with other iterable filters.
   * Non-composite filters are applied outside the iteration through filters, for example:
   * - quayIds filtering is a standalone operation and would skip any other present filters;
   * - limiting by number of stop places is a step done once the composite filters did their round of filtering.
   */
  default boolean isAppliedCompositely() {
    return true;
  }
}
