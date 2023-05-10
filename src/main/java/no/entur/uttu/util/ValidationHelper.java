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

import java.time.LocalTime;

public class ValidationHelper {

  /**
   * Check whether localTime + day offset is after another localTime + day offset.
   *
   * @param thisTime       the  local time to check
   * @param thisDayOffset  the day offset for the local time to check
   * @param otherTime      the local time to compare against
   * @param otherDayOffset the day offset for the local time to compare against
   * @return false if thisTime+thisDayOffset > otherTime +otherDayOffset. else true, including when either local time is not specified
   */
  public static boolean isNotAfter(
    LocalTime thisTime,
    int thisDayOffset,
    LocalTime otherTime,
    int otherDayOffset
  ) {
    if (thisTime == null || otherTime == null) {
      return true;
    }

    if (thisDayOffset != otherDayOffset) {
      return thisDayOffset < otherDayOffset;
    }

    return !thisTime.isAfter(otherTime);
  }

  public static boolean isNotSame(
    LocalTime thisTime,
    int thisDayOffset,
    LocalTime otherTime,
    int otherDayOffset
  ) {
    if (thisTime == null || otherTime == null) {
      return true;
    }

    if (thisDayOffset != otherDayOffset) {
      return true;
    }

    return !thisTime.equals(otherTime);
  }
}
