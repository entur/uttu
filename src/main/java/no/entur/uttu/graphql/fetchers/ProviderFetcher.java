package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Component;

@Component
public class ProviderFetcher implements DataFetcher<List<Provider>> {

  private static final String ROR_UTTU_PROVIDERS = "ror-nplan-providers";

  @Autowired
  private ProviderRepository repository;

  @Override
  /*@PostFilter(
          "hasRole('" +
                  ROLE_ROUTE_DATA_ADMIN +
                  "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" +
                  ROLE_ROUTE_DATA_EDIT +
                  "',filterObject.getCode())"
  )*/
  @PostFilter("hasPermission('" + ROR_UTTU_PROVIDERS + "', 'les')")
  public List<Provider> get(DataFetchingEnvironment dataFetchingEnvironment) {
    return repository.findAll();
  }
}
