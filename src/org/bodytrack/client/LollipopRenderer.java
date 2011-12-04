package org.bodytrack.client;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

public class LollipopRenderer extends DotRenderer {
	public LollipopRenderer(final boolean drawComments) {
		super(drawComments);
	}

	@Override
	protected void paintDot(final BoundedDrawingBox drawing,
			final double x,
			final double y,
			final boolean highlighted) {
		super.paintDot(drawing, x, y, highlighted);

		final Canvas canvas = drawing.getCanvas();
		final Surface surface = canvas.getSurface();
		final DirectShapeRenderer renderer = canvas.getRenderer();

		// The Y-value in pixels corresponding to the lowest point to draw on the rectangle
		final double minDrawY = getYAxis().project2D(0).getY();

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
