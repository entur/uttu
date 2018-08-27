package no.entur.uttu.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Sets;
import graphql.ErrorType;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.GraphQLException;
import io.swagger.annotations.Api;
import no.entur.uttu.config.ProviderAuthenticationService;
import no.entur.uttu.config.Context;
import org.rutebanken.helper.organisation.NotAuthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.rutebanken.helper.organisation.AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;

@Component
@Api
@Path("{providerId}")
public class GraphQLResource {


    private static final Logger logger = LoggerFactory.getLogger(GraphQLResource.class);

    /**
     * Exception classes that should cause data fetching exceptions to be rethrown and mapped to corresponding HTTP status code outside transaction.
     */
    private static final Set<Class<? extends RuntimeException>> RETHROW_EXCEPTION_TYPES
            = Sets.newHashSet(NotAuthenticatedException.class, NotAuthorizedException.class, AccessDeniedException.class, DataIntegrityViolationException.class);

    @Autowired
    private FlexibleLineGraphQLSchema graphQLSchema;

    @Autowired
    private ProviderAuthenticationService providerAuthenticationService;

    private final TransactionTemplate transactionTemplate;


    public GraphQLResource(PlatformTransactionManager transactionManager) {
        org.springframework.util.Assert.notNull(transactionManager, "The 'transactionManager' argument must not be null.");
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @PostConstruct
    public void init() {
        graphQL = GraphQL.newGraphQL(graphQLSchema.graphQLSchema).build();
    }

    private GraphQL graphQL;


    @POST
    @SuppressWarnings("unchecked")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGraphQL(@PathParam("providerId") Long providerId, HashMap<String, Object> request) {
        Map<String, Object> variables;

        providerAuthenticationService.hasRoleForProvider(SecurityContextHolder.getContext().getAuthentication(), ROLE_ROUTE_DATA_EDIT, providerId);

        if (request.get("variables") instanceof Map) {
            variables = (Map) request.get("variables");

        } else if (request.get("variables") instanceof String && !((String) request.get("variables")).isEmpty()) {
            String s = (String) request.get("variables");

            ObjectMapper mapper = new ObjectMapper();

            // convert JSON string to Map
            try {
                variables = mapper.readValue(s, TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class));
            } catch (IOException e) {
                HashMap<String, Object> content = new HashMap<>();
                content.put("errors", e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(content).build();
            }

        } else {
            variables = new HashMap<>();
        }
        return getGraphQLResponseInTransaction(providerId, (String) request.get("operationName"), (String) request.get("query"), variables);
    }

    @POST
    @Consumes("application/graphql")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGraphQL(@PathParam("providerId") Long providerId, String query) {
        return getGraphQLResponseInTransaction(providerId, "query", query, new HashMap<>());
    }

    /**
     * Use programmatic transaction because graphql catches RuntimeExceptions.
     * With multiple transaction interceptors (Transactional annotation), this causes the rolled back transaction (in case of errors) to be commed (? TODO from tiamat) by the outer transaction interceptor.
     * NRP-1992
     */
    private Response getGraphQLResponseInTransaction(Long providerId, String operationName, String query, Map<String, Object> variables) {
        return transactionTemplate.execute((transactionStatus) -> getGraphQLResponse(providerId, operationName, query, variables, transactionStatus));
    }

    private Response getGraphQLResponse(Long providerId, String operationName, String query, Map<String, Object> variables, TransactionStatus transactionStatus) {
        Response.ResponseBuilder res = Response.status(Response.Status.OK);
        HashMap<String, Object> content = new HashMap<>();
        try {
            Context.setProvider(providerId);
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                                                    .query(query)
                                                    .operationName(operationName)
                                                    .context(providerId)
                                                    .root(null)
                                                    .variables(variables)
                                                    .build();
            ExecutionResult executionResult = graphQL.execute(executionInput);

            if (!executionResult.getErrors().isEmpty()) {
                List<GraphQLError> errors = executionResult.getErrors();
                if (errors.stream().anyMatch(error -> error.getErrorType().equals(ErrorType.DataFetchingException))) {
                    logger.warn("Detected DataFetchingException from errors: {} Setting transaction to rollback only", errors);
                    transactionStatus.setRollbackOnly();
                }

                content.put("errors", errors);
            }
            if (executionResult.getData() != null) {
                content.put("data", executionResult.getData());
            }


        } catch (GraphQLException e) {
            logger.warn("Caught graphqlException. Setting rollback only", e);
            res = Response.status(Response.Status.INTERNAL_SERVER_ERROR);

            content.put("errors", Arrays.asList(e));
            transactionStatus.setRollbackOnly();
        } finally {
            Context.clear();
        }
        // TODO from tiamat: needed? removeErrorStacktraces(content);
        return res.entity(content).build();
    }

    private Response.Status getStatusCodeFromThrowable(Throwable e) {
        Throwable rootCause = getRootCause(e);

        if (RETHROW_EXCEPTION_TYPES.stream().anyMatch(c -> c.isAssignableFrom(rootCause.getClass()))) {
            throw (RuntimeException) rootCause;
        }

        if (IllegalArgumentException.class.isAssignableFrom(rootCause.getClass())) {
            return Response.Status.BAD_REQUEST;
        }

        return Response.Status.OK;
    }

    private Throwable getRootCause(Throwable e) {
        Throwable rootCause = e;

        if (e instanceof NestedRuntimeException) {
            NestedRuntimeException nestedRuntimeException = ((NestedRuntimeException) e);
            if (nestedRuntimeException.getRootCause() != null) {
                rootCause = nestedRuntimeException.getRootCause();
            }
        }
        return rootCause;
    }


}
