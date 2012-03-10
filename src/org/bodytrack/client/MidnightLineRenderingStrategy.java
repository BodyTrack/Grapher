package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

public class MidnightLineRenderingStrategy implements DataIndependentRenderingStrategy {
	private static final int SECONDS_PER_DAY = 24 * 60 * 60;

	private static final double STROKE_WIDTH = 0.5;
	private static final Color STROKE_COLOR = ColorUtils.SILVER;

	/**
	 * Renders vertical lines at midnight across the entire channel.
	 */
	@Override
	public void render(BoundedDrawingBox drawing, GraphAxis xAxis,
			GraphAxis yAxis) {
		final double xMin = xAxis.getMin();
		final double xMax = xAxis.getMax();
		final double yTop = yAxis.project2D(yAxis.getMin()).getY();
		final double yBottom = yAxis.project2D(yAxis.getMax()).getY();

		final double firstDay = getNextDay(xMin);

		double midnight = firstDay;
		while (midnight <= xMax) {
			drawVerticalLine(drawing, xAxis, yTop, yBottom, midnight);
			midnight += SECONDS_PER_DAY;
		}
	}

	private double getNextDay(final double time) {
		return Math.ceil(time / SECONDS_PER_DAY) * SECONDS_PER_DAY;
	}

	private void drawVerticalLine(final BoundedDrawingBox drawing,
			final GraphAxis xAxis,
			final double yTop,
			final double yBottom,
			final double time) {
		final double x = xAxis.project2D(time).getX();
		drawing.drawLineSegment(x, yTop, x, yBottom);
	}

	@Override
	public void beforeRender(Canvas canvas, BoundedDrawingBox drawing,
			boolean isAnyPointHighlighted) {
		canvas.setLineWidth(STROKE_WIDTH);
		canvas.setStrokeStyle(STROKE_COLOR);

		drawing.beginClippedPath();
	}

	@Override
	public void afterRender(Canvas canvas, BoundedDrawingBox drawing) {
		drawing.strokeClippedPath();

		// Clean up after ourselves
		canvas.setLineWidth(DEFAULT_STROKE_WIDTH);
		canvas.setStrokeStyle(DEFAULT_STROKE_COLOR);
		canvas.setFillStyle(DEFAULT_FILL_COLOR);
	}
}
