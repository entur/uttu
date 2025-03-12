package no.entur.uttu.organisation.netex;

import java.io.File;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "uttu.organisations.netex-file-uri")
public class NetexPublicationDeliveryFileOrganisationRegistry
  extends NetexPublicationDeliveryOrganisationRegistry {

  private final String netexFileUri;

  public NetexPublicationDeliveryFileOrganisationRegistry(
          @Value("${uttu.organisations.netex-file-uri}") String netexFileUri
  ) {
   this.netexFileUri = netexFileUri;
  }

  @Override
  protected Source getPublicationDeliverySource() {
    return new StreamSource(new File(netexFileUri));
  }
}
