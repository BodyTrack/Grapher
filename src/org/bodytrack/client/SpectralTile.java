package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representation of a spectral view tile for data.
 *
 * <p>This is a JavaScript overlay type for a tile.  This tile is
 * assumed to have dictionary entries called &quot;level&quot;,
 * &quot;offset&quot;, and &quot;dft&quot;.</p>
 */
public final class SpectralTile extends JavaScriptObject {
    // Overlay types always have protected, zero-arg constructors
    // with empty bodies
    protected SpectralTile() {
    }

    /**
     * Returns a single {@link SpectralTile}, given a JSON string containing
     * the data.
     *
     * @param json
     *   A JSON string containing data for a single tile
     * @return
     * 	A {@link SpectralTile} object with the same data as is found in json
     */
    public static SpectralTile buildTile(final String json) {
        return JSONParser.parseStrict(json).isObject().getJavaScriptObject().cast();
    }

    public static SpectralTile buildTile(final JSONValue jsonTile) {
        return jsonTile.isObject().getJavaScriptObject().cast();
    }

    public native int getLevel() /*-{
        return this.level;
    }-*/;

    public long getOffset() {
        return (long)getRawOffset();
    }

    private native double getRawOffset() /*-{
        return this.offset;
    }-*/;

    public native int getNumSteps() /*-{
        return this.num_values;
    }-*/;

    public native String getRawDFT() /*-{
        return this.dft;
    }-*/;

    /**
     * Converts the raw DFT into discrete, usable values.
     *
     * <p>
     * The raw DFT, as returned by {@link #getRawDFT()}, is a string containing amplitude
     * values, packed into one value per character (scaling is such that the max amplitude
     * i.e. the DC channel has value equal to {@link #getNumSteps()}).  The server passes
     * that string as a value in a JSON dictionary (key is &quot;DFT&quot;), which means
     * that the value is UTF8-encoded.  This is cumbersome for drawing, so this method
     * converts the raw DFT to a nicer, cleaner DFT.
     * </p>
     *
     * @return
     *  The discretized DFT, as an unmodifiable list of nonnegative integers
     */
    public List<Integer> getDFT() {
        if (!isCached("DFT"))
            addToCache("DFT", computeDiscretizedDFT());

        return Collections.unmodifiableList((List<Integer>)getFromCache("DFT"));
    }

    private native <T> void addToCache(final String key, final T value) /*-{
        if (!this.hasOwnProperty("cache")) {
            this.cache = {};
        }

        this.cache[key] = value;
    }-*/;

    private native boolean isCached(final String key) /*-{
        if (!this.hasOwnProperty("cache")) {
            this.cache = {};
        }

        return "key" in this.cache;
    }-*/;

    private native <T> T getFromCache(final String key) /*-{
        return this.cache[key];
    }-*/;

    private List<Integer> computeDiscretizedDFT() {
        assert (getNumSteps() < Character.MAX_VALUE);

        final String rawDFT = getRawDFT();
        final List<Integer> result = new ArrayList<Integer>(rawDFT.length());
        for (int i = 0; i < rawDFT.length(); i++)
            result.add((int)rawDFT.charAt(i));

        return result;
    }

    /**
     * Returns a {@link TileDescription} that describes this tile.
     */
    public TileDescription getDescription() {
        return new TileDescription(getLevel(), getOffset());
    }

}
