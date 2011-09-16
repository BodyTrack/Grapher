package org.bodytrack.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A global channel management class, which keeps track of which
 * channels are showing and which are not.
 *
 * <p>Objects of this class are mutable, and no internal synchronization
 * is implemented, so external synchronization is required to make
 * objects of this class thread-safe.</p>
 */
public class ChannelManager {
	private final List<DataPlot> dataPlots;
	private final List<DataPlot> unmodDataPlots;

	/*
	 * xAxisMap and yAxisMap provide the reverse mapping from
	 * dataPlots.get(i).getXAxis() and dataPlots.get(i).getYAxis().
	 * They map from axes to sets of data plots associated with
	 * those axes.
	 *
	 * An invariant is that each element of xAxisMap and yAxisMap
	 * has at least one plot in dataPlots referencing it.
	 */
	private final Map<GraphAxis, List<DataPlot>> xAxisMap;
	private final Map<GraphAxis, List<DataPlot>> unmodXAxisMap;

	private final List<GraphAxis> xAxes;
	private final List<GraphAxis> unmodXAxes;

	private final Map<GraphAxis, List<DataPlot>> yAxisMap;
	private final Map<GraphAxis, List<DataPlot>> unmodYAxisMap;

	private final List<GraphAxis> yAxes;
	private final List<GraphAxis> unmodYAxes;

	// There is some redundancy provided by this variable, but it
	// does increase efficiency when dealing with channel names
	private final Map<StringPair, DataPlot> channelMap;
	private final Map<StringPair, DataPlot> unmodChannelMap;
	private final Set<StringPair> unmodChannels;

	private final List<ChannelChangedListener> listeners;

	/**
	 * Creates a new <tt>ChannelManager</tt> that does not include
	 * any channels.
	 */
	public ChannelManager() {
		dataPlots = new ArrayList<DataPlot>();
		xAxisMap = new HashMap<GraphAxis, List<DataPlot>>();
		xAxes = new ArrayList<GraphAxis>();
		yAxisMap = new HashMap<GraphAxis, List<DataPlot>>();
		yAxes = new ArrayList<GraphAxis>();
		channelMap = new HashMap<StringPair, DataPlot>();
		listeners = new ArrayList<ChannelChangedListener>();

		// Set up the unmodifiable objects that are exposed to the world
		unmodDataPlots = Collections.unmodifiableList(dataPlots);

		unmodXAxisMap = Collections.unmodifiableMap(xAxisMap);
		unmodXAxes = Collections.unmodifiableList(xAxes);
		unmodYAxisMap = Collections.unmodifiableMap(yAxisMap);
		unmodYAxes = Collections.unmodifiableList(yAxes);

		unmodChannelMap = Collections.unmodifiableMap(channelMap);
		unmodChannels = Collections.unmodifiableSet(channelMap.keySet());
	}

	/**
	 * Returns an unmodifiable view of the list of
	 * {@link org.bodytrack.client.DataPlot DataPlot} objects
	 * held by this {@link ChannelManager}.
	 *
	 * @return
	 * 		an unmodifiable view of the list of {@link DataPlot}
	 * 		objects this holds
	 */
	public List<DataPlot> getDataPlots() {
		return unmodDataPlots;
	}

	/**
	 * Returns an unmodifiable view of the map from channel names
	 * back to the plots that actually make those channels
	 * available to the user.
	 *
	 * @return
	 * 		a map from channel names to plots that graph those
	 * 		channels
	 */
	public Map<StringPair, DataPlot> getChannelMap() {
		return unmodChannelMap;
	}

	/**
	 * Returns an unmodifiable view of the set of currently available
	 * channel names.
	 *
	 * @return
	 * 		an unmodifiable view of the set of names for all channels
	 * 		currently held by this <tt>ChannelManager</tt>
	 */
	public Set<StringPair> getChannelNames() {
		return unmodChannels;
	}

	/**
	 * Returns an unmodifiable view of a map from X-axes back to their
	 * associated {@link org.bodytrack.client.DataPlot DataPlot} objects.
	 *
	 * @return
	 * 		an unmodifiable view of a map from X-axes back to their
	 * 		associated {@link org.bodytrack.client.DataPlot DataPlot}
	 * 		objects
	 */
	public Map<GraphAxis, List<DataPlot>> getXAxisMap() {
		return unmodXAxisMap;
	}

	/**
	 * Returns an unmodifiable view of a map from Y-axes back to their
	 * associated {@link org.bodytrack.client.DataPlot DataPlot} objects.
	 *
	 * @return
	 * 		an unmodifiable view of a map from Y-axes back to their
	 * 		associated {@link org.bodytrack.client.DataPlot DataPlot}
	 * 		objects
	 */
	public Map<GraphAxis, List<DataPlot>> getYAxisMap() {
		return unmodYAxisMap;
	}

	/**
	 * Returns an unmodifiable view of the set of X-axes held by
	 * any {@link org.bodytrack.client.DataPlot DataPlot} object
	 * held by this <tt>ChannelManager</tt>.
	 *
	 * @return
	 * 		an unmodifiable view of the set of X-axes on
	 * 		<tt>DataPlot</tt> objects this holds
	 */
	public List<GraphAxis> getXAxes() {
		return unmodXAxes;
	}

	/**
	 * Returns an unmodifiable view of the set of Y-axes held by
	 * any {@link org.bodytrack.client.DataPlot DataPlot} object
	 * held by this {@link ChannelManager}.
	 *
	 * @return
	 * 		an unmodifiable view of the set of Y-axes on {@link DataPlot}
	 * 		objects this holds
	 */
	public List<GraphAxis> getYAxes() {
		return unmodYAxes;
	}

	/**
	 * Checks for inclusion of plot in the set of <tt>DataPlot</tt>
	 * objects this <tt>ChannelManager</tt> maintains.
	 *
	 * @param plot
	 * 		the <tt>DataPlot</tt> to check for
	 * @return
	 * 		<tt>false</tt> if plot is <tt>null</tt>, and the
	 * 		equivalent of {@code getDataPlots().contains(plot)}
	 * 		otherwise
	 */
	public boolean hasChannel(DataPlot plot) {
		return (plot != null) && dataPlots.contains(plot);
	}

	/**
	 * Checks for whether the specified channel is contained in this
	 * <tt>ChannelManager</tt>.
	 *
	 * @param deviceName
	 * 		the name of the device for the channel to check
	 * @param channelName
	 * 		the name of the channel on the device
	 * @return
	 * 		<tt>true</tt> if and only if there is some plot with
	 * 		the specified device and channel name in this
	 * 		<tt>ChannelManager</tt>.  If deviceName or channelName
	 * 		is <tt>null</tt>, always returns <tt>false</tt>
	 */
	public boolean hasChannel(String deviceName, String channelName) {
		return deviceName != null && channelName != null
			&& channelMap.containsKey(new StringPair(deviceName, channelName));
	}

	/**
	 * Returns <tt>true</tt> if and only if axis is an X-axis or
	 * Y-axis for some <tt>DataPlot</tt> object this holds.
	 *
	 * @param axis
	 * 		the axis to check for membership
	 * @return
	 * 		<tt>true</tt> if and only if axis is not <tt>null</tt>,
	 * 		and there is some <tt>DataPlot</tt> {@code d} in the
	 * 		return value of {@code getDataPlots()} such that
	 * 		{@code d.getXAxis().equals(axis)} or such that
	 * 		{@code d.getYAxis().equals(axis)}
	 */
	public boolean hasAxis(GraphAxis axis) {
		return axis != null
			&& (xAxisMap.containsKey(axis) || yAxisMap.containsKey(axis));
	}

	/**
	 * Returns the {@link org.bodytrack.client.DataPlot DataPlot} associated
	 * with the specified (deviceName, channelName) pair, if such a channel
	 * is available.
	 *
	 * @param name
	 * 		the name of the channel
	 * @return
	 * 		the <tt>DataPlot</tt> associated with name, if possible, or
	 * 		<tt>null</tt> if either name is <tt>null</tt> or has not
	 * 		been added through any channel
	 */
	public DataPlot getChannel(StringPair name) {
		if (name == null)
			return null;

		return channelMap.get(name);
	}

	/**
	 * Adds the specified channel to the list of
	 * {@link org.bodytrack.client.DataPlot DataPlot} objects
	 * held by this <tt>ChannelManager</tt>, and updates axis references
	 * as well.
	 *
	 * @param plot
	 * 		the <tt>DataPlot</tt> to add to the set of plots this hold
	 * @throws NullPointerException
	 * 		if plot or one of its axes is <tt>null</tt>
	 */
	public void addChannel(DataPlot plot) {
		if (plot == null)
			throw new NullPointerException("Cannot add a null DataPlot");
		if (plot.getXAxis() == null || plot.getYAxis() == null)
			throw new NullPointerException("Cannot add plot with null axis");

		StringPair name =
			new StringPair(plot.getDeviceName(), plot.getChannelName());
		if (dataPlots.contains(plot) || channelMap.containsKey(name))
			return;

		dataPlots.add(plot);

		// TODO: Check for bug if the same axis is both an X-axis
		// and a Y-axis, which should never happen in reality

		if (! xAxisMap.containsKey(plot.getXAxis())) {
			List<DataPlot> axisList = new ArrayList<DataPlot>();
			axisList.add(plot);
			xAxisMap.put(plot.getXAxis(), axisList);
			xAxes.add(plot.getXAxis());
		} else
			xAxisMap.get(plot.getXAxis()).add(plot);

		if (! yAxisMap.containsKey(plot.getYAxis())) {
			List<DataPlot> axisList = new ArrayList<DataPlot>();
			axisList.add(plot);
			yAxisMap.put(plot.getYAxis(), axisList);
			yAxes.add(plot.getYAxis());
		} else
			yAxisMap.get(plot.getYAxis()).add(plot);

		channelMap.put(name, plot);

		// Notify our event listeners to the occurrence of an event
		for (ChannelChangedListener l: listeners)
			l.channelAdded(plot.getDeviceName(), plot.getChannelName());
	}

	/**
	 * Removes the specified channel from the list of
	 * {@link org.bodytrack.client.DataPlot DataPlot} objects
	 * held by this <tt>ChannelManager</tt>, if it is part of that list,
	 * and updates axis references as well.
	 *
	 * <p>If plot is <tt>null</tt> or has not yet been added using a call
	 * to {@link #addChannel(DataPlot)}, does absolutely nothing.</p>
	 *
	 * @param plot
	 * 		the <tt>DataPlot</tt> to remove from the set of plots this holds
	 */
	public void removeChannel(DataPlot plot) {
		if (plot == null)
			return;

		GraphAxis xAxis = plot.getXAxis();
		GraphAxis yAxis = plot.getYAxis();

		if (! dataPlots.contains(plot))
			return;

		dataPlots.remove(plot);

		if (xAxisMap.get(xAxis).size() > 1)
			xAxisMap.get(xAxis).remove(plot);
		else {
			xAxisMap.remove(xAxis);
			xAxes.remove(xAxis);
		}

		if (yAxisMap.get(yAxis).size() > 1)
			yAxisMap.get(yAxis).remove(plot);
		else {
			yAxisMap.remove(yAxis);
			yAxes.remove(yAxis);
		}

		channelMap.remove(new StringPair(
				plot.getDeviceName(), plot.getChannelName()));

		// Notify our event listeners to the occurrence of an event
		for (ChannelChangedListener l: listeners)
			l.channelRemoved(plot.getDeviceName(), plot.getChannelName());
	}

	/**
	 * Removes the channel with the specified device and channel names,
	 * if such a channel is part of this <tt>ChannelManager</tt>.
	 *
	 * @param deviceName
	 * 		the device name for the channel to remove
	 * @param channelName
	 * 		the name for the channel to remove on the device
	 */
	public void removeChannel(String deviceName, String channelName) {
		StringPair name = new StringPair(deviceName, channelName);

		if (channelMap.containsKey(name))
			removeChannel(channelMap.get(name));
	}

	/**
	 * Moves the specified X-axis to a new index in the return value of
	 * {@link #getXAxes()}.
	 *
	 * <p>Does <strong>not</strong> notify the event listeners attached to this
	 * object.</p>
	 *
	 * @param oldIndex
	 * 		the index of the X-axis to move, as determined by {@link #getXAxes()}
	 * @param newIndex
	 * 		the index that the X-axis should occupy after the move, again
	 * 		as determined by {@link #getXAxes()}
	 * @throws IndexOutOfBoundsException
	 * 		if either oldIndex or newIndex is negative or is greater than or
	 * 		equal to {@code getXAxes().size()}
	 */
	public void moveXAxis(int oldIndex, int newIndex) {
		int length = xAxes.size();

		if (oldIndex < 0)
			throw new IndexOutOfBoundsException("Negative oldIndex value");
		if (newIndex < 0)
			throw new IndexOutOfBoundsException("Negative newIndex value");
		if (oldIndex >= length)
			throw new IndexOutOfBoundsException("Too large oldIndex value");
		if (newIndex >= length)
			throw new IndexOutOfBoundsException("Too large newIndex value");

		if (oldIndex == newIndex)
			return;

		// Actually perform the move
		GraphAxis xAxis = xAxes.get(oldIndex);
		xAxes.remove(oldIndex);
		xAxes.add(newIndex, xAxis);
	}

	/**
	 * Moves the specified Y-axis to a new index in the return value of
	 * {@link #getYAxes()}.
	 *
	 * <p>Does <strong>not</strong> notify the event listeners attached to this
	 * object.</p>
	 *
	 * @param oldIndex
	 * 		the index of the Y-axis to move, as determined by {@link #getYAxes()}
	 * @param newIndex
	 * 		the index that the Y-axis should occupy after the move, again
	 * 		as determined by {@link #getYAxes()}
	 * @throws IndexOutOfBoundsException
	 * 		if either oldIndex or newIndex is negative or is greater than or
	 * 		equal to {@code getYAxes().size()}
	 */
	public void moveYAxis(int oldIndex, int newIndex) {
		int length = yAxes.size();

		if (oldIndex < 0)
			throw new IndexOutOfBoundsException("Negative oldIndex value");
		if (newIndex < 0)
			throw new IndexOutOfBoundsException("Negative newIndex value");
		if (oldIndex >= length)
			throw new IndexOutOfBoundsException("Too large oldIndex value");
		if (newIndex >= length)
			throw new IndexOutOfBoundsException("Too large newIndex value");

		if (oldIndex == newIndex)
			return;

		// Actually perform the move
		GraphAxis yAxis = yAxes.get(oldIndex);
		yAxes.remove(oldIndex);
		yAxes.add(newIndex, yAxis);
	}

	/**
	 * Adds a listener to the list of listeners to receive event
	 * notifications whenever a channel is added, removed, etc.
	 *
	 * @param l
	 * 		some {@link org.bodytrack.client.ChannelChangedListener
	 * 		ChannelChangedListener} that should receive notifications
	 * 		whenever an event occurs
	 * @throws NullPointerException
	 * 		if l is <tt>null</tt>
	 */
	public void addChannelListener(ChannelChangedListener l) {
		if (l == null)
			throw new NullPointerException(
				"Can't fire events to null listener");

		listeners.add(l);
	}

	/**
	 * Clears the contents of this <tt>ChannelManager</tt>, except
	 * for the listeners.
	 *
	 * <p>In other words, removes all channels from this
	 * <tt>ChannelManager</tt>.  Additionally, the listeners are
	 * notified about each change to the set of channels.</p>
	 */
	public void clear() {
		while (dataPlots.size() > 0)
			removeChannel(dataPlots.get(0));
	}

	/**
	 * Replaces all the channels of this with the channels of other.
	 *
	 * <p>This is a thread-unsafe method - any call in multithreaded
	 * code must be externally synchronized to prevent any concurrent
	 * changes to this or to other.  Then again, JavaScript is
	 * single-threaded, which stops this problem.</p>
	 *
	 * @param other
	 * 		the <tt>ChannelManager</tt> whose channels to use
	 * 		for this
	 * @throws NullPointerException
	 * 		if other is <tt>null</tt>
	 */
	public void replaceChannels(ChannelManager other) {
		if (other == null)
			throw new NullPointerException(
				"Can't replace contents of this with a null set of channels");

		clear();

		for (DataPlot plot: other.getDataPlots())
			addChannel(plot);
	}

	/**
	 * A simple class to hold an immutable pair of non-<tt>null</tt> strings.
	 */
	public static final class StringPair {
		private String s1;
		private String s2;

		/**
		 * Initializes a new <tt>StringPair</tt>.
		 *
		 * @param s1
		 * 		the first string to hold
		 * @param s2
		 * 		the second string to hold
		 */
		public StringPair(String s1, String s2) {
			if (s1 == null || s2 == null)
				throw new NullPointerException(
					"Cannot use null string with the StringPair class");

			this.s1 = s1;
			this.s2 = s2;
		}

		/**
		 * Returns the first string this holds.
		 *
		 * @return
		 * 		the value of s1 passed to the constructor when this
		 * 		<tt>StringPair</tt> was created
		 */
		public String getFirst() {
			return s1;
		}

		/**
		 * Returns the second string this holds.
		 *
		 * @return
		 * 		the value of s2 passed to the constructor when this
		 * 		<tt>StringPair</tt> was created
		 */
		public String getSecond() {
			return s2;
		}

		/**
		 * Builds and returns a string representation of this object,
		 * suitable for display to a user.
		 *
		 * <p>Never throws an exception or returns <tt>null</tt>.</p>
		 */
		public String toDisplayString() {
			return s1 + "." + s2;
		}

		/**
		 * Builds and returns a string representation of this object.
		 * Never throws an exception or returns <tt>null</tt>.
		 */
		@Override
		public String toString() {
			return toDisplayString();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + s1.hashCode();
			result = prime * result + s2.hashCode();
			return result;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (! (obj instanceof StringPair))
				return false;
			StringPair other = (StringPair) obj;
			// We know that s1 and s2 are never null
			return s1.equals(other.s1) && s2.equals(other.s2);
		}
	}
}
