package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bodytrack.client.WebDownloader.DownloadAlertable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * A class with methods to create new objects of
 * type {@link org.bodytrack.client.DataPlot DataPlot}.
 *
 * <p>Objects of this class are mutable, so a caller should be careful to
 * keep only one of these objects available at once.</p>
 *
 * <p>This class is instance-controlled, meaning that, if a request
 * is made to build a new <tt>DataPlotFactory</tt> using the
 * {@link #getInstance(GraphWidget)} method, an existing
 * <tt>DataPlotFactory</tt> for the supplied widget will be returned, if
 * such a factory exists.</p>
 */
// TODO: Make sure we only allow the use of window.initializeGrapher
// information while the grapher is being populated for the first time
// TODO: Support externally supplied axes for plots other than the
// standard DataPlot
public final class DataPlotFactory {
	/**
	 * The set of colors we use in our returned <tt>DataPlot</tt>
	 * objects.  We use numCreatedPlots to rotate among these values,
	 * so that a widget will rarely, if ever, have two data plots with
	 * the same color.
	 */
	private static final Color[] DATA_PLOT_COLORS = {Canvas.BLACK,
		Canvas.GREEN,
		Canvas.BLUE,
		Canvas.RED,
		Canvas.GRAY,
		Canvas.YELLOW,
		ColorUtils.AQUA,
		ColorUtils.FUCHSIA,
		ColorUtils.MAROON,
		ColorUtils.LIME,
		ColorUtils.NAVY,
		ColorUtils.PURPLE,
		ColorUtils.TEAL,
		ColorUtils.OLIVE};

	/**
	 * Anything less than this is intended to be a sentinel value for
	 * &quot;no data present&quot;.
	 */
	private static final double MIN_USABLE_VALUE = -1e300;

	// Used for instance control
	private static Map<GraphWidget, DataPlotFactory> instances;
	static {
		instances = new HashMap<GraphWidget, DataPlotFactory>();
	}

	private final GraphWidget widget;
	private final double axisMargin;
	private final int userId;
	private final int minLevel;
	private final GraphAxis timeAxis;
	private int numCreatedPlots;
	// For now, only counts DataPlot objects, not Zeo or photo plots

	/**
	 * Private constructor that stops any outside class from directly
	 * constructing a new <tt>DataPlotFactory</tt>.
	 */
	private DataPlotFactory(GraphWidget gw) {
		widget = gw;

		axisMargin = Grapher2.getAxisMargin();
		userId = findUserId();
		minLevel = getMinLevel();
		timeAxis = new TimeGraphAxis(
				getInitialStartTime(),
				getInitialEndTime(),
				Basis.xDownYRight,
				axisMargin * 7,
				true);

		numCreatedPlots = 0;
	}

	/**
	 * Returns a new <tt>DataPlotFactory</tt> for an outside class
	 * to use.
	 *
	 * @param widget
	 * 		the {@link org.bodytrack.client.GraphWidget GraphWidget}
	 * 		on which the data plots produced by the new object will
	 * 		draw themselves
	 * @return
	 * 		a non-<tt>null</tt> ready-to-use <tt>DataPlotFactory</tt>
	 * 		instance
	 * @throws NullPointerException
	 * 		if widget is <tt>null</tt>
	 */
	public static DataPlotFactory getInstance(GraphWidget widget) {
		if (widget == null)
			throw new NullPointerException(
				"Cannot use null widget to show plots");

		if (instances.containsKey(widget))
			return instances.get(widget);

		DataPlotFactory result = new DataPlotFactory(widget);
		instances.put(widget, result);
		return result;
	}

	/**
	 * Returns the ID of the current user.
	 *
	 * @return
	 * 		the ID of the current user
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Returns the widget that this uses as the parent widget for plots.
	 *
	 * @return
	 * 		the widget that this object uses as the parent widget for
	 * 		all new plots
	 */
	public GraphWidget getWidget() {
		return widget;
	}

	/**
	 * Builds a new {@link org.bodytrack.client.DataPlot DataPlot}
	 * with the specified device and channel name.
	 *
	 * @param deviceName
	 * 		the name of the device from which this channel came
	 * @param channelName
	 * 		the name of this channel on the device
	 * @return
	 * 		a <tt>DataPlot</tt> with the specified device and channel
	 * 		name, ready to add to the graph widget used by this
	 * 		factory
	 * @throws NullPointerException
	 * 		if deviceName or channelName is <tt>null</tt>
	 */
	// TODO: This, like all the other buildXXXPlot methods that use the
	// initial specs only, is meant only for use by the Grapher2 class
	public DataPlot buildDataPlot(String deviceName, String channelName) {
		return buildPlotFromSpecs(getInitialSpecs(deviceName, channelName),
			"plot", deviceName, channelName);
	}

	/**
	 * Attempts to get the initial specs from the window.initializeGrapher
	 * function.
	 *
	 * @param deviceName
	 * 		the name of the device for the channel
	 * @param channelName
	 * 		the name of the channel on the device
	 * @return
	 * 		some set of specs based on the pair (deviceName, channelName)
	 * 		and coming from the window.initializeGrapher function.  If it
	 * 		is impossible to meet both those objectives, returns an
	 * 		empty {@link JSONObject}
	 */
	private JSONObject getInitialSpecs(String deviceName, String channelName) {
		if (deviceName == null || channelName == null)
			return new JSONObject();

		String channelKey =
			DataPlot.getDeviceChanName(deviceName, channelName);

		JSONObject initializeGrapher = initializeGrapher();
		if (initializeGrapher.containsKey("channel_specs")) {
			JSONValue overallSpecsVal = initializeGrapher.get("channel_specs");
			JSONObject overallSpecs = overallSpecsVal.isObject();

			if (overallSpecs != null && overallSpecs.containsKey(channelKey)) {
				JSONValue specsVal = overallSpecs.get(channelKey);
				JSONObject specs = specsVal.isObject();
				if (specs != null)
					return specs;
				// Otherwise, the default value is returned
			}
		}

		return new JSONObject();
	}

	/**
	 * Builds a new {@link org.bodytrack.client.DataPlot DataPlot}
	 * with the specified device and channel names, and axes.
	 *
	 * @param deviceName
	 * 		the name of the device from which this channel came
	 * @param channelName
	 * 		the name of this channel on the device
	 * @param xAxis
	 * 		the X-axis to use to build the plot
	 * @param yAxis
	 * 		the Y-axis to use to build the plot
	 * @return
	 * 		a <tt>DataPlot</tt> with the specified device and channel
	 * 		name, ready to add to the graph widget used by this
	 * 		factory
	 * @throws NullPointerException
	 * 		if any parameter is <tt>null</tt>
	 */
	public DataPlot buildDataPlot(String deviceName, String channelName,
			GraphAxis xAxis, GraphAxis yAxis) {
		if (deviceName == null || channelName == null)
			throw new NullPointerException("Cannot build plot with null name");
		if (deviceName == null || channelName == null)
			throw new NullPointerException("Cannot build plot with null axis");

		String baseUrl =
			DataPlot.buildBaseUrl(userId, deviceName, channelName);

		return new DataPlot(widget, xAxis, yAxis, deviceName, channelName,
			baseUrl, minLevel, getNextColor(), true);
	}

	/**
	 * Returns the current color in {@link #DATA_PLOT_COLORS}, and moves
	 * one color forward.
	 *
	 * @return
	 * 		some color from {@link #DATA_PLOT_COLORS}
	 */
	private Color getNextColor() {
		Color result =
			DATA_PLOT_COLORS[numCreatedPlots % DATA_PLOT_COLORS.length];
		numCreatedPlots++;

		return result;
	}

	/**
	 * Asynchronously builds the specified plot, then calls a success
	 * continuation with that value.
	 *
	 * <p>This actually makes a web request to get the channel specs, then
	 * uses those specs to build the correct type of plot, with the correct
	 * Y-axis bounds.  Then, calls succ.  However, if there is any kind
	 * of error at all with the request, the channel is not built and
	 * the failure continuation is called.</p>
	 *
	 * @param deviceName
	 * 		the name of the device for the channel to add
	 * @param channelName
	 * 		the name of the channel on the device
	 * @param succ
	 * 		a success continuation that will be called with the plot
	 * 		that is built, as long as that succeeds
	 * @param fail
	 * 		a failure continuation that will be called with parameter
	 * 		<tt>null</tt> if any part of the process fails.  May be
	 * 		<tt>null</tt> to signify to do nothing in case of a
	 * 		failure
	 * @throws NullPointerException
	 * 		if deviceName, channelName, or succ is <tt>null</tt>
	 */
	public void buildDataPlotAsync(final String deviceName,
			final String channelName, final Continuation<DataPlot> succ,
			Continuation<Object> fail) {
		if (deviceName == null || channelName == null)
			throw new NullPointerException(
				"Can't request for channel with null part of name");

		if (succ == null)
			throw new NullPointerException(
				"Can't pass values to null continuation");

		if (fail == null)
			fail = new Continuation<Object>() {
				@Override
				public void call(Object result) { }
			};

		final Continuation<Object> fc = fail; // Failure continuation

		WebDownloader.doGet(getSpecsUrl(deviceName, channelName),
			new DownloadAlertable() {
				@Override
				public void onSuccess(String response) {
					JSONValue parsedValue = JSONParser.parseStrict(response);
					if (parsedValue == null) {
						fc.call(null);
						return;
					}

					JSONObject specs = parsedValue.isObject();
					if (specs == null) {
						fc.call(null);
						return;
					}

					try {
						DataPlot plot = buildPlotFromSpecs(specs,
							deviceName, channelName);

						if (plot != null)
							succ.call(plot);
						else
							fc.call(null);
					} catch (Exception e) {
						// I know it is usually bad form to catch
						// Exception, but here it matches the spec
						fc.call(null);
					}
				}

				@Override
				public void onFailure(Request failed) {
					fc.call(null);
				}
			});
	}

	/**
	 * Returns the URL to use for getting the channel specs.
	 *
	 * @param deviceName
	 * 		the non-<tt>null</tt> name of the device for this channel
	 * @param channelName
	 * 		the non-<tt>null</tt> name of the channel on the device
	 * @return
	 * 		the URL that can be used to get the channel specs for the
	 * 		specified channel
	 */
	private String getSpecsUrl(String deviceName, String channelName) {
		return "/users/" + userId + "/channel_infos/"
			+ deviceName + "." + channelName + "/get.json";
	}

	/**
	 * Builds a new data plot based on specs.
	 *
	 * <p>This switches on the type field in specs to determine which
	 * kind of plot to return, and on the bounds for the Y-axis on
	 * that plot.</p>
	 *
	 * @param specs
	 * 		the channel specs JSON dictionary for the channel
	 * @param deviceName
	 * 		the name of the device for the channel
	 * @param channelName
	 * 		the name of the channel on the device
	 * @return
	 * 		a new {@link org.bodytrack.client.DataPlot DataPlot} with
	 * 		the correct type and Y-axis bounds, based on the information
	 * 		from specs
	 * @throws NullPointerException
	 * 		if any parameter is <tt>null</tt>
	 */
	// TODO: Handle units
	private DataPlot buildPlotFromSpecs(JSONObject specs, String deviceName,
			String channelName) {
		if (specs == null || deviceName == null || channelName == null)
			throw new NullPointerException("Can't use null to create plot");

		String chartType = "plot";
		if (specs.containsKey("type")) {
			JSONValue typeValue = specs.get("type");
			JSONString typeString = typeValue.isString();
			if (typeString != null)
				chartType = typeString.stringValue();
		}

		return buildPlotFromSpecs(specs, chartType, deviceName, channelName);
	}

	/**
	 * Returns a plot with axis bounds based on specs, but actual type based
	 * on the chartType parameter.
	 *
	 * @param specs
	 * 		the channel specs JSON dictionary for the channel
	 * @param chartType
	 * 		the type of chart to use.  Should be &quot;plot&quot; for a
	 * 		standard {@link DataPlot}, &quot;zeo&quot; for a
	 * 		{@link ZeoDataPlot}, or &quot;photo&quot; for a
	 * 		{@link PhotoDataPlot}.  Anything that is neither &quot;zeo&quot;
	 * 		nor &quot;photo&quot; will be interpreted as &quot;plot&quot;.  Of
	 * 		course, in the above discussion, the quotes around values were
	 * 		meant to set them off as literal strings to be used.  A caller
	 * 		should not actually add escaped quotes to pass in to this method
	 * @param deviceName
	 * 		the name of the device for the channel
	 * @param channelName
	 * 		the name of the channel on the device
	 * @return
	 * 		a new {@link org.bodytrack.client.DataPlot DataPlot} with
	 * 		the correct type and Y-axis bounds, based on the information
	 * 		from specs
	 * @throws NullPointerException
	 * 		if any parameter is <tt>null</tt>
	 */
	private DataPlot buildPlotFromSpecs(JSONObject specs, String chartType,
			String deviceName, String channelName) {
		if (specs == null || chartType == null
				|| deviceName == null || channelName == null)
			throw new NullPointerException("Can't use null to create plot");

		chartType = chartType.toLowerCase();

		double minVal = getNumber(specs, "min_val");
		double maxVal = getNumber(specs, "max_val");

		// Handle the case in which there is a missing
		// or invalid min_val or max_val field
		if (minVal < MIN_USABLE_VALUE
				&& maxVal < MIN_USABLE_VALUE) {
			minVal = -1;
			maxVal = 1;
		} else if (minVal < MIN_USABLE_VALUE)
			minVal = Math.min(maxVal - 2, -1);
		else if (maxVal < MIN_USABLE_VALUE)
			maxVal = Math.max(minVal + 2, 1);

		// Now build the axis, and put together the URL that the plot
		// should use to get more data
		GraphAxis yAxis;
		String baseUrl;
		if ("photo".equals(chartType)) {
			yAxis = new PhotoGraphAxis(getYAxisWidth());
			baseUrl = "/photos/" + userId + "/";
		} else {
			yAxis = new GraphAxis(minVal, maxVal,
				Basis.xRightYUp,
				getYAxisWidth(),
				false);
			baseUrl = DataPlot.buildBaseUrl(userId, deviceName, channelName);
		}

		// Now actually build the data plot
		DataPlot plot;
		if ("zeo".equals(chartType))
			plot = new ZeoDataPlot(widget, getXAxis(), yAxis,
				deviceName, channelName,
				baseUrl, minLevel);
		else if ("photo".equals(chartType))
			// The cast on yAxis will succeed because we made
			// yAxis into a PhotoGraphAxis above whenever the
			// chartType was photo
			plot = new PhotoDataPlot(widget,
				getXAxis(), (PhotoGraphAxis) yAxis,
				deviceName, channelName,
				baseUrl, userId, minLevel);
		else
			plot = new DataPlot(widget, getXAxis(), yAxis,
				deviceName, channelName,
				baseUrl, minLevel,
				getNextColor(),
				true);

		return plot;
	}

	/**
	 * Builds a new {@link org.bodytrack.client.ZeoDataPlot ZeoDataPlot}
	 * with the specified device and channel name.
	 *
	 * @param deviceName
	 * 		the name of the device from which this channel came
	 * @param channelName
	 * 		the name of this channel on the device
	 * @return
	 * 		a <tt>ZeoDataPlot</tt> with the specified device and channel
	 * 		name, ready to add to the graph widget used by this
	 * 		factory
	 * @throws NullPointerException
	 * 		if deviceName or channelName is <tt>null</tt>
	 */
	public ZeoDataPlot buildZeoPlot(String deviceName, String channelName) {
		if (deviceName == null || channelName == null)
			throw new NullPointerException(
				"Cannot build plot with null name");

		return (ZeoDataPlot) buildPlotFromSpecs(
			getInitialSpecs(deviceName, channelName), "zeo",
			deviceName, channelName);
	}

	/**
	 * Builds a new {@link org.bodytrack.client.PhotoDataPlot PhotoDataPlot}
	 * with the specified device and channel name.
	 *
	 * @param deviceName
	 * 		the name of the device from which this channel came
	 * @param channelName
	 * 		the name of this channel on the device
	 * @return
	 * 		a <tt>PhotoDataPlot</tt> with the specified device and
	 * 		channel name, ready to add to the graph widget used by
	 * 		this factory
	 * @throws NullPointerException
	 * 		if deviceName or channelName is <tt>null</tt>
	 */
	public PhotoDataPlot buildPhotoPlot(String deviceName,
			String channelName) {
		if (deviceName == null || channelName == null)
			throw new NullPointerException(
				"Cannot build plot with null name");

		return (PhotoDataPlot) buildPlotFromSpecs(
			getInitialSpecs(deviceName, channelName), "photo",
			deviceName, channelName);
	}

	/**
	 * Returns the correct X-axis to use for a new plot.
	 *
	 * @return
	 * 		the first axis currently being used, or the timeAxis
	 * 		private variable, if no axes are currently being used
	 */
	private GraphAxis getXAxis() {
		ChannelManager channels = widget.getChannelManager();
		if (channels != null && channels.getXAxes().size() > 0)
			return CollectionUtil.getFirst(channels.getXAxes());

		return timeAxis;
	}

	/**
	 * Computes the width a Y-axis should be.
	 *
	 * @return
	 * 		the width a Y-axis should be, based on the value of
	 * 		axisMargin
	 */
	private double getYAxisWidth() {
		return axisMargin * 3;
	}

	/**
	 * Returns the starting time of this grapher widget, or one hour
	 * prior to the current time if that cannot be determined.
	 *
	 * @return
	 * 		the time, in seconds since the epoch, which should be used
	 * 		for the start time of the grapher
	 */
	private static double getInitialStartTime() {
		double initMinTime = getNumber(initializeGrapher(), "init_min_time");

		return initMinTime < MIN_USABLE_VALUE ? (now() - 3600)
											: (int) initMinTime;
	}

	/**
	 * Returns the starting time of this grapher widget, or the
	 * current time if that cannot be determined.
	 *
	 * <p>Uses the init_max_time field in the return value of
	 * window.initializeGrapher() if possible.</p>
	 *
	 * @return
	 * 		the time, in seconds, which should be used for the
	 * 		initial end time of the grapher
	 */
	private static double getInitialEndTime() {
		double initMinTime = getNumber(initializeGrapher(), "init_max_time");

		return initMinTime < MIN_USABLE_VALUE ? now() : (int) initMinTime;
	}

	/**
	 * Returns the number of seconds since the epoch.
	 *
	 * @return
	 * 		the number of seconds since the epoch
	 */
	private static double now() {
		Date now = new Date();
		return now.getTime() / 1000.0;
	}

	/**
	 * Returns the user ID of the current user, or 0 if the
	 * user's ID cannot be determined.
	 *
	 * @return
	 * 		the integer user id of the current user
	 */
	private static int findUserId() {
		double id = getNumber(initializeGrapher(), "user_id");

		return id < MIN_USABLE_VALUE ? 0 : (int) id;
	}

	/**
	 * Returns the supplied min_level variable from window.initializeGrapher.
	 *
	 * @return
	 * 		the supplied min_level, or -20 if no such value exists
	 */
	private static int getMinLevel() {
		double level = getNumber(initializeGrapher(), "min_level");

		return level < MIN_USABLE_VALUE ? -20 : (int) level;
	}

	/**
	 * Pulls a number out of the specified object, if possible.
	 *
	 * @param obj
	 * 		the object for which key may or may not be present
	 * @param key
	 * 		the key to search for in obj
	 * @return
	 * 		the value at obj[key] if possible, or something less
	 * 		than {@link #MIN_USABLE_VALUE}, if there is some kind
	 * 		of error or if either parameter is <tt>null</tt>
	 */
	private static double getNumber(JSONObject obj, String key) {
		// This method takes advantage of the fact that MIN_USABLE_VALUE
		// is negative, making the result of multiplying by 1.01 less
		// than MIN_USABLE_VALUE

		if (obj == null || key == null)
			return MIN_USABLE_VALUE * 1.01;

		if (obj.containsKey(key)) {
			JSONValue rawJson = obj.get(key);
			JSONNumber num = rawJson.isNumber();
			if (num != null)
				return num.doubleValue();
		}

		return MIN_USABLE_VALUE * 1.01;
	}

	/**
	 * Calls window.initializeGrapher and returns the result as a
	 * {@link JSONObject}.
	 *
	 * @return
	 * 		a {@link JSONObject} version of the result of calling
	 * 		window.initializeGrapher
	 */
	public static JSONObject initializeGrapher() {
		JavaScriptObject obj = callInitializeGrapher();
		if (obj == null)
			return new JSONObject();

		return new JSONObject(obj);
	}

	/**
	 * Simply calls the native window.initializeGrapher and returns
	 * the result.
	 *
	 * @return
	 * 		the result of calling window.initializeGrapher in
	 * 		native JavaScript
	 */
	private static native JavaScriptObject callInitializeGrapher() /*-{
		if ($wnd.initializeGrapher)
			return $wnd.initializeGrapher();
		else
			return null;
	}-*/;
}
