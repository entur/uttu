package no.entur.uttu.graphql.fetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.ArrayList;
import java.util.List;
import no.entur.uttu.graphql.model.Organisation;
import no.entur.uttu.organisation.spi.OrganisationRegistry;
import org.rutebanken.netex.model.OrganisationTypeEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("organisationsFetcher")
public class OrganisationsFetcher implements DataFetcher<List<Organisation>> {

  @Autowired
  OrganisationRegistry organisationRegistry;

  @Override
  public List<Organisation> get(DataFetchingEnvironment environment) throws Exception {
    List<Organisation> organisations = new ArrayList<>();

    organisationRegistry
      .getAuthorities()
      .forEach(authority ->
        organisations.add(
          new Organisation(
            authority.getId(),
            authority.getName(),
            authority.getLegalName(),
            OrganisationTypeEnumeration.AUTHORITY
          )
        )
      );

    organisationRegistry
      .getOperators()
      .forEach(operator ->
        organisations.add(
          new Organisation(
            operator.getId(),
            operator.getName(),
            operator.getLegalName(),
            OrganisationTypeEnumeration.OPERATOR
          )
        )
      );
    return organisations;
  }
}
