package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import no.entur.uttu.organisation.spi.OrganisationRegistry;
import org.rutebanken.netex.model.GeneralOrganisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("organisationsFetcher")
public class OrganisationsFetcher implements DataFetcher<List<GeneralOrganisation>> {

  @Autowired
  OrganisationRegistry organisationRegistry;

  @Override
  public List<GeneralOrganisation> get(DataFetchingEnvironment environment)
    throws Exception {
    return organisationRegistry.getOrganisations();
  }
}
