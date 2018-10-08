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
import io.swagger.annotations.Api;
import no.entur.uttu.graphql.ProviderGraphQLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@Component
@Api
@Path("/providers/graphql")
public class ProviderGraphQLResource {

    @Autowired
    private ProviderGraphQLSchema providerSchema;

    private GraphQLResourceHelper graphQLResourceHelper;

    @PostConstruct
    public void init() {
        graphQLResourceHelper = new GraphQLResourceHelper(GraphQL.newGraphQL(providerSchema.graphQLSchema).build());
    }


    @POST
    @SuppressWarnings("unchecked")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeProviderStatement(HashMap<String, Object> request) {
        return graphQLResourceHelper.executeStatement(request);
    }


    @POST
    @Consumes("application/graphql")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeProviderStatement(String query) {
        return graphQLResourceHelper.getGraphQLResponse("query", query, new HashMap<>());
    }


}
