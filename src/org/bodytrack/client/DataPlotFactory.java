package org.bodytrack.client;

import java.util.HashMap;
import java.util.Map;

import gwt.g2d.client.graphics.Color;

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
	public DataPlot buildDataPlot(String deviceName,
			String channelName) {
		if (deviceName == null || channelName == null)
			throw new NullPointerException(
				"Cannot build plot with null name");

		Color color =
			DATA_PLOT_COLORS[numCreatedPlots % DATA_PLOT_COLORS.length];
		numCreatedPlots++;

		String baseUrl =
			DataPlot.buildBaseUrl(userId, deviceName, channelName);

		return new DataPlot(widget, timeAxis,
				getValueAxis(
					DataPlot.getDeviceChanName(deviceName, channelName)),
				deviceName, channelName, baseUrl, minLevel, color, true);
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
		return new ZeoDataPlot(widget, timeAxis,
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
		String deviceChanName =
			DataPlot.getDeviceChanName(deviceName, channelName);

		return new PhotoDataPlot(widget, timeAxis,
			new PhotoGraphAxis(deviceChanName, getYAxisWidth()),
			deviceName, channelName,
			baseUrl, userId, minLevel);
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

		return new GraphAxis(deviceChanName,
			initialMin > -1e300 ? initialMin : -1,
			initialMax > -1e300 ? initialMax : 1,
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
