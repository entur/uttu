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

package no.entur.uttu.graphql.mappers;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_END_QUAY_REF;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_FLEXIBLE_AREA;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_FLEXIBLE_AREAS;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_HAIL_AND_RIDE_AREA;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_KEY;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_KEY_VALUES;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_POLYGON;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_START_QUAY_REF;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_TRANSPORT_MODE;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_VALUES;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.FlexibleArea;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.model.HailAndRideArea;
import no.entur.uttu.model.Value;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import no.entur.uttu.stopplace.StopPlaceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlexibleStopPlaceMapper
  extends AbstractGroupOfEntitiesMapper<FlexibleStopPlace> {

  private GeometryMapper geometryMapper;

  @Autowired
  private StopPlaceRegistry stopPlaceRegistry;

  public FlexibleStopPlaceMapper(
    ProviderRepository providerRepository,
    ProviderEntityRepository<FlexibleStopPlace> repository,
    GeometryMapper geometryMapper
  ) {
    super(providerRepository, repository);
    this.geometryMapper = geometryMapper;
  }

  @Override
  protected FlexibleStopPlace createNewEntity(ArgumentWrapper input) {
    return new FlexibleStopPlace();
  }

  @Override
  protected void populateEntityFromInput(
    FlexibleStopPlace entity,
    ArgumentWrapper input
  ) {
    input.apply(FIELD_TRANSPORT_MODE, entity::setTransportMode);
    input.apply(FIELD_FLEXIBLE_AREA, this::mapFlexibleAreas, entity::setFlexibleAreas);
    input.applyList(
      FIELD_FLEXIBLE_AREAS,
      this::mapFlexibleArea,
      entity::setFlexibleAreas
    );
    input.apply(
      FIELD_HAIL_AND_RIDE_AREA,
      this::mapHailAndRideArea,
      entity::setHailAndRideArea
    );
    input.apply(FIELD_KEY_VALUES, this::mapKeyValues, entity::replaceKeyValues);
  }

  protected List<FlexibleArea> mapFlexibleAreas(Map<String, Object> inputMap) {
    return List.of(mapFlexibleArea(inputMap));
  }

  protected FlexibleArea mapFlexibleArea(Map<String, Object> inputMap) {
    ArgumentWrapper input = new ArgumentWrapper(inputMap);

    FlexibleArea entity = new FlexibleArea();
    input.apply(FIELD_POLYGON, geometryMapper::createJTSPolygon, entity::setPolygon);
    input.apply(FIELD_KEY_VALUES, this::mapKeyValues, entity::replaceKeyValues);
    return entity;
  }

  protected HailAndRideArea mapHailAndRideArea(Map<String, Object> inputMap) {
    ArgumentWrapper input = new ArgumentWrapper(inputMap);

    HailAndRideArea entity = new HailAndRideArea();
    input.apply(FIELD_START_QUAY_REF, this::getVerifiedQuayRef, entity::setStartQuayRef);
    input.apply(FIELD_END_QUAY_REF, this::getVerifiedQuayRef, entity::setEndQuayRef);
    return entity;
  }

  private Map<String, Value> mapKeyValues(List<Map<String, Object>> inputKeyValues) {
    Map<String, Value> keyValues = new HashMap<>();

    inputKeyValues.forEach(inputMap -> {
      String key = (String) inputMap.get(FIELD_KEY);
      List<String> values = (List<String>) inputMap.get(FIELD_VALUES);
      keyValues.put(key, new Value(values));
    });

    return keyValues;
  }

  protected String getVerifiedQuayRef(String quayRef) {
    return stopPlaceRegistry.getStopPlaceByQuayRef(quayRef).isPresent() ? quayRef : null;
  }
}
