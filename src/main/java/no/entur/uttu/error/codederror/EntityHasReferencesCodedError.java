package no.entur.uttu.error.codederror;

import no.entur.uttu.error.codes.ErrorCodeEnumeration;

import java.util.Map;

public class EntityHasReferencesCodedError extends CodedError {
    private static final String NUMBER_OF_REFERENCES_KEY = "numberOfReferences";

    public static EntityHasReferencesCodedError fromNumberOfReferences(int numberOfReferences) {
        return new EntityHasReferencesCodedError(numberOfReferences);
    }

    private EntityHasReferencesCodedError(int numberOfReferences) {
        super(
                ErrorCodeEnumeration.ENTITY_IS_REFERENCED,
                null,
                Map.of(
                    NUMBER_OF_REFERENCES_KEY,
                    numberOfReferences
                )
        );
    }
}
