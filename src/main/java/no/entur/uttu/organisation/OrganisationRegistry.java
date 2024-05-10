package no.entur.uttu.organisation;

import java.util.List;
import java.util.Optional;
import org.rutebanken.netex.model.GeneralOrganisation;

/**
 *
 */
public interface OrganisationRegistry {

  /**
   *
   * @return
   */
  List<GeneralOrganisation> getOrganisations();

  /**
   *
   * @param id
   * @return
   */
  Optional<GeneralOrganisation> getOrganisation(String id);

  /**
   *
   * @param operatorRef
   * @return
   */
  String getVerifiedOperatorRef(String operatorRef);

  /**
   *
   * @param authorityRef
   * @return
   */
  String getVerifiedAuthorityRef(String authorityRef);
}
