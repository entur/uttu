package no.entur.uttu.organisation.netex;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.xml.transform.Source;

@Component
@ConditionalOnProperty(
        name = "uttu.organisations.netex-http-uri"
)
public class NetexPublicationDeliveryHttpOrganisationRegistry extends NetexPublicationDeliveryOrganisationRegistry {

    @Override
    public Source getPublicationDeliverySource() {

        // TODO: here is where we will call out to an http endpoint
        // to get our netex data and return it as a source

        return null;
    }
}
