package no.entur.uttu.stopplace.filter;

/**
 * Filter stop places, for example by containing the search text either in stop name, stop id or quay id
 * @param searchText
 */
public record SearchTextStopPlaceFilter(String searchText) implements StopPlaceFilter {}
