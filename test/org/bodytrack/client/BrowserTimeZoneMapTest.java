package org.bodytrack.client;

import java.util.Date;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

// Note that any or all of these tests may break if the machine changes time zones
// or changes between daylight savings time and standard time while tests are running.
// They make the implicit assumption that time zone and daylight savings time versus
// standard time don't change during test execution.
@SuppressWarnings("deprecation")
public final class BrowserTimeZoneMapTest {
    private static final BrowserTimeZoneMap timeMap = new BrowserTimeZoneMap();
    private static final int timeZoneOffset = new Date().getTimezoneOffset() * 60;

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
}
