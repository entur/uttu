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

package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.mappers.AbstractProviderEntityMapper;
import no.entur.uttu.model.ProviderEntity;
import no.entur.uttu.repository.generic.ProviderEntityRepository;
import org.springframework.transaction.annotation.Transactional;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_ID;
import static no.entur.uttu.graphql.GraphQLNames.FIELD_INPUT;

public abstract class AbstractProviderEntityUpdater<T extends ProviderEntity> implements DataFetcher<T> {

    protected AbstractProviderEntityMapper<T> mapper;

    protected ProviderEntityRepository<T> repository;

    public AbstractProviderEntityUpdater(AbstractProviderEntityMapper<T> mapper, ProviderEntityRepository<T> repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    @Transactional
    public T get(DataFetchingEnvironment env) {
        if (env.getField().getName().startsWith("delete")) {
            return deleteEntity(env);
        } else {
            return saveEntity(env);
        }
    }

    protected T deleteEntity(DataFetchingEnvironment env) {
        String id = env.getArgument(FIELD_ID);
        verifyDeleteAllowed(id);
        return repository.delete(id);
    }

    protected T saveEntity(DataFetchingEnvironment env) {
        T entity = mapper.map(env.getArgument(FIELD_INPUT));
        entity.checkPersistable();
        return repository.save(entity);
    }


    protected void verifyDeleteAllowed(String id) {

    }
}
