package no.entur.uttu.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Component(value = "dateUtils")
public class DateUtils {

    private final static DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd")
                                                               .optionalStart().appendPattern("XXXXX").optionalEnd()
                                                               .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                                                               .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                                                               .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                                                               .parseDefaulting(ChronoField.OFFSET_SECONDS, OffsetDateTime.now().getLong(ChronoField.OFFSET_SECONDS))
                                                               .toFormatter();

    public static ZonedDateTime parseDate(String dateWithZone) {
        return ZonedDateTime.parse(dateWithZone, formatter);
    }

    @Value("${netex.export.time.zone.id:CET}")
    private ZoneId exportZoneId;

    public LocalDateTime toExportLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, exportZoneId);
    }

    public LocalDateTime toExportLocalDateTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(exportZoneId).toLocalDateTime();
    }

    public LocalTime toExportLocalTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(exportZoneId).toLocalTime();
    }

}