package no.entur.uttu.graphql.resource;

import graphql.GraphQL;
import io.swagger.annotations.Api;
import no.entur.uttu.graphql.ProviderGraphQLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN;

@Component
@Api
@Path("/providers")
public class ProviderGraphQLResource {

    @Autowired
    private ProviderGraphQLSchema providerSchema;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private GraphQLResourceHelper graphQLResourceHelper;

    @PostConstruct
    public void init() {
        org.springframework.util.Assert.notNull(transactionManager, "The 'transactionManager' argument must not be null.");
        graphQLResourceHelper = new GraphQLResourceHelper(GraphQL.newGraphQL(providerSchema.graphQLSchema).build(), new TransactionTemplate(transactionManager));
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
        return graphQLResourceHelper.getGraphQLResponseInTransaction("query", query, new HashMap<>());
    }


}
