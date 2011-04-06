package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import org.bodytrack.client.ChannelManager.StringPair;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * A class representing a view and the information that needs to be saved
 * or restored.
 *
 * <p>A <tt>SavableView</tt> is represented as a JSON dictionary, with four
 * required keys.  At the moment, all other keys are ignored, although future
 * implementations may add more keys and use those keys.  The first required
 * required field is name, which has a non-null, nonempty string as its
 * value.  Another required field is x_axes, which has a list as its value.
 * Each element of this list should be a dictionary, with two keys, min_time
 * and max_time.  Each of these values is a number representing seconds
 * since the epoch.  A third required field is y_axes, which has a list as
 * its value.  Each element of the list should be a dictionary, with two
 * required keys, min_val and max_val, and two optional keys, units and
 * label.  The min_val and max_val values should be numbers representing
 * the minimum and maximum bounds on the Y-axis, and the units and label
 * values should be strings.  The final required field in the top-level
 * dictionary is channels.  The value for this should be itself a
 * dictionary, with device names as keys.  Each device name points to a
 * nonempty dictionary, with channel names as keys.  Each channel name points
 * to a dictionary, with required keys x_axis and y_axis. The x_axis key has
 * a nonnegative integer value, which points to the index in x_axes to use
 * for the x-axis on that channel name.  Similarly, the y_axis key is a
 * pointer into the y_axes array.  In both cases, indices start at 0.</p>
 */
// TODO: implement units and label fields for Y-axes, which are not
// currently used or supported in the DataPlot and ChannelManager classes
// TODO: firm up and comment current API
public final class SavableView extends JavaScriptObject {
	/* JavaScript overlay types always have protected empty constructors */
	protected SavableView() {}

	/**
	 * Returns a single SavableView, given a JSON string containing the data.
	 *
	 * <h2 style="color: red">WARNING:</h2>
	 *
	 * <p>Note that the JSON string is assumed to be trusted.  This method uses
	 * the JavaScript eval() function, <strong>which could allow arbitrary
	 * code to execute on a user's browser</strong> if this string was not
	 * completely generated by BodyTrack servers over a secure connection.
	 * This could allow an attacker to view all of a user's data, simply by
	 * filling in code to request all valid data tiles and then to send
	 * those tiles to the attacker's machine.  As such, data from insecure
	 * connections, and especially from cross-site requests, should not be
	 * passed in as the data parameter here.</p>
	 *
	 * @param json
	 * 		a JSON string containing data for a single tile
	 * @return
	 * 		a SavableView object with the same data as is found in json
	 */
	public static native SavableView buildView(String json) /*-{
		eval("var view = " + json);
		return view;
	}-*/;

	public static SavableView buildView(ChannelManager mgr, String name) {
		List<GraphAxis> xAxes = new ArrayList<GraphAxis>(mgr.getXAxes());
		List<GraphAxis> yAxes = new ArrayList<GraphAxis>(mgr.getYAxes());

		// SavableView result = newEmptyView();
		// result.setProperty("name", name);
		// Need to finish filling in the properties for result, perhaps
		// building result as a JavaScriptObject or JSONValue and then
		// making a final cast in the end

		// Handle X-axes
		JSONArray xAxesJson = new JSONArray();
		for (int i = 0; i < xAxes.size(); i++) {
			JSONObject axis = new JSONObject();
			axis.put("min_value", new JSONNumber(xAxes.get(i).getMin()));
			axis.put("max_value", new JSONNumber(xAxes.get(i).getMax()));
			xAxesJson.set(i, axis);
		}

		// Handle Y-axes
		// Don't set units or label
		JSONArray yAxesJson = new JSONArray();
		for (int i = 0; i < yAxes.size(); i++) {
			JSONObject axis = new JSONObject();
			axis.put("min_value", new JSONNumber(yAxes.get(i).getMin()));
			axis.put("max_value", new JSONNumber(yAxes.get(i).getMax()));
			yAxesJson.set(i, axis);
		}

		// Handle the plots themselves
		JSONObject plotsJson = new JSONObject();

		// First, fill in the set of device names
		for (StringPair channel: mgr.getChannelNames())
			if (! plotsJson.containsKey(channel.getFirst()))
				plotsJson.put(channel.getFirst(), new JSONObject());
		// Now fill in the x_axis and y_axis fields for each device
		for (DataPlot plot: mgr.getDataPlots()) {
			String deviceName = plot.getDeviceName();
			String channelName = plot.getChannelName();

			JSONObject axisIndices = new JSONObject();
			axisIndices.put("x_axis",
				new JSONNumber(xAxes.indexOf(plot.getXAxis())));
			axisIndices.put("y_axis",
				new JSONNumber(yAxes.indexOf(plot.getYAxis())));

			// We know this conversion is safe, since we just
			// created the object in the previous loop
			JSONObject deviceJson = plotsJson.get(deviceName).isObject();
			if (deviceJson == null) continue; // Should never happen
			deviceJson.put(channelName, axisIndices);
		}

		JSONObject result = new JSONObject();
		result.put("name", new JSONString(name));
		result.put("x_axes", xAxesJson);
		result.put("y_axes", yAxesJson);
		result.put("channels", plotsJson);

		return buildView(result.toString());
	}

	/**
	 * Converts this object into a
	 * {@link org.bodytrack.client.ChannelManager ChannelManager}.
	 *
	 * @param widget
	 * 		the widget on which the new plots should be drawn
	 * @return
	 * 		a new <tt>ChannelManager</tt> that has plots for the
	 * 		information contained in this <tt>SavableView</tt>, and
	 * 		is attached to widget
	 * @throws NullPointerException
	 * 		if widget is <tt>null</tt>
	 */
	public ChannelManager getDataPlots(GraphWidget widget) {
		if (widget == null)
			throw new NullPointerException("Cannot draw on null widget");

		int axisMargin = Grapher2.getAxisMargin();
		DataPlotFactory factory = DataPlotFactory.getInstance(widget);
		ChannelManager result = new ChannelManager();

		GraphAxis[] xAxes = generateXAxes(axisMargin);

		for (StringPair channel: getChannels()) {
			DataPlot plot = factory.buildDataPlot(channel.getFirst(),
				channel.getSecond(),
				xAxes[getXAxisIndex(channel.getFirst(), channel.getSecond())],
				generateYAxis(channel, axisMargin));
			result.addChannel(plot);
		}

		return result;
	}

	private GraphAxis[] generateXAxes(int axisMargin) {
		GraphAxis[] xAxes = new GraphAxis[countXAxes()];

		for (int i = 0; i < xAxes.length; i++)
			xAxes[i] = new TimeGraphAxis(getMinTime(i),
					getMaxTime(i),
					Basis.xDownYRight,
					axisMargin * 7,
					true);

		return xAxes;
	}

	private GraphAxis generateYAxis(StringPair channel,
			int axisMargin) {
		int yAxisIndex = getYAxisIndex(channel.getFirst(),
			channel.getSecond());

		return new GraphAxis(
			DataPlot.getDeviceChanName(channel.getFirst(),
				channel.getSecond()),
			getMinValue(yAxisIndex),
			getMaxValue(yAxisIndex),
			Basis.xRightYUp,
			axisMargin * 3,
			false);
	}

	/**
	 * Returns the name for this view.
	 *
	 * @return
	 * 		the value of the name field in the native object
	 */
	public native String getName() /*-{
		return this.name;
	}-*/;

	/**
	 * Returns the number of X-axes for this object.
	 *
	 * @return
	 * 		the number of X-axes for this object
	 */
	public native int countXAxes() /*-{
		return this.x_axes.length;
	}-*/;

	/**
	 * Retrieves the min_time field from the X-axis at the specified
	 * index.
	 *
	 * <p>Performs no checks on the index used.</p>
	 *
	 * @param xAxisIndex
	 * 		the index of the axis for which to get the min time
	 * @return
	 * 		the min time of the X-axis at index xAxisIndex
	 */
	private native double getMinTimeUnchecked(int xAxisIndex) /*-{
		return this.x_axes[xAxisIndex].min_time;
	}-*/;

	/**
	 * Retrieves the minimum time from the X-axis at the specified index.
	 *
	 * @param xAxisIndex
	 * 		the index of the X-axis for which to get the minimum time
	 * @return
	 * 		the minimum time for the X-axis at index xAxisIndex
	 * @throws ArrayIndexOutOfBoundsException
	 * 		if xAxisIndex is negative or greater than or equal to
	 * 		the return value of {@link #countXAxes()}
	 */
	public double getMinTime(int xAxisIndex) {
		if (xAxisIndex < 0 || xAxisIndex >= countXAxes())
			throw new ArrayIndexOutOfBoundsException("Index " + xAxisIndex
				+ " cannot index into array of size " + countXAxes());

		return getMinTimeUnchecked(xAxisIndex);
	}

	/**
	 * Retrieves the max_time field from the X-axis at the specified
	 * index.
	 *
	 * <p>Performs no checks on the index used.</p>
	 *
	 * @param xAxisIndex
	 * 		the index of the axis for which to get the max time
	 * @return
	 * 		the max time of the X-axis at index xAxisIndex
	 */
	private native double getMaxTimeUnchecked(int xAxisIndex) /*-{
		return this.x_axes[xAxisIndex].max_time;
	}-*/;

	/**
	 * Retrieves the maximum time from the X-axis at the specified index.
	 *
	 * @param xAxisIndex
	 * 		the index of the X-axis for which to get the maximum time
	 * @return
	 * 		the maximum time for the X-axis at index xAxisIndex
	 * @throws ArrayIndexOutOfBoundsException
	 * 		if xAxisIndex is negative or greater than or equal to
	 * 		the return value of {@link #countXAxes()}
	 */
	public double getMaxTime(int xAxisIndex) {
		if (xAxisIndex < 0 || xAxisIndex >= countXAxes())
			throw new ArrayIndexOutOfBoundsException("Index " + xAxisIndex
				+ " cannot index into array of size " + countXAxes());

		return getMaxTimeUnchecked(xAxisIndex);
	}

	/**
	 * Returns the number of Y-axes for this object.
	 *
	 * @return
	 * 		the number of Y-axes for this object
	 */
	public native int countYAxes() /*-{
		return this.y_axes.length;
	}-*/;

	/**
	 * Retrieves the min_val field from the Y-axis at the specified
	 * index.
	 *
	 * <p>Performs no checks on the index used.</p>
	 *
	 * @param yAxisIndex
	 * 		the index of the axis for which to get the min value
	 * @return
	 * 		the min value of the Y-axis at index yAxisIndex
	 */
	private native double getMinValueUnchecked(int yAxisIndex) /*-{
		return this.y_axes[yAxisIndex].min_val;
	}-*/;

	/**
	 * Retrieves the minimum value from the Y-axis at the specified index.
	 *
	 * @param yAxisIndex
	 * 		the index of the Y-axis for which to get the minimum value
	 * @return
	 * 		the minimum time for the Y-axis at index yAxisIndex
	 * @throws ArrayIndexOutOfBoundsException
	 * 		if yAxisIndex is negative or greater than or equal to
	 * 		the return value of {@link #countYAxes()}
	 */
	public double getMinValue(int yAxisIndex) {
		if (yAxisIndex < 0 || yAxisIndex >= countYAxes())
			throw new ArrayIndexOutOfBoundsException("Index " + yAxisIndex
				+ " cannot index into array of size " + countYAxes());

		return getMinValueUnchecked(yAxisIndex);
	}

	/**
	 * Retrieves the max_val field from the Y-axis at the specified
	 * index.
	 *
	 * <p>Performs no checks on the index used.</p>
	 *
	 * @param yAxisIndex
	 * 		the index of the axis for which to get the max value
	 * @return
	 * 		the max value of the Y-axis at index yAxisIndex
	 */
	private native double getMaxValueUnchecked(int yAxisIndex) /*-{
		return this.y_axes[yAxisIndex].max_val;
	}-*/;

	/**
	 * Retrieves the maximum value from the Y-axis at the specified index.
	 *
	 * @param yAxisIndex
	 * 		the index of the Y-axis for which to get the maximum value
	 * @return
	 * 		the maximum time for the Y-axis at index yAxisIndex
	 * @throws ArrayIndexOutOfBoundsException
	 * 		if yAxisIndex is negative or greater than or equal to
	 * 		the return value of {@link #countYAxes()}
	 */
	public double getMaxValue(int yAxisIndex) {
		if (yAxisIndex < 0 || yAxisIndex >= countYAxes())
			throw new ArrayIndexOutOfBoundsException("Index " + yAxisIndex
				+ " cannot index into array of size " + countYAxes());

		return getMaxValueUnchecked(yAxisIndex);
	}

	/**
	 * Generates the list of channels in this <tt>SavableView</tt>.
	 *
	 * <p>Returns an array of arrays, where the outer array is the list of
	 * channels, and each inner array represents a channel, with the first
	 * element representing the device and the second representing the
	 * channel name.</p>
	 *
	 * @return
	 * 		the list of channel names in this <tt>SavableView</tt>
	 */
	private native JsArray<JsArrayString> getChannelNames() /*-{
		var channelNames = [];
		// The calls to hasOwnProperty filters out keys that come because
		// of some function being grafted onto the JavaScript core
		for (var device in keys(this.channels)) {
			if (this.channels.hasOwnProperty(device)) {
				var channeldict = this.channels[device];
				for (var chan in keys(channeldict)) {
					if (channeldict.hasOwnProperty(chan)) {
						channelNames.push([device, chan]);
					}
				}
			}
		}
		return channelNames;
	}-*/;

	/**
	 * Generates a list of channels stored in this view.
	 *
	 * @return
	 * 		a list of the channels stored in this view
	 */
	public List<StringPair> getChannels() {
		List<StringPair> channels = new ArrayList<StringPair>();

		JsArray<JsArrayString> channelNames = getChannelNames();

		for (int i = 0; i < channelNames.length(); i++) {
			JsArrayString rawChannel = channelNames.get(i);
			// Silently ignore errors.  Not ideal, but probably the best
			// for the user in the face of such a rare event
			if (rawChannel.length() != 2)
				continue;

			String device = rawChannel.get(0);
			String channel = rawChannel.get(1);
			channels.add(new StringPair(device, channel));
		}

		return channels;
	}

	/**
	 * Returns <tt>true</tt> if and only if this contains the specified
	 * channel.
	 *
	 * @param deviceName
	 * 		the device name we want to check
	 * @param channelName
	 * 		the channel name we want to check
	 * @return
	 * 		<tt>true</tt> if and only if this contains the specified channel.
	 * 		If deviceName or channelName is <tt>null</tt>, automatically
	 * 		returns <tt>false</tt>
	 */
	public native boolean hasChannel(String deviceName,
			String channelName) /*-{
		return (!! deviceName) && (!! channelName) && (!! this.channels) &&
			(!! this.channels[deviceName]) &&
			(!! this.channels[deviceName][channelName]);
	}-*/;

	/**
	 * Retrieves the x_axis field from the specified channel.
	 *
	 * <p>Performs no checks on the key and value.</p>
	 *
	 * @param deviceName
	 * 		the device name for which we want the X-axis index
	 * @param channelName
	 * 		the channel name for which we want the X-axis index
	 * @return
	 * 		the X-axis index for the specified channel
	 */
	private native int getXAxisIndexUnchecked(String deviceName,
			String channelName) /*-{
		return this.channels[deviceName][channelName].x_axis;
	}-*/;

	/**
	 * Returns the X-axis index for the specified channel.
	 *
	 * @param deviceName
	 * 		the device name for which we want the X-axis index
	 * @param channelName
	 * 		the channel name for which we want the X-axis index
	 * @return
	 * 		the X-axis index for the specified channel
	 * @throws IllegalArgumentException
	 * 		if the specified channel is not an element of the
	 * 		return values of {@link #getChannels()}
	 */
	public int getXAxisIndex(String deviceName, String channelName) {
		if (! hasChannel(deviceName, channelName))
			throw new IllegalArgumentException(
				"Cannot find index for channel that does not exist");

		return getXAxisIndexUnchecked(deviceName, channelName);
	}

	/**
	 * Retrieves the y_axis field from the specified channel.
	 *
	 * <p>Performs no checks on the key and value.</p>
	 *
	 * @param deviceName
	 * 		the device name for which we want the Y-axis index
	 * @param channelName
	 * 		the channel name for which we want the Y-axis index
	 * @return
	 * 		the Y-axis index for the specified channel
	 */
	private native int getYAxisIndexUnchecked(String deviceName,
			String channelName) /*-{
		return this.channels[deviceName][channelName].y_axis;
	}-*/;

	/**
	 * Returns the Y-axis index for the specified channel.
	 *
	 * @param deviceName
	 * 		the device name for which we want the Y-axis index
	 * @param channelName
	 * 		the channel name for which we want the Y-axis index
	 * @return
	 * 		the Y-axis index for the specified channel
	 * @throws IllegalArgumentException
	 * 		if the specified channel is not an element of the
	 * 		return values of {@link #getChannels()}
	 */
	public int getYAxisIndex(String deviceName, String channelName) {
		if (! hasChannel(deviceName, channelName))
			throw new IllegalArgumentException(
				"Cannot find index for channel that does not exist");

		return getYAxisIndexUnchecked(deviceName, channelName);
	}
}
