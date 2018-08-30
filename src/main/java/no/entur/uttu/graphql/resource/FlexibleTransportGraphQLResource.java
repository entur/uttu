package no.entur.uttu.graphql.resource;

import graphql.GraphQL;
import io.swagger.annotations.Api;
import no.entur.uttu.config.Context;
import no.entur.uttu.graphql.FlexibleTransportGraphQLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
@Path("/flexible-transport")
public class FlexibleTransportGraphQLResource {

    @Autowired
    private FlexibleTransportGraphQLSchema flexibleLineSchema;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private GraphQLResourceHelper graphQLResourceHelper;

    @PostConstruct
    public void init() {
        org.springframework.util.Assert.notNull(transactionManager, "The 'transactionManager' argument must not be null.");
        graphQLResourceHelper = new GraphQLResourceHelper(GraphQL.newGraphQL(flexibleLineSchema.graphQLSchema).build(), new TransactionTemplate(transactionManager));
    }


    @POST
    @SuppressWarnings("unchecked")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{providerId}")
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerId)")
    public Response executeFlexibleLineStatement(@PathParam("providerId") Long providerId, HashMap<String, Object> request) {
        Context.setProvider(providerId);
        try {
            return graphQLResourceHelper.executeStatement(request);
        } finally {
            Context.clear();
        }
    }


    @POST
    @Consumes("application/graphql")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{providerId}")
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "') or @providerAuthenticationService.hasRoleForProvider(authentication,'" + ROLE_ROUTE_DATA_EDIT + "',#providerId)")
    public Response executeFlexibleLineStatement(@PathParam("providerId") Long providerId, String query) {
        Context.setProvider(providerId);
        try {
            return graphQLResourceHelper.getGraphQLResponseInTransaction("query", query, new HashMap<>());
        } finally {
            Context.clear();
        }
    }

    @POST
    @SuppressWarnings("unchecked")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "')")
    public Response executeProviderStatement(HashMap<String, Object> request) {
        return graphQLResourceHelper.executeStatement(request);
    }


    @POST
    @Consumes("application/graphql")
    @Produces(MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('" + ROLE_ROUTE_DATA_ADMIN + "')")
    public Response executeProviderStatement(String query) {
        return graphQLResourceHelper.getGraphQLResponseInTransaction("query", query, new HashMap<>());
    }


}
