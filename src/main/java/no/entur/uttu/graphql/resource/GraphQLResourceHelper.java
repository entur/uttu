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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Sets;
import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.GraphQLException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import no.entur.uttu.error.CodedGraphQLError;
import org.rutebanken.helper.organisation.NotAuthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class GraphQLResourceHelper {

  private static final Logger logger = LoggerFactory.getLogger(
    LinesGraphQLResource.class
  );

  /**
   * Exception classes that should cause data fetching exceptions to be rethrown and mapped to corresponding HTTP status code outside transaction.
   */
  private static final Set<Class<? extends RuntimeException>> RETHROW_EXCEPTION_TYPES =
    Sets.newHashSet(
      NotAuthenticatedException.class,
      NotAuthorizedException.class,
      AccessDeniedException.class,
      DataIntegrityViolationException.class
    );

  private final TransactionTemplate transactionTemplate;

  public GraphQLResourceHelper(PlatformTransactionManager transactionManager) {
    org.springframework.util.Assert.notNull(
      transactionManager,
      "The 'transactionManager' argument must not be null."
    );
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  public Response executeStatement(GraphQL graphQL, Map<String, Object> request) {
    Map<String, Object> variables;
    if (request.get("variables") instanceof Map) {
      variables = (Map) request.get("variables");
    } else if (
      request.get("variables") instanceof String &&
      !((String) request.get("variables")).isEmpty()
    ) {
      String s = (String) request.get("variables");

      ObjectMapper mapper = new ObjectMapper();

      // convert JSON string to Map
      try {
        variables =
          mapper.readValue(
            s,
            TypeFactory
              .defaultInstance()
              .constructMapType(HashMap.class, String.class, Object.class)
          );
      } catch (IOException e) {
        ErrorResponseEntity content = new ErrorResponseEntity(e.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(content).build();
      }
    } else {
      variables = new HashMap<>();
    }
    return getGraphQLResponse(
      graphQL,
      (String) request.get("operationName"),
      (String) request.get("query"),
      variables
    );
  }

  public Response getGraphQLResponse(
    GraphQL graphQL,
    String operationName,
    String query,
    Map<String, Object> variables
  ) {
    return getGraphQLResponseInTransaction(graphQL, operationName, query, variables);
  }

  private Response getGraphQLResponseInTransaction(
    GraphQL graphQL,
    String operationName,
    String query,
    Map<String, Object> variables
  ) {
    return transactionTemplate.execute(transactionStatus -> {
      Response.ResponseBuilder res = Response.status(Response.Status.OK);
      HashMap<String, Object> content = new HashMap<>();
      try {
        ExecutionInput executionInput = ExecutionInput
          .newExecutionInput()
          .query(query)
          .operationName(operationName)
          .root(null)
          .variables(variables)
          .build();
        ExecutionResult executionResult = graphQL.execute(executionInput);

        if (!executionResult.getErrors().isEmpty()) {
          List<GraphQLError> errors = executionResult.getErrors();
          if (
            errors
              .stream()
              .anyMatch(error ->
                error.getErrorType().equals(ErrorType.DataFetchingException)
              )
          ) {
            logger.info(
              "Detected DataFetchingException from errors: {} Setting transaction to rollback only",
              errors
            );
            transactionStatus.setRollbackOnly();
          }

          errors =
            errors
              .stream()
              .map(exception ->
                exception instanceof ExceptionWhileDataFetching
                  ? new CodedGraphQLError((ExceptionWhileDataFetching) exception)
                  : exception
              )
              .collect(Collectors.toList());

          content.put("errors", errors);
        }
        if (executionResult.getData() != null) {
          content.put("data", executionResult.getData());
        }
      } catch (GraphQLException e) {
        logger.warn("Caught graphqlException. Setting rollback only", e);
        res = Response.status(getStatusCodeFromThrowable(e));
        content.put("errors", Arrays.asList(e));
      }
      removeErrorStackTraces(content);
      return res.entity(content).build();
    });
  }

  private void removeErrorStackTraces(Map<String, Object> content) {
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
    return errors
      .stream()
      .map(e -> {
        HashMap<String, Object> response = new HashMap<>();

        if (e instanceof GraphQLError) {
          GraphQLError graphQLError = (GraphQLError) e;
          response.put("message", graphQLError.getMessage());
          response.put("errorType", graphQLError.getErrorType());
          response.put("locations", graphQLError.getLocations());
          response.put("extensions", graphQLError.getExtensions());
          response.put("path", graphQLError.getPath());
        } else {
          if (e instanceof Exception) {
            response.put("message", ((Exception) e).getMessage());
          }
          response.put("errorType", e.getClass().getSimpleName());
        }

        return response;
      })
      .collect(Collectors.toList());
  }

  private Response.Status getStatusCodeFromThrowable(Throwable e) {
    Throwable rootCause = getRootCause(e);

    if (
      RETHROW_EXCEPTION_TYPES
        .stream()
        .anyMatch(c -> c.isAssignableFrom(rootCause.getClass()))
    ) {
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
