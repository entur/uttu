package no.entur.uttu.export.netex.producer.line;

import no.entur.uttu.export.model.AvailabilityPeriod;
import no.entur.uttu.model.*;

import java.util.List;
import java.util.Objects;

public class NetexLineUtilities {

    public static AvailabilityPeriod calculateAvailabilityPeriodForLine(Line line) {
        return line.getJourneyPatterns().stream()
                .map(JourneyPattern::getServiceJourneys).flatMap(List::stream)
                .map(ServiceJourney::getDayTypes).flatMap(List::stream)
                .map(DayType::getDayTypeAssignments).flatMap(List::stream)
                .map(NetexLineUtilities::getAvailabilityPeriodFromDayTypeAssignment)
                .filter(Objects::nonNull)
                .reduce(AvailabilityPeriod::union)
                .orElse(null);
    }

    private static AvailabilityPeriod getAvailabilityPeriodFromDayTypeAssignment(DayTypeAssignment dayTypeAssignment) {
        if (dayTypeAssignment.getOperatingPeriod() != null) {
            return new AvailabilityPeriod(dayTypeAssignment.getOperatingPeriod().getFromDate(), dayTypeAssignment.getOperatingPeriod().getToDate());
        } else if (dayTypeAssignment.getDate() != null) {
            return new AvailabilityPeriod(dayTypeAssignment.getDate(), dayTypeAssignment.getDate());
        }
        return null;
    }
}
