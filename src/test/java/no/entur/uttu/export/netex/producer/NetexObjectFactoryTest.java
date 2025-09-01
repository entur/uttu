package no.entur.uttu.export.netex.producer;

import no.entur.uttu.config.AdditionalCodespacesConfig;
import no.entur.uttu.config.ExportTimeZone;
import no.entur.uttu.model.Ref;
import no.entur.uttu.util.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.ServiceLinkInJourneyPattern_VersionedChildStructure;

public class NetexObjectFactoryTest {

  @Test
  void populateIdReturnsValidIdForVersionedChildStructureElements() {
    NetexObjectFactory factory = new NetexObjectFactory(
      new DateUtils(),
      new ExportTimeZone(),
      new AdditionalCodespacesConfig()
    );

    Assertions.assertEquals(
      "ENT:ServiceLinkInJourneyPattern:1",
      factory
        .populateId(
          new ServiceLinkInJourneyPattern_VersionedChildStructure(),
          new Ref("ENT:ServiceLink:1", "1")
        )
        .getId()
    );
  }
}
