package no.entur.uttu.graphql.fetchers;

import com.google.common.base.Preconditions;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.Codespace;
import no.entur.uttu.repository.CodeSpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Map;

import static no.entur.uttu.graphql.GraphQLNames.*;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;

@Component
public class CodeSpaceUpdater implements DataFetcher<Codespace> {

    @Autowired
    private CodeSpaceRepository repository;

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
