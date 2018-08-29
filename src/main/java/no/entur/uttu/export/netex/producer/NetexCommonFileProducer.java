package no.entur.uttu.export.netex.producer;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.NetexFile;
import org.rutebanken.netex.model.CompositeFrame;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;

@Component
public class NetexCommonFileProducer {
    @Autowired
    private NetexObjectFactory objectFactory;

    public NetexFile toCommonFile(NetexExportContext exportContext) {

        String fileName = "_common.xml";


        CompositeFrame compositeFrame = new CompositeFrame();

        JAXBElement<PublicationDeliveryStructure> publicationDelivery = objectFactory.createPublicationDelivery(exportContext, compositeFrame);

        return new NetexFile(fileName, publicationDelivery);
    }
}
