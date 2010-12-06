package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * Represents a single "tile" of data.
 *
 * <p>An overlay type for a generic data tile.  This data type can be loaded
 * from JSON using the {@link #buildTile(String)} method.</p>
 *
 * <p>Note that this class is final because GWT requires that all method
 * calls on an overlay type be resolved at compile time.  We had the choice
 * between making the class final and making all public instance methods
 * final.  We opted for the former option.</p>
 */
public final class GrapherTile extends JavaScriptObject {
	/**
	 * The width of a tile, in data points.
	 * 
	 * <p>Each tile is defined to encompass all data in a time range
	 * equal to {@code Math.pow(2, level) * TILE_WIDTH} seconds.</p>
	 */
	public static final int TILE_WIDTH = 512;

	// Overlay types always have protected, zero-arg constructors
	// with empty bodies
	protected GrapherTile() { }
	
	/**
	 * Returns a single GrapherTile, given a JSON string containing the data.
	 * 
	 * <h2 style="color: red">WARNING:</h2>
	 * 
	 * <p>Note that the JSON string is assumed to be trusted.  This method uses
	 * the JavaScript eval() function, <strong>which could allow arbitrary
	 * code to execute on a user's browser</strong> if this string was not
	 * completely generated by BodyTrack servers over a secure connection.
	 * This could allow an attacker to view all of a user's data, simply by
	 * filling in code to request all valid data tiles and then to send
	 * those tiles to the attacker's machine.  As such, data from insecure
	 * connections, and especially from cross-site requests, should not be
	 * passed in as the data parameter here.</p>
	 *   
	 * @param json
	 * 		a JSON string containing data for a single tile
	 * @return
	 * 		a GrapherTile object with the same data as is found in json
	 */
	public static native GrapherTile buildTile(String json) /*-{
		eval("var tile = " + json);
		return tile;
	}-*/;
	
	/**
	 * Retrives a tile from the specified URL.
	 * 
	 * Adds a tile retrieved from url into destination whenever that tile
	 * arrives.  Since GWT only supports asynchronous server requests, there
	 * is no clean way to make this method block until the object comes in.
	 * Thus, this method takes a {@link java.util.List List}, into which
	 * the GrapherTile is placed when received.
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
	 * @param url
	 * 		the URL from which to retrieve the data
	 * @param destination
	 * 		the {@link java.util.List List} into which this method
	 * 		will add the tile when the tile is loaded from the server
	 */
	public static void retrieveTile(String url,
			final List<GrapherTile> destination) {		
		// Send request to server and catch any errors.
		RequestBuilder builder =
			new RequestBuilder(RequestBuilder.GET, url);
		
		try {
			builder.sendRequest(null, new RequestCallback() {
				@Override
				public void onError(Request request,
						Throwable exception) {
					// Need sensible way to handle this error
				}
				
				@Override
				public void onResponseReceived(Request request,
						Response response) {
					if (response.getStatusCode() == 200)
						destination.add(buildTile(response.getText()));
				}
			});
		} catch (RequestException e) {
			// No sensible way to handle this error, except by not
			// adding any tile to response
		}
	}
	
	/**
	 * Returns the level of resolution at which this tile operates.
	 * 
	 * <p>Note that level 0 corresponds to data points 1 second apart,
	 * and that levels scale in powers of 2.  For instance, level 1
	 * corresponds to data points 2 seconds apart, level 2 corresponds
	 * to data points 4 seconds apart, etc.  Levels can be positive or
	 * negative.</p>
	 * 
	 * @return
	 * 		the level of resolution at which this tile operates
	 */
	public native int getLevel() /*-{
		return this.level;
	}-*/;

	/**
	 * Returns the offset from the epoch at which this tile is found.
	 * 
	 * <p>If we consider a very larger array of tiles, each one at the same
	 * level as this one, and tile 0 beginning on midnight of 1/1/1970,
	 * the offset is the index in this array at which we can find this
	 * tile.</p>
	 * 
	 * @return
	 * 		the offset value for this tile
	 */
	public native int getOffset() /*-{
		return this.offset;
	}-*/;

	/**
	 * Returns the list of field names for this tile.
	 * 
	 * @return
	 * 		the list of field names for this tile
	 */
	public native String[] getFields() /*-{
		return this.fields;
	}-*/;

	/**
	 * Returns the data stored in this tile.
	 * 
	 * @return
	 * 		the data stored in this tile, as a two-dimensional array of
	 * 		double-precision values
	 */
	public native JsArray<JsArrayNumber> getData() /*-{
		return this.data;
	}-*/;

	/**
	 * Returns the value of the optional sample_width parameter in a tile.
	 *
	 * Currently, this is a field only used by the Zeo data.
	 *
	 * @return
	 * 		the value of the sample_width field for this tile, or -1 if
	 * 		such a field is not present
	 */
	public native int getSampleWidth() /*-{
		if (! this.sample_width)
			return -1;

		return this.sample_width;
	}-*/;

	/**
	 * Returns the data points that should be graphed for this GrapherTile.
	 * 
	 * @return
	 * 		a {@link java.util.List List} of
	 * 		{@link org.bodytrack.client.PlottablePoint PlottablePoint}
	 * 		objects that represent the data in this GrapherTile, or
	 * 		<tt>null</tt> if the required data does not seem to be
	 * 		available (i.e. the field names &quot;time&quot; and
	 * 		&quot;mean&quot; are not elements of the array returned
	 * 		by {@link #getFields() getFields()}).
	 */
	public List<PlottablePoint> getDataPoints() {
		int timeIndex = -1;
		int meanIndex = -1;

		String[] fieldNames = getFields();

		for (int i = 0; i < fieldNames.length; i++) {
			if (fieldNames[i].equalsIgnoreCase("time"))
				timeIndex = i;
			else if (fieldNames[i].equalsIgnoreCase("mean"))
				meanIndex = i;
		}

		if (timeIndex < 0 || meanIndex < 0)
			return null;

		List<PlottablePoint> result = new ArrayList<PlottablePoint>();
		JsArray<JsArrayNumber> dataPoints = getData();

		for (int i = 0; i < dataPoints.length(); i++) {
			JsArrayNumber dataPoint = dataPoints.get(i);

			double time = dataPoint.get(timeIndex);
			double mean = dataPoint.get(meanIndex);

			result.add(new PlottablePoint(time, mean));
		}

		return result;
	}

	/**
	 * Returns a TileDescription that describes this tile.
	 *
	 * @return
	 * 		a {@link org.bodytrack.client.TileDescription TileDescription}
	 * 		that describes the level and offset for this tile
	 */
	public TileDescription getDescription() {
		return new TileDescription(getLevel(), getOffset());
	}
}
