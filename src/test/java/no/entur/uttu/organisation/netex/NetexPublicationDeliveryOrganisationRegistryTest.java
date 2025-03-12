package no.entur.uttu.organisation.netex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NetexPublicationDeliveryOrganisationRegistryTest {

  @Test
  void testCanQueryOrganisations() {
    var registry = new NetexPublicationDeliveryFileOrganisationRegistry(
      "src/test/resources/fixtures/organisations.xml"
    );
    registry.init();
    Assertions.assertEquals(1, registry.getAuthorities().size());
    Assertions.assertEquals(1, registry.getOperators().size());
  }

  @Test
  void testUnsupportedOrganisationTypeThrows() {
    var registry = new NetexPublicationDeliveryFileOrganisationRegistry(
      "src/test/resources/organisation/netex/unsupported_org_type.xml"
    );
    Assertions.assertThrows(UnsupportedOrganisationTypeException.class, registry::init);
  }
}
