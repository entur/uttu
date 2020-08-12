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
import no.entur.uttu.config.Context;
import no.entur.uttu.graphql.LinesGraphQLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;
import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;

@Component
@Api
@Path("/{providerCode}/graphql")
public class LinesGraphQLResource {

    @Autowired
    private LinesGraphQLSchema timetableEditorSchema;

    @Autowired
    private GraphQLResourceHelper graphQLResourceHelper;

    private GraphQL timetableEditorGraphQL;

    @PostConstruct
    public void init() {
        timetableEditorGraphQL = GraphQL.newGraphQL(timetableEditorSchema.graphQLSchema).build();
    }

    @POST
    @SuppressWarnings("unchecked")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerCode)")
    public Response executeTimetableEditorStatement(@PathParam("providerCode") String providerCode, HashMap<String, Object> request) {
        Context.setProvider(providerCode);
        try {
            return graphQLResourceHelper.executeStatement(timetableEditorGraphQL, request);
        } finally {
            Context.clear();
        }
    }

    @POST
    @Consumes("application/graphql")
    @Produces(MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerCode)")
    public Response executeTimetableEditorStatement(@PathParam("providerCode") String providerCode, String query) {
        Context.setProvider(providerCode);
        try {
            return graphQLResourceHelper.getGraphQLResponse(timetableEditorGraphQL, "query", query, new HashMap<>());
        } finally {
            Context.clear();
        }
    }
}
