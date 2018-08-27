package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.FlexibleStopPlace;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_FLEXIBLE_AREA;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_TRANSPORT_MODE;

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
        input.apply(FIELD_FLEXIBLE_AREA, geometryMapper::createJTSPolygon, entity::setPolygon);
    }
}
