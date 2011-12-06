package org.bodytrack.client;


public class DotRenderer extends AbstractPlotRenderer {
	private static final int DOT_RADIUS = 3;

	public DotRenderer(final boolean drawComments) {
		super(drawComments);
	}

	protected void paintDot(final BoundedDrawingBox drawing,
			final double x,
			final double y,
			final boolean highlighted,
			final PlottablePoint rawDataPoint) {
		if (drawing.contains(x, y)) {
			final boolean willPaintLargerDot =
				highlighted
				|| (isDrawingComments()
						&& rawDataPoint != null
						&& rawDataPoint.hasComment());
			final double radius =
				willPaintLargerDot ? HIGHLIGHTED_DOT_RADIUS : DOT_RADIUS;
			drawing.drawFilledDot(x, y, radius);
		}
	}

	@Override
	protected final void paintDataPoint(final BoundedDrawingBox drawing,
			final GrapherTile tile,
			final double prevX,
			final double prevY,
			final double x,
			final double y,
			final PlottablePoint rawDataPoint) {
		paintDot(drawing, x, y, rawDataPoint.hasComment(), rawDataPoint);
	}

	@Override
	protected final void paintEdgePoint(final BoundedDrawingBox drawing,
			final GrapherTile tile,
			final double x,
			final double y,
			final PlottablePoint rawDataPoint) {
		paintDot(drawing, x, y, rawDataPoint.hasComment(), rawDataPoint);
	}

	@Override
	protected final void paintHighlightedPoint(final BoundedDrawingBox drawing,
			final double x,
			final double y,
			final PlottablePoint rawDataPoint) {
		paintDot(drawing, x, y, true, null);
	}
}
