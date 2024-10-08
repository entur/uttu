package no.entur.uttu.graphql.model;

import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.OrganisationTypeEnumeration;

public record Organisation(
  String id,
  MultilingualString name,
  MultilingualString legalName,
  OrganisationTypeEnumeration type
) {}
