package no.entur.uttu.error.codederror;

import java.util.Map;
import no.entur.uttu.error.codes.ErrorCodeEnumeration;

public class FlexibleAreaValidationCodedError extends CodedError {

  private static final String VALIDATION_MESSAGE_KEY = "validationMessage";

  public static FlexibleAreaValidationCodedError fromValidationMessage(
    String validationMessage
  ) {
    return new FlexibleAreaValidationCodedError(validationMessage);
  }

  private FlexibleAreaValidationCodedError(String validationMessage) {
    super(
      ErrorCodeEnumeration.FLEXIBLE_AREA_VALIDATION_FAILED,
      null,
      Map.of(VALIDATION_MESSAGE_KEY, validationMessage)
    );
  }
}
