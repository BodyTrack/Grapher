package org.bodytrack.client;

import java.util.Date;

public abstract class TimeZoneMap {

    protected static final long MILLIS_PER_SECOND = 1000;
    protected static final long SECONDS_PER_MINUTE = 60;
    protected static final long MINUTES_PER_HOUR = 60;
    protected static final long HOURS_PER_DAY = 24;

    /**
     * Converts <code>utc</code> from UTC to local time, however a specific
     * time map implementation defines local time.
     *
     * @param utc
     *  The number of seconds since the epoch in UTC
     * @return
     *  The number of seconds since the epoch in local time
     */
    public abstract double getLocalTime(final double utc);

    /**
     * Converts <code>local</code> from local time to UTC, however a specific
     * time map implementation defines local time.
     *
     * @param local
     *  The number of seconds since the epoch in local time
     * @return
     *  The number of seconds since the epoch in local UTC
     */
    public abstract double getUtcTime(final double local);

    /**
     * Returns a new {@link java.util.Date Date} object taken from <code>seconds</code>.
     *
     * @param seconds
     *  The number of seconds since the epoch, where &quot;epoch&quot; is interpreted
     *  differently based on the value of <code>utc</code>
     * @param utc
     *  <code>true</code> to mean that seconds is interpreted as the number of seconds
     *  since the true UTC epoch, and <code>false</code> to mean that seconds is
     *  interpreted as the number of seconds since January 1, 1970 local time
     * @return
     *  A new {@link java.util.Date Date} object that represents the time equal to
     *  <code>seconds</code> seconds since January 1, 1970, where <code>utc</code>
     *  determines whether January 1, 1970 is interpreted in local time or UTC
     */
    public final Date getDate(final double seconds, final boolean utc) {
        final double utcSeconds = utc ? seconds : getUtcTime(seconds);
        return new Date((long)(utcSeconds * MILLIS_PER_SECOND));
    }

    /**
     * Just a little wrapper that allows an outside class to persist the <code>utc</code>
     * parameter to {@link TimeZoneMap#getDate(double, boolean)}.
     */
    public static final class DateBuilder {
        private final TimeZoneMap map;
        private final boolean utc;

        public DateBuilder(final TimeZoneMap map, final boolean utc) {
            if (map == null)
                throw new NullPointerException();

            this.map = map;
            this.utc = utc;
        }

        public Date getDate(final double seconds) {
            return map.getDate(seconds, utc);
        }
    }
}
