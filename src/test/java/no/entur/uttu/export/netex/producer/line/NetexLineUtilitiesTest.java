package no.entur.uttu.export.netex.producer.line;

import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.model.*;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class NetexLineUtilitiesTest {

    @Test
    public void lineWithOneServiceJourneyReturnsCorrectAvailabilityPeriod() {
        Line line = new FlexibleLine();
        OperatingPeriod operatingPeriod = new OperatingPeriod();
        operatingPeriod.setFromDate(LocalDate.of(2022, 3, 1));
        operatingPeriod.setToDate(LocalDate.of(2022, 3, 7));
        line.setJourneyPatterns(List.of(createJourneyPatternForGivenOperatingPeriods(operatingPeriod)));

        AvailabilityPeriod availabilityPeriod = NetexLineUtilities.calculateAvailabilityPeriodForLine(line);

        assertEquals(availabilityPeriod.getFrom(), LocalDate.of(2022, 3, 1));
        assertEquals(availabilityPeriod.getTo(), LocalDate.of(2022, 3, 7));
    }

    @Test
    public void lineWithTwoServiceJourneysReturnCorrectAvailabilityPeriod() {
        Line line = new FlexibleLine();
        OperatingPeriod operatingPeriod1 = new OperatingPeriod();
        operatingPeriod1.setFromDate(LocalDate.of(2022, 3, 1));
        operatingPeriod1.setToDate(LocalDate.of(2022, 3, 7));
        OperatingPeriod operatingPeriod2 = new OperatingPeriod();
        operatingPeriod2.setFromDate(LocalDate.of(2022, 3, 10));
        operatingPeriod2.setToDate(LocalDate.of(2022, 3, 15));
        line.setJourneyPatterns(List.of(createJourneyPatternForGivenOperatingPeriods(operatingPeriod1, operatingPeriod2)));

        AvailabilityPeriod availabilityPeriod = NetexLineUtilities.calculateAvailabilityPeriodForLine(line);

        assertEquals(availabilityPeriod.getFrom(), LocalDate.of(2022, 3, 1));
        assertEquals(availabilityPeriod.getTo(), LocalDate.of(2022, 3, 15));
    }

    @Test
    public void shouldCreateAvailabilityPeriodWithDateWhenOperatingPeriodIsMissingInDayTypeAssignment() {
        Line line = new FlexibleLine();
        line.setJourneyPatterns(List.of(createJourneyPatternForGivenDates(LocalDate.of(2022, 3, 1))));

        AvailabilityPeriod availabilityPeriod = NetexLineUtilities.calculateAvailabilityPeriodForLine(line);

        assertEquals(availabilityPeriod.getFrom(), LocalDate.of(2022, 3, 1));
        assertEquals(availabilityPeriod.getTo(), LocalDate.of(2022, 3, 1));
    }

    @Test
    public void shouldNotReturnAvailabilityPeriodWithDateWhenOperatingPeriodIsMissing() {
        Line line = new FlexibleLine();
        line.setJourneyPatterns(List.of(createJourneyPatternWithEmptyDateTypeAssignment()));

        AvailabilityPeriod availabilityPeriod = NetexLineUtilities.calculateAvailabilityPeriodForLine(line);

        assertNull(availabilityPeriod);
    }


    private JourneyPattern createJourneyPatternForGivenOperatingPeriods(OperatingPeriod... operatingPeriods) {
        JourneyPattern journeyPattern = new JourneyPattern();
        journeyPattern.setServiceJourneys(
                Stream.of(operatingPeriods).map(operatingPeriod -> {
                    ServiceJourney serviceJourney = new ServiceJourney();
                    DayType dayType = new DayType();
                    DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
                    dayTypeAssignment.setOperatingPeriod(operatingPeriod);
                    dayType.setDayTypeAssignments(List.of(dayTypeAssignment));
                    serviceJourney.updateDayTypes(List.of(dayType));
                    return serviceJourney;
                }).collect(Collectors.toList()));
        return journeyPattern;
    }

    private JourneyPattern createJourneyPatternForGivenDates(LocalDate... dates) {
        JourneyPattern journeyPattern = new JourneyPattern();
        journeyPattern.setServiceJourneys(
                Stream.of(dates).map(date -> {
                    ServiceJourney serviceJourney = new ServiceJourney();
                    DayType dayType = new DayType();
                    DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
                    dayTypeAssignment.setDate(date);
                    dayType.setDayTypeAssignments(List.of(dayTypeAssignment));
                    serviceJourney.updateDayTypes(List.of(dayType));
                    return serviceJourney;
                }).collect(Collectors.toList()));
        return journeyPattern;
    }

    private JourneyPattern createJourneyPatternWithEmptyDateTypeAssignment() {
        JourneyPattern journeyPattern = new JourneyPattern();
        ServiceJourney serviceJourney = new ServiceJourney();
        DayType dayType = new DayType();
        DayTypeAssignment dayTypeAssignment = new DayTypeAssignment();
        dayType.setDayTypeAssignments(List.of(dayTypeAssignment));
        serviceJourney.updateDayTypes(List.of(dayType));
        journeyPattern.setServiceJourneys(List.of(serviceJourney));
        return journeyPattern;
    }

}