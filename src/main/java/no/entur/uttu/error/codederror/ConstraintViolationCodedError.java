package no.entur.uttu.error.codederror;

import java.util.Map;
import no.entur.uttu.error.codes.ConstraintSubCodeEnumeration;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import no.entur.uttu.model.Constraints;

public class ConstraintViolationCodedError extends CodedError {

  private static final Map<String, ConstraintSubCodeEnumeration> constraintMap = Map.of(
    Constraints.FLEXIBLE_STOP_PLACE_UNIQUE_NAME,
    ConstraintSubCodeEnumeration.FLEXIBLE_STOP_PLACE_UNIQUE_NAME,
    Constraints.LINE_UNIQUE_NAME,
    ConstraintSubCodeEnumeration.LINE_UNIQUE_NAME,
    Constraints.JOURNEY_PATTERN_UNIQUE_NAME,
    ConstraintSubCodeEnumeration.JOURNEY_PATTERN_UNIQUE_NAME,
    Constraints.NETWORK_UNIQUE_NAME,
    ConstraintSubCodeEnumeration.NETWORK_UNIQUE_NAME,
    Constraints.SERVICE_JOURNEY_UNIQUE_NAME,
    ConstraintSubCodeEnumeration.SERVICE_JOURNEY_UNIQUE_NAME,
    Constraints.PROVIDER_UNIQUE_CODE,
    ConstraintSubCodeEnumeration.PROVIDER_UNIQUE_CODE,
    Constraints.CODESPACE_UNIQUE_XMLNS,
    ConstraintSubCodeEnumeration.CODESPACE_UNIQUE_XMLNS
  );

  public static ConstraintViolationCodedError fromConstraintName(String constraintName) {
    return new ConstraintViolationCodedError(constraintName);
  }

  private ConstraintViolationCodedError(String constraintName) {
    super(ErrorCodeEnumeration.CONSTRAINT_VIOLATION, getConstraint(constraintName), null);
  }

  private static ConstraintSubCodeEnumeration getConstraint(String constraintName) {
    return constraintMap.get(constraintName);
  }
}
