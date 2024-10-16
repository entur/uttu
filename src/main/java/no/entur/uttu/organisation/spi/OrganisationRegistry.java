package no.entur.uttu.organisation.spi;

import java.util.List;
import java.util.Optional;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.Operator;

/**
 * Represents an organisation registry used to populate authorities and operators references
 */
public interface OrganisationRegistry {
  /**
   * Get a list of all authorities in the registry
   */
  List<Authority> getAuthorities();

  /**
   * Get an authority with the given ID, which may not exist
   */
  Optional<Authority> getAuthority(String id);

  /**
   * Get a list of all operators in the registry
   */
  List<Operator> getOperators();

  /**
   * Get an operator with the given ID, which may not exist
   */
  Optional<Operator> getOperator(String id);

  /**
   * Check if the organisation represented by the operator reference id is a valid operator
   * @param operatorRef The organisation id
   * @throws CodedIllegalArgumentException if the organisation is not a valid operator
   */
  void validateOperatorRef(String operatorRef);

  /**
   * Check if the organisation represented by the operator reference id is a valid authority
   * @param authorityRef The organisation id
   * @throws CodedIllegalArgumentException if the organisation is not a valid authority
   */
  void validateAuthorityRef(String authorityRef);
}
