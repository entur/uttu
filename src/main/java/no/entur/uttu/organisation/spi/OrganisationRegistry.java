package no.entur.uttu.organisation.spi;

import java.util.List;
import java.util.Optional;
import no.entur.uttu.error.codedexception.CodedIllegalArgumentException;
import org.rutebanken.netex.model.GeneralOrganisation;

/**
 * Represents an organisation registry used to populate authorities and operators references
 */
public interface OrganisationRegistry {
  /**
   * Get a list of all organisations in the registry
   */
  List<GeneralOrganisation> getOrganisations();

  /**
   * Get an organisation with the given ID, which may not exist
   */
  Optional<GeneralOrganisation> getOrganisation(String id);

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
