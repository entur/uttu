package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.DestinationDisplay;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_FRONT_TEXT;

@Component
public class DestinationDisplayMapper extends AbstractProviderEntityMapper<DestinationDisplay> {

    public DestinationDisplayMapper(ProviderRepository providerRepository, ProviderEntityRepository<DestinationDisplay> entityRepository) {
        super(providerRepository, entityRepository);
    }

    @Override
    protected DestinationDisplay createNewEntity(ArgumentWrapper input) {
        return new DestinationDisplay();
    }

    @Override
    protected void populateEntityFromInput(DestinationDisplay entity, ArgumentWrapper input) {
        input.apply(FIELD_FRONT_TEXT, entity::setFrontText);
    }
}
