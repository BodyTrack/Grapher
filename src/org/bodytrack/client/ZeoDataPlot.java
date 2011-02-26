package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

/**
 * Represents a data plot for Zeo data.
 *
 * Overrides the getDataPoints, paintEdgePoint, and paintDataPoint methods
 * of DataPlot, allowing this class to take advantage of the capabilities
 * of DataPlot without much code.
 *
 * @see org.bodytrack.client.DataPlot DataPlot
 */
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
			Canvas.DEFAULT_COLOR, false);
		// Doesn't make sense to publish data values as 1, 2, 3, 4,
		// at least until we have a way to provide more description
	}

	/**
	 * Returns the ordered list of points this DataPlot should draw
	 * in {@link DataPlot#paintAllDataPoints()}.
	 *
	 * This method processes all the points returned by
	 * {@code tile.getDataPoints()}, so that all the points (except the
	 * first in each group) represent top right corners of bars.  This
	 * is necessary because the
	 * {@link ZeoDataPlot#paintDataPoint(BoundedDrawingBox, double, double, double, double)}
	 * method uses the top right corner of a bar when drawing a point.
	 *
	 * @param tile
	 * 		the {@link org.bodytrack.client.GrapherTile GrapherTile}
	 * 		from which to pull the data points
	 * @return
	 * 		a list of
	 * 		{@link org.bodytrack.client.PlottablePoint PlottablePoint}
	 * 		objects to be drawn by paintAllDataPoints, which may be
	 * 		empty (this will be the case if no sample width is available
	 * 		for tile)
	 */
	@Override
	protected List<PlottablePoint> getDataPoints(GrapherTile tile) {
		List<PlottablePoint> points = tile.getDataPoints();

		List<PlottablePoint> transformedPoints =
			new ArrayList<PlottablePoint>();

		double width = tile.getPlottableTile().getSampleWidth();
		if (width <= 0)
			return new ArrayList<PlottablePoint>();

		// An optimizing compiler should do this anyway, but we want
		// to make sure we are safe
		double halfWidth = width / 2.0;

		PlottablePoint prev = null;

		for (PlottablePoint point: points) {
			double time = point.getDate();
			double mean = point.getValue();

			if (mean < MIN_DRAWABLE_VALUE) {
				prev = null;
				continue;
			}

			if (prev == null) {
				// Left edge
				// Add two points - the two edges for the first bar

				transformedPoints.add(
					new PlottablePoint(time - halfWidth, mean));
				transformedPoints.add(
					new PlottablePoint(time + halfWidth, mean));
			} else {
				// Not on the left edge
				transformedPoints.add(
					new PlottablePoint(time + halfWidth, mean));
			}
		}

		return transformedPoints;
	}

	/**
	 * Implemented here as a no-op, since we handle the edges properly
	 * in paintDataPoint.
	 */
	@Override
	protected void paintEdgePoint(BoundedDrawingBox drawing, double x,
			double y) {}

	/**
	 * Paints the specified data point as a translucent rectangle.
	 *
	 * This is the most important way in which this class modifies
	 * its behavior from the &quot;default&quot; of the parent DataPlot
	 * class.
	 */
	@Override
	protected void paintDataPoint(BoundedDrawingBox drawing,
			double prevX, double prevY, double x, double y) {
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

		// Define variables for the corners, making variable names
		// explicit so the code is clear
		// GWT will optimize away these variables, so there will be
		// no performance issues from these
		// Also, this allows us to ensure the bounds on the X-axis are
		// correct
		double leftX = Math.max(prevX, getXAxis().getMin());
		double rightX = Math.min(x, getXAxis().getMax());
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
