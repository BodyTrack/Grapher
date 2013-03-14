package org.bodytrack.client;

import java.util.Date;

import com.google.gwt.canvas.dom.client.CssColor;

public class MidnightLineRenderingStrategy implements DataIndependentRenderingStrategy {
	private static final int SECONDS_PER_DAY = 24 * 60 * 60;
	private static final int MILLISECONDS_PER_DAY = SECONDS_PER_DAY * 1000;

	private static final double STROKE_WIDTH = 0.5;
	private static final CssColor STROKE_COLOR = ColorUtils.SILVER;

	// Roughly the number of pixels between days whenever the time axis stops
	// showing day bars
	private static final int MIN_SPACING = 20;

	/**
	 * Renders vertical lines at midnight across the entire channel.
	 */
	@Override
	public void render(BoundedDrawingBox drawing, GraphAxis xAxis,
			GraphAxis yAxis) {
		if (!shouldDrawMidnightLines(xAxis))
			return;

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

	// Returns true if and only if there are at least MIN_SPACING pixels
	// between neighboring midnight lines, and xAxis is a TimeGraphAxis object
	private static boolean shouldDrawMidnightLines(final GraphAxis xAxis) {
		if (!(xAxis instanceof TimeGraphAxis))
			return false;

		final double min = xAxis.getMin();
		final double oneDayLater = min + SECONDS_PER_DAY;
		final double dayWidth = xAxis.project2D(oneDayLater).getX()
			- xAxis.project2D(min).getX();

		return dayWidth >= MIN_SPACING;
	}

	// This is roughly based on TimeGraphAxis.closestDay, but this doesn't
	// mutate internal TimeGraphAxis data structures.  Also, this always
	// moves to the NEXT day rather than the closest day.
	@SuppressWarnings("deprecation")
	private static double getNextDay(final double time) {
		Date timeDate = new Date(((long)time) * 1000);

		// Move forward by 1 day - 1 millisecond
		timeDate.setTime(timeDate.getTime() + MILLISECONDS_PER_DAY - 1);

		// Truncate to beginning of day
		timeDate.setHours(0);
		timeDate.setMinutes(0);
		timeDate.setSeconds(0);

		return timeDate.getTime() / 1000; // Convert milliseconds to seconds
	}

	private static void drawVerticalLine(final BoundedDrawingBox drawing,
			final GraphAxis xAxis,
			final double yTop,
			final double yBottom,
			final double time) {
		final double x = xAxis.project2D(time).getX();
		drawing.drawLineSegment(x, yTop, x, yBottom);
	}

	@Override
	public void beforeRender(GrapherCanvas canvas, BoundedDrawingBox drawing,
			boolean isAnyPointHighlighted) {
		canvas.setLineWidth(STROKE_WIDTH);
		canvas.setStrokeStyle(STROKE_COLOR);

		drawing.beginClippedPath();
	}

	@Override
	public void afterRender(GrapherCanvas canvas, BoundedDrawingBox drawing) {
		drawing.strokeClippedPath();

		// Clean up after ourselves
		canvas.setLineWidth(DEFAULT_STROKE_WIDTH);
		canvas.setStrokeStyle(DEFAULT_STROKE_COLOR);
		canvas.setFillStyle(DEFAULT_FILL_COLOR);
	}
}
