package no.entur.uttu.graphql.model;

import java.util.List;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.Quay;

public record StopPlace(
  String id,
  MultilingualString name,
  String transportMode,
  String stopPlaceType,
  List<Quay> quays
) {}
