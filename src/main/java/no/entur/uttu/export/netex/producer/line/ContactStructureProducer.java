/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

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
