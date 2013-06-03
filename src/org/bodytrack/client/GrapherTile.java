package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * Represents a single tile of data, which is one of the types enumerated in {@link TileType}.
 */
public final class GrapherTile {

    private final TileDescription description;

    private final TileType tileType;
    private final PlottablePointTile pointTile;
    private final SpectralTile spectralTile;
    private final List<PhotoDescription> photoDescs;

    public enum TileType { PLOTTABLE_TILE, SPECTRAL_TILE, PHOTO_TILE }

    /**
     * The width of a tile, in data points.
     *
     * <p>
     * Each tile is defined to encompass all data in a time range
     * equal to {@code Math.pow(2, level) * TILE_WIDTH} seconds.
     * </p>
     */
    public static final int TILE_WIDTH = 512;

    /**
     * Initializes this GrapherTile.
     *
     * @param level
     * 	The level of this tile
     * @param offset
     * 	The offset of this tile
     * @param tileObj
     * 	Either a list of {@link PhotoDescription} objects or a single
     * 	{@link PlottablePointTile} object.  Another option is to pass
     * 	<code>null</code> to represent that this tile contains no data
     */
    public GrapherTile(final int level, final long offset,
            final JavaScriptObject tileObj) {
        if (tileObj != null) {
            final JSONValue jsonTile = JSONParser.parseStrict(tileObj.toString());

            switch (determineTileType(jsonTile)) {
                case PLOTTABLE_TILE:
                    tileType = TileType.PLOTTABLE_TILE;
                    spectralTile = null;
                    photoDescs = null;
                    pointTile = PlottablePointTile.buildTile(jsonTile.toString());

                    if (pointTile != null)
                        description = new TileDescription(pointTile.getLevel(), pointTile.getOffset());
                    else
                        description = new TileDescription(level, offset);

                    return;
                case SPECTRAL_TILE:
                    tileType = TileType.SPECTRAL_TILE;
                    pointTile = null;
                    photoDescs = null;
                    spectralTile = SpectralTile.buildTile(jsonTile);

                    if (spectralTile != null)
                        description = new TileDescription(spectralTile.getLevel(), spectralTile.getOffset());
                    else
                        description = new TileDescription(level, offset);

                    return;
                case PHOTO_TILE:
                    description = new TileDescription(level, offset);
                    tileType = TileType.PHOTO_TILE;

                    pointTile = null;
                    spectralTile = null;
                    photoDescs = new ArrayList<PhotoDescription>();

                    final JSONArray arr = jsonTile.isArray();

                    for (int i = 0; i < arr.size(); i++) {
                        final PhotoDescription desc =
                                PhotoDescription.buildDescription(arr.get(i).toString());
                        photoDescs.add(desc);
                    }

                    return;
            }
        }

        // Fall-through that is reached either if the passed-in tile object is null,
        // or does not match a known tile type
        tileType = null;
        pointTile = null;
        spectralTile = null;
        photoDescs = null;
        description = new TileDescription(level, offset);
    }

    // A version of the constructor that can be called from JSNI, even under
    // GWT's restrictions on long in JSNI
    public GrapherTile(final int level, final String offsetString,
            final JavaScriptObject tileObj) {
        this(level, Long.parseLong(offsetString), tileObj);
    }

    /**
     * Returns a value representing the type of the tile object, or <code>null</code> if
     * the tile object does not meet the basic tests for any tile type.
     *
     * @throws NullPointerException
     *  If <code>tileObj</code> is <code>null</code>
     */
    public static TileType determineTileType(final JavaScriptObject tileObj) {
        if (tileObj == null)
            throw new NullPointerException();

        return determineTileType(JSONParser.parseStrict(tileObj.toString()));
    }

    public static TileType determineTileType(final JSONValue jsonTile) {
        if (jsonTile == null)
            throw new NullPointerException();

        if (jsonTile.isArray() != null)
            return TileType.PHOTO_TILE;

        final JSONObject jsonObj = jsonTile.isObject();
        if (!(jsonObj.containsKey("level") && jsonObj.containsKey("offset")))
            return null;
        if (jsonObj.containsKey("dft"))
            return TileType.SPECTRAL_TILE;
        if (jsonObj.containsKey("fields") && jsonObj.containsKey("data"))
            return TileType.PLOTTABLE_TILE;

        return null;
    }

    public int getLevel() {
        return description.getLevel();
    }

    public long getOffset() {
        return description.getOffset();
    }

    public TileDescription getDescription() {
        return description;
    }

    public TileType getTileType() {
        return tileType;
    }

    /**
     * Returns a {@link PlottablePointTile} object representing any (x, y)
     * points in this {@link GrapherTile}, or <code>null</code> if this
     * object contains no such information.
     */
    public PlottablePointTile getPlottableTile() {
        return pointTile;
    }

    public SpectralTile getSpectralTile() {
        return spectralTile;
    }

    /**
     * Returns a (possibly empty) list of available {@link PhotoDescription} objects
     * if this tile contains photo descriptions, or <code>null</code> otherwise.
     */
    public List<PhotoDescription> getPhotoDescriptions() {
        return photoDescs;
    }

    /**
     * Returns a list of plottable points found in the plottable
     * tile this stores, or <code>null</code> if
     * {@link #getPlottableTile()} returns <code>null</code>.
     *
     * <p>
     * This is exactly equivalent to
     * {@code getPlottableTile() == null ? null : getPlottableTile().getDataPoints()}.
     * </p>
     *
     * @return
     * 	A list of plottable points found in the plottable tile this stores, or
     * 	<code>null</code> if {@link #getPlottableTile()} returns <code>null</code>
     */
    public List<PlottablePoint> getDataPoints() {
        return (pointTile == null) ? null : pointTile.getDataPoints();
    }

}
