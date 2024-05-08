package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import no.entur.uttu.graphql.model.UserContext;
import no.entur.uttu.security.spi.UserContextService;
import org.springframework.stereotype.Component;

@Component
public class UserContextFetcher implements DataFetcher<UserContext> {

  private final UserContextService userContextService;

  public UserContextFetcher(UserContextService userContextService) {
    this.userContextService = userContextService;
  }

  @Override
  public UserContext get(DataFetchingEnvironment environment) throws Exception {
    return new UserContext(
      userContextService.getPreferredName(),
      userContextService.isAdmin()
    );
  }
}
