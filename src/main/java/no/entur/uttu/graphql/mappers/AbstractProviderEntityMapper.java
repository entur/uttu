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
            entity.setProvider(getVerifiedProvider(Context.getVerifiedProviderCode()));
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


    private Provider getVerifiedProvider(String providerCode) {
        Provider provider = providerRepository.getOne(providerCode);
        Preconditions.checkArgument(provider != null,
                "Provider not found [code=%s]", providerCode);
        return provider;
    }

}
