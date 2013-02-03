package org.bodytrack.client;

import java.util.Date;

/**
 * {@link BrowserTimeZoneMap} is a {@link TimeZoneMap} implementation that always
 * uses browser time, and nothing else.
 */
public final class BrowserTimeZoneMap extends TimeZoneMap {

    @SuppressWarnings("deprecation")
    private static final Date localEpoch = new Date(70, 0, 1);
    private static final double localEpochSeconds = localEpoch.getTime() / (double)MILLIS_PER_SECOND;

    @Override
    public double getLocalTime(final double utc) {
        final Date date = new Date((long)(MILLIS_PER_SECOND * utc));
        @SuppressWarnings("deprecation")
        final double localTime = utc - (date.getTimezoneOffset() * SECONDS_PER_MINUTE);
        return localTime;
    }

    @Override
    public double getUtcTime(final double local) {
        return localEpochSeconds + local;
    }
}
