package no.entur.uttu.stopplace;

import java.util.stream.Collectors;

public class StopPlaceMapper {
    public static StopPlace mapStopPlace(org.rutebanken.netex.model.StopPlace stopPlace) {
        StopPlace mapped = new StopPlace();
        mapped.setId(stopPlace.getId());
        mapped.setName(mapMultilingualString(stopPlace.getName()));
        mapped.setQuays(stopPlace.getQuays().getQuayRefOrQuay().stream().map(v -> (org.rutebanken.netex.model.Quay)v).map(StopPlaceMapper::mapQuay).collect(Collectors.toList()));
        return mapped;
    }

    private static MultilingualString mapMultilingualString(org.rutebanken.netex.model.MultilingualString multilingualString) {
        MultilingualString mapped = new MultilingualString();
        mapped.setLang(multilingualString.getLang());
        mapped.setValue(multilingualString.getValue());
        return mapped;
    }

    private static Quay mapQuay(org.rutebanken.netex.model.Quay quay) {
        Quay mapped = new Quay();
        mapped.setId(quay.getId());
        mapped.setPublicCode(quay.getPublicCode());
        return mapped;
    }
}
