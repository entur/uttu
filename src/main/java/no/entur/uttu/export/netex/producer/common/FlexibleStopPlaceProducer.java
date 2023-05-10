/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.export.netex.producer.common;

import java.util.List;
import java.util.stream.Collectors;
import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.FlexibleArea;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.HailAndRideArea;
import org.rutebanken.netex.model.FlexibleStopPlace_VersionStructure;
import org.rutebanken.netex.model.PointRefStructure;
import org.rutebanken.netex.model.ScheduledStopPointRefStructure;
import org.rutebanken.netex.model.VehicleModeEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlexibleStopPlaceProducer {

  @Autowired
  private NetexObjectFactory objectFactory;

  public List<org.rutebanken.netex.model.FlexibleStopPlace> produce(
    NetexExportContext context
  ) {
    return context.flexibleStopPlaces
      .stream()
      .map(localStopPlace -> mapFlexibleStopPlace(localStopPlace, context))
      .collect(Collectors.toList());
  }

  private org.rutebanken.netex.model.FlexibleStopPlace mapFlexibleStopPlace(
    FlexibleStopPlace localStopPlace,
    NetexExportContext context
  ) {
    org.rutebanken.netex.model.FlexibleQuay_VersionStructure netexQuay;

    if (localStopPlace.getFlexibleArea() != null) {
      netexQuay = mapFlexibleArea(localStopPlace, context);
    } else {
      netexQuay = mapHailAndRideArea(localStopPlace, context);
    }

    return new org.rutebanken.netex.model.FlexibleStopPlace()
      .withId(localStopPlace.getNetexId())
      .withVersion(localStopPlace.getNetexVersion())
      .withName(objectFactory.createMultilingualString(localStopPlace.getName()))
      .withDescription(
        objectFactory.createMultilingualString(localStopPlace.getDescription())
      )
      .withTransportMode(
        objectFactory.mapEnum(
          localStopPlace.getTransportMode(),
          VehicleModeEnumeration.class
        )
      )
      .withPrivateCode(
        objectFactory.createPrivateCodeStructure(localStopPlace.getPrivateCode())
      )
      .withAreas(
        new FlexibleStopPlace_VersionStructure.Areas()
          .withFlexibleAreaOrFlexibleAreaRefOrHailAndRideArea(netexQuay)
      )
      .withKeyList(objectFactory.mapKeyValues(localStopPlace.getKeyValues()));
  }

  private org.rutebanken.netex.model.FlexibleArea mapFlexibleArea(
    FlexibleStopPlace flexibleStopPlace,
    NetexExportContext context
  ) {
    FlexibleArea localArea = flexibleStopPlace.getFlexibleArea();
    return objectFactory
      .populateId(
        new org.rutebanken.netex.model.FlexibleArea(),
        flexibleStopPlace.getRef()
      )
      .withPolygon(NetexGeoUtil.toNetexPolygon(localArea.getPolygon(), context));
  }

  private org.rutebanken.netex.model.HailAndRideArea mapHailAndRideArea(
    FlexibleStopPlace flexibleStopPlace,
    NetexExportContext context
  ) {
    HailAndRideArea localArea = flexibleStopPlace.getHailAndRideArea();

    PointRefStructure startPoint = objectFactory.populateRefStructure(
      new ScheduledStopPointRefStructure(),
      objectFactory.createScheduledStopPointRefFromQuayRef(
        localArea.getStartQuayRef(),
        context
      ),
      true
    );
    PointRefStructure endPoint = objectFactory.populateRefStructure(
      new ScheduledStopPointRefStructure(),
      objectFactory.createScheduledStopPointRefFromQuayRef(
        localArea.getEndQuayRef(),
        context
      ),
      true
    );

    return objectFactory
      .populateId(
        new org.rutebanken.netex.model.HailAndRideArea(),
        flexibleStopPlace.getRef()
      )
      .withStartPointRef(startPoint)
      .withEndPointRef(endPoint);
  }
}
