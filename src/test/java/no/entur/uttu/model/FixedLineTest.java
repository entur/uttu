package no.entur.uttu.model;

import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static no.entur.uttu.model.ModelTestUtil.assertCheckPersistableFails;
import static no.entur.uttu.model.ModelTestUtil.assertCheckPersistableFailsWithErrorCode;

public class FixedLineTest {
    @Test
    public void checkPersistable_whenUsingFlexibleStopPlace_giveException() {
        FixedLine fixedLine = new FixedLine();
        fixedLine.setTransportMode(VehicleModeEnumeration.BUS);
        fixedLine.setTransportSubmode(VehicleSubmodeEnumeration.LOCAL_BUS);
        JourneyPattern journeyPattern = new JourneyPattern();
        StopPointInJourneyPattern stopPointInJourneyPattern = new StopPointInJourneyPattern();
        FlexibleStopPlace flexibleStopPlace = new FlexibleStopPlace();
        stopPointInJourneyPattern.setFlexibleStopPlace(flexibleStopPlace);
        stopPointInJourneyPattern.setDestinationDisplay(new DestinationDisplay());
        journeyPattern.setPointsInSequence(Arrays.asList(stopPointInJourneyPattern, stopPointInJourneyPattern));
        fixedLine.setJourneyPatterns(Collections.singletonList(journeyPattern));
        assertCheckPersistableFailsWithErrorCode(fixedLine, ErrorCodeEnumeration.FLEXIBLE_STOP_PLACE_NOT_ALLOWED);
    }
}
