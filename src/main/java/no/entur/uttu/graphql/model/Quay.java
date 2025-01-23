package no.entur.uttu.graphql.model;

import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;

public record Quay(String id, MultilingualString name, String publicCode, SimplePoint_VersionStructure centroid) {
}
