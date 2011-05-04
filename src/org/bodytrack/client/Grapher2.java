package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
// TODO: Replace native JavaScript code with calls to
// DataPlotFactory.initializeGrapher()
public class Grapher2 implements EntryPoint {
	// Values that don't make sense in standalone mode
	private VerticalPanel mainLayout;
	private GraphWidget gw;
	private List<DataPlot> plots;
	private DataPlotFactory factory;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		if (isStandAlone()) {
			RootPanel.get("graph").add(new BodyTrackWidget());
		} else {		
			mainLayout = new VerticalPanel();
			// Ensure that everything on the page is the same
			// width
			mainLayout.setWidth((int) (getGrapherWidth()) + "px");

			gw = new GraphWidget(getGrapherWidth(), getGrapherHeight(),
				getAxisMargin());
			factory = DataPlotFactory.getInstance(gw);
			plots = new ArrayList<DataPlot>();

			ChannelManager mgr = gw.getChannelManager();
			ViewSwitchWidget viewSwitcher = new ViewSwitchWidget(gw,
				factory.getUserId(), mgr);
			CurrentChannelsWidget currentChans =
				new CurrentChannelsWidget(mgr);
			ChannelNamesWidget allChans = new ChannelNamesWidget(mgr, factory);

			// Now that we have a ViewSwitchWidget, we want to make
			// InfoPublisher work for everyone
			InfoPublisher.setWidget(viewSwitcher);

			if (shouldBuildFromView())
				buildFromView(viewSwitcher);
			else
				addChannelsToGraphWidget();

			gw.paint();

			mainLayout.add(viewSwitcher);
			mainLayout.add(gw);
			mainLayout.add(currentChans);
			mainLayout.add(allChans);
			RootPanel.get(getDivName()).add(mainLayout);
		}
	}

	/**
	 * Adds all the channels found in the return value of the
	 * window.initializeGrapher function.
	 */
	private void addChannelsToGraphWidget() {
		JsArrayString channels = getChannelNames();

		// This is used to ensure the plots are added in the
		// right order (Zeo first, default type last)
		List<DataPlot> temporaryPlots = new ArrayList<DataPlot>();

		for (int i = 0; i < channels.length(); i++) {
			String deviceChanName = channels.get(i);

			String chartType = getPlotType(channels.get(i)).toLowerCase();

			// Pull out the device name, channel name, and base URL
			String deviceName, channelName;
			int dotIndex = deviceChanName.indexOf('.');
			if (dotIndex > 0) {
				deviceName = deviceChanName.substring(0, dotIndex);
				channelName = deviceChanName.substring(dotIndex + 1);
			} else {
				deviceName = "";
				channelName = deviceChanName;
			}

			buildAndAddPlot(factory, temporaryPlots,
				chartType, deviceName, channelName);
		}

		// Now actually add the plots to the GraphWidget and to
		// our global list of visible plots
		for (DataPlot plot: temporaryPlots) {
			gw.addDataPlot(plot);
			plots.add(plot);
		}
	}

	/**
	 * Switches on the chartType parameter for the type of plot to build.
	 *
	 * <p>It is expected that none of the parameters will be <tt>null</tt>.
	 * This is a safe expectation, since this is an internal method.</p>
	 *
	 * <p>If the new plot is a Zeo plot, this adds it at the beginning of
	 * temporaryPlots; otherwise, the new plot is added to the end of
	 * temporaryPlots.</p>
	 *
	 * @param factory
	 * 		the {@link DataPlotFactory} to use to build the chart
	 * @param temporaryPlots
	 * 		the list of data plots to which we will add the newly created
	 * 		plot
	 * @param chartType
	 * 		a string representation of the type of chart to build
	 * @param deviceName
	 * 		the name of the device for the channel to add
	 * @param channelName
	 * 		the name of the channel to add on the device
	 * @return
	 * 		a {@link org.bodytrack.client.DataPlot DataPlot} constructed
	 * 		from the appropriate method on factory
	 */
	private DataPlot buildAndAddPlot(DataPlotFactory factory,
			List<DataPlot> temporaryPlots,
			String chartType, String deviceName, String channelName) {
		DataPlot plot;

		if ("zeo".equals(chartType)) {
			plot = factory.buildZeoPlot(deviceName, channelName);
			temporaryPlots.add(0, plot);
			return plot;
		}

		if ("photo".equals(chartType))
			plot = factory.buildPhotoPlot(deviceName, channelName);
		else
			plot = factory.buildDataPlot(deviceName, channelName);

		temporaryPlots.add(plot);
		return plot;
	}

	/**
	 * Returns <tt>true</tt> if and only if this grapher should build its
	 * channel list from some view named in window.initializeGrapher.
	 *
	 * @return
	 * 		<tt>true</tt> if and only if the result of calling
	 * 		window.initializeGrapher is an object which has a key
	 * 		&quot;view&quot;, which is connected to a value that
	 * 		JavaScript consider to be <tt>true</tt> (in particular,
	 * 		any nonempty string meets this description)
	 */
	private static native boolean shouldBuildFromView() /*-{
		if (! $wnd.initializeGrapher) {
			return false;
		}

		var data = $wnd.initializeGrapher();
		return (!! data) && (!! data.view);
	}-*/;

	/**
	 * Sets the current view to the view named in window.initializeGrapher.
	 *
	 * @param viewSwitcher
	 * 		the object that keeps track of this grapher's current view
	 */
	private void buildFromView(final ViewSwitchWidget viewSwitcher) {
		final JSONObject params = DataPlotFactory.initializeGrapher();
		if (params.containsKey("view")) {
			JSONString nameValue = params.get("view").isString();
			if (nameValue != null) {
				viewSwitcher.navigateToView(nameValue.stringValue(),
					new Continuation<Object>() {
					@Override
					public void call(Object result) {
						double startTime = DataPlotFactory.getNumber(params,
								"init_min_time");
						double endTime = DataPlotFactory.getNumber(params,
								"init_max_time");
						if (startTime < DataPlotFactory.MIN_USABLE_VALUE
								|| endTime < DataPlotFactory.MIN_USABLE_VALUE)
							return;

						Set<GraphAxis> xAxes =
							viewSwitcher.getChannelManager().getXAxes();
						for (GraphAxis xAxis: xAxes)
							xAxis.replaceBounds(startTime, endTime);
					}
					});
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if and only if there is no
	 * window.initializeGrapher method.
	 *
	 * @return
	 * 		<tt>true</tt> if there is no window.initializeGrapher,
	 * 		<tt>false</tt> otherwise
	 */
	private native boolean isStandAlone() /*-{
		return ! $wnd.initializeGrapher;
	}-*/;

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
		var DEFAULT_VALUE = [];
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
	 * <p>Calls the window.initializeGrapher() function from JavaScript,
	 * and checks the return value for a widget_height key.  If such
	 * a key is found, returns the value (which is an integer)
	 * corresponding to that key.  Otherwise, returns 400.</p>
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
	 * <p>Calls the window.initializeGrapher() function from JavaScript,
	 * and checks the return value for an axis_margin key.  If such
	 * a key is found, returns the value (which is an integer)
	 * corresponding to that key.  Otherwise, returns 10.</p>
	 *
	 * @return
	 * 		the integer axis margin to use for the grapher, as determined
	 * 		from the return value of window.initializeGrapher()
	 */
	static native int getAxisMargin() /*-{
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
	 * Returns the plot type that should be used when the channel
	 * channelName is showing.
	 *
	 * <p>Uses the channel_specs field in the return value of
	 * window.initializeGrapher() if possible, and returns
	 * &quot;default&quot; otherwise (also returns &quot;default&quot;
	 * if there is no type field in the channel_specs field for the
	 * specified channel.</p>
	 *
	 * @return
	 * 		the Y-value to show as the initial maximum of the
	 * 		plot for the data
	 */
	private native String getPlotType(String channelName) /*-{
		var DEFAULT_VALUE = "default";
		var KEY_1 = "channel_specs";
		var KEY_2 = "type";

		if (! $wnd.initializeGrapher) {
			return DEFAULT_VALUE;
		}

		var data = $wnd.initializeGrapher();

		if (! (data && data[KEY_1] && data[KEY_1][channelName]
				&& data[KEY_1][channelName][KEY_2])) {
			return DEFAULT_VALUE;
		}

		return data[KEY_1][channelName][KEY_2];
	}-*/;
}
