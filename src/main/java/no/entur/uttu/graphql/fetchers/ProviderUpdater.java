package no.entur.uttu.graphql.fetchers;

import com.google.common.base.Preconditions;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.model.CodeSpace;
import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.CodeSpaceRepository;
import no.entur.uttu.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import static no.entur.uttu.graphql.GraphQLNames.*;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;

@Component
public class ProviderUpdater implements DataFetcher<Provider> {

    @Autowired
    private ProviderRepository repository;
    @Autowired
    private CodeSpaceRepository codeSpaceRepository;

    @Override
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "')")
    public Provider get(DataFetchingEnvironment env) {
        ArgumentWrapper input = new ArgumentWrapper(env.getArgument("input"));
        Long id = input.get(FIELD_ID);
        Provider entity;
        if (id == null) {
            entity = new Provider();
        } else {
            entity = repository.getOne(id);
            Preconditions.checkArgument(entity != null,
                    "Attempting to update Provider with id=%s, but Provider does not exist.", id);
        }

        populateEntityFromInput(entity, input);

        return repository.save(entity);
    }

    private void populateEntityFromInput(Provider entity, ArgumentWrapper input) {
        input.apply(FIELD_NAME, entity::setName);
        input.apply(FIELD_CODE_SPACE_REF, this::getVerifiedCodeSpace, entity::setCodeSpace);
    }

    private CodeSpace getVerifiedCodeSpace(Long codeSpaceId) {
        CodeSpace codeSpace = codeSpaceRepository.getOne(codeSpaceId);
        Preconditions.checkArgument(codeSpace != null,
                "CodeSpace not found [pk=%s]", codeSpaceId);
        return codeSpace;
    }
}
