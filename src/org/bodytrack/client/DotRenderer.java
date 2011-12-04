package org.bodytrack.client;

import gwt.g2d.client.graphics.shapes.CircleShape;
import gwt.g2d.client.math.Vector2;

public class DotRenderer extends AbstractPlotRenderer {
	private static final int DOT_RADIUS = 3;

	public DotRenderer(final boolean drawComments) {
		super(drawComments);
	}

	protected void paintDot(final BoundedDrawingBox drawing,
			final double x,
			final double y,
			final boolean highlighted) {
		if (drawing.contains(x, y)) {
			drawing.getCanvas().getSurface().fillShape(
					new CircleShape(new Vector2(x, y),
							highlighted ? HIGHLIGHTED_DOT_RADIUS : DOT_RADIUS));
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
		paintDot(drawing, x, y, rawDataPoint.hasComment());
	}

	@Override
	protected final void paintEdgePoint(final BoundedDrawingBox drawing,
			final GrapherTile tile,
			final double x,
			final double y,
			final PlottablePoint rawDataPoint) {
		paintDot(drawing, x, y, rawDataPoint.hasComment());
	}

	@Override
	protected final void paintHighlightedPoint(final BoundedDrawingBox drawing,
			final double x,
			final double y,
			final PlottablePoint rawDataPoint) {
		paintDot(drawing, x, y, true);
	}
}
