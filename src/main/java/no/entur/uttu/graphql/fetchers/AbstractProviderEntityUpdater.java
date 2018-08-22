package no.entur.uttu.graphql.fetchers;

import com.google.common.base.Preconditions;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_ID;

public abstract class AbstractProviderEntityUpdater<T extends ProviderEntity> implements DataFetcher<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractProviderEntityUpdater.class);


    private ProviderEntityRepository<T> repository;

    public AbstractProviderEntityUpdater(ProviderEntityRepository<T> repository) {
        this.repository = repository;
    }

    @Override
    public T get(DataFetchingEnvironment env) {

        ArgumentWrapper input = new ArgumentWrapper(env.getArgument("network"));

        String netexId = input.get(FIELD_ID);
        T entity;
        if (netexId == null) {
            entity = createNewEntity(input);
        } else {
            entity = repository.getOne(netexId);
            Preconditions.checkArgument(entity != null,
                    "Attempting to update Entity with netexId=%s, but Entity does not exist.", netexId);
            logger.info("Updating Entity[{}]", netexId);
        }

        populateEntityFromInput(entity, input);

        repository.save(entity);
        return entity;
    }


    protected abstract T createNewEntity(ArgumentWrapper input);

    protected abstract void populateEntityFromInput(T entity, ArgumentWrapper input);
}
