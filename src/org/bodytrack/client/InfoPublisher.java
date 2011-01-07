package org.bodytrack.client;

/**
 * Publishes information to the page outside of the Grapher
 * widget.  This information is available outside of GWT and
 * offers a consistent interface to the rest of the page,
 * regardless of how GWT compiles this widget.
 *
 * <p>This class contains several methods for publishing
 * information to the rest of the page.  They would be
 * static, except that some initialization must be done
 * for the first call into this interface.  As such, this
 * class does not rely on static methods, but is
 * instance-controlled.  There is never more than one instance
 * of this class available at any time.</p>
 *
 * <p>Note that race conditions are possible, since this modifies the
 * global state, but race conditions are not a problem with a single
 * thread.  Since JavaScript is single-threaded (notwithstanding the
 * very limited threading model provided by Web Workers), there is
 * no harm in guarantees that only hold for a single-threaded
 * program.</p>
 */
public final class InfoPublisher {
	private static final InfoPublisher INSTANCE = new InfoPublisher();

	// Don't make constructors for this class available to the
	// rest of the widget
	private InfoPublisher() {
		initialize();
	}

	/**
	 * Used by the constructor to initialize the
	 * window.grapherState global variable.
	 */
	private native void initialize() /*-{
		$wnd.grapherState = {};
		$wnd.grapherState['x_axis'] = {};
		$wnd.grapherState['y_axis'] = {};
		$wnd.grapherState['channel_colors'] = {};
	}-*/;

	/**
	 * Returns an InfoPublisher instance.
	 *
	 * @return
	 * 		an InfoPublisher to be used by this widget
	 */
	public static InfoPublisher getInstance() {
		return INSTANCE;
	}

	/**
	 * Publishes the min/max values for the X-axis.
	 *
	 * @param min
	 * 		the current min value for the X-axis
	 * @param max
	 * 		the current max value for the X-axis
	 */
	public native void publishXAxisBounds(double min, double max) /*-{
		$wnd.grapherState['x_axis']['min'] = min;
		$wnd.grapherState['x_axis']['max'] = max;
	}-*/;

	/**
	 * Publishes the min/max values for the Y-axis.
	 *
	 * @param min
	 * 		the current min value for the Y-axis
	 * @param max
	 * 		the current max value for the Y-axis
	 */
	public native void publishYAxisBounds(double min, double max) /*-{
		$wnd.grapherState['y_axis']['min'] = min;
		$wnd.grapherState['y_axis']['max'] = max;
	}-*/;

	/*
	 * TODO: Add this, or something similar
	 *
	 * Manipulate a $wnd.grapherState['plot_type'] dictionary, perhaps
	 *
	 * Could even use an InfoPublisher.PlotType enum, giving nice
	 *   properties in Java, with only a little glue code needed to
	 *   convert to JavaScript values and publish to the page
	 *
	 * Perhaps expect that Zeo plots are published with the special
	 * color &quot;ZEO&quot;, which will alert any outside scripts to
	 * the type of channel.

	public native void publishPlotType(String channelName, int plotType);
	*/

	/**
	 * Publishes the color for a channel.
	 *
	 * @param channelName
	 * 		the name of the channel
	 * @param color
	 * 		the color of the data plot with the specified channel name
	 */
	public native void publishChannelColor(String channelName,
			String color) /*-{
		$wnd.grapherState['channel_colors'][channelName] = color;
	}-*/;
}
