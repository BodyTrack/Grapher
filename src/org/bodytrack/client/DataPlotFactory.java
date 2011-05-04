package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

import java.util.HashMap;
import java.util.Map;

import org.bodytrack.client.WebDownloader.DownloadSuccessAlertable;

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
// TODO: Make sure we only allow window.initializeGrapher information
// whenever the grapher is originally populated
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
	public DataPlot buildDataPlot(String deviceName, String channelName) {
		return buildDataPlot(deviceName, channelName, getXAxis(),
			getValueAxis(DataPlot.getDeviceChanName(deviceName, channelName)));
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
	 * Asynchronously adds the specified plot to the grapher.
	 *
	 * <p>This actually makes a web request to get the channel specs, then
	 * uses those specs to add the correct type of plot, with the correct
	 * Y-axis bounds.  Note that, if there is any kind of error at
	 * all with the request, the channel is not added and is simply
	 * ignored.</p>
	 *
	 * @param deviceName
	 * 		the name of the device for the channel to add
	 * @param channelName
	 * 		the name of the channel on the device
	 * @throws NullPointerException
	 * 		if either deviceName or channelName is <tt>null</tt>
	 */
	// TODO: Do something to alert the user to a failure
	public void addDataPlotAsync(final String deviceName,
			final String channelName) {
		if (deviceName == null || channelName == null)
			throw new NullPointerException(
				"Can't request for channel with null part of name");

		WebDownloader.doGet(getSpecsUrl(deviceName, channelName),
			WebDownloader.convertToDownloadAlertable(
				new DownloadSuccessAlertable() {
				@Override
				public void onSuccess(String response) {
					JSONValue parsedValue = JSONParser.parseStrict(response);
					if (parsedValue == null) return;

					JSONObject specs = parsedValue.isObject();
					if (specs == null) return;

					DataPlot plot = buildPlotFromSpecs(specs,
						deviceName, channelName);

					// Finally add that plot to the widget
					widget.addDataPlot(plot);
				}
			}));
	}

	/**
	 * Builds a new data plot based on specs.
	 *
	 * <p>This switches on the type field in specs to determine which
	 * kind of plot to return.</p>
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
	// TODO: Use this to replace all the native code, and much of the
	// managed code for adding data plots
	private DataPlot buildPlotFromSpecs(JSONObject specs, String deviceName,
			String channelName) {
		if (specs == null || deviceName == null || channelName == null)
			throw new NullPointerException("Can't use null to create plot");

		String chartType = "plot";
		if (specs.containsKey("type")) {
			JSONValue typeValue = specs.get("type");
			JSONString typeString = typeValue.isString();
			if (typeString != null)
				chartType = typeString.stringValue().toLowerCase();
		}

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

		// Now build the axis
		GraphAxis yAxis;
		if ("photo".equals(chartType))
			yAxis = new PhotoGraphAxis(getYAxisWidth());
		else
			yAxis = new GraphAxis(minVal, maxVal,
				Basis.xRightYUp,
				getYAxisWidth(),
				false);

		// Now actually build the data plot
		String baseUrl =
			DataPlot.buildBaseUrl(userId, deviceName, channelName);
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
	private double getNumber(JSONObject obj, String key) {
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

		String baseUrl = DataPlot.buildBaseUrl(userId,
			deviceName, channelName);
		return new ZeoDataPlot(widget, getXAxis(),
			getValueAxis(DataPlot.getDeviceChanName(deviceName, channelName)),
			deviceName, channelName, baseUrl, minLevel);
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

		// baseUrl should be /photos/:user_id/ for photos
		String baseUrl = "/photos/" + userId + "/";

		return new PhotoDataPlot(widget, getXAxis(),
			new PhotoGraphAxis(getYAxisWidth()),
			deviceName, channelName,
			baseUrl, userId, minLevel);
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
	 * Builds a new axis to use as the value axis for a plain
	 * vanilla {@link org.bodytrack.client.DataPlot DataPlot}.
	 *
	 * @param deviceChanName
	 * 		the deviceName.channelName representation of a
	 * 		device and channel name
	 * @return
	 * 		a new {@link org.bodytrack.client.GraphAxis} available
	 * 		to pass as a parameter to a <tt>DataPlot</tt>
	 * 		constructor
	 * @throws NullPointerException
	 * 		if deviceChanName is <tt>null</tt>
	 */
	private GraphAxis getValueAxis(String deviceChanName) {
		if (deviceChanName == null)
			throw new NullPointerException(
				"Can't build axis for null device and channel name");

		double initialMin = getInitialMin(deviceChanName);
		double initialMax = getInitialMax(deviceChanName);

		// Get the correct values for the bounds
		if (initialMin < MIN_USABLE_VALUE && initialMax < MIN_USABLE_VALUE) {
			initialMin = -1;
			initialMax = 1;
		} else if (initialMin < MIN_USABLE_VALUE)
			initialMin = Math.min(initialMax - 2, -1);
		else if (initialMax < MIN_USABLE_VALUE)
			initialMax = Math.max(initialMin + 2, 1);

		return new GraphAxis(initialMin, initialMax,
			Basis.xRightYUp,
			getYAxisWidth(),
			false);
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
	 * <p>Uses the init_min_time field in the return value of
	 * window.initializeGrapher() if possible.</p>
	 *
	 * @return
	 * 		the time, in seconds, which should be used for the
	 * 		start time of the grapher
	 */
	private static native double getInitialStartTime() /*-{
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
	private static native double getInitialEndTime() /*-{
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
	 * Returns the user ID of the current user, or 0 if the
	 * user's ID cannot be determined.
	 *
	 * <p>Calls the window.initializeGrapher() function from JavaScript,
	 * and checks the return value for a user_id key.  If such
	 * a key is found, returns the value (which is an integer)
	 * corresponding to that key.  Otherwise, returns 0.</p>
	 *
	 * @return
	 * 		the integer user id of the current user, as determined
	 * 		from the return value of window.initializeGrapher()
	 */
	private static native int findUserId() /*-{
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
	 * Returns the minimum value for the axes when the channel
	 * channelName is showing.
	 *
	 * <p>Uses the channel_specs field in the return value of
	 * window.initializeGrapher() if possible, and -1e308 otherwise.</p>
	 *
	 * @return
	 * 		the Y-value to show as the initial minimum of the
	 * 		plot for the data
	 */
	private static native double getInitialMin(String channelName) /*-{
		var DEFAULT_VALUE = -1e308;
		var KEY_1 = "channel_specs";
		var KEY_2 = "min_val";

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

	/**
	 * Returns the maximum value for the axes when the channel
	 * channelName is showing.
	 *
	 * <p>Uses the channel_specs field in the return value of
	 * window.initializeGrapher() if possible, and -1e308 otherwise.</p>
	 *
	 * @return
	 * 		the Y-value to show as the initial maximum of the
	 * 		plot for the data
	 */
	private static native double getInitialMax(String channelName) /*-{
		var DEFAULT_VALUE = -1e308;
		var KEY_1 = "channel_specs";
		var KEY_2 = "max_val";

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

	/**
	 * Returns the supplied min_level variable from window.initializeGrapher.
	 *
	 * @return
	 * 		the supplied min_level, or -20 if no such value exists
	 */
	static native int getMinLevel() /*-{
		var DEFAULT_VALUE = -1000;
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
