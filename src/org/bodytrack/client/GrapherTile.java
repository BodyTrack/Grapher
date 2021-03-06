package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * Represents a single tile of data.
 *
 * <p>
 * This is generic enough to hold an array of
 * {@link PhotoDescription PhotoDescription} objects, or a single
 * {@link PlottablePointTile PlottablePointTile} object.  A {@link GrapherTile}
 * object has two very important methods: {@link #getPlottableTile()}
 * and {@link #getPhotoDescriptions()}.  At least one of these methods
 * will always return <code>null</code> for any given {@link GrapherTile}
 * object, but normally one of these methods will return a
 * non-<code>null</code> value that is useful.  In this way, a
 * {@link GrapherTile} object is a useful container for objects of two
 * other types.  The concept is similar to the discriminated union or sum type
 * from ML-family languages.
 * </p>
 */
public final class GrapherTile {
	private final TileDescription description;
	private final PlottablePointTile tile;
	private final List<PhotoDescription> photoDescs;

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
			if (jsonTile.isObject() != null) {
				photoDescs = null;
				this.tile = PlottablePointTile.buildTile(jsonTile.toString());

				// Use the tile's actual level and offset in this case only
				// Only in this case will the server ever return a level
				// different from the level requested
				if (this.tile != null) {
					this.description = new TileDescription(
							this.tile.getLevel(),
							this.tile.getOffset());
				} else {
					this.description = new TileDescription(level, offset);
				}
			} else if (jsonTile.isArray() != null) {
				this.description = new TileDescription(level, offset);

				this.tile = null;
				photoDescs = new ArrayList<PhotoDescription>();

				final JSONArray arr = jsonTile.isArray();

				for (int i = 0; i < arr.size(); i++) {
					final PhotoDescription desc =
						PhotoDescription.buildDescription(arr.get(i).toString());
					photoDescs.add(desc);
				}
			} else {
				// unknown type, so just treat it as null
				this.tile = null;
				photoDescs = null;
				this.description = new TileDescription(level, offset);
			}
		} else {
			this.tile = null;
			photoDescs = null;
			this.description = new TileDescription(level, offset);
		}
	}

	// A version of the constructor that can be called from JSNI, even under
	// GWT's restrictions on long in JSNI
	public GrapherTile(final int level, final String offsetString,
			final JavaScriptObject tileObj) {
		this(level, Long.parseLong(offsetString), tileObj);
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

	/**
	 * Returns a {@link PlottablePointTile} object representing any (x, y)
	 * points in this {@link GrapherTile}, or <code>null</code> if this
	 * object contains no such information.
	 */
	public PlottablePointTile getPlottableTile() {
		return tile;
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
	 * {@code getPlottableTile() == null ? null :
	 * getPlottableTile().getDataPoints()}.
	 * </p>
	 *
	 * @return
	 * 	A list of plottable points found in the plottable
	 * 	tile this stores, or <code>null</code> if
	 * 	{@link #getPlottableTile()} returns <code>null</code>.
	 */
	public List<PlottablePoint> getDataPoints() {
		return (tile == null) ? null : tile.getDataPoints();
	}
}
