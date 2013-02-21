package org.bodytrack.client;

import java.util.Date;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wraps {@link TimeZoneMap} to persist a UTC/local flag along with the stateless map.
 *
 * <p>
 * The {@link #convert(double)} and {@link #reverseConvert(double)} methods also allow
 * outside code to use this class as a way to work completely in terms of UTC, and
 * pass off the UTC-local conversions if some work is begin done in local time mode,
 * and do nothing if the work is being done in UTC mode.
 * </p>
 *
 * <p>
 * Objects of this type are &quot;modal&quot; and not stateful, because they are
 * immutable, but they do have an attached mode (UTC or local).
 * </p>
 */
public final class ModalTimeZoneMap {

    private final TimeZoneMap map;
    private final boolean utcMode;

    public ModalTimeZoneMap(final TimeZoneMap map, final boolean utcMode) {
        if (map == null)
            throw new NullPointerException();

        this.map = map;
        this.utcMode = utcMode;
    }

    public Date getDate(final double seconds) {
        return map.getDate(seconds, utcMode);
    }

    /**
     * If this object is in UTC mode, returns <code>utc</code> unchanged.
     * Otherwise, returns the local-time version of <code>utc</code>.
     */
    public double convert(final double utc) {
        return utcMode ? utc : map.getLocalTime(utc);
    }

    /**
     * If this object is in UTC mode, returns <code>conversionResult</code>
     * unchanged.  Otherwise, shifts <code>conversionResult</code> from local to UTC.
     */
    public double reverseConvert(final double conversionResult) {
        return utcMode ? conversionResult : map.getUtcTime(conversionResult);
    }

    public native String reverseConvert(final String tileString) /*-{
        if (this.@org.bodytrack.client.ModalTimeZoneMap::utcMode) {
            return tile;
        }

        var map = this.@org.bodytrack.client.ModalTimeZoneMap::map;
        var getUtcTime = map.@org.bodytrack.client.TimeZoneMap::getUtcTime(D);

        var tile = JSON.parse(tileString);

        if (!!tile.isArray && tile.isArray()) {
            // Photo tile
            for (var i = 0; i < tile.length; i++) {
                tile[i].begin_d = getUtcTime(tile[i].begin_d);
                tile[i].end_d = getUtcTime(tile[i].end_d);
            }
        } else {
            // Normal tile
            var timeIdx = tile.fields.indexOf("time");
            for (var i = 0; i < tile.data.length; i++) {
                tile.data[i][timeIdx] = getUtcTime(tile.data[i][timeIdx]);
            }
        }

        return JSON.stringify(tile);
    }-*/;
}
