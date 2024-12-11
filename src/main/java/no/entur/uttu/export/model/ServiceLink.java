package no.entur.uttu.export.model;

import no.entur.uttu.model.Ref;
import no.entur.uttu.model.VehicleModeEnumeration;

public record ServiceLink(
  String quayRefFrom,
  String quayRefTo,
  VehicleModeEnumeration transportMode,
  Ref serviceLinkRef
) {}
