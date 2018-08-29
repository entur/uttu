package no.entur.uttu.export.netex.producer;

import no.entur.uttu.export.netex.NetexExportContext;
import no.entur.uttu.export.netex.NetexFile;
import no.entur.uttu.model.FlexibleLine;
import org.rutebanken.netex.model.CompositeFrame;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;

@Component
public class NetexLineFileProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    public NetexFile toNetexFile(FlexibleLine line, NetexExportContext exportContext) {

        String fileName = line.getPublicCode() + "_" + line.getName() + ".xml";


        CompositeFrame compositeFrame = new CompositeFrame();

        JAXBElement<PublicationDeliveryStructure> publicationDelivery = objectFactory.createPublicationDelivery(exportContext, compositeFrame);

        return new NetexFile(fileName, publicationDelivery);
    }
}
