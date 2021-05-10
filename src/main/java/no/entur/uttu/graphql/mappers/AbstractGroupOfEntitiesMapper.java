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
