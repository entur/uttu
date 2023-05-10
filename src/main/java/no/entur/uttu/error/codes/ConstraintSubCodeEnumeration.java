package no.entur.uttu.error.codes;

import no.entur.uttu.error.ErrorCode;
import no.entur.uttu.error.SubCode;

public enum ConstraintSubCodeEnumeration implements SubCode {
  FLEXIBLE_STOP_PLACE_UNIQUE_NAME,
  LINE_UNIQUE_NAME,
  JOURNEY_PATTERN_UNIQUE_NAME,
  NETWORK_UNIQUE_NAME,
  SERVICE_JOURNEY_UNIQUE_NAME,
  PROVIDER_UNIQUE_CODE,
  CODESPACE_UNIQUE_XMLNS,
}
