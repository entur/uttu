package no.entur.uttu.export.netex.producer.line;


import no.entur.uttu.export.netex.producer.NetexObjectFactory;
import no.entur.uttu.model.Contact;
import org.rutebanken.netex.model.ContactStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContactStructureProducer {

    @Autowired
    private NetexObjectFactory objectFactory;

    public ContactStructure mapContactStructure(Contact local) {
        if (local == null) {
            return null;
        }
        return new ContactStructure().withEmail(local.getEmail()).withPhone(local.getPhone())
                       .withUrl(local.getUrl())
                       .withContactPerson(objectFactory.createMultilingualString(local.getContactPerson()))
                       .withFurtherDetails(objectFactory.createMultilingualString(local.getFurtherDetails()));
    }
}
