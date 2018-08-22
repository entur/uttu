package no.entur.uttu.graphql.fetchers;

import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.GroupOfEntities_VersionStructure;
import no.entur.uttu.repository.generic.ProviderEntityRepository;

import static no.entur.uttu.graphql.GraphQLNames.*;

public abstract class AbstractGroupOfEntitiesUpdater<T extends GroupOfEntities_VersionStructure> extends AbstractProviderEntityUpdater<T>{

    public AbstractGroupOfEntitiesUpdater(ProviderEntityRepository<T> repository) {
        super(repository);
    }

    protected void populateEntityFromInput(T entity, ArgumentWrapper input) {
        input.apply(FIELD_NAME, entity::setName);
        input.apply(FIELD_DESCRIPTION, entity::setDescription);
        input.apply(FIELD_PRIVATE_CODE, entity::setPrivateCode);
    }
}
