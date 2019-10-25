package no.entur.uttu.error;

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
