package no.entur.uttu.export.netex;

import org.rutebanken.netex.model.PublicationDeliveryStructure;

import javax.xml.bind.JAXBElement;

public class NetexFile {

    private String fileName;

    private JAXBElement<PublicationDeliveryStructure> publicationDeliveryStructure;

    public NetexFile(String fileName, JAXBElement<PublicationDeliveryStructure> publicationDeliveryStructure) {
        this.fileName = fileName;
        this.publicationDeliveryStructure = publicationDeliveryStructure;
    }

    public String getFileName() {
        return fileName;
    }

    public JAXBElement<PublicationDeliveryStructure> getPublicationDeliveryStructure() {
        return publicationDeliveryStructure;
    }
}
