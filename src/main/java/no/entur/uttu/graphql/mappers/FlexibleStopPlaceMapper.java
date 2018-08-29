package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.FlexibleArea;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

import static no.entur.uttu.graphql.GraphQLNames.*;

@Component
public class FlexibleStopPlaceMapper extends AbstractGroupOfEntitiesMapper<FlexibleStopPlace> {

    private GeometryMapper geometryMapper;

    public FlexibleStopPlaceMapper(ProviderRepository providerRepository, ProviderEntityRepository<FlexibleStopPlace> repository, GeometryMapper geometryMapper) {
        super(providerRepository, repository);
        this.geometryMapper = geometryMapper;
    }

    @Override
    protected FlexibleStopPlace createNewEntity(ArgumentWrapper input) {
        return new FlexibleStopPlace();
    }

    @Override
    protected void populateEntityFromInput(FlexibleStopPlace entity, ArgumentWrapper input) {
        input.apply(FIELD_TRANSPORT_MODE, entity::setTransportMode);
        input.apply(FIELD_FLEXIBLE_AREA, this::mapFlexibleArea, entity::setFlexibleArea);
    }


    protected FlexibleArea mapFlexibleArea(Map<String, Object> inputMap) {
        ArgumentWrapper input = new ArgumentWrapper(inputMap);

        FlexibleArea entity = new FlexibleArea();
        input.apply(FIELD_POLYGON, geometryMapper::createJTSPolygon, entity::setPolygon);
        return entity;
    }
}
