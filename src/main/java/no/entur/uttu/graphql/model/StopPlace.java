package no.entur.uttu.graphql.model;

import java.util.List;
import org.rutebanken.netex.model.AllVehicleModesOfTransportEnumeration;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.Quay;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;

public record StopPlace(
  String id,
  MultilingualString name,
  AllVehicleModesOfTransportEnumeration transportMode,
  SimplePoint_VersionStructure centroid,
  List<Quay> quays
) {}
