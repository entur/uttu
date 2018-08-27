package no.entur.uttu.graphql.mappers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.GroupOfEntities_VersionStructure;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;

import java.util.Map;

import static no.entur.uttu.graphql.GraphQLNames.*;

public abstract class AbstractGroupOfEntitiesMapper<T extends GroupOfEntities_VersionStructure> extends AbstractProviderEntityMapper<T> {

    public AbstractGroupOfEntitiesMapper(ProviderRepository providerRepository, ProviderEntityRepository<T> repository) {
        super(providerRepository, repository);
    }

    @Override
    public T map(Object input) {
        return populateGroupOfEntitiesFieldsFromInput(super.map(input), new ArgumentWrapper((Map) input));
    }

    protected T populateGroupOfEntitiesFieldsFromInput(T entity, ArgumentWrapper input) {
        input.apply(FIELD_NAME, entity::setName);
        input.apply(FIELD_DESCRIPTION, entity::setDescription);
        input.apply(FIELD_PRIVATE_CODE, entity::setPrivateCode);
        return entity;
    }
}
