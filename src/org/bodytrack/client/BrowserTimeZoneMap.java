package org.bodytrack.client;

import java.util.Date;

/**
 * {@link BrowserTimeZoneMap} is a {@link TimeZoneMap} implementation that always
 * uses browser time, and nothing else.
 */
public final class BrowserTimeZoneMap extends TimeZoneMap {

    private static final int DATE_CONSTRUCTOR_START_YEAR = 1900;
    private static final String[] TOGMTSTRING_MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

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
        // Be sure to get the fractional seconds correct
        return Math.floor(getUTCFloor(local)) + (local - Math.floor(local));
    }

    /*
    // TODO: Use this method instead of string parsing.  However, this breaks testing code.
    // It is possible to make BrowserTimeZoneMapTest inherit from GWTTestCase, but
    // getting the tests to run under both the IDE and Ant is tricky.
    private double getUTCFloor(final double local) {
        final JsDate utcDate = JsDate.create(local * MILLIS_PER_SECOND);
        final JsDate localDate = JsDate.create(utcDate.getUTCFullYear(),
                utcDate.getUTCMonth(), utcDate.getUTCDate(), utcDate.getUTCHours(),
                utcDate.getUTCMinutes(), utcDate.getUTCSeconds(),
                utcDate.getUTCMilliseconds());
        return localDate.getTime() / 1000.0;
    }
    */

    private double getUTCFloor(final double local) {
        final Date utcDate = new Date((long)(local * MILLIS_PER_SECOND));

        @SuppressWarnings("deprecation")
        final String[] utcParts = utcDate.toGMTString().split("\\s+");
        assert (utcParts.length == 5);
        assert (utcParts[4].equals("GMT"));

        final int date = Integer.parseInt(utcParts[0]);
        final int month = parseMonth(utcParts[1]);
        final int year = Integer.parseInt(utcParts[2]) - DATE_CONSTRUCTOR_START_YEAR;

        final String[] timeParts = utcParts[3].split(":");
        assert (timeParts.length == 3);

        final int hour = Integer.parseInt(timeParts[0]);
        final int minute = Integer.parseInt(timeParts[1]);
        final int second = Integer.parseInt(timeParts[2]);

        @SuppressWarnings("deprecation")
        final Date localDate = new Date(year, month, date, hour, minute, second);
        return (double)localDate.getTime() / MILLIS_PER_SECOND;
    }

    /**
     * Parses the given month into an integer from 0 to 11.
     *
     * <p>
     * It is expected that <code>month</code> is an element of the list
     * [Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec], which is the
     * set of legal months that might be returned from {@link Date#toGMTString()}.
     * </p>
     */
    private static int parseMonth(final String month) {
        for (int i = 0; i < TOGMTSTRING_MONTHS.length; i++)
            if (TOGMTSTRING_MONTHS[i].equals(month))
                return i;

        throw new IllegalArgumentException("Unexpected month " + month);
    }

}
