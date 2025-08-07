package com.celonis.challenge.util;


import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TimeUtil {

    public static Date getCurrentTime() {
        return Date.from(Instant.now(Clock.systemUTC()));
    }

//    public static Long getBeforeTimeInSeconds() {
//        return 7 * 24 * 60 * 60L; // 7 days in seconds
//    }

    public static Instant getSevenDaysPriorDate() {
        return Instant.now(Clock.systemUTC()).minus(7, ChronoUnit.DAYS);
    }
}
