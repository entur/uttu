package no.entur.uttu.error.codederror;

import no.entur.uttu.error.ErrorCodeEnumeration;
import no.entur.uttu.model.Constraints;

import javax.annotation.PostConstruct;
import java.util.Map;

public class ConstraintViolationCodedError extends CodedError {
    private static final String CONSTRAINT_NAME_KEY = "constraint";

    private static final Map<String, ConstraintEnumeration> constraintMap = Map.of(
            Constraints.FLEXIBLE_STOP_PLACE_UNIQUE_NAME,  ConstraintEnumeration.FLEXIBLE_STOP_PLACE_UNIQUE_NAME,
            Constraints.FLEXIBLE_LINE_UNIQUE_NAME, ConstraintEnumeration.FLEXIBLE_LINE_UNIQUE_NAME,
            Constraints.JOURNEY_PATTERN_UNIQUE_NAME, ConstraintEnumeration.JOURNEY_PATTERN_UNIQUE_NAME,
            Constraints.NETWORK_UNIQUE_NAME, ConstraintEnumeration.NETWORK_UNIQUE_NAME,
            Constraints.SERVICE_JOURNEY_UNIQUE_NAME, ConstraintEnumeration.SERVICE_JOURNEY_UNIQUE_NAME,
            Constraints.PROVIDER_UNIQUE_CODE, ConstraintEnumeration.PROVIDER_UNIQUE_CODE,
            Constraints.CODESPACE_UNIQUE_XMLNS, ConstraintEnumeration.CODESPACE_UNIQUE_XMLNS
    );

    public ConstraintViolationCodedError(String constraintName) {
        super(ErrorCodeEnumeration.CONSTRAINT_VIOLATION,
                Map.of(
                        CONSTRAINT_NAME_KEY,
                        getConstraint(constraintName)
                )
        );
    }

    private static ConstraintEnumeration getConstraint(String constraintName) {
        return constraintMap.get(constraintName);
    }

    private enum ConstraintEnumeration {
        FLEXIBLE_STOP_PLACE_UNIQUE_NAME,
        FLEXIBLE_LINE_UNIQUE_NAME,
        JOURNEY_PATTERN_UNIQUE_NAME,
        NETWORK_UNIQUE_NAME,
        SERVICE_JOURNEY_UNIQUE_NAME,
        PROVIDER_UNIQUE_CODE,
        CODESPACE_UNIQUE_XMLNS
    }


}
