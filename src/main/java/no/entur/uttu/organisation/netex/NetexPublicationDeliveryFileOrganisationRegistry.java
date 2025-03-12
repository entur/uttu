package no.entur.uttu.organisation.netex;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

@Component
@ConditionalOnProperty(
        name = "uttu.organisations.netex-file-uri"
)
public class NetexPublicationDeliveryFileOrganisationRegistry extends NetexPublicationDeliveryOrganisationRegistry {
    @Value("${uttu.organisations.netex-file-uri}")
    String netexFileUri;

    @Override
    public Source getPublicationDeliverySource() {
        return new StreamSource(new File(netexFileUri));
    }
}
