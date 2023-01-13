package no.entur.uttu.stopplace;

import no.entur.uttu.stubs.StopPlaceRegistryStub;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class StopPlaceServiceTest {

    StopPlaceService stopPlaceService = new StopPlaceServiceImpl(new StopPlaceRegistryStub());

    @Test
    public void test() {
        String quayRef = "NSR:Quay:1";
        Assertions.assertEquals(quayRef, stopPlaceService.getVerifiedQuayRef(quayRef));
    }
}
