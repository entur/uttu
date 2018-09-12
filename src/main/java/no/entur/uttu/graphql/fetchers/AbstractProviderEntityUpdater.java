package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.repository.generic.ProviderEntityRepository;

public abstract class AbstractProviderEntityUpdater<T extends ProviderEntity> implements DataFetcher<T> {

    private AbstractProviderEntityMapper<T> mapper;

    private ProviderEntityRepository<T> repository;

    public AbstractProviderEntityUpdater(AbstractProviderEntityMapper<T> mapper, ProviderEntityRepository<T> repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public T get(DataFetchingEnvironment env) {
        T entity = mapper.map(env.getArgument("input"));

        entity.checkPersistable();

        return repository.save(entity);
    }


}
