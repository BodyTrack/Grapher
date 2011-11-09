package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single tile of data.
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
// TODO: Use JSONParser.parseStrict() rather than eval() - this is for safety
// OR use http://www.ietf.org/rfc/rfc4627.txt, section 6, which has an
// expression that will parse JSON safely
// TODO: Make sure the level and offset we store here are consistent with
// that sent to us in a PlottablePointTile

public final class GrapherTile {
   private final int level;
   private final int offset;
   private final PlottablePointTile tile;
   private final List<PhotoDescription> photoDescs;

   /**
    * The width of a tile, in data points.
    *
    * <p>Each tile is defined to encompass all data in a time range
    * equal to {@code Math.pow(2, level) * TILE_WIDTH} seconds.</p>
    */
   public static final int TILE_WIDTH = 512;

   /**
    * Initializes this GrapherTile.
    *
    * @param level
    * 		the level of this tile
    * @param offset
    * 		the offset of this tile
    * @param tile
    * 		the JSON representation of either a list of PhotoDescription
    * 		objects or a single PlottablePointTile object.  Another
    * 		possibility is to pass the <code>null</code>, to represent
    * 		that this tile contains no data
    */
   public GrapherTile(final int level, final int offset, final JSONObject tile) {
      if (tile != null) {
         if (tile.isObject() != null) {
            photoDescs = null;
            this.tile = PlottablePointTile.buildTile(tile.toString());

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
         } else if (tile.isArray() != null) {
            this.level = level;
            this.offset = offset;

            this.tile = null;
            photoDescs = new ArrayList<PhotoDescription>();

            final JSONArray arr = tile.isArray();

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
    * Retrieves a tile from the specified URL.
    *
    * <p>Adds a tile retrieved from url into destination whenever that tile
    * arrives.  Since GWT only supports asynchronous server requests, there
    * is no clean way to make this method block until the object comes in.
    * Thus, this method takes a {@link List List}, into which
    * the GrapherTile is placed when received.</p>
    *
    * <h2 style="color: red">WARNING:</h2>
    *
    * <p>Note that the URL is assumed to be trusted.  This method uses
    * the JavaScript eval() function, <strong>which could allow arbitrary
    * code to execute on a user's browser</strong> if this URL is not to
    * a BodyTrack site over a secure connection.  This could allow an
    * attacker to view all of a user's data, simply by filling in code
    * to request all valid data tiles and then to send those tiles
    * to the attacker's machine.  As such, URLs for insecure connections,
    * and especially for other websites, should not be passed in as
    * the data parameter here.</p>
    *
    * @param datasource
    * 		the datasource from which to retrieve the data
    * @param level
    * 		the level of the tile we are retrieving
    * @param offset
    * 		the offset of the tile we are retrieving
    * @param destination
    * 		the {@link List List} into which this method
    * 		will add the tile when the tile is loaded from the server
    * @param callback
    * 		an {@link Alertable<String>} that
    * 		is notified whenever the tile arrives, using a message
    * 		of the url parameter.  This url parameter is helpful
    * 		in notifications because it allows callback to
    * 		differentiate between several requested tiles.
    */
   public static native void retrieveTile(final JavaScriptObject datasource,
                                          final int level,
                                          final int offset,
                                          final List<GrapherTile> destination,
                                          final Alertable<GrapherTile> callback) /*-{
      datasource(level,
                 offset,
                 function (tile) {
                    var jsonObject = @com.google.gwt.json.client.JSONObject::new(Lcom/google/gwt/core/client/JavaScriptObject;)(tile);
                    var success_tile = @org.bodytrack.client.GrapherTile::new(IILcom/google/gwt/json/client/JSONObject;)(level, offset, jsonObject);

                    // The following two methods are generic in Java, but changing
                    // the parameter specification to Object seems to work, if
                    // only because of type erasure
                    callback.@org.bodytrack.client.Alertable::onSuccess(Ljava/lang/Object;)(success_tile);

                    destination.@java.util.List::add(Ljava/lang/Object;)(success_tile);
                 },
                 function () {
                    var failure_tile = @org.bodytrack.client.GrapherTile::new(IILcom/google/gwt/json/client/JSONObject;)(level, offset, null);

                    // Again, replacing a Java generic with Object seems to work
                    callback.@org.bodytrack.client.Alertable::onFailure(Ljava/lang/Object;)(failure_tile);
                 });
   }-*/;

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
   public int getOffset() {
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
