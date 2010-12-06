package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

/**
 * Represents a data plot for Zeo data.
 */
// TODO: See if the implementation of paintDataPoint works properly, or if
// more needs to be done (i.e. the native drawRectangle method)
public class ZeoDataPlot extends DataPlot {
	private static final Color WAKE_COLOR = new Color(0xFF, 0x45, 0x00);
	private static final Color REM_COLOR = new Color(0x90, 0xEE, 0x90);
	private static final Color LIGHT_COLOR = new Color(0xA9, 0xA9, 0xA9);
	private static final Color DEEP_COLOR = new Color(0x22, 0x8B, 0x22);

	/**
	 * Used to indicate that no data should be drawn at this point.
	 */
	private static final Color NO_DATA_COLOR = null;

	/**
	 * The alpha value used when drawing rectangles for Zeo plots.
	 */
	private static final double NORMAL_ALPHA = 0.4;

	/**
	 * The alpha used when drawing rectangles for highlighted Zeo plots.
	 */
	private static final double HIGHLIGHTED_ALPHA = 0.5;

	/**
	 * Initializes this ZeoDataPlot with the specified parameters.
	 *
	 * @param container
	 * 		the {@link org.bodytrack.client.GraphWidget GraphWidget} on
	 * 		which this ZeoDataPlot will draw itself and its axes
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
	 * 		the minimum level to which this will zoom
	 * @see DataPlot#DataPlot(GraphWidget, GraphAxis, GraphAxis,
	 * 		String, int, Color)
	 */
	public ZeoDataPlot(GraphWidget container, GraphAxis xAxis,
			GraphAxis yAxis, String url, int minLevel) {
		super(container, xAxis, yAxis, url, minLevel,
			Canvas.DEFAULT_COLOR);
	}

	/**
	 * Paints the specified data point as a translucent rectangle.
	 *
	 * This is the most important way in which this class modifies
	 * its behavior from the &quot;default&quot; of the parent DataPlot
	 * class.
	 */
	@Override
	protected void paintDataPoint(double prevX, double prevY, double x,
			double y) {
		drawRectangle(getColor(y), prevX, prevY, x, y);
	}

	/**
	 * Draws a rectangle with the specified corners, stretching down
	 * to 0.
	 *
	 * @param color
	 * 		the color to use for the interior of the rectangle.  The
	 * 		borders of the rectangle will be drawn in the color already
	 * 		set by the paint method.
	 * @param prevX
	 * 		the X-value (in pixels) for the left edge of the rectangle
	 * @param prevY
	 * 		unused
	 * @param x
	 * 		the X-value (in pixels) for the right edge of the rectangle
	 * @param y
	 * 		the Y-value (in pixels) for the top of the rectangle
	 */
	private void drawRectangle(Color color, double prevX, double prevY,
			double x, double y) {
		if (color == NO_DATA_COLOR)
			return;

		GraphAxis yAxis = getYAxis();

		Canvas canvas = getCanvas();
		Surface surface = canvas.getSurface();
		DirectShapeRenderer renderer = canvas.getRenderer();

		// The Y-value in units on the Y-axis corresponding to the lowest
		// point to draw on the rectangle
		double minDrawUnits = Math.max(0.0, yAxis.getMin());

		// The Y-value in pixels corresponding to the lowest point to
		// draw on the rectangle
		double minDrawY = yAxis.project2D(minDrawUnits).getY();

		/*
		// We will resort to this if need be
		drawRectangle(getCanvasId(canvas.getSurface().getCanvas()),
			color.getR(), color.getG(), color.getB(), ZEO_ALPHA,
			prevX, y, x - prevX, y - minDrawY);
		*/

		// Define variables for the corners, making variable names
		// explicit so the code is clear
		// GWT will optimize away these variables, so there will be
		// no performance issues from these
		double leftX = prevX;
		double rightX = x;
		double bottomY = minDrawY;
		double topY = y;

		boolean highlighted = isHighlighted();

		// Draw the Zeo plot with the specified color
		surface.setGlobalAlpha(highlighted
			? HIGHLIGHTED_ALPHA : NORMAL_ALPHA);
		surface.setFillStyle(color);

		renderer.drawRect(leftX, topY,
			rightX - leftX, topY - bottomY);

		// Draw lines around rectangles
		// We go clockwise, starting at the top left corner
		surface.setGlobalAlpha(Canvas.DEFAULT_ALPHA);
		surface.setFillStyle(Canvas.DEFAULT_COLOR);

		double oldLineWidth = surface.getLineWidth();
		surface.setLineWidth(highlighted
			? HIGHLIGHT_STROKE_WIDTH : NORMAL_STROKE_WIDTH);

		renderer.drawLineSegment(leftX, topY, rightX, topY);
		renderer.drawLineSegment(rightX, topY, rightX, bottomY);
		renderer.drawLineSegment(rightX, bottomY, leftX, bottomY);
		renderer.drawLineSegment(leftX, bottomY, leftX, topY);

		// Clean up after ourselves - it is preferable to put things
		// back the way they were rather than setting the values to
		// defaults
		surface.setLineWidth(oldLineWidth);
	}

	/**
	 * Returns the color to use to draw the specified value for a Zeo plot.
	 *
	 * @param value
	 * 		the value, which is expected to be 0, 1, 2, 3, or 4
	 * @return
	 * 		the color to use to draw the bar for that value
	 */
	private Color getColor(double value) {
		int val = (int) Math.round(value);

		switch (val) {
			case 1:
				return DEEP_COLOR;
			case 2:
				return LIGHT_COLOR;
			case 3:
				return REM_COLOR;
			case 4:
				return WAKE_COLOR;
		}

		return NO_DATA_COLOR;
	}
}
