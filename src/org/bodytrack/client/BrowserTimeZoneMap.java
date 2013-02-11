package org.bodytrack.client;

import java.util.Date;

/**
 * {@link BrowserTimeZoneMap} is a {@link TimeZoneMap} implementation that always
 * uses browser time, and nothing else.
 */
public final class BrowserTimeZoneMap extends TimeZoneMap {

    private static final int TIME_EPSILON = 1;

    @Override
    public double getLocalTime(final double utc) {
        return utc + getLocalOffset(utc);
    }

    /**
     * Returns the UTC time that matches the local second count.
     *
     * <p>
     * The goal of this method is to return a time <code>utc</code> such that
     * <code>getLocalTime(getUtcTime(local)) == local</code>
     * always holds, as does <code>getUtcTime(getLocalTime(utc)) == utc</code>.
     * There is, however, a case in which the second of these is impossible.
     * When a wall-clock time (for example, between 1:00 and 1:59:59 am at the
     * end of Daylight Savings time, in a region where the end of DST is implemented
     * by turning the clock back from 1:59:59 am to 1:00 am in the next second)
     * can mean two different UTC times, we do not guarantee which of those times
     * this method returns.  This is an inherent limitation of making
     * <code>local</code> refer to wall-clock time in the local time zone, since
     * wall-clock time is ambiguous in this case.
     * </p>
     *
     * @param local
     *  The number of seconds since the epoch in local time
     * @return
     *  The number of seconds since the epoch in UTC, corresponding to
     *  <code>local</code>
     */
    @Override
    public double getUtcTime(final double local) {
        // Seed the local offset by noting that the time zone is usually the same
        // in local and getUtcTime(local), in which case we never enter the loop.
        // However, when DST is involved, we do have to adjust the utc value
        // so that getLocalTime(getUtcTime(local)) == local always
        double utc = local - getLocalOffset(local);
        while (Math.abs(getLocalTime(utc) - local) > TIME_EPSILON) {
            utc = local - getLocalOffset(utc);
        }
        return utc;
    }

    private double getLocalOffset(final double utc) {
        final Date date = new Date((long)(TimeZoneMap.MILLIS_PER_SECOND * utc));
        @SuppressWarnings("deprecation")
        final double delta = -(date.getTimezoneOffset() * TimeZoneMap.SECONDS_PER_MINUTE);
        return delta;
    }
}
