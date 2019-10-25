package no.entur.uttu.error.codederror;

import no.entur.uttu.error.ErrorCodeEnumeration;
import no.entur.uttu.error.codederror.CodedError;

import java.util.Map;

public class ConstraintViolationCodedError extends CodedError {
    private static final String NUMBER_OF_REFERENCES_KEY = "numberOfReferences";

    public ConstraintViolationCodedError(int numberOfReferences) {
        super(
                ErrorCodeEnumeration.CONSTRAINT_VIOLATION,
                Map.of(
                    NUMBER_OF_REFERENCES_KEY,
                    numberOfReferences
                )
        );
    }
}
