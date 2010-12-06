package org.bodytrack.client;


import gwt.g2d.client.graphics.Color;

import java.util.ArrayList;
import java.util.List;

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
	private List<DataPlot> plots;

	private static final Color[] DATA_PLOT_COLORS = {Canvas.BLACK,
													Canvas.GREEN,
													Canvas.BLUE,
													Canvas.RED};

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
		int axisMargin = getAxisMargin();

		gw = new GraphWidget(getGrapherWidth(),
			getGrapherHeight(), axisMargin);

		GraphAxis time = new TimeGraphAxis(
				getInitialStartTime(),
				getInitialEndTime(),
				Basis.xDownYRight,
				axisMargin * 7);				// width, in pixels

		plots = new ArrayList<DataPlot>();

		int userid = getUserId();
		JsArrayString channels = getChannelNames();
		int minLevel = getMinLevel();

		for (int i = 0; i < channels.length(); i++) {
			double initialMin = getInitialMin(channels.get(i));
			double initialMax = getInitialMax(channels.get(i));

			GraphAxis value = new GraphAxis(
					initialMin > -1e100 ? initialMin : -1,
					initialMax > -1e100 ? initialMax : 1,
					Basis.xRightYUp,
					axisMargin * 3);				// width, in pixels

			DataPlot plot = new DataPlot(gw, time, value,
				"/tiles/" + userid + "/" + channels.get(i) + "/",
				minLevel, DATA_PLOT_COLORS[i % DATA_PLOT_COLORS.length]);

			gw.addDataPlot(plot);
			plots.add(plot);
		}

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
	 * 		a {@link com.google.gwt.core.client.JsArrayString
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

	/**
	 * Returns the width the grapher should be, or 400 if that parameter
	 * is missing or cannot be determined.
	 *
	 * Calls the window.initializeGrapher() function from JavaScript,
	 * and checks the return value for a widget_width key.  If such
	 * a key is found, returns the value (which is an integer)
	 * corresponding to that key.  Otherwise, returns 400.
	 *
	 * @return
	 * 		the integer width to use for the grapher, as determined
	 * 		from the return value of window.initializeGrapher()
	 */
	private native int getGrapherWidth() /*-{
		var DEFAULT_VALUE = 400;
		var KEY = "widget_width";

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
	 * Returns the height the grapher should be, or 400 if that parameter
	 * is missing or cannot be determined.
	 *
	 * Calls the window.initializeGrapher() function from JavaScript,
	 * and checks the return value for a widget_height key.  If such
	 * a key is found, returns the value (which is an integer)
	 * corresponding to that key.  Otherwise, returns 400.
	 *
	 * @return
	 * 		the integer height to use for the grapher, as determined
	 * 		from the return value of window.initializeGrapher()
	 */
	private native int getGrapherHeight() /*-{
		var DEFAULT_VALUE = 400;
		var KEY = "widget_height";

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
	 * Returns the axis margin the page says the grapher should use, or
	 * 10 if that parameter is missing or cannot be determined.
	 *
	 * Calls the window.initializeGrapher() function from JavaScript,
	 * and checks the return value for an axis_margin key.  If such
	 * a key is found, returns the value (which is an integer)
	 * corresponding to that key.  Otherwise, returns 10.
	 *
	 * @return
	 * 		the integer axis margin to use for the grapher, as determined
	 * 		from the return value of window.initializeGrapher()
	 */
	private native int getAxisMargin() /*-{
		var DEFAULT_VALUE = 10;
		var KEY = "axis_margin";

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
	 * Returns the minimum value for the axes when the channel
	 * channelName is showing.
	 *
	 * Uses the channel_specs field in the return value of
	 * window.initializeGrapher() if possible, and -1e308 otherwise.
	 *
	 * @return
	 * 		the Y-value to show as the initial minimum of the
	 * 		plot for the data
	 */
	private native double getInitialMin(String channelName) /*-{
		var DEFAULT_VALUE = -1e308;
		var KEY_1 = "channel_specs";
		var KEY_2 = "min_val";

		if (! $wnd.initializeGrapher) {
			return DEFAULT_VALUE;
		}

		var data = $wnd.initializeGrapher();

		if (! (data && data[KEY_1] && data[KEY_1][channelName])) {
			return DEFAULT_VALUE;
		}

		return data[KEY_1][channelName][KEY_2];
	}-*/;

	/**
	 * Returns the maximum value for the axes when the channel
	 * channelName is showing.
	 *
	 * Uses the channel_specs field in the return value of
	 * window.initializeGrapher() if possible, and -1e308 otherwise.
	 *
	 * @return
	 * 		the Y-value to show as the initial maximum of the
	 * 		plot for the data
	 */
	private native double getInitialMax(String channelName) /*-{
		var DEFAULT_VALUE = -1e308;
		var KEY_1 = "channel_specs";
		var KEY_2 = "max_val";

		if (! $wnd.initializeGrapher) {
			return DEFAULT_VALUE;
		}

		var data = $wnd.initializeGrapher();

		if (! (data && data[KEY_1] && data[KEY_1][channelName])) {
			return DEFAULT_VALUE;
		}

		return data[KEY_1][channelName][KEY_2];
	}-*/;

	/**
	 * Returns the supplied min_level variable from window.initializeGrapher.
	 *
	 * @return
	 * 		the supplied min_level, or -20 if no such value exists
	 */
	private native int getMinLevel() /*-{
		var DEFAULT_VALUE = -20;
		var KEY = "min_level";

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
