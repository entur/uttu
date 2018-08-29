package no.entur.uttu.graphql.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Sets;
import graphql.ErrorType;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.GraphQLException;
import no.entur.uttu.config.Context;
import org.rutebanken.helper.organisation.NotAuthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class GraphQLResourceHelper {
    private static final Logger logger = LoggerFactory.getLogger(FlexibleTransportGraphQLResource.class);

    /**
     * Exception classes that should cause data fetching exceptions to be rethrown and mapped to corresponding HTTP status code outside transaction.
     */
    private static final Set<Class<? extends RuntimeException>> RETHROW_EXCEPTION_TYPES
            = Sets.newHashSet(NotAuthenticatedException.class, NotAuthorizedException.class, AccessDeniedException.class, DataIntegrityViolationException.class);

    private GraphQL graphQL;

    private TransactionTemplate transactionTemplate;

    public GraphQLResourceHelper(GraphQL graphQL, TransactionTemplate transactionTemplate) {
        this.graphQL = graphQL;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Use programmatic transaction because graphql catches RuntimeExceptions.
     * With multiple transaction interceptors (Transactional annotation), this causes the rolled back transaction (in case of errors) to be commed (? TODO from tiamat) by the outer transaction interceptor.
     * NRP-1992
     */
    public Response getGraphQLResponseInTransaction(String operationName, String query, Map<String, Object> variables) {
        return transactionTemplate.execute((transactionStatus) -> getGraphQLResponse(operationName, query, variables, transactionStatus));
    }


    public Response executeStatement(Map<String, Object> request) {
        Map<String, Object> variables;
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
        return getGraphQLResponseInTransaction((String) request.get("operationName"), (String) request.get("query"), variables);
    }

    public Response getGraphQLResponse(String operationName, String query, Map<String, Object> variables, TransactionStatus transactionStatus) {
        Response.ResponseBuilder res = Response.status(Response.Status.OK);
        HashMap<String, Object> content = new HashMap<>();
        try {
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                                                    .query(query)
                                                    .operationName(operationName)
                                                    .context(null)
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
        removeErrorStacktraces(content);
        return res.entity(content).build();
    }

    private void removeErrorStacktraces(Map<String, Object> content) {
        if (content.containsKey("errors")) {
            @SuppressWarnings("unchecked")
            List<GraphQLError> errors = (List<GraphQLError>) content.get("errors");

            try {
                content.put("errors", mapErrors(errors));
            } catch (Exception e) {
                logger.warn("Exception caught during stacktrace removal", e);
            }
        }
    }

    private List<Map<String, Object>> mapErrors(Collection<?> errors) {
        return errors.stream().map(e -> {
            HashMap<String, Object> response = new HashMap<>();

            if (e instanceof GraphQLError) {
                GraphQLError graphQLError = (GraphQLError) e;
                response.put("message", graphQLError.getMessage());
                response.put("errorType", graphQLError.getErrorType());
                response.put("locations", graphQLError.getLocations());
                response.put("path", graphQLError.getPath());
            } else {
                if (e instanceof Exception) {
                    response.put("message", ((Exception) e).getMessage());
                }
                response.put("errorType", e.getClass().getSimpleName());
            }

            return response;
        }).collect(Collectors.toList());
    }


    // TODO from tiamat, do we need this? only if access control is invoked in graphql
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
