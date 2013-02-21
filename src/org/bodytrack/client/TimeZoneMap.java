package org.bodytrack.client;

import java.util.Date;

/**
 * A stateless converter between local time and UTC.
 *
 * <p>
 * If you need a stateful converter, which keeps track of whether or not it is
 * in UTC mode, and converts accordingly, see {@link ModalTimeZoneMap}.
 * </p>
 */
public abstract class TimeZoneMap {

    public static final TimeZoneMap IDENTITY_MAP = new UTCTimeZoneMap();

    public static final long MILLIS_PER_SECOND = 1000;
    public static final long SECONDS_PER_MINUTE = 60;
    public static final long MINUTES_PER_HOUR = 60;
    public static final long HOURS_PER_DAY = 24;

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
     * The simplest possible time zone map, which considers &quot;local time&quot;
     * to be equal to UTC.
     */
    private static final class UTCTimeZoneMap extends TimeZoneMap {

        @Override
        public double getLocalTime(final double utc) {
            return utc;
        }

        @Override
        public double getUtcTime(final double local) {
            return local;
        }
    }
}
