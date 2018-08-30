package no.entur.uttu.export.netex.producer.common;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Network;
import org.rutebanken.netex.model.AuthorityRefStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NetworkProducer {

    @Autowired
    private NetexObjectFactory objectFactory;


    public List produce(NetexExportContext context) {
        return context.networks.stream().map(this::mapNetwork).collect(Collectors.toList());
    }

    private org.rutebanken.netex.model.Network mapNetwork(Network local) {

        AuthorityRefStructure authorityRefStruct = new AuthorityRefStructure().withRef(local.getNetexId()).withVersion(local.getNetexVersion());

        return new org.rutebanken.netex.model.Network()
                       .withId(local.getNetexId())
                       .withVersion(local.getNetexVersion())
                       .withName(objectFactory.createMultilingualString(local.getName()))
                       .withDescription(objectFactory.createMultilingualString(local.getDescription()))
                       .withTransportOrganisationRef(objectFactory.wrapAsJAXBElement(authorityRefStruct));
    }
}
