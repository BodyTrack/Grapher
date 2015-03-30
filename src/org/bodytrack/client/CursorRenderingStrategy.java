package org.bodytrack.client;

import com.google.gwt.canvas.dom.client.CssColor;

public class CursorRenderingStrategy implements DataIndependentRenderingStrategy {

	private static final double STROKE_WIDTH = 2;
	private static final CssColor STROKE_COLOR = ColorUtils.RED;
	private final double strokeWidth;
	private final CssColor strokeColor;

	public CursorRenderingStrategy(final StyleDescription.CursorDescription cursorDescription) {
		Double specifiedStrokeWidth = STROKE_WIDTH;
		CssColor specifiedStrokeColor = STROKE_COLOR;
		if (cursorDescription != null) {
			specifiedStrokeWidth = cursorDescription.getLineWidth();
			specifiedStrokeColor = cursorDescription.getColor();
		}
		strokeWidth = (specifiedStrokeWidth == null) ? STROKE_WIDTH : specifiedStrokeWidth;
		strokeColor = (specifiedStrokeColor == null) ? STROKE_COLOR : specifiedStrokeColor;
	}

	@Override
	public void beforeRender(GrapherCanvas canvas, BoundedDrawingBox drawing, boolean isAnyPointHighlighted) {
		canvas.setLineWidth(strokeWidth);
		canvas.setStrokeStyle(strokeColor);

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

	@Override
	public void render(BoundedDrawingBox drawing, GraphAxis xAxis,
			GraphAxis yAxis) {
		final double xMin = xAxis.getMin();
		final double xMax = xAxis.getMax();
		final double yMin = yAxis.getMin();
		final double yMax = yAxis.getMax();
		final double yTop = yAxis.project2D(yMax).getY();
		final double yBottom = yAxis.project2D(yMin).getY();
		
		Double cursorPos = xAxis.getCursorPosition();
		if (cursorPos != null && cursorPos >= xMin && cursorPos <= xMax){
			drawVerticalLine(drawing,xAxis,yTop,yBottom,cursorPos);
		}
		
	}
	
	private static void drawVerticalLine(final BoundedDrawingBox drawing,
			final GraphAxis xAxis,
			final double yTop,
			final double yBottom,
			final double time) {
		final double x = xAxis.project2D(time).getX();
		drawing.drawLineSegment(x, yTop, x, yBottom);
	}

}
