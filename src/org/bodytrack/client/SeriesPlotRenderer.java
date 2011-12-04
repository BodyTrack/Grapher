package org.bodytrack.client;

/**
 * An interface for objects that are capable of rendering a series plot.
 */
public interface SeriesPlotRenderer {

	/**
	 * Never render a point with value less than this - use anything
	 * less as a sentinel for &quot;data point not present&quot;.
	 *
	 * <p>This value is intended to be used as a sentinel value.</p>
	 */
	double MIN_DRAWABLE_VALUE = -1e300;

	/**
	 * Draws the supplied tiles on a canvas
	 *
	 * @param drawing
	 * 	The {@link BoundedDrawingBox} that should constrain the drawing.
	 * 	Forwarding graphics calls through drawing will ensure that everything
	 * 	draws up to the edge of the viewing window but no farther
	 * @param tiles
	 * 	The tiles to draw using drawing
	 * @param xAxis
	 * 	The X-axis to use for lining up points from tiles
	 * @param yAxis
	 * @param highlightedPoint
	 */
	void render(BoundedDrawingBox drawing,
			Iterable<GrapherTile> tiles,
			GraphAxis xAxis,
			GraphAxis yAxis,
			PlottablePoint highlightedPoint);
}
