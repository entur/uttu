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

package no.entur.uttu.export.netex;

import com.google.common.base.Preconditions;
import no.entur.uttu.export.model.ExportException;
import no.entur.uttu.export.netex.producer.common.NetexCommonFileProducer;
import no.entur.uttu.export.netex.producer.line.NetexLineFileProducer;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.Provider;
import no.entur.uttu.repository.FlexibleLineRepository;
import no.entur.uttu.repository.ProviderRepository;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.validation.NeTExValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.OutputStream;
import java.util.List;

import static javax.xml.bind.JAXBContext.newInstance;

@Component
@Transactional(readOnly = true)
public class NetexExporter {

    @Autowired
    private FlexibleLineRepository flexibleLineRepository;

    @Autowired
    private NetexLineFileProducer netexLineFileProducer;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private NetexCommonFileProducer commonFileProducer;

    private JAXBContext jaxbContext;

    private NeTExValidator netexValidator;


    @PostConstruct
    public void init() {
        try {
            jaxbContext = newInstance(PublicationDeliveryStructure.class);
            netexValidator = new NeTExValidator();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void exportDataSet(String providerCode, DataSetProducer dataSetProducer, boolean validateAgainstSchema) {
        NetexExportContext exportContext = new NetexExportContext(getVerifiedProvider(providerCode));

        List<FlexibleLine> flexibleLines = flexibleLineRepository.findAll();

        Preconditions.checkArgument(!flexibleLines.isEmpty(), "No FlexibleLines defined");

        flexibleLines.stream().map(line -> netexLineFileProducer.toNetexFile(line, exportContext))
                .forEach(netexFile -> marshalToFile(netexFile, dataSetProducer, validateAgainstSchema));
        marshalToFile(commonFileProducer.toCommonFile(exportContext), dataSetProducer, validateAgainstSchema);
    }

    private void marshalToFile(NetexFile lineFile, DataSetProducer dataSetProducer, boolean validateAgainstSchema) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            if (validateAgainstSchema) {
                marshaller.setSchema(netexValidator.getSchema());
            }
            OutputStream outputStream = dataSetProducer.addFile(lineFile.getFileName());
            marshaller.marshal(lineFile.getPublicationDeliveryStructure(), outputStream);
        } catch (Exception e) {
            throw new ExportException("Failed to marshal NeTEx XML to file: " + e.getMessage(), e);
        }


    }

    private Provider getVerifiedProvider(String providerCode) {
        Provider provider = providerRepository.getOne(providerCode);
        Preconditions.checkArgument(provider != null,
                "Provider not found: %s", providerCode);
        return provider;
    }

}
