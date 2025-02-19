package no.entur.uttu.stopplace.filter;

import java.util.List;

public record QuayIdFilter(List<String> quayIds) implements StopPlaceFilter {}
