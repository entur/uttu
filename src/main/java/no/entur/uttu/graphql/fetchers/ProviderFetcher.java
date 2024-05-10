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

  @Autowired
  private ProviderRepository repository;

  @Override
  @PostFilter("@userContextService.hasAccessToProvider(filterObject.getCode())")
  public List<Provider> get(DataFetchingEnvironment dataFetchingEnvironment) {
    return repository.findAll();
  }
}
