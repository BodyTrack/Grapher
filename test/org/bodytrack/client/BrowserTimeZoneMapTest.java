package org.bodytrack.client;

import java.util.Date;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Note that any or all of these tests may break if the machine changes time zones
// or changes between daylight savings time and standard time while tests are running,
// which is admittedly a pretty unlikely possibility.
// They make the implicit assumption that time zone and daylight savings time versus
// standard time don't change during test execution.
@SuppressWarnings("deprecation")
public final class BrowserTimeZoneMapTest {
    private static final BrowserTimeZoneMap timeMap = new BrowserTimeZoneMap();
    private static final int timeZoneOffset = new Date().getTimezoneOffset() * 60;

    @SuppressWarnings("deprecation")
    private static final Date localEpoch = new Date(70, 0, 1);
    private static final double localEpochSeconds = localEpoch.getTime() / 1000.0;

    @Test
    public void testBrowserTimeMapUtcToLocal() {
        final double utcTime = System.currentTimeMillis() / 1000.0;
        final double localTime = timeMap.getLocalTime(utcTime);

        assertEquals(utcTime - timeZoneOffset, localTime, 1.0);
    }

    @Test
    public void testBrowserTimeMapLocalToUtc() {
        final double localTime = (System.currentTimeMillis() / 1000.0) - timeZoneOffset;
        final double utcTime = timeMap.getUtcTime(localTime);

        assertEquals(localTime, utcTime - timeZoneOffset, 1.0);
    }

    @Test
    public void testGetDate() {
        final long currMillis = System.currentTimeMillis();
        final double currSeconds = currMillis / 1000.0;

        assertEquals(new Date(currMillis), timeMap.getDate(currSeconds, true));
        assertFalse(new Date(currMillis).equals(timeMap.getDate(currSeconds, false)));
    }

    @Test
    public void testEpoch() {
        assertEquals(new Date(0), timeMap.getDate(0, true)); // The true UTC epoch
        assertEquals(new Date(70, 0, 1), timeMap.getDate(0, false)); // January 1, 1970 local time
    }

    @Test
    public void testRoundTripBasic() {
        testRoundTrip(0);
        testRoundTrip(System.currentTimeMillis() / 1000.0);
    }

    @Test
    public void testRoundTripDST() {
        // Daylight savings time 2013 starts on March 10
        testRoundTrip(new Date(113, 2, 10)); // Midnight
        testRoundTrip(new Date(113, 2, 10, 0, 59, 0)); // 12:59 am
        testRoundTrip(new Date(113, 2, 10, 1, 0, 0)); // 1:00 am (which doesn't exist!)
        testRoundTrip(new Date(113, 2, 10, 1, 30, 0)); // 1:30 am (which doesn't exist!)
        testRoundTrip(new Date(113, 2, 10, 2, 0, 0)); // 2:00 am
        testRoundTrip(new Date(113, 2, 10, 2, 30, 0)); // 2:30 am

        // Daylight savings time 2013 ends on November 3
        testRoundTrip(new Date(113, 10, 3)); // Midnight
        testRoundTrip(new Date(113, 10, 3, 0, 59, 0)); // 12:59 am
        testRoundTripDoubleTime(new Date(113, 10, 3, 1, 0, 0), -1); // 1:00 am (second time)
        testRoundTripDoubleTime(new Date(113, 10, 3, 1, 30, 0), -1); // 1:30 am (second time)
        testRoundTrip(new Date(113, 10, 3, 2, 0, 0)); // 2:00 am
        testRoundTrip(new Date(113, 10, 3, 2, 30, 0)); // 2:30 am
        testRoundTrip(new Date(113, 10, 3, 3, 0, 0)); // 3:00 am
        // No clear way to represent the first pass over 1:00-1:59:59 am
        // using this Date constructor
    }

    private void testRoundTrip(final Date d) {
        testRoundTrip(d.getTime() / 1000.0);
        testRoundTrip((d.getTime() / 1000.0) - (d.getTimezoneOffset() * 60));
    }

    private void testRoundTrip(final double seconds) {
        assertEquals(seconds, timeMap.getLocalTime(timeMap.getUtcTime(seconds)), 1);
        assertEquals(seconds, timeMap.getUtcTime(timeMap.getLocalTime(seconds)), 1);
    }

    private void testRoundTripDoubleTime(final Date d, final int hoursDiff) {
        testRoundTripDoubleTime(d.getTime() / 1000.0, hoursDiff);
        testRoundTripDoubleTime((d.getTime() / 1000.0) - (d.getTimezoneOffset() * 60), hoursDiff);
    }

    private void testRoundTripDoubleTime(final double seconds, final int hoursDiff) {
        final double absHours = Math.abs(hoursDiff);

        final double utcLocal = timeMap.getLocalTime(timeMap.getUtcTime(seconds));
        final double deltaUtcLocal = Math.abs(seconds - utcLocal);

        assertTrue(deltaUtcLocal < 1
            || (deltaUtcLocal >= 3600 * absHours && deltaUtcLocal < (3601 * absHours) + 1));

        final double localUtc = timeMap.getUtcTime(timeMap.getLocalTime(seconds));
        final double deltaLocalUtc = Math.abs(seconds - localUtc);

        assertTrue(deltaLocalUtc < 1
            || (deltaLocalUtc >= 3600 * absHours && deltaLocalUtc < (3601 * absHours) + 1));
    }
}
