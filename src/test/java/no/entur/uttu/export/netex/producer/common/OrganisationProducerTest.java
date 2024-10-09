package no.entur.uttu.export.netex.producer.common;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.KeyListStructure;
import org.rutebanken.netex.model.KeyValueStructure;
import org.rutebanken.netex.model.Operator;

public class OrganisationProducerTest {

  @Test
  public void testExtractLegacyId() {
    Assertions.assertEquals(
      "TST:Operator:2",
      OrganisationProducer.extractLegacyId(
        new Operator()
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
      ).get()
    );

    Assertions.assertEquals(
      "TST:Authority:1",
      OrganisationProducer.extractLegacyId(
        new Authority()
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
      ).get()
    );

    Assertions.assertFalse(
      OrganisationProducer.extractLegacyId(
        new Operator()
          .withId("TST:Operator:2")
          .withKeyList(
            new KeyListStructure()
              .withKeyValue(
                new KeyValueStructure().withKey("LegacyId").withValue("TST:Authority:1")
              )
          ),
        "Operator"
      ).isPresent()
    );

    Assertions.assertFalse(
      OrganisationProducer.extractLegacyId(
        new Operator().withId("TST:Operator:2"),
        "Operator"
      ).isPresent()
    );
  }
}
