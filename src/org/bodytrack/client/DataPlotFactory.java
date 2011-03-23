package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

/**
 * A class with methods to create new objects of
 * type {@link org.bodytrack.client.DataPlot DataPlot}.
 *
 * <p>Objects of this class are mutable, so a caller should be careful to
 * keep only one of these objects available at once.</p>
 */
// TODO: Switch to instance-controlled in the manner of Canvas, with
// an internal static map that allows only one instance per GraphWidget
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
		userId = Grapher2.getUserId();
		minLevel = Grapher2.getMinLevel();
		timeAxis = new TimeGraphAxis(
				Grapher2.getInitialStartTime(),
				Grapher2.getInitialEndTime(),
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
	 */
	public static DataPlotFactory getInstance(GraphWidget widget) {
		return new DataPlotFactory(widget);
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

	// TODO: COMMENT
	public DataPlot buildDataPlot(String deviceName,
			String channelName) {
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

	// TODO: COMMENT
	public ZeoDataPlot buildZeoPlot(String deviceName, String channelName) {
		String baseUrl = DataPlot.buildBaseUrl(userId,
			deviceName, channelName);
		return new ZeoDataPlot(widget, timeAxis,
			getValueAxis(DataPlot.getDeviceChanName(deviceName, channelName)),
			deviceName, channelName, baseUrl, minLevel);
	}

	// TODO: COMMENT
	public PhotoDataPlot buildPhotoPlot(String deviceName, String channelName) {
		// baseUrl should be /photos/:user_id/ for photos
		String baseUrl = "/photos/" + userId + "/";
		String deviceChanName =
			DataPlot.getDeviceChanName(deviceName, channelName);

		return new PhotoDataPlot(widget, timeAxis,
			new PhotoGraphAxis(deviceChanName, getYAxisWidth()),
			deviceName, channelName,
			baseUrl, userId, minLevel);
	}

	// TODO: COMMENT
	private double getYAxisWidth() {
		return axisMargin * 3;
	}

	// TODO: COMMENT
	private GraphAxis getValueAxis(String deviceChanName) {
		double initialMin = Grapher2.getInitialMin(deviceChanName);
		double initialMax = Grapher2.getInitialMax(deviceChanName);

		return new GraphAxis(deviceChanName,
			initialMin > -1e300 ? initialMin : -1,
			initialMax > -1e300 ? initialMax : 1,
			Basis.xRightYUp,
			getYAxisWidth(),
			false);
	}
}
