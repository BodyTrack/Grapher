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
	private List<DataPlot> unmodDataPlots;

	/* xAxisMap and yAxisMap provide the reverse mapping from
	 * dataPlots.get(i).getXAxis() and dataPlots.get(i).getYAxis().
	 * They map from axes to sets of data plots associated with
	 * those axes.
	 */
	private final Map<GraphAxis, List<DataPlot>> xAxisMap;
	private Map<GraphAxis, List<DataPlot>> unmodXAxisMap;
	private Set<GraphAxis> unmodXAxes;

	private final Map<GraphAxis, List<DataPlot>> yAxisMap;
	private Map<GraphAxis, List<DataPlot>> unmodYAxisMap;
	private Set<GraphAxis> unmodYAxes;

	/**
	 * Creates a new <tt>ChannelManager</tt> that does not include
	 * any channels.
	 */
	public ChannelManager() {
		dataPlots = new ArrayList<DataPlot>();
		xAxisMap = new HashMap<GraphAxis, List<DataPlot>>();
		yAxisMap = new HashMap<GraphAxis, List<DataPlot>>();

		refreshUnmodifiableCaches();
	}

	/**
	 * Drops and then recalculates the caches of unmodifiable copies
	 * of the private variables.
	 */
	private void refreshUnmodifiableCaches() {
		unmodDataPlots = Collections.unmodifiableList(dataPlots);

		unmodXAxisMap = Collections.unmodifiableMap(xAxisMap);
		unmodXAxes = Collections.unmodifiableSet(xAxisMap.keySet());

		unmodYAxisMap = Collections.unmodifiableMap(yAxisMap);
		unmodYAxes = Collections.unmodifiableSet(yAxisMap.keySet());
	}

	/**
	 * Returns an unmodifiable view of the list of
	 * {@link org.bodytrack.client.DataPlot DataPlot} objects
	 * held by this <tt>ChannelManager</tt>.
	 *
	 * @return
	 * 		an unmodifiable view of the list of <tt>DataPlot</tt>
	 * 		objects this holds
	 */
	public List<DataPlot> getDataPlots() {
		return unmodDataPlots;
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
	public Set<GraphAxis> getXAxes() {
		return unmodXAxes;
	}

	/**
	 * Returns an unmodifiable view of the set of Y-axes held by
	 * any {@link org.bodytrack.client.DataPlot DataPlot} object
	 * held by this <tt>ChannelManager</tt>.
	 *
	 * @return
	 * 		an unmodifiable view of the set of Y-axes on
	 * 		<tt>DataPlot</tt> objects this holds
	 */
	public Set<GraphAxis> getYAxes() {
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
	 * Adds the specified channel to the list of
	 * {@link org.bodytrack.client.DataPlot DataPlot} objects
	 * held by this <tt>ChannelManager</tt>, and updates axis references
	 * as well.
	 *
	 * @param plot
	 * 		the <tt>DataPlot</tt> to add to the set of plots this hold
	 * @throws NullPointerException
	 * 		if plot is <tt>null</tt>
	 */
	public void addChannel(DataPlot plot) {
		if (plot == null)
			throw new NullPointerException("Cannot add a null DataPlot");

		if (! dataPlots.contains(plot))
			dataPlots.add(plot);
		else
			return;

		// TODO: Check for bug if the same axis is both an X-axis
		// and a Y-axis, which should never happen in reality

		if (! xAxisMap.containsKey(plot.getXAxis())) {
			List<DataPlot> axisList = new ArrayList<DataPlot>();
			axisList.add(plot);
			xAxisMap.put(plot.getXAxis(), axisList);
		} else
			xAxisMap.get(plot.getXAxis()).add(plot);

		if (! yAxisMap.containsKey(plot.getYAxis())) {
			List<DataPlot> axisList = new ArrayList<DataPlot>();
			axisList.add(plot);
			yAxisMap.put(plot.getYAxis(), axisList);
		} else
			yAxisMap.get(plot.getYAxis()).add(plot);

		// Very important to refresh the cache after any mutation
		refreshUnmodifiableCaches();
	}

	/**
	 * Adds the specified channel to the list of
	 * {@link org.bodytrack.client.DataPlot DataPlot} objects
	 * held by this <tt>ChannelManager</tt>, and updates axis references
	 * as well.
	 *
	 * <p>If plot is <tt>null</tt>, does absolutely nothing.</p>
	 *
	 * @param plot
	 * 		the <tt>DataPlot</tt> to add to the set of plots this hold
	 */
	public void removeChannel(DataPlot plot) {
		if (plot == null)
			return;

		GraphAxis xAxis = plot.getXAxis();
		GraphAxis yAxis = plot.getYAxis();

		dataPlots.remove(plot);

		if (xAxisMap.get(xAxis).size() > 1)
			xAxisMap.get(xAxis).remove(plot);
		else
			xAxisMap.remove(xAxis);

		if (yAxisMap.get(yAxis).size() > 1)
			yAxisMap.get(yAxis).remove(plot);
		else
			yAxisMap.remove(yAxis);

		// Very important to refresh the cache after any mutation
		refreshUnmodifiableCaches();
	}
}
