/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.model;

import no.entur.uttu.error.codes.ErrorCodeEnumeration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

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
        StopPointInJourneyPattern secondStopPointInJourneyPattern = new StopPointInJourneyPattern();
        secondStopPointInJourneyPattern.setFlexibleStopPlace(flexibleStopPlace);
        journeyPattern.setPointsInSequence(Arrays.asList(stopPointInJourneyPattern, secondStopPointInJourneyPattern));
        fixedLine.setJourneyPatterns(Collections.singletonList(journeyPattern));
        assertCheckPersistableFailsWithErrorCode(fixedLine, ErrorCodeEnumeration.FLEXIBLE_STOP_PLACE_NOT_ALLOWED);
    }
}
