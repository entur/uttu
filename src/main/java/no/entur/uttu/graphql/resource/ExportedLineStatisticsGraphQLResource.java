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

package no.entur.uttu.graphql.resource;

import graphql.GraphQL;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import javax.annotation.PostConstruct;
import no.entur.uttu.config.Context;
import no.entur.uttu.graphql.LinesGraphQLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@Path("exportedLineStatistics/graphql")
public class ExportedLineStatisticsGraphQLResource {

  @Autowired
  private LinesGraphQLSchema linesSchema;

  @Autowired
  private GraphQLResourceHelper graphQLResourceHelper;

  private GraphQL linesGraphQL;

  @PostConstruct
  public void init() {
    linesGraphQL = GraphQL.newGraphQL(linesSchema.getGraphQLSchema()).build();
  }

  @POST
  @SuppressWarnings("unchecked")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PreAuthorize(
    "@userContextService.isAdmin() or @userContextService.hasAccessToProvider(#request.get('variables').get('providerCode'))"
  )
  public Response executeLinesStatement(Map<String, Object> request) {
    try {
      return graphQLResourceHelper.executeStatement(linesGraphQL, request);
    } finally {
      Context.clear();
    }
  }
}
