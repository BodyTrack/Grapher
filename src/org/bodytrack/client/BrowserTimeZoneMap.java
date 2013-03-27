package org.bodytrack.client;

import java.util.Date;

/**
 * {@link BrowserTimeZoneMap} is a {@link TimeZoneMap} implementation that always
 * uses browser time, and nothing else.
 */
public final class BrowserTimeZoneMap extends TimeZoneMap {

    // In getUtcTime, the difference between local and utc must be no more than 2 hours
    // different from getLocalOffset(local), since the offset between local and UTC is
    // usually the same in both directions of transformation, and in the worst case there
    // is a difference of one hour for DST, plus up to 120 (2 leap seconds are allowed
    // per minute) leap seconds, plus I'm leaving a fudge factor in case that situation
    // changes in the future.
    private static final double MAX_REVERSE_DISCREPANCY =
            2 * TimeZoneMap.MINUTES_PER_HOUR * TimeZoneMap.SECONDS_PER_MINUTE;

    @Override
    public double getLocalTime(final double utc) {
        return utc + getLocalOffset(utc);
    }

    private double getLocalOffset(final double utc) {
        final Date date = new Date((long)(TimeZoneMap.MILLIS_PER_SECOND * utc));
        @SuppressWarnings("deprecation")
        final double delta = -(date.getTimezoneOffset() * TimeZoneMap.SECONDS_PER_MINUTE);
        return delta;
    }

    /**
     * Returns the UTC time that matches the local second count.
     *
     * <p>
     * This method returns a time <code>utc</code> such that
     * <code>getLocalTime(getUtcTime(local)) == local</code> always holds, as
     * does <code>getUtcTime(getLocalTime(utc)) == utc</code>.  There is,
     * however, a case in which the second of these is impossible.
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
        // However, when DST is involved, we do have to adjust the utc value so that
        // getLocalTime(getUtcTime(local)) == local always.

        double min = local - getLocalOffset(local) - MAX_REVERSE_DISCREPANCY;
        double max = local - getLocalOffset(local) + MAX_REVERSE_DISCREPANCY;
        double median = (min + max) / 2.0;

        assert (getLocalTime(min) <= local);
        assert (getLocalTime(max) >= local);

        // Binary search over the range [min, max] to find the correct UTC time
        while ((long)Math.floor(getLocalTime(median)) != (long)Math.floor(local)) {
            // Narrow the range of possible values
            if (getLocalTime(median) < local)
                min = median;
            else
                max = median;

            median = (min + max) / 2.0;
        }

        // Be sure to get the fractional seconds correct
        return Math.floor(median) + (local - Math.floor(local));
    }

}
