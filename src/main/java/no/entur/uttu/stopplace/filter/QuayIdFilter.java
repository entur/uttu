package no.entur.uttu.stopplace.filter;

import java.util.List;

/**
 * Get stop places containing quay(-s) of certain id-s
 * @param quayIds
 */
public record QuayIdFilter(List<String> quayIds) implements StopPlaceFilter {
  @Override
  public boolean isAppliedCompositely() {
    return false;
  }
}
