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

import no.entur.uttu.error.ErrorCodeEnumeration;
import no.entur.uttu.error.codederror.CodedError;
import no.entur.uttu.error.codederror.ConstraintViolationCodedError;
import no.entur.uttu.model.Constraints;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import javax.validation.Constraint;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class DataIntegrityViolationExceptionMapper implements ExceptionMapper<DataIntegrityViolationException> {

    private final Map<String, String> errorMessagePerConstraintMap;

    public DataIntegrityViolationExceptionMapper() {
        errorMessagePerConstraintMap = new HashMap<>();
        init();
    }

    @Override
    public Response toResponse(DataIntegrityViolationException e) {
        String errorMsg = null;
        if (e.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) e.getCause();

            errorMsg = errorMessagePerConstraintMap.get(constraintViolationException.getConstraintName());
            if (errorMsg == null) {
                errorMsg = constraintViolationException.getConstraintName();
            }

            return Response.status(Response.Status.OK)
                    .entity(new ErrorResponseEntity(errorMsg, new ConstraintViolationCodedError(constraintViolationException.getConstraintName())))
                    .build();
        }

        if (errorMsg == null) {
            errorMsg = e.getMessage();
        }

        return Response.status(Response.Status.OK)
                       .entity(new ErrorResponseEntity(errorMsg))
                       .build();
    }


    private void init() {
        errorMessagePerConstraintMap.put(Constraints.NETWORK_UNIQUE_NAME, "A Network with this name already exists");
        errorMessagePerConstraintMap.put(Constraints.FLEXIBLE_STOP_PLACE_UNIQUE_NAME, "A FlexibleStopPlace with this name already exists");
        errorMessagePerConstraintMap.put(Constraints.FLEXIBLE_LINE_UNIQUE_NAME, "A FlexibleLine with this name already exists");
        errorMessagePerConstraintMap.put(Constraints.JOURNEY_PATTERN_UNIQUE_NAME, "A JourneyPattern with this name already exists");
        errorMessagePerConstraintMap.put(Constraints.SERVICE_JOURNEY_UNIQUE_NAME, "A ServiceJourney with this name already exists");

        errorMessagePerConstraintMap.put(Constraints.CODESPACE_UNIQUE_XMLNS, "A Codespace with this xmlns already exists");
        errorMessagePerConstraintMap.put(Constraints.PROVIDER_UNIQUE_CODE, "A Provider with this code already exists");
    }
}
