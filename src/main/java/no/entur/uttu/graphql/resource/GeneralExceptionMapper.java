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

import com.google.common.collect.Sets;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import org.rutebanken.helper.organisation.NotAuthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    GeneralExceptionMapper.class
  );

  private final Map<Response.Status, Set<Class<?>>> mapping;

  public GeneralExceptionMapper() {
    mapping = new EnumMap<>(Response.Status.class);
    mapping.put(
      Response.Status.BAD_REQUEST,
      Sets.newHashSet(
        ValidationException.class,
        OptimisticLockException.class,
        EntityNotFoundException.class,
        DataIntegrityViolationException.class
      )
    );
    mapping.put(Response.Status.CONFLICT, Sets.newHashSet(EntityExistsException.class));
    mapping.put(Response.Status.FORBIDDEN, Sets.newHashSet(AccessDeniedException.class));
    mapping.put(
      Response.Status.UNAUTHORIZED,
      Sets.newHashSet(NotAuthorizedException.class, NotAuthenticatedException.class)
    );
  }

  public Response toResponse(Exception ex) {
    Throwable rootCause = getRootCause(ex);
    int status;
    if (rootCause instanceof WebApplicationException webApplicationException) {
      status = webApplicationException.getResponse().getStatus();
    } else {
      status = toStatus(rootCause);
    }

    return Response.status(status)
      .entity(new ErrorResponseEntity(rootCause.getMessage()))
      .build();
  }

  protected int toStatus(Throwable e) {
    Integer entry = getMappedStatus(e);
    if (entry != null) {
      return entry;
    }
    LOGGER.error("Internal Server Error", e);
    return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
  }

  private Integer getMappedStatus(Throwable e) {
    for (Map.Entry<Response.Status, Set<Class<?>>> entry : mapping.entrySet()) {
      if (entry.getValue().stream().anyMatch(c -> c.isAssignableFrom(e.getClass()))) {
        return entry.getKey().getStatusCode();
      }
    }
    return null;
  }

  private Throwable getRootCause(Throwable e) {
    Throwable rootCause = e;

    // Use e if types is mapped
    if (getMappedStatus(e) != null) {
      return e;
    }
    if (
      e instanceof NestedRuntimeException nestedRuntimeException &&
      nestedRuntimeException.getRootCause() != null
    ) {
      rootCause = nestedRuntimeException.getRootCause();
    }

    return rootCause;
  }
}
