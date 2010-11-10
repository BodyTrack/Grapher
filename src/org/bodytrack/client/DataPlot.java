package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a single set of data, along with its associated axes.
 *
 * Has the ability to draw itself and its axes on a
 * {@link org.bodytrack.client.Canvas Canvas} object, and to update
 * the positions of its dots based on the zoom level.  Also, if the
 * zoom level changes enough, the {@link #zoom(double, double) zoom()}
 * method will automatically fetch the data from the server via Ajax
 * and redraw the data whenever the data comes in from the server.
 */
public class DataPlot {
	/*
	 * All the following colors should have exactly the same values that
	 * their CSS counterparts by the same name do.
	 */
	public static final Color BLACK = new Color(0x00, 0x00, 0x00);
	public static final Color DARK_GRAY = new Color(0xA9, 0xA9, 0xA9);
	public static final Color GRAY = new Color(0x80, 0x80, 0x80);
	public static final Color RED = new Color(0xFF, 0x00, 0x00);
	public static final Color GREEN = new Color(0x00, 0x80, 0x00);
	public static final Color BLUE = new Color(0x00, 0x00, 0xFF);

	/**
	 * The maximum size we allow currentData to be before we consider
	 * pruning away unnecessary data.
	 */
	private static final int MAX_CURRENT_DATA_SIZE = 1024;

	/**
	 * Never render a point with value less than this - use anything
	 * less as a sentinel.
	 */
	private static final double MIN_DRAWABLE_VALUE = -1e300;

	private GraphWidget container;
	private GraphAxis xAxis;
	private GraphAxis yAxis;
	private Canvas canvas;

	private final int minLevel;
	private final Color color;

	private boolean shouldZoomIn;

	// Values related to getting new values from the server
	private String baseUrl;
	private List<GrapherTile> currentData;
	private Set<TileDescription> pendingDescriptions;
	private List<GrapherTile> pendingData;

	// Determining whether or not we should retrieve more data from
	// the server
	private int currentLevel;
	private int currentMinOffset;
	private int currentMaxOffset;

	/**
	 * Constructor for the DataPlot object that allows unlimited zoom.
	 * 
	 * The parameter url is the trickiest to get right.  This parameter
	 * should be the <strong>beginning</strong> (the text up to, but
	 * not including, the &lt;level&gt;.&lt;offset&gt;.json part of the
	 * URL to fetch) of the URL which will be used to get more data.
	 * Note that this <strong>must</strong> be a trusted BodyTrack
	 * URL.  As described in the documentation for
	 * {@link org.bodytrack.client.GrapherTile#retrieveTile(String, List)
	 *  GrapherTile.retriveTile()}, an untrusted connection could allow
	 * unauthorized access to all of a user's data.
	 *
	 * @param container
	 * 		the {@link org.bodytrack.client.GraphWidget GraphWidget} on
	 * 		which this DataPlot will draw itself and its axes
	 * @param xAxis
	 * 		the X-axis along which this data set will be aligned when
	 * 		drawn.  Usually this is a
	 * 		{@link org.bodytrack.client.TimeGraphAxis TimeGraphAxis}
	 * @param yAxis
	 * 		the Y-axis along which this data set will be aligned when
	 * 		drawn
	 * @param url
	 * 		the beginning of the URL for fetching this data with Ajax
	 * 		calls
	 * @throws NullPointerException
	 * 		if container, xAxis, yAxis, or url is <tt>null</tt>
	 */
	public DataPlot(GraphWidget container, GraphAxis xAxis, GraphAxis yAxis,
			String url) {
		this(container, xAxis, yAxis, url, Integer.MIN_VALUE, BLACK);
	}

	/**
	 * Main constructor for the DataPlot object.
	 *
	 * The parameter url is the trickiest to get right.  This parameter
	 * should be the <strong>beginning</strong> (the text up to, but
	 * not including, the &lt;level&gt;.&lt;offset&gt;.json part of the
	 * URL to fetch) of the URL which will be used to get more data.
	 * Note that this <strong>must</strong> be a trusted BodyTrack
	 * URL.  As described in the documentation for
	 * {@link org.bodytrack.client.GrapherTile#retrieveTile(String, List)
	 *  GrapherTile.retriveTile()}, an untrusted connection could allow
	 * unauthorized access to all of a user's data.
	 *
	 * @param container
	 * 		the {@link org.bodytrack.client.GraphWidget GraphWidget} on
	 * 		which this DataPlot will draw itself and its axes
	 * @param xAxis
	 * 		the X-axis along which this data set will be aligned when
	 * 		drawn.  Usually this is a
	 * 		{@link org.bodytrack.client.TimeGraphAxis TimeGraphAxis}
	 * @param yAxis
	 * 		the Y-axis along which this data set will be aligned when
	 * 		drawn
	 * @param url
	 * 		the beginning of the URL for fetching this data with Ajax
	 * 		calls
	 * @param minLevel
	 * 		the minimum level to which the user will be allowed to zoom
	 * @param color
	 * 		the color in which to draw these data points (note that
	 * 		the axes are always drawn in black)
	 * @throws NullPointerException
	 * 		if container, xAxis, yAxis, url, or color is <tt>null</tt>
	 */
	public DataPlot(GraphWidget container, GraphAxis xAxis, GraphAxis yAxis,
			String url, int minLevel, Color color) {
		if (container == null || xAxis == null
				|| yAxis == null || url == null || color == null)
			throw new NullPointerException(
				"Cannot have a null container, axis, url, or color");

		this.container = container;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		baseUrl = url;
		shouldZoomIn = true;
		this.minLevel = minLevel;

		this.color = color;

		canvas = Canvas.buildCanvas(this.container);

		// The data will be pulled in with the checkForFetch call
		pendingData = new ArrayList<GrapherTile>();
		pendingDescriptions = new HashSet<TileDescription>();
		currentData = new ArrayList<GrapherTile>();

		currentLevel = Integer.MIN_VALUE;
		currentMinOffset = Integer.MAX_VALUE;
		currentMaxOffset = Integer.MIN_VALUE;

		shouldZoomIn = checkForFetch();
	}

	/**
	 * Returns <tt>true</tt> if and only if the X-axis is allowed to
	 * zoom in farther, based on the zoom policy of this DataPlot.
	 *
	 * @return
	 * 		<tt>true</tt> if the X-axis should be allowed to
	 * 		zoom in more, <tt>false</tt> otherwise
	 */
	public boolean shouldZoomIn() {
		return shouldZoomIn;
	}

	/**
	 * Checks for and performs a fetch for data from the server if
	 * necessary.
	 *
	 * @return
	 * 		<tt>true</tt> if the user should be allowed to zoom past
	 * 		this point, <tt>false</tt> if the user shouldn't be allowed
	 * 		to zoom past this point
	 */
	private boolean checkForFetch() {
		int correctLevel = computeCurrentLevel();
		int correctMinOffset = computeMinOffset(correctLevel);
		int correctMaxOffset = computeMaxOffset(correctLevel);

		if (correctLevel != currentLevel) {
			for (int i = correctMinOffset; i <= correctMaxOffset; i++)
				fetchFromServer(correctLevel, i);
		} else if (correctMinOffset < currentMinOffset)
			fetchFromServer(correctLevel, correctMinOffset);
		else if (correctMaxOffset > currentMaxOffset)
			fetchFromServer(correctLevel, correctMaxOffset);

		// This way we don't fetch the same data multiple times
		currentLevel = correctLevel;
		currentMinOffset = correctMinOffset;
		currentMaxOffset = correctMaxOffset;

		// Remove for any data out of range, but only if we are not
		// waiting on the data that is in range and we do not have
		// enough available space to store our data
		// TODO: Put more intelligent pruning in place
		if (pendingDescriptions.size() > 0
				|| currentData.size() < MAX_CURRENT_DATA_SIZE)
			return correctLevel > minLevel;

		/*
		Iterator<GrapherTile> it = currentData.iterator();

		while (it.hasNext()) {
			GrapherTile curr = it.next();

			int level = curr.getLevel();
			int offset = curr.getOffset();

			if (level != currentLevel) // Wrong level
				it.remove();
			else if (offset < currentMinOffset - 1) // Too far left
				it.remove();
			else if (offset > currentMaxOffset + 1) // Too far right
				it.remove();
		}
		*/

		return correctLevel > minLevel;
	}

	/**
	 * Fetches the specified tile from the server.
	 *
	 * Note that this checks the pendingDescriptions instance variable
	 * to determine if this tile has already been requested.  If so,
	 * does not request anything from the server.
	 *
	 * @param level
	 * 		the level of the tile to fetch
	 * @param offset
	 * 		the offset of the tile to fetch
	 */
	private void fetchFromServer(int level, int offset) {
		TileDescription desc = new TileDescription(level, offset);

		// Ensures we don't fetch the same tile twice unnecessarily
		if (pendingDescriptions.contains(desc))
			return;

		String url = baseUrl + level + "." + offset + ".json";
		GrapherTile.retrieveTile(url, pendingData);

		// Make sure we don't fetch this again unnecessarily
		pendingDescriptions.add(desc);
	}

	/**
	 * Paints this DataPlot on the stored GraphWidget.
	 */
	public void paint() {
		canvas.getSurface().setStrokeStyle(DARK_GRAY);

		// Draw the axes in all cases
		// TODO: Possibly (for performance reasons) make sure that
		// the same axes never get painted multiple times (one option
		// is to paint axes only in GraphWidget, and not here)
		xAxis.paint(canvas.getSurface());
		yAxis.paint(canvas.getSurface());

		// If we have received data from the server
		if (pendingData.size() > 0) {
			// Pull all the data out of the tile
			for (GrapherTile tile: pendingData) {
				if (tile == null)
					continue;

				currentData.add(tile);

				// Make sure we don't still mark this as pending
				pendingDescriptions.remove(new TileDescription(
					tile.getLevel(), tile.getOffset()));
			}

			pendingData.clear();
		}

		canvas.getSurface().setStrokeStyle(color);

		paintAllDataPoints();

		shouldZoomIn = checkForFetch();
	}

	/**
	 * Renders all the salient data points in currentData.
	 */
	private void paintAllDataPoints() {
		// TODO: improve the algorithm for getting the best resolution tile
		// Current algorithm is O(n m), where n is the currentData.length()
		// and m is getBestResolutionTiles.length()

		for (GrapherTile tile: getBestResolutionTiles()) {
			double prevX = - Double.MAX_VALUE;
			double prevY = - Double.MAX_VALUE;

			List<PlottablePoint> dataPoints = tile.getDataPoints();

			if (dataPoints == null)
				continue;

			canvas.beginPath();

			for (PlottablePoint point: dataPoints) {
				// Don't draw points too far to the left or right
				if (point.getDate() < xAxis.getMin()
						|| point.getDate() > xAxis.getMax()) {
					// Make sure we don't draw lines between points
					// that aren't adjacent
					prevX = prevY = - Double.MAX_VALUE;
					continue;
				}

				// Don't draw points too high or low to be rendered
				if (point.getValue() < yAxis.getMin()
						|| point.getValue() > yAxis.getMax()) {
					// Make sure we don't draw lines between points
					// that aren't adjacent
					prevX = prevY = - Double.MAX_VALUE;
					continue;
				}

				double x = xAxis.project2D(point.getDate()).getX();
				double y = yAxis.project2D(point.getValue()).getY();

				// Draw this part of the line, reaching to all points
				// except the first (the first point has a line coming from
				// it, but not to it)
				if (prevX > MIN_DRAWABLE_VALUE && prevY > MIN_DRAWABLE_VALUE)
					canvas.getRenderer().drawLineSegment(prevX, prevY, x, y);

				prevX = x;
				prevY = y;
			}

			canvas.stroke();
		}
	}

	/**
	 * Returns a sorted list of all best resolution tiles available.
	 *
	 * @return
	 * 		a sorted list of all the best resolution tiles in
	 * 		currentData
	 */
	private List<GrapherTile> getBestResolutionTiles() {
		List<GrapherTile> best = new ArrayList<GrapherTile>();

		// When minTime and maxTime are used in calculations, they are
		// used to make the calculations scale-independent
		double minTime = xAxis.getMin();
		double maxTime = xAxis.getMax();

		double maxCoveredTime = minTime;

		int bestLevel = computeCurrentLevel();

		while (maxCoveredTime <= maxTime) {
			GrapherTile bestAtCurrTime = getBestResolutionTileAt(
				maxCoveredTime + (maxTime - minTime) * 1e-6,
				bestLevel);
			// We need to move a little to the right of the current time
			// so we don't get the same tile twice

			if (bestAtCurrTime == null) {
				maxCoveredTime += (maxTime - minTime) * 1e-3;
			} else {
				best.add(bestAtCurrTime);

				maxCoveredTime =
					bestAtCurrTime.getDescription().getMaxTime();
			}
		}

		return best;
	}

	/**
	 * Returns the best-resolution tile that covers the specified
	 * point.
	 *
	 * @param time
	 * 		the time which must be covered by the tile
	 * @param bestLevel
	 * 		the level to which we want the returned tile to be close
	 * @return
	 * 		the best-resolution (lowest-level) tile which has min value
	 * 		less than or equal to time, and max value greater than or
	 * 		equal to time, or <tt>null</tt> if no such tile exists
	 */
	private GrapherTile getBestResolutionTileAt(double time, int bestLevel) {
		GrapherTile best = null;

		for (GrapherTile tile: currentData) {
			TileDescription desc = tile.getDescription();

			if (desc.getMinTime() > time || desc.getMaxTime() < time)
				continue;

			if (best == null)
				best = tile;
			else if(Math.abs(desc.getLevel() - bestLevel) <
					Math.abs(best.getLevel() - bestLevel))
				best = tile;
			else if (Math.abs(desc.getLevel() - bestLevel) ==
					Math.abs(best.getLevel() - bestLevel)) {
				if (desc.getLevel() < best.getLevel())
					best = tile;
			}
		}

		return best;
	}

	/**
	 * Returns the X-Axis for this DataPlot.
	 *
	 * @return
	 * 		the X-axis for this DataPlot
	 */
	public GraphAxis getXAxis() {
		return xAxis;
	}

	/**
	 * Returns the Y-Axis for this DataPlot.
	 *
	 * @return
	 * 		the Y-axis for this DataPlot
	 */
	public GraphAxis getYAxis() {
		return yAxis;
	}

	/**
	 * Computes the value for currentLevel based on xAxis.
	 *
	 * @return
	 * 		the level at which xAxis is operating
	 */
	private int computeCurrentLevel() {
		double xAxisWidth = xAxis.getMax() - xAxis.getMin();
		double dataPointWidth = xAxisWidth / GrapherTile.TILE_WIDTH;

		return log2(dataPointWidth);
	}

	/**
	 * Computes the floor of the log (base 2) of n.
	 *
	 * @param n
	 * 		the value for which we want to take the log
	 * @return
	 * 		the floor of the log (base 2) of n
	 */
	private int log2(double x) {
		if (x == 0)
			return 0;

		if (x < 0)
			return -1 * log2(-x);

		return (int) (Math.log(x) / Math.log(2));
	}

	/**
	 * Returns the offset at which the left edge of the X-axis is operating.
	 *
	 * Returns the offset of the tile in which the minimum value
	 * of the X-axis is found.
	 *
	 * @param level
	 * 		the level at which we assume we are operating when calculating
	 * 		offsets
	 * @return
	 * 		the current offset of the X-axis, based on level
	 * 		and the private variable xAxis
	 */
	private int computeMinOffset(int level) {
		double min = xAxis.getMin();

		double tileWidth = getTileWidth(level);

		// Tile offset computation
		return (int) (min / tileWidth);
	}

	/**
	 * Returns the offset at which the right edge of the X-axis is operating.
	 *
	 * Returns the offset of the tile in which the maximum value
	 * of the X-axis is found.
	 *
	 * @param level
	 * 		the level at which we assume we are operating when calculating
	 * 		offsets
	 * @return
	 * 		the current offset of the X-axis, based on level
	 * 		and the private variable xAxis
	 */
	private int computeMaxOffset(int level) {
		double max = xAxis.getMax();

		double tileWidth = getTileWidth(level);

		// Tile number computation
		return (int) (max / tileWidth);
	}

	/**
	 * Returns the width of a single tile.
	 *
	 * @param level
	 * 		the level of the tile for which we will find the width
	 * @return
	 * 		the width of a tile at the given level
	 */
	private double getTileWidth(int level) {
		return (new TileDescription(level, 0)).getTileWidth();
	}
}
