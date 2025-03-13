package no.entur.uttu.organisation.netex;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConditionalOnProperty(name = "uttu.organisations.netex-http-uri")
public class NetexPublicationDeliveryHttpOrganisationRegistry
  extends NetexPublicationDeliveryOrganisationRegistry {

  private final String netexHttpUri;
  private final WebClient orgRegisterClient;

  public NetexPublicationDeliveryHttpOrganisationRegistry(
    @Value("${uttu.organisations.netex-http-uri}") String netexHttpUri,
    WebClient orgRegisterClient
  ) {
    this.netexHttpUri = netexHttpUri;
    this.orgRegisterClient = orgRegisterClient;
  }

  @Override
  protected Source getPublicationDeliverySource() {
    byte[] response = orgRegisterClient
      .get()
      .uri(netexHttpUri)
      .retrieve()
      .bodyToMono(byte[].class)
      .block(Duration.ofSeconds(30));

    if (response == null) {
      return null;
    }

    return new StreamSource(new ByteArrayInputStream(response));
  }
}
