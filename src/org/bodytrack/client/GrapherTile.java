package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

/**
 * Represents a single tile of data.
 *
 * <p>This is generic enough to hold an array of
 * {@link org.bodytrack.client.PhotoDescription PhotoDescription}
 * objects, or a single
 * {@link org.bodytrack.client.PlottablePointTile PlottablePointTile}
 * object.  There are essentially two interfaces for this, but it
 * allows compatibility with existing code and promotes code
 * reuse.</p>
 *
 * <p>Since this almost always holds a reference to a JavaScript
 * overlay object, we do not bother making copies of return values,
 * meaning that this class is <span style="color: red">not</span>
 * immutable.</p>
 */
// TODO: See if using JSONParser.parseStrict() is better than eval()
// (it is definitely safer)
public class GrapherTile {
	// One or the other of these fields will always be null
	private final String url;
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
	 * @param url
	 * 		the URL from which json came
	 * @param json
	 * 		the JSON representation of either a list of PhotoDescription
	 * 		objects or a single PlottablePointTile object.  Another
	 * 		possibility is to pass the empty string as this parameter,
	 * 		to represent that this tile contains no data
	 * @throws NullPointerException
	 * 		if url or json is <tt>null</tt>
	 */
	public GrapherTile(String url, String json) {
		this.url = url;

		// Allow for the special case in which JSON is the empty string
		if (json.equals("")) {
			tile = null;
			photoDescs = null;
			return;
		}

		if (isArray(json)) {
			tile = null;
			photoDescs = new ArrayList<PhotoDescription>();

			JsArray<JavaScriptObject> arr = evalArray(json);

			for (int i = 0; i < arr.length(); i++)
				photoDescs.add((PhotoDescription) arr.get(i));
		} else {
			photoDescs = null;
			tile = PlottablePointTile.buildTile(json);
		}
	}

	/**
	 * Tells if the specified text refers to a JavaScript
	 * Array object rather than to a dictionary.
	 *
	 * @param json
	 * 		the JSON string to parse
	 * @return
	 * 		<tt>true</tt> if json represents a JavaScript
	 * 		Array value, not a dictionary
	 */
	private static boolean isArray(String json) {
		// TODO: This only works in GWT 2.1 and later:
		// JSONValue parsed = JSONParser.parseStrict(json);

		// This is a hack until GWT 2.1
		int bracketIndex = json.indexOf('[');
		int curlyBracketIndex = json.indexOf('{');

		// An object is an array if the bracketed portion
		// surrounds the first set of curly braces
		return bracketIndex >= 0
			&& bracketIndex < curlyBracketIndex;
	}

	// TODO: Document like PlottablePointTile#buildTile()
	// Possibly move to PhotoDescription
	private static native JsArray<JavaScriptObject> evalArray(String json) /*-{
		eval("var arr = " + json);
		return arr;
	}-*/;

	/**
	 * Retrieves a tile from the specified URL.
	 *
	 * <p>Adds a tile retrieved from url into destination whenever that tile
	 * arrives.  Since GWT only supports asynchronous server requests, there
	 * is no clean way to make this method block until the object comes in.
	 * Thus, this method takes a {@link java.util.List List}, into which
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
	 * @param url
	 * 		the URL from which to retrieve the data
	 * @param destination
	 * 		the {@link java.util.List List} into which this method
	 * 		will add the tile when the tile is loaded from the server
	 * @param callback
	 * 		an {@link org.bodytrack.client.Alertable<String>} that
	 * 		is notified whenever the tile arrives, using a message
	 * 		of the url parameter.  This url parameter is helpful
	 * 		in notifications because it allows callback to
	 * 		differentiate between several requested tiles.
	 */
	public static void retrieveTile(String url,
			final List<GrapherTile> destination,
			Alertable<String> callback) {
		// Send request to server and catch any errors.
		RequestBuilder builder =
			new RequestBuilder(RequestBuilder.GET, url);

		try {
			// Required because an inner class can only access
			// a local variable if that variable is final
			final Alertable<String> callbackFinal = callback;
			final String urlFinal = url;

			builder.sendRequest(null, new RequestCallback() {
				@Override
				public void onError(Request request,
						Throwable exception) {
					callbackFinal.onFailure(urlFinal);
				}

				@Override
				public void onResponseReceived(Request request,
						Response response) {
					if (response.getStatusCode() == 200) {
						callbackFinal.onSuccess(urlFinal);

						destination.add(new GrapherTile(urlFinal,
							response.getText()));
					} else
						callbackFinal.onFailure(urlFinal);
				}
			});
		} catch (RequestException e) {
			callback.onFailure(url);
		}
	}

	public String getUrl() {
		return url;
	}

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

	// A convenience method that also helps with backward
	// compatibility
	public List<PlottablePoint> getDataPoints() {
		if (tile == null)
			return null;

		return tile.getDataPoints();
	}

	// A convenience method that also helps with backward
	// compatibility
	public TileDescription getDescription() {
		if (tile == null)
			return null;

		return tile.getDescription();
	}
}
