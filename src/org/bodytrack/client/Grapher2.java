package org.bodytrack.client;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * This is currently a "Hello World" with a sine wave drawn on a
 * set of axes.
 */
public class Grapher2 implements EntryPoint {
	private VerticalPanel mainLayout;
	private GraphWidget gw;
	private DataPlot plot;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		mainLayout = new VerticalPanel();

		setupGraphWidget();

		mainLayout.add(gw);
		RootPanel.get(getDivName()).add(mainLayout);
	}

	private void setupGraphWidget() {
		gw = new GraphWidget(400, 400, 10);

		GraphAxis time = new TimeGraphAxis(
				getInitialStartTime(),
				getInitialEndTime(),
				Basis.xDownYRight,
				70);					// width, in pixels

		// TODO: Determine these min and max values on the fly, perhaps
		// by fetching a tile and pulling in that data
		GraphAxis value = new GraphAxis(-1, 1,	// min, max value
				Basis.xRightYUp,
				30);							// width, in pixels

		plot = new DataPlot(gw, time, value,
			"/tiles/" + getUserId() + "/" + getChannelNames().get(0) + "/");

		gw.addDataPlot(plot);

		gw.paint();
	}

	/**
	 * Returns the name of the div into which this grapher widget should
	 * place itself, or &quot;graph&quot; if that is not available.
	 *
	 * This checks for the div_name key in the return value of the
	 * window.initializeGrapher() function, and returns the value
	 * associated with that key if possible, or &quot;graph&quot;
	 * if that cannot be found.
	 *
	 * @return
	 * 		window.initializeGrapher()[&quot;div_name&quot;] if
	 * 		possible, or &quot;graph&quot; otherwise
	 */
	private native String getDivName() /*-{
		var DEFAULT_VALUE = "graph";
		var KEY = "div_name";

		if (! $wnd.initializeGrapher) {
			return DEFAULT_VALUE;
		}

		var data = $wnd.initializeGrapher();

		if (! (data && data[KEY])) {
			return DEFAULT_VALUE;
		}

		return data[KEY];
	}-*/;

	/**
	 * Returns the starting time of this grapher widget, or one hour
	 * prior to the current time if that cannot be determined.
	 *
	 * Uses the init_min_time field in the return value of
	 * window.initializeGrapher() if possible.
	 *
	 * @return
	 * 		the time, in seconds, which should be used for the
	 * 		start time of the grapher
	 */
	private native double getInitialStartTime() /*-{
		// Equal to the current time, minus one hour
		var DEFAULT_VALUE = ((new Date()).valueOf() / 1000.0) - 3600.0;
		var KEY = "init_min_time";

		if (! $wnd.initializeGrapher) {
			return DEFAULT_VALUE;
		}

		var data = $wnd.initializeGrapher();

		if (! (data && data[KEY])) {
			return DEFAULT_VALUE;
		}

		return data[KEY];
	}-*/;

	/**
	 * Returns the starting time of this grapher widget, or the
	 * current time if that cannot be determined.
	 *
	 * Uses the init_max_time field in the return value of
	 * window.initializeGrapher() if possible.
	 *
	 * @return
	 * 		the time, in seconds, which should be used for the
	 * 		initial end time of the grapher
	 */
	private native double getInitialEndTime() /*-{
		// Equal to the current time
		var DEFAULT_VALUE = (new Date()).valueOf() / 1000.0;
		var KEY = "init_max_time";

		if (! $wnd.initializeGrapher) {
			return DEFAULT_VALUE;
		}

		var data = $wnd.initializeGrapher();

		if (! (data && data[KEY])) {
			return DEFAULT_VALUE;
		}

		return data[KEY];
	}-*/;

	/**
	 * Returns the list of channel names, or [&quot;foo.bar&quot;] if
	 * the channel names cannot be determined.
	 *
	 * Calls the window.initializeGrapher() function from JavaScript,
	 * and checks the return value for a channel_names key.  If such
	 * a key is found, returns the value (which is a JavaScript array
	 * of strings) corresponding to that key.
	 *
	 * @return
	 * 		a {@link com.google.core.gwt.client.JsArrayString
	 * 		JsArrayString} with all the names of channels offered
	 * 		by the return value of window.initializeGrapher()
	 */
	private native JsArrayString getChannelNames() /*-{
		var DEFAULT_VALUE = ["foo.bar"];
		var KEY = "channel_names";

		if (! $wnd.initializeGrapher) {
			return DEFAULT_VALUE;
		}

		var data = $wnd.initializeGrapher();

		if (! (data && data[KEY])) {
			return DEFAULT_VALUE;
		}

		return data[KEY];
	}-*/;

	/**
	 * Returns the user ID of the current user, or 0 if the
	 * user's ID cannot be determined.
	 *
	 * Calls the window.initializeGrapher() function from JavaScript,
	 * and checks the return value for a user_id key.  If such
	 * a key is found, returns the value (which is an integer)
	 * corresponding to that key.  Otherwise, returns 0.
	 *
	 * @return
	 * 		the integer user id of the current user, as determined
	 * 		from the return value of window.initializeGrapher()
	 */
	private native int getUserId() /*-{
		var DEFAULT_VALUE = 0;
		var KEY = "user_id";

		if (! $wnd.initializeGrapher) {
			return DEFAULT_VALUE;
		}

		var data = $wnd.initializeGrapher();

		if (! (data && data[KEY])) {
			return DEFAULT_VALUE;
		}

		return data[KEY];
	}-*/;
}
