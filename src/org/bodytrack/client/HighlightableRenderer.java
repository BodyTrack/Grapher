package org.bodytrack.client;

/**
 * An interface describing a SeriesPlotRenderer that supports a single
 * highlighted point
 */
public interface HighlightableRenderer extends SeriesPlotRenderer {
	/**
	 * Returns the highlighted point for this renderer
	 *
	 * @return
	 * 	A point equal to the parameter to the most recent
	 * 	{@link #setHighlightedPoint(PlottablePoint)} call, or null
	 * 	if {@link #setHighlightedPoint(PlottablePoint)} has never been
	 * 	called on this object
	 */
	PlottablePoint getHighlightedPoint();

	/**
	 * Sets the highlighted point for this renderer
	 *
	 * @param highlightedPoint
	 * 	The point to highlight in future calls to render, or null to
	 * 	indicate that no point should be highlighted in future calls to render
	 */
	void setHighlightedPoint(PlottablePoint highlightedPoint);
}
