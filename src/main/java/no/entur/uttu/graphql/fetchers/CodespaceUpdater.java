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

import com.google.common.base.Preconditions;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Codespace;
import no.entur.uttu.repository.CodespaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.*;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;

@Component
public class CodespaceUpdater implements DataFetcher<Codespace> {

    @Autowired
    private CodespaceRepository repository;

    @Override
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "')")
    public Codespace get(DataFetchingEnvironment env) {

        ArgumentWrapper input = new ArgumentWrapper(env.getArgument(FIELD_INPUT));
        Long id = input.get(FIELD_ID);
        Codespace entity;
        if (id == null) {
            entity = new Codespace();
        } else {
            entity = repository.getOne(id);
            Preconditions.checkArgument(entity != null,
                    "Attempting to update Codespace with id=%s, but Codespace does not exist.", id);
        }

        populateEntityFromInput(entity, input);

        return repository.save(entity);
    }

    private void populateEntityFromInput(Codespace entity, ArgumentWrapper input) {
        input.apply(FIELD_XMLNS, entity::setXmlns);
        input.apply(FIELD_XMLNS_URL, entity::setXmlnsUrl);
    }

}
