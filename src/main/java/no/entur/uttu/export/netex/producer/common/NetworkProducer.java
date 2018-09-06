package no.entur.uttu.export.netex.producer.common;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Network;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NetworkProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    @Autowired
    private OrganisationProducer organisationProducer;

    public List produce(NetexExportContext context) {
        return context.networks.stream().map(local -> mapNetwork(local, context)).collect(Collectors.toList());
    }

    private org.rutebanken.netex.model.Network mapNetwork(Network local, NetexExportContext context) {
        return new org.rutebanken.netex.model.Network()
                       .withId(local.getNetexId())
                       .withVersion(local.getNetexVersion())
                       .withName(objectFactory.createMultilingualString(local.getName()))
                       .withDescription(objectFactory.createMultilingualString(local.getDescription()))
                       .withTransportOrganisationRef(objectFactory.wrapAsJAXBElement(organisationProducer.produceAuthorityRef(local.getAuthorityRef(), true, context)));
    }
}
