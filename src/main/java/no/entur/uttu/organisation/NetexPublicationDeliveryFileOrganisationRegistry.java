package no.entur.uttu.organisation;

import no.entur.uttu.netex.NetexUnmarshallerUnmarshalFromSourceException;
import no.entur.uttu.organisation.spi.OrganisationRegistry;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.Operator;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.ResourceFrame;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.io.File;

@Component
@ConditionalOnProperty(
        name = "uttu.organisations.netex-file-uri"
)
public class NetexPublicationDeliveryFileOrganisationRegistry extends NetexPublicationDeliveryOrganisationRegistry{
    @Value("${uttu.organisations.netex-file-uri}")
    String netexFileUri;

    @Override
    public void init() {
        try {
            PublicationDeliveryStructure publicationDeliveryStructure =
                    netexUnmarshaller.unmarshalFromSource(new StreamSource(new File(netexFileUri)));
            publicationDeliveryStructure
                    .getDataObjects()
                    .getCompositeFrameOrCommonFrame()
                    .forEach(frame -> {
                        var frameValue = frame.getValue();
                        if (frameValue instanceof ResourceFrame resourceFrame) {
                            resourceFrame
                                    .getOrganisations()
                                    .getOrganisation_()
                                    .forEach(org -> {
                                        if (org.getDeclaredType().isAssignableFrom(Authority.class)) {
                                            authorities.add((Authority) org.getValue());
                                        } else if (org.getDeclaredType().isAssignableFrom(Operator.class)) {
                                            operators.add((Operator) org.getValue());
                                        } else {
                                            throw new RuntimeException(
                                                    "Unsupported organisation type: " + org.getDeclaredType()
                                            );
                                        }
                                    });
                        }
                    });
        } catch (NetexUnmarshallerUnmarshalFromSourceException e) {
            logger.warn(
                    "Unable to unmarshal organisations xml, organisation registry will be an empty list",
                    e
            );
        }
    }
}
