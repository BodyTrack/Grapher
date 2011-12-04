package org.bodytrack.client;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

public class ZeoRenderer extends AbstractPlotRenderer {
	/**
	 * The alpha value used when drawing rectangles for Zeo plots.
	 */
	//private static final double NORMAL_ALPHA = 0.4;
	private static final double NORMAL_ALPHA = 1.0;

	/**
	 * The alpha used when drawing rectangles for highlighted Zeo plots.
	 */
	//private static final double HIGHLIGHTED_ALPHA = 0.5;
	private static final double HIGHLIGHTED_ALPHA = 1.0;

	public ZeoRenderer(boolean drawComments) {
		super(drawComments);
	}

	@Override
	protected void paintDataPoint(BoundedDrawingBox drawing, GrapherTile tile,
			double prevX, double prevY, double x, double y,
			PlottablePoint rawDataPoint) {
		paintEdgePoint(drawing, tile, x, y, rawDataPoint);
	}

	@Override
	protected void paintEdgePoint(BoundedDrawingBox drawing, GrapherTile tile,
			double x, double y, PlottablePoint rawDataPoint) {
		// get the ZeoState
		final int val = (int)Math.round(rawDataPoint.getValue());
		final ZeoState zeoState = ZeoState.findByValue(val);

		// use the sample width to compute the left and right x values for the bar (we want the data point to be in the
		// center of the bar)
		final double sampleHalfWidth = tile.getPlottableTile().getSampleWidth() / 2;
		final double leftX = getXAxis().project2D(rawDataPoint.getDate() - sampleHalfWidth).getX();
		final double rightX = getXAxis().project2D(rawDataPoint.getDate() + sampleHalfWidth).getX();

		// draw the rectangle
		drawRectangle(drawing.getCanvas(), zeoState, leftX, rightX, y);
	}

	@Override
	protected void paintHighlightedPoint(BoundedDrawingBox drawing, double x,
			double y, PlottablePoint rawDataPoint) {
		// Don't need to do anything extra for a highlighted point
	}

	/**
	 * Draws a rectangle with the specified corners, stretching down
	 * to 0.
	 *
	 * @param zeoState
	 * 		the ZeoState for the data point we're rendering.  If the state
	 * 		is <code>null</code> this method does nothing
	 * @param x
	 * 		the X-value (in pixels) for the right edge of the rectangle
	 * @param y
	 * 		the Y-value (in pixels) for the top edge of the rectangle
	 * @param rectHalfWidth
	 * 		the half-width (in pixels) of the rectangle to be drawn
	 */
	private void drawRectangle(final Canvas canvas, final ZeoState zeoState,
			final double leftX, final double rightX, final double y) {
		if (zeoState == null) {
			return;
		}

		final GraphAxis yAxis = getYAxis();

		final Surface surface = canvas.getSurface();
		final DirectShapeRenderer renderer = canvas.getRenderer();

		// The Y-value in units on the Y-axis corresponding to the lowest
		// point to draw on the rectangle
		final double minDrawUnits = Math.max(0.0, yAxis.getMin());

		// The Y-value in pixels corresponding to the lowest point to
		// draw on the rectangle
		final double minDrawY = yAxis.project2D(minDrawUnits).getY();

		// Define variables for the corners, making variable names
		// explicit so the code is clear
		// GWT will optimize away these variables, so there will be
		// no performance issues from these
		final double bottomY = minDrawY;
		final double topY = y;

		final boolean highlighted = isHighlighted();

		// Draw the Zeo plot with the specified color
		surface.setGlobalAlpha(highlighted ? HIGHLIGHTED_ALPHA : NORMAL_ALPHA);
		surface.setFillStyle(zeoState.getColor());

		final boolean isNoDataState = ZeoState.NO_DATA.equals(zeoState);

		if (isNoDataState) {
			// Draw a line
			final double oldLineWidth = surface.getLineWidth();
			surface.setLineWidth(highlighted
					? AbstractPlotRenderer.HIGHLIGHT_STROKE_WIDTH
							: AbstractPlotRenderer.NORMAL_STROKE_WIDTH);

			renderer.beginPath();
			renderer.moveTo(leftX, bottomY);
			renderer.drawLineTo(rightX, bottomY);
			renderer.closePath();

			renderer.stroke();
			surface.setLineWidth(oldLineWidth);
		}
		else {
			// Fill rectangle, without outline
			// Round to nearest pixels and offset by half a pixel, so that we're always completely filling pixels
			// Otherwise antialiasing will cause us to paint partial pixels, which will make the graph fade on the edges of the rectangles
			surface.fillRectangle(Math.round(leftX)+.5, Math.round(topY)+.5,
					Math.round(rightX)-Math.round(leftX), Math.round(bottomY)-Math.round(topY));
		}

		// Draw lines around rectangles, but only if the width in pixels is large
		// enough and it's not the NO_DATA state
		final int widthInPixels = (int)Math.round(rightX - leftX);
		if (!isNoDataState && widthInPixels > 6) {
			surface.setGlobalAlpha(Canvas.DEFAULT_ALPHA);
			surface.setFillStyle(Canvas.DEFAULT_COLOR);

			final double oldLineWidth = surface.getLineWidth();
			surface.setLineWidth(highlighted
					? AbstractPlotRenderer.HIGHLIGHT_STROKE_WIDTH
							: AbstractPlotRenderer.NORMAL_STROKE_WIDTH);

			// Stroke the outside of the rectangle
			// Round to nearest pixels so we draw the line in such a way that
			// it completely fills pixels.  Otherwise a 1-pixel line turns into
			// a 2-pixel grey blurry line.
			surface.strokeRectangle(Math.round(leftX),
					Math.round(topY),
					Math.round(rightX) - Math.round(leftX),
					Math.round(bottomY) - Math.round(topY));
			surface.setLineWidth(oldLineWidth);
		}
	}
}
