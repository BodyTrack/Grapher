package org.bodytrack.client;

public class LineRenderer extends AbstractPlotRenderer {
	public LineRenderer(boolean highlighted, boolean drawComments) {
		super(highlighted, drawComments);
	}

	protected void paintEdgePoint(final BoundedDrawingBox drawing,
			final GrapherTile tile,
			final double x,
			final double y,
			final PlottablePoint rawDataPoint) {
		drawing.drawDot(x, y, DOT_RADIUS);

		if (isDrawingComments() && rawDataPoint.hasComment()) {
			paintHighlightedPoint(drawing, x, y);
		}
	}

	protected void paintDataPoint(final BoundedDrawingBox drawing,
			final double prevX,
			final double prevY,
			final double x,
			final double y,
			final PlottablePoint rawDataPoint) {
		drawing.drawLineSegment(prevX, prevY, x, y);

		if (isDrawingComments() && rawDataPoint.hasComment()) {
			paintHighlightedPoint(drawing, x, y);
		}
	}

	protected void paintHighlightedPoint(final BoundedDrawingBox drawing,
			double x, double y) {
		// Draw three concentric circles to look like one filled-in circle
		// The real radius is the first one used: HIGHLIGHTED_DOT_RADIUS
		drawing.drawDot(x, y, HIGHLIGHTED_DOT_RADIUS);
		drawing.drawDot(x, y, HIGHLIGHT_STROKE_WIDTH);
		drawing.drawDot(x, y, NORMAL_STROKE_WIDTH);
	}
}
