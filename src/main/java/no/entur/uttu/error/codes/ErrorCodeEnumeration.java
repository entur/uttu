package no.entur.uttu.error.codes;

import no.entur.uttu.error.ErrorCode;
import no.entur.uttu.error.codederror.ConstraintViolationCodedError;
import no.entur.uttu.error.codederror.EntityHasReferencesCodedError;

public enum ErrorCodeEnumeration implements ErrorCode {
  /**
   * Organisation is not a valid operator
   */
  ORGANISATION_NOT_VALID_OPERATOR,

  /**
   * Organisation not found in the organisation registry
   */
  ORGANISATION_NOT_IN_ORGANISATION_REGISTRY,

  /**
   * Service journey must have operator or inherit one from flexible line
   */
  MISSING_OPERATOR,

  /**
   * Found no valid lines in data space, while exporting
   */
  NO_VALID_LINES_IN_DATA_SPACE,

  /**
   * Logical error: Provided from date was after to date
   */
  FROM_DATE_AFTER_TO_DATE,

  /**
   * Entity is referenced by other entities
   *
   * @see EntityHasReferencesCodedError
   */
  ENTITY_IS_REFERENCED,

  /**
   * Journey pattern does not meet requirement of minimum points in sequence
   */
  MINIMUM_POINTS_IN_SEQUENCE,

  /**
   * Encountered a database constraint violation
   *
   * @see ConstraintViolationCodedError
   */
  CONSTRAINT_VIOLATION,

  /**
   * StopPointInPattern on fixed lines may only have quayRef not flexibleStopPlace
   */
  FLEXIBLE_STOP_PLACE_NOT_ALLOWED,

  /**
   * Notices can't be empty
   */
  NO_EMPTY_NOTICES,

  /**
   * Flexible lines must have booking information on line, journey pattern and/or service journey
   */
  FLEXIBLE_LINE_REQUIRES_BOOKING,

  /**
   * Flexible areas must have polygons with valid geometry
   */
  INVALID_POLYGON,

  /**
   * If supplied stop place filter isn't something we support
   */
  INVALID_STOP_PLACE_FILTER,

  /**
   * Flexible area validation failed - area marked as UnrestrictedPublicTransportAreas must contain valid stop places
   */
  FLEXIBLE_AREA_VALIDATION_FAILED,
}
