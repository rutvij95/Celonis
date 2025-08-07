package com.celonis.challenge.util;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TimeUtil {

    public static Date getCurrentTime() {
        return new Date();
    }

    public static Long getBeforeTimeInSeconds() {
        return 7 * 24 * 60 * 60L; // 7 days in seconds
    }

    public static Instant getSevenDaysPriorDate() {
        return TimeUtil.getCurrentTime().toInstant().minus(getBeforeTimeInSeconds(), ChronoUnit.SECONDS);
    }
}
