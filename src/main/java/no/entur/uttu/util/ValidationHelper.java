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
    public static boolean isNotAfter(LocalTime thisTime, int thisDayOffset, LocalTime otherTime, int otherDayOffset) {
        if (thisTime == null || otherTime == null) {
            return true;
        }

        if (thisDayOffset != otherDayOffset) {
            return thisDayOffset < otherDayOffset;
        }


        return !thisTime.isAfter(otherTime);
    }

}
