package org.bodytrack.client;

import java.util.ArrayList;
import java.util.Date;
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
 * An overlay type for a generic data tile.  This data type can be loaded
 * from JSON using the {@link #buildTile(String)} method.
 */
public class GrapherTile extends JavaScriptObject {
	
	// Overlay types always have protected, zero-arg constructors
	protected GrapherTile() { }
	
	/**
	 * Returns a single GrapherTile, given a JSON string containing the data.
	 * 
	 * <h2 style="color: red">WARNING:</h2>
	 * 
	 * Note that the JSON string is assumed to be trusted.  This method uses
	 * the JavaScript eval() function, <strong>which could allow arbitrary
	 * code to execute on a user's browser</strong> if this string was not
	 * completely generated by BodyTrack servers over a secure connection.
	 * This could allow an attacker to view all of a user's data, simply by
	 * filling in code to request all valid data tiles and then to send
	 * those tiles to the attacker's machine.  As such, data from insecure
	 * connections, and especially from cross-site requests, should not be
	 * passed in as the data parameter here.
	 *   
	 * @param json
	 * 		a JSON string containing data for a single tile
	 * @return
	 * 		a GrapherTile object with the same data as is found in json
	 */
	// TODO: is the eval() really needed, or could we just use "return json"?
	// See http://code.google.com/webtoolkit/doc/latest
	//		/DevGuideCodingBasicsOverlay.html
	// Also see http://code.google.com/webtoolkit/doc/latest
	//		/DevGuideCodingBasicsJSON.html#parsing
	public static native GrapherTile buildTile(String json) /*-{
		return eval(json);
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
	 * Note that the URL is assumed to be trusted.  This method uses
	 * the JavaScript eval() function, <strong>which could allow arbitrary
	 * code to execute on a user's browser</strong> if this URL is not to
	 * a BodyTrack site over a secure connection.  This could allow an
	 * attacker to view all of a user's data, simply by filling in code
	 * to request all valid data tiles and then to send those tiles
	 * to the attacker's machine.  As such, URLs for insecure connections,
	 * and especially for other websites, should not be passed in as
	 * the data parameter here.
	 * 
	 * @param url
	 * 		the URL from which to retrieve the data
	 * @param destination
	 * 		the {@link java.util.List List} into which this method will add the
	 * 		tile when the tile is loaded from the server
	 */
	public static void retrieveTile(String url,
			final List<GrapherTile> destination) {		
		// Send request to server and catch any errors.
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		
		try {
			builder.sendRequest(null, new RequestCallback() {
				@Override
				public void onError(Request request, Throwable exception) { }
				
				@Override
				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == 200)
						destination.add(buildTile(response.getText()));
				}
			});
		} catch (RequestException e) { }
	}
	
	/**
	 * Returns the level of resolution at which this tile operates.
	 * 
	 * Note that level 0 corresponds to data points 1 second apart, and that
	 * levels scale in powers of 2.  For instance, level 1 corresponds to
	 * data points 2 seconds apart, level 2 corresponds to data points 4
	 * seconds apart, etc.  Levels can be positive or negative.
	 * 
	 * @return
	 * 		the level of resolution at which this tile operates
	 */
	public final native int getLevel() /*-{
		return this.level;
	}-*/;
	
	/**
	 * Returns the offset from the epoch at which this tile is found.
	 * 
	 * If we consider a very larger array of tiles, each one at the same
	 * level as this one, and tile 0 beginning on midnight of 1/1/1970,
	 * the offset is the index in this array at which we can find this
	 * tile.
	 * 
	 * @return
	 * 		the offset value for this tile
	 */
	public final native int getOffset() /*-{
		return this.offset;
	}-*/;
	
	/**
	 * Returns the list of field names for this tile.
	 * 
	 * @return
	 * 		the list of field names for this tile
	 */
	public final native String[] getFields() /*-{
		return this.fields;
	}-*/;
	
	/**
	 * Returns the data stored in this tile.
	 * 
	 * @return
	 * 		the data stored in this tile, as a two-dimensional array of
	 * 		double-precision values
	 */
	public final native JsArray<JsArrayNumber> getData() /*-{
		return this.data;
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
	public final List<PlottablePoint> getDataPoints() {
		int timeIndex = -1;
		int meanIndex = -1;
		
		String[] fieldNames = getFields();
		
		for (int i = 0; i < fieldNames.length; i++) {
			if (fieldNames[i].equalsIgnoreCase("time"))
				timeIndex = i;
			else if (fieldNames[i].equalsIgnoreCase("mean"))
				meanIndex = i;
		}
		
		List<PlottablePoint> result = new ArrayList<PlottablePoint>();
		JsArray<JsArrayNumber> dataPoints = getData();
		
		for (int i = 0; i < dataPoints.length(); i++) {
			JsArrayNumber dataPoint = dataPoints.get(i);
			
			double time = dataPoint.get(timeIndex);
			double mean = dataPoint.get(meanIndex);
			
			Date sampleDate = new Date((long) time);
			
			result.add(new PlottablePoint(sampleDate, mean));
		}
		
		return result;
	}
}
