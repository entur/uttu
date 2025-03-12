package no.entur.uttu.organisation.netex;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetexPublicationDeliveryOrganisationRegistryTest {

  @Test
  void testUnsupportedOrganisationTypeThrows() {
    Assertions.assertThrows(
      UnsupportedOrganisationTypeException.class,
      () -> {
        new NetexPublicationDeliveryFileOrganisationRegistry(
          "src/test/resources/organisation/netex/unsupported_org_type.xml"
        )
          .init();
      }
    );
  }
}
