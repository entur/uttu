package no.entur.uttu.export.netex.producer.common;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.FlexibleArea;
import no.entur.uttu.model.FlexibleStopPlace;
import org.rutebanken.netex.model.FlexibleStopPlace_VersionStructure;
import org.rutebanken.netex.model.VehicleModeEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FlexibleStopPlaceProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    public List<org.rutebanken.netex.model.FlexibleStopPlace> produce(NetexExportContext context) {
        return context.flexibleStopPlaces.stream().map(localStopPlace -> mapFlexibleStopPlace(localStopPlace, context)).collect(Collectors.toList());
    }

    private org.rutebanken.netex.model.FlexibleStopPlace mapFlexibleStopPlace(FlexibleStopPlace localStopPlace, NetexExportContext context) {
        org.rutebanken.netex.model.FlexibleArea netexArea = mapFlexibleArea(localStopPlace, context);

        return new org.rutebanken.netex.model.FlexibleStopPlace()
                       .withId(localStopPlace.getNetexId())
                       .withVersion(localStopPlace.getNetexVersion())
                       .withName(objectFactory.createMultilingualString(localStopPlace.getName()))
                       .withDescription(objectFactory.createMultilingualString(localStopPlace.getDescription()))
                       .withTransportMode(objectFactory.mapEnum(localStopPlace.getTransportMode(), VehicleModeEnumeration.class))
                       .withPrivateCode(objectFactory.createPrivateCodeStructure(localStopPlace.getPrivateCode()))
                       .withAreas(new FlexibleStopPlace_VersionStructure.Areas().withFlexibleAreaOrFlexibleAreaRefOrHailAndRideArea(netexArea));
    }

    private org.rutebanken.netex.model.FlexibleArea mapFlexibleArea(FlexibleStopPlace flexibleStopPlace, NetexExportContext context) {
        FlexibleArea localArea = flexibleStopPlace.getFlexibleArea();
        return objectFactory.populateId(new org.rutebanken.netex.model.FlexibleArea(), flexibleStopPlace.getRef())
                       .withPolygon(NetexGeoUtil.toNetexPolygon(localArea.getPolygon(), context));
    }
}
