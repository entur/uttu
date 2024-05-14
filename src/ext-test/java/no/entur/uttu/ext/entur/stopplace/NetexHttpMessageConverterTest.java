package no.entur.uttu.ext.entur.stopplace;

import no.entur.uttu.netex.NetexUnmarshallerReadFromSourceException;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.rutebanken.netex.model.StopPlace;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.xmlunit.builder.Input;

public class NetexHttpMessageConverterTest {

  @Test
  public void testConvertStopPlace() throws NetexUnmarshallerReadFromSourceException {
    NetexHttpMessageConverter converter = new NetexHttpMessageConverter();

    Assertions.assertTrue(converter.supports(StopPlace.class));

    Assertions.assertTrue(
      converter.getSupportedMediaTypes().contains(MediaType.APPLICATION_XML)
    );

    StopPlace stopPlace = (StopPlace) converter.readFromSource(
      StopPlace.class,
      HttpHeaders.EMPTY,
      Input.fromFile("src/ext-test/resources/stopPlaceFixture.xml").build()
    );

    Assertions.assertEquals("NSR:StopPlace:337", stopPlace.getId());
  }
}
