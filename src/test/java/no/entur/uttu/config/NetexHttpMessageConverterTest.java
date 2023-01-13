package no.entur.uttu.config;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.rutebanken.netex.model.StopPlace;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.xmlunit.builder.Input;

import javax.xml.bind.JAXBException;

public class NetexHttpMessageConverterTest {

    @Test
    public void testConvertStopPlace() throws JAXBException {
        NetexHttpMessageConverter converter = new NetexHttpMessageConverter();

        Assertions.assertTrue(converter.supports(StopPlace.class));

        Assertions.assertTrue(converter.getSupportedMediaTypes().contains(MediaType.APPLICATION_XML));

        StopPlace stopPlace = (StopPlace) converter.readFromSource(
            StopPlace.class,
            HttpHeaders.EMPTY,
            Input.fromFile("src/test/resources/stopPlaceFixture.xml").build()
        );

        Assertions.assertEquals("NSR:StopPlace:337", stopPlace.getId());
    }
}
