package no.entur.uttu.export.netex.producer.common;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.rutebanken.netex.model.GeneralOrganisation;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;

public class OrganisationProducerTest {

  @Test
  public void testGetNetexId() {
    Assertions.assertEquals(
      "TST:Operator:2",
      OrganisationProducer.getNetexId(
        new GeneralOrganisation()
          .withId("notThis")
          .withKeyList(
            new KeyListStructure()
              .withKeyValue(
                new KeyValueStructure()
                  .withKey("LegacyId")
                  .withValue("TST:Authority:1,TST:Operator:2")
              )
          ),
        "Operator"
      )
    );

    Assertions.assertEquals(
      "TST:Authority:1",
      OrganisationProducer.getNetexId(
        new GeneralOrganisation()
          .withId("notThis")
          .withKeyList(
            new KeyListStructure()
              .withKeyValue(
                new KeyValueStructure()
                  .withKey("LegacyId")
                  .withValue("TST:Authority:1,TST:Operator:2")
              )
          ),
        "Authority"
      )
    );

    Assertions.assertEquals(
      "TST:Operator:2",
      OrganisationProducer.getNetexId(
        new GeneralOrganisation()
          .withId("TST:Operator:2")
          .withKeyList(
            new KeyListStructure()
              .withKeyValue(
                new KeyValueStructure().withKey("LegacyId").withValue("TST:Authority:1")
              )
          ),
        "Operator"
      )
    );

    Assertions.assertEquals(
      "TST:Operator:2",
      OrganisationProducer.getNetexId(
        new GeneralOrganisation().withId("TST:Operator:2"),
        "Operator"
      )
    );
  }
}
