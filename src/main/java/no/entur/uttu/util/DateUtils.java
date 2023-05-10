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

package no.entur.uttu.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component(value = "dateUtils")
public class DateUtils {

  private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd")
    .optionalStart()
    .appendPattern("XXXXX")
    .optionalEnd()
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .parseDefaulting(
      ChronoField.OFFSET_SECONDS,
      OffsetDateTime.now().getLong(ChronoField.OFFSET_SECONDS)
    )
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
