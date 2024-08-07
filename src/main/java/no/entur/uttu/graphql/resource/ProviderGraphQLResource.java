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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import no.entur.uttu.graphql.ProviderGraphQLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@Path("/providers/graphql")
public class ProviderGraphQLResource {

  @Autowired
  private ProviderGraphQLSchema providerSchema;

  private GraphQL providerGraphQL;

  @Autowired
  private GraphQLResourceHelper graphQLResourceHelper;

  @PostConstruct
  public void init() {
    providerGraphQL = GraphQL.newGraphQL(providerSchema.getGraphQLSchema()).build();
  }

  @POST
  @SuppressWarnings("unchecked")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PreAuthorize("isAuthenticated()")
  public Response executeProviderStatement(HashMap<String, Object> request) {
    return graphQLResourceHelper.executeStatement(providerGraphQL, request);
  }

  @POST
  @Consumes("application/graphql")
  @Produces(MediaType.APPLICATION_JSON)
  @PreAuthorize("isAuthenticated()")
  public Response executeProviderStatement(String query) {
    return graphQLResourceHelper.getGraphQLResponse(
      providerGraphQL,
      "query",
      query,
      new HashMap<>()
    );
  }
}
