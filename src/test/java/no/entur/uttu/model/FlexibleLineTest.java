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

import org.junit.Test;

import java.util.List;

import static no.entur.uttu.model.ModelTestUtil.assertCheckPersistableFails;

public class FlexibleLineTest {

    @Test
    public void checkPersistable_whenTransportSubmodeNotSet_giveException() {
        FlexibleLine flexibleLine = new FlexibleLine();
        flexibleLine.setTransportMode(VehicleModeEnumeration.BUS);
        flexibleLine.setBookingArrangement(validBookingArrangement());
        assertCheckPersistableFails(flexibleLine);
    }


    @Test
    public void checkPersistable_whenTransportModeNotSet_giveException() {
        FlexibleLine flexibleLine = new FlexibleLine();
        flexibleLine.setTransportSubmode(VehicleSubmodeEnumeration.CAR_TRANSPORT_RAIL_SERVICE);
        flexibleLine.setBookingArrangement(validBookingArrangement());
        assertCheckPersistableFails(flexibleLine);
    }


    @Test
    public void checkPersistable_whenTransportSubmodeNotValidForTransportMode_giveException() {
        FlexibleLine flexibleLine = new FlexibleLine();
        flexibleLine.setTransportMode(VehicleModeEnumeration.BUS);
        flexibleLine.setTransportSubmode(VehicleSubmodeEnumeration.CAR_TRANSPORT_RAIL_SERVICE);
        flexibleLine.setBookingArrangement(validBookingArrangement());
        assertCheckPersistableFails(flexibleLine);
    }

    @Test
    public void checkPersistable_whenTransportSubmodeValidForTransportMode_success() {
        FlexibleLine flexibleLine = new FlexibleLine();
        flexibleLine.setTransportMode(VehicleModeEnumeration.BUS);
        flexibleLine.setTransportSubmode(VehicleSubmodeEnumeration.AIRPORT_LINK_BUS);
        flexibleLine.setBookingArrangement(validBookingArrangement());
        flexibleLine.checkPersistable();
    }

    @Test
    public void checkPersistable_whenBookingInformationOnJourneyPattern_success() {
        FlexibleLine flexibleLine = new FlexibleLine();
        flexibleLine.setTransportMode(VehicleModeEnumeration.BUS);
        flexibleLine.setTransportSubmode(VehicleSubmodeEnumeration.AIRPORT_LINK_BUS);
        JourneyPattern journeyPattern = JourneyPatternTest.validJourneyPattern();
        journeyPattern.getPointsInSequence().get(0).setBookingArrangement(validBookingArrangement());
        flexibleLine.setJourneyPatterns(List.of(journeyPattern));
        flexibleLine.checkPersistable();
    }

    @Test
    public void checkPersistable_whenBookingInformationOnServiceJourney_success() {
        FlexibleLine flexibleLine = new FlexibleLine();
        flexibleLine.setTransportMode(VehicleModeEnumeration.BUS);
        flexibleLine.setTransportSubmode(VehicleSubmodeEnumeration.AIRPORT_LINK_BUS);
        flexibleLine.setOperatorRef("TST:Operator:1");
        JourneyPattern journeyPattern = JourneyPatternTest.validJourneyPattern();
        ServiceJourney serviceJourney = ServiceJourneyTest.validServiceJourney();
        serviceJourney.setBookingArrangement(validBookingArrangement());
        journeyPattern.setServiceJourneys(List.of(serviceJourney));
        flexibleLine.setJourneyPatterns(List.of(journeyPattern));
        flexibleLine.checkPersistable();
    }

    @Test
    public void checkPersistable_whenMissingBookingInformation_giveException() {
        FlexibleLine flexibleLine = new FlexibleLine();
        flexibleLine.setTransportMode(VehicleModeEnumeration.BUS);
        flexibleLine.setTransportSubmode(VehicleSubmodeEnumeration.AIRPORT_LINK_BUS);
        JourneyPattern journeyPattern = JourneyPatternTest.validJourneyPattern();
        ServiceJourney serviceJourney = ServiceJourneyTest.validServiceJourney();
        journeyPattern.setServiceJourneys(List.of(serviceJourney));
        flexibleLine.setJourneyPatterns(List.of(journeyPattern));
        assertCheckPersistableFails(flexibleLine);
    }

    private BookingArrangement validBookingArrangement() {
        BookingArrangement bookingArrangement = new BookingArrangement();
        bookingArrangement.setBookWhen(PurchaseWhenEnumeration.DAY_OF_TRAVEL_ONLY);
        return bookingArrangement;
    }
}
