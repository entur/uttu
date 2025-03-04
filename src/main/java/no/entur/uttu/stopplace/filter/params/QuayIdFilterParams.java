package no.entur.uttu.stopplace.filter.params;

import java.util.List;

/**
 * Get stop places containing quay(-s) of certain id-s
 * @param quayIds
 */
public record QuayIdFilterParams(List<String> quayIds) implements StopPlaceFilterParams {
  @Override
  public boolean isFilterAppliedCompositely() {
    return false;
  }
}
