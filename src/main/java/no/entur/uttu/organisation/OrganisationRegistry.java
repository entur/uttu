package no.entur.uttu.organisation;

import org.rutebanken.netex.model.GeneralOrganisation;

import java.util.List;
import java.util.Optional;

public interface OrganisationRegistry {
    List<GeneralOrganisation> getOrganisations();
    Optional<GeneralOrganisation> getOrganisation(String id);
    String getVerifiedOperatorRef(String operatorRef);
    String getVerifiedAuthorityRef(String authorityRef);
}
