package no.entur.uttu.error;

public enum ErrorCodeEnumeration {

    /**
     * Organisation is not a valid operator
     */
    ORGANISATION_NOT_VALID_OPERATOR,

    /**
     * Service journey must have operator or inherit one from flexible line
     */
    MISSING_OPERATOR,

    /**
     * Found no valid flexible lines in data space, while exporting
     */
    NO_VALID_FLEXIBLE_LINES_IN_DATA_SPACE,

    /**
     * Logical error: Provided from date was after to date
     */
    FROM_DATE_AFTER_TO_DATE,

    /**
     * Entity is referenced by other entities
     *
     * @see no.entur.uttu.error.codederror.ConstraintViolationCodedError
     */
    CONSTRAINT_VIOLATION
}
