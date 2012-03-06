package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single tile of data
 *
 * <p>This is generic enough to hold an array of {@link PhotoDescription PhotoDescription}
 * objects, or a single {@link PlottablePointTile PlottablePointTile}
 * object.  A GrapherTile object has two very important methods:
 * {@link #getPlottableTile()} and {@link #getPhotoDescriptions()}.
 * At least one of these methods will always return <tt>null</tt> for
 * any given <tt>GrapherTile</tt> object, but normally one of these
 * methods will return a non-<tt>null</tt> value that is useful.  In
 * this way, a <tt>GrapherTile</tt> object is a useful container for
 * objects of two other types.</p>
 *
 * <p>Since this almost always holds a reference to a JavaScript
 * overlay object, for efficiency we do not bother making copies of
 * return values, meaning that this class is <em>not</em> immutable.</p>
 */
public final class GrapherTile {
   private final int level;
   private final long offset;
   private final PlottablePointTile tile;
   private final List<PhotoDescription> photoDescs;

   /**
    * The width of a tile, in data points.
    *
    * <p>Each tile is defined to encompass all data in a time range
    * equal to {@code Math.pow(2, level) * TILE_WIDTH} seconds.</p>
    */
   public static final int TILE_WIDTH = 512;

   public GrapherTile(final int level, final String offset,
         final JavaScriptObject tileObj) {
      this(level, Long.parseLong(offset), tileObj);
   }

   /**
    * Initializes this GrapherTile.
    *
    * @param level
    * 		the level of this tile
    * @param offset
    * 		the offset of this tile
    * @param tileObj
    * 		A JSON string representation of either a list of PhotoDescription
    * 		objects or a single PlottablePointTile object.  Another
    * 		possibility is to pass the <code>null</code>, to represent
    * 		that this tile contains no data
    */
   public GrapherTile(final int level, final long offset, final JavaScriptObject tileObj) {
      if (tileObj != null) {
         final JSONValue jsonTile = JSONParser.parseStrict(tileObj.toString());
         if (jsonTile.isObject() != null) {
            photoDescs = null;
            this.tile = PlottablePointTile.buildTile(jsonTile.toString());

            // Use the tile's actual level and offset in this case only
            // Only in this case will the server ever return a level
            // different from the level requested
            if (this.tile != null) {
               this.level = this.tile.getLevel();
               this.offset = this.tile.getOffset();
            } else {
               this.level = level;
               this.offset = offset;
            }
         } else if (jsonTile.isArray() != null) {
            this.level = level;
            this.offset = offset;

            this.tile = null;
            photoDescs = new ArrayList<PhotoDescription>();

            final JSONArray arr = jsonTile.isArray();

            for (int i = 0; i < arr.size(); i++) {
               // TODO: This is a hack
               final PhotoDescription desc = PhotoDescription.buildDescription(arr.get(i).toString());
               photoDescs.add(desc);
            }
         } else {
            // unknown type, so just treat it as null
            this.tile = null;
            photoDescs = null;
            this.level = level;
            this.offset = offset;
         }
      } else {
         this.tile = null;
         photoDescs = null;
         this.level = level;
         this.offset = offset;
      }
   }

   /**
    * Returns the level used to create this <tt>GrapherTile</tt>.
    *
    * @return
    * 		the level passed to the constructor when this <tt>GrapherTile</tt>
    * 		was created
    */
   public int getLevel() {
      return level;
   }

   /**
    * Returns the offset used to create this <tt>GrapherTile</tt>.
    *
    * @return
    * 		the offset passed to the constructor when this
    * 		<tt>GrapherTile</tt> was created
    */
   public long getOffset() {
      return offset;
   }

   /**
    * Returns a description of the current tile.
    *
    * @return
    * 		a new {@link TileDescription TileDescription}
    * 		object representing the level and offset of this tile
    */
   public TileDescription getDescription() {
      return new TileDescription(level, offset);
   }

   /**
    * Returns a {@link PlottablePointTile} object representing any (x, y)
    * points in this <tt>GrapherTile</tt>, or <tt>null</tt> if this
    * <tt>GrapherTile</tt> contains no such information.
    *
    * @return
    * 		the <tt>PlottablePointTile</tt> that was parsed out of the json
    * 		parameter sent to the constructor when this object was
    * 		initialized, or <tt>null</tt> if json was not a
    * 		<tt>PlottablePointTile</tt>
    */
   public PlottablePointTile getPlottableTile() {
      return tile;
   }

   /**
    * Returns a list of available PhotoDescription objects if
    * this tile contains photo descriptions, or <tt>null</tt>
    * otherwise.
    *
    * @return
    * 		a (possibly empty) List of PhotoDescriptions if
    * 		this tile contains photo descriptions, or <tt>null</tt>
    * 		otherwise
    */
   public List<PhotoDescription> getPhotoDescriptions() {
      return photoDescs;
   }

   /**
    * Returns <tt>true</tt> if and only if there is some data
    * available from this <tt>GrapherTile</tt> object.
    *
    * <p>If this returns <tt>true</tt>, then exactly one
    * of {@link GrapherTile#getPlottableTile()} and
    * {@link GrapherTile#getPhotoDescriptions()} will return a
    * non-<tt>null</tt> value.  If this returns <tt>false</tt>,
    * both methods will return <tt>null</tt> if called.</p>
    *
    * @return
    * 		<tt>true</tt> if there is some data available from
    * 		this object, <tt>false</tt> otherwise
    */
   public boolean containsData() {
      return tile != null || photoDescs != null;
   }

   /**
    * Returns a list of plottable points found in the plottable
    * tile this stores, or <tt>null</tt> if
    * {@link #getPlottableTile()} returns <tt>null</tt>.
    *
    * <p>This is exactly equivalent to
    * {@code getPlottableTile() == null ? null :
    * getPlottableTile().getDataPoints()}.</p>
    *
    * @return
    * 		a list of plottable points found in the plottable
    * 		tile this stores, or <tt>null</tt> if
    * 		{@link #getPlottableTile()} returns <tt>null</tt>.
    */
   public List<PlottablePoint> getDataPoints() {
      if (tile == null) {
         return null;
      }

      return tile.getDataPoints();
   }
}
