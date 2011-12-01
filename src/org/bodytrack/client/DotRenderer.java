package org.bodytrack.client;

import gwt.g2d.client.graphics.shapes.CircleShape;
import gwt.g2d.client.math.Vector2;

public class DotRenderer extends AbstractPlotRenderer {
	private static final int DOT_RADIUS = 3;

	public DotRenderer(boolean highlighted, boolean drawComments) {
		super(highlighted, drawComments);
	}

	protected void paintDot(BoundedDrawingBox drawing, double x, double y,
			boolean highlighted) {
		if (drawing.contains(x, y)) {
			drawing.getCanvas().getSurface().fillShape(
					new CircleShape(new Vector2(x, y),
							highlighted ? HIGHLIGHTED_DOT_RADIUS : DOT_RADIUS));
		}
	}

	@Override
	protected final void paintDataPoint(BoundedDrawingBox drawing,
			GrapherTile tile, double prevX, double prevY, double x,
			double y, PlottablePoint rawDataPoint) {
		paintDot(drawing, x, y, rawDataPoint.hasComment());
	}

	@Override
	protected final void paintEdgePoint(BoundedDrawingBox drawing, GrapherTile tile,
			double x, double y, PlottablePoint rawDataPoint) {
		paintDot(drawing, x, y, rawDataPoint.hasComment());
	}

	@Override
	protected final void paintHighlightedPoint(BoundedDrawingBox drawing, double x,
			double y, PlottablePoint rawDataPoint) {
		paintDot(drawing, x, y, true);
	}

}
