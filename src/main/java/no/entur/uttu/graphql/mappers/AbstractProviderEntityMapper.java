package no.entur.uttu.graphql.mappers;

import com.google.common.base.Preconditions;
import no.entur.uttu.config.Context;
import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.repository.ProviderRepository;
import no.entur.uttu.repository.generic.ProviderEntityRepository;

import java.util.Map;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_ID;

public abstract class AbstractProviderEntityMapper<T extends ProviderEntity> {

    private ProviderEntityRepository<T> entityRepository;

    private ProviderRepository providerRepository;


    public AbstractProviderEntityMapper(ProviderRepository providerRepository, ProviderEntityRepository<T> entityRepository) {
        this.providerRepository = providerRepository;
        this.entityRepository = entityRepository;
    }

    public T map(Object inputObj) {
        ArgumentWrapper input = new ArgumentWrapper((Map) inputObj);
        String netexId = input.get(FIELD_ID);
        T entity;
        if (netexId == null) {
            entity = createNewEntity(input);
            entity.setProvider(getVerifiedProvider(Context.getVerifiedProviderId()));
        } else {
            entity = entityRepository.getOne(netexId);
            Preconditions.checkArgument(entity != null,
                    "Attempting to update Entity with netexId=%s, but Entity does not exist.", netexId);
        }


        populateEntityFromInput(entity, input);
        return entity;
    }


    protected abstract T createNewEntity(ArgumentWrapper input);

    protected abstract void populateEntityFromInput(T entity, ArgumentWrapper input);


    private Provider getVerifiedProvider(Long providerId) {
        Provider provider = providerRepository.getOne(providerId);
        Preconditions.checkArgument(provider != null,
                "Provider not found [pk=%s]", providerId);
        return provider;
    }

}
