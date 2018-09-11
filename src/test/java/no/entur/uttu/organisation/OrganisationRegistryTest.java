package no.entur.uttu.organisation;

public class OrganisationRegistryTest {

// TODO remove med. Used for manual test
    public void t() {
        OrganisationRegistry organisationRegistry = new OrganisationRegistry("https://tjenester.entur.org/organisations/v1/organisations/");

        Organisation org = organisationRegistry.getOrganisation("20");
        toString();
    }
}
