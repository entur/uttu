package no.entur.uttu.organisation;

import java.util.List;
import java.util.Optional;
import org.rutebanken.netex.model.GeneralOrganisation;

public interface OrganisationRegistry {
  List<GeneralOrganisation> getOrganisations();
  Optional<GeneralOrganisation> getOrganisation(String id);
  String getVerifiedOperatorRef(String operatorRef);
  String getVerifiedAuthorityRef(String authorityRef);
}
