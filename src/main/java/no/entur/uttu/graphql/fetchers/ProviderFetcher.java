package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;

@Component
public class ProviderFetcher implements DataFetcher<List<Provider>> {

    @Autowired
    private ProviderRepository repository;

    @Override
    @PostFilter("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',filterObject.getCode())")
    public List<Provider> get(DataFetchingEnvironment dataFetchingEnvironment) {
        return repository.findAll();
    }
}
