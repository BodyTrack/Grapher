package org.bodytrack.client;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

public class LollipopRenderer extends DotRenderer {
	// The Y-axis that paintDot can use to find the 0 location
	private GraphAxis yAxis;

	public LollipopRenderer(boolean highlighted, boolean drawComments) {
		super(highlighted, drawComments);
		yAxis = null;
	}

	@Override
	public void render(final BoundedDrawingBox drawing,
                      final Iterable<GrapherTile> tiles,
                      final GraphAxis xAxis,
                      final GraphAxis yAxis, PlottablePoint highlightedPoint) {
		// Just save the Y-axis for use in paintDot
		this.yAxis = yAxis;
		super.render(drawing, tiles, xAxis, yAxis, highlightedPoint);
	}

	@Override
	protected void paintDot(BoundedDrawingBox drawing, double x, double y,
			boolean highlighted) {
		super.paintDot(drawing, x, y, highlighted);

		// This should never happen, since we set yAxis in render
		if (yAxis == null)
			return;

		final Canvas canvas = drawing.getCanvas();
		final Surface surface = canvas.getSurface();
		final DirectShapeRenderer renderer = canvas.getRenderer();

		// The Y-value in pixels corresponding to the lowest point to draw on the rectangle
		final double minDrawY = yAxis.project2D(0).getY();

		// Draw a line
		final double oldLineWidth = surface.getLineWidth();
		surface.setLineWidth(isHighlighted()
				? AbstractPlotRenderer.HIGHLIGHT_STROKE_WIDTH
				: AbstractPlotRenderer.NORMAL_STROKE_WIDTH);

		renderer.beginPath();
		renderer.moveTo(x, minDrawY);
		renderer.drawLineTo(x, y);
		renderer.closePath();

		renderer.stroke();
		surface.setLineWidth(oldLineWidth);
	}
}
