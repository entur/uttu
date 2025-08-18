package no.entur.uttu.organisation.netex;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class NetexPublicationDeliveryOrganisationRegistryTest {

  @Test
  void testCanQueryOrganisations() {
    var registry = new NetexPublicationDeliveryFileOrganisationRegistry(
      "src/test/resources/fixtures/organisations.xml"
    );
    registry.init();
    Assertions.assertEquals(1, registry.getAuthorities().size());
    Assertions.assertEquals(1, registry.getOperators().size());
  }

  @Test
  void testUnsupportedOrganisationTypeThrows() {
    var registry = new NetexPublicationDeliveryFileOrganisationRegistry(
      "src/test/resources/organisation/netex/unsupported_org_type.xml"
    );
    Assertions.assertThrows(UnsupportedOrganisationTypeException.class, registry::init);
  }

  @Test
  void testLoadFromHttpSource() throws IOException {
    var netexFile = new File("src/test/resources/fixtures/organisations.xml");
    var webClientMock = Mockito.mock(WebClient.class);

    var requestHeadersUriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    var requestHeadersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
    var responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

    when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
    when(requestHeadersUriSpecMock.uri("/organisations")).thenReturn(
      requestHeadersSpecMock
    );
    when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
    when(responseSpecMock.bodyToMono(byte[].class)).thenReturn(
      Mono.just(Files.readAllBytes(netexFile.toPath()))
    );

    var registry = new NetexPublicationDeliveryHttpOrganisationRegistry(
      "/organisations",
      webClientMock
    );
    registry.init();

    Assertions.assertEquals(1, registry.getAuthorities().size());
  }
}
