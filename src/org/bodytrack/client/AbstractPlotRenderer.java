package org.bodytrack.client;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Draws lines for the default DataSeriesPlot view
 *
 * <p>An AbstractPlotRenderer maintains a highlighted point, as set by the
 * {@link #setHighlightedPoint(PlottablePoint)} method.  If this value is
 * not null, that point is drawn at a larger radius whenever
 * {@link #render(BoundedDrawingBox, Iterable, JavaScriptObject, JavaScriptObject)}
 * is called.  Other than this highlighted point, however, an
 * AbstractPlotRenderer is logically immutable.  Hidden from the user of this
 * object is a mutable comment panel, which may be visible or invisible at
 * different points in time.  This mutable comment panel is never initialized
 * and never shown if the drawComments parameter to the constructor is passed
 * in as false.</p>
 */
public abstract class AbstractPlotRenderer implements HighlightableRenderer {
	/**
	 * The width at which a normal line is drawn.
	 */
	public static final int NORMAL_STROKE_WIDTH = 1;

	/**
	 * The width at which a highlighted line is drawn.
	 */
	public static final int HIGHLIGHT_STROKE_WIDTH = 3;

	/**
	 * The radius to use when drawing a dot on the grapher.
	 */
	public static final double DOT_RADIUS = 0.5;

	/**
	 * The radius to use when drawing a highlighted dot on the grapher.
	 */
	public static final double HIGHLIGHTED_DOT_RADIUS = 4;

	/**
	 * The preferred width in pixels of a comment popup panel. The comment
	 * panel's actual width will be the minimum of this value, the drawing
	 * width, and the preferred width of the comment.
	 */
	private static final int PREFERRED_MAX_COMMENT_WIDTH = 600;

	/**
	 * Whenever the {@link #highlight()} method is called, we don't know
	 * which points on the axes should be highlighted, so we use this
	 * value to indicate this.  As such, testing with == is OK as a test
	 * for this point, since we set highlightedPoint to this exact
	 * memory location whenever we don't know which point should be
	 * highlighted.
	 */
	public static final PlottablePoint HIGHLIGHTED_NO_SINGLE_POINT =
		new PlottablePoint(Double.MIN_VALUE, 0.0);

	private final boolean highlighted;
	private final boolean drawComments;
	private PopupPanel commentPanel;
	private PlottablePoint highlightedPoint;

	/**
	 * Creates a new AbstractPlotRenderer object
	 *
	 * @param highlighted
	 * 	True if this AbstractPlotRenderer should draw in highlighted,
	 * 	thicker lines, and false otherwise
	 * @param drawComments
	 * 	True if this AbstractPlotRenderer should draw comment boxes,
	 * 	and false otherwise
	 */
	public AbstractPlotRenderer(boolean highlighted, boolean drawComments) {
		this.highlighted = highlighted;
		this.drawComments = drawComments;
		highlightedPoint = null;
	}

	public PlottablePoint getHighlightedPoint() {
		return highlightedPoint;
	}

	public void setHighlightedPoint(PlottablePoint highlightedPoint) {
		this.highlightedPoint = highlightedPoint;
	}

	protected boolean isHighlighted() {
		return highlighted;
	}

	protected boolean isDrawingComments() {
		return drawComments;
	}

	@Override
	public void render(BoundedDrawingBox drawing, Iterable<GrapherTile> tiles,
			JavaScriptObject nativeXAxis, JavaScriptObject nativeYAxis) {
		GraphAxis xAxis = getAxis(nativeXAxis);
		GraphAxis yAxis = getAxis(nativeYAxis);

		drawing.getCanvas().getSurface().setLineWidth(highlighted
				? HIGHLIGHT_STROKE_WIDTH
						: NORMAL_STROKE_WIDTH);

		drawing.beginClippedPath();

		// Putting these declarations outside the loop ensures
		// that no gaps appear between lines
		double prevX = -Double.MAX_VALUE;
		double prevY = -Double.MAX_VALUE;

		for (final GrapherTile tile: tiles) {
			final List<PlottablePoint> dataPoints = tile.getDataPoints();

			if (dataPoints == null) {
				continue;
			}

			for (final PlottablePoint point: dataPoints) {
				final double x = xAxis.project2D(point.getDate()).getX();
				final double y = yAxis.project2D(point.getValue()).getY();

				if (x < MIN_DRAWABLE_VALUE || y < MIN_DRAWABLE_VALUE
						|| Double.isInfinite(x) || Double.isInfinite(y)) {
					// To avoid drawing a boundary point, we set prevY
					// to something smaller than MIN_DRAWABLE_VALUE, to
					// cause paintEdgePoint to be called on the next
					// loop iteration
					prevY = MIN_DRAWABLE_VALUE * 1.01;

					continue;
				}

				// Skip any "reverse" drawing
				if (prevX > x) {
					continue;
				}

				// Draw this part of the line
				if (prevX > MIN_DRAWABLE_VALUE
						&& prevY > MIN_DRAWABLE_VALUE) {
					paintDataPoint(drawing, prevX, prevY, x, y, point);
				} else {
					paintEdgePoint(drawing, tile, x, y, point);
				}

				prevX = x;
				prevY = y;
			}
		}

		drawing.strokeClippedPath();

		hideComment();
		if (highlightedPoint != null
				&& highlightedPoint != HIGHLIGHTED_NO_SINGLE_POINT) {
			// TODO: should this be an .equals() comparison instead?
			drawing.beginClippedPath();
			paintHighlightedPoint(drawing,
					xAxis.project2D(highlightedPoint.getDate()).getX(),
					yAxis.project2D(highlightedPoint.getValue()).getY());
			drawing.strokeClippedPath();
			if (highlightedPoint.hasComment()) {
				paintComment(drawing, highlightedPoint,
						xAxis.project2D(highlightedPoint.getDate()).getX(),
						yAxis.project2D(highlightedPoint.getValue()).getY());
			}
		}

		drawing.getCanvas().getSurface().setLineWidth(NORMAL_STROKE_WIDTH);
	}

	/**
	 * Paints a left edge point for a segment of the plot
	 *
	 * <p>Note that this is only called for the left edge of a plot segment.
	 * This particular implementation draws a small dot, although a subclass
	 * implementation does not have to do the same.  Note that all x and y
	 * values are assumed to be in terms of pixels, not logical values on the
	 * axes.</p>
	 *
	 * @param drawing
	 * 	The {@link BoundedDrawingBox} that should constrain the drawing.
	 * 	Forwarding graphics calls through drawing will ensure that everything
	 * 	draws up to the edge of the viewing window but no farther
	 * @param tile
	 * 	The tile from which the data point to be drawn was obtained
	 * @param x
	 * 	The X-coordinate of the point to draw
	 * @param y
	 * 	The Y-coordinate of the point to draw
	 * @param rawDataPoint
	 * 	The raw {@link PlottablePoint}
	 */
	protected abstract void paintEdgePoint(final BoundedDrawingBox drawing,
			final GrapherTile tile,
			final double x,
			final double y,
			final PlottablePoint rawDataPoint);

	/**
	 * Draws a single data point on the graph.
	 *
	 * <p>Note that this method has as a precondition that {@code prevX < x}.
	 * Note that all x and y values are assumed to be in terms of pixels.</p>
	 *
	 * @param drawing
	 * 	The {@link BoundedDrawingBox} that should constrain the drawing.
	 * 	Forwarding graphics calls through drawing will ensure that everything
	 * 	draws up to the edge of the viewing window but no farther
	 * @param prevX
	 * 	The previous X-value, which will be greater than MIN_DRAWABLE_VALUE
	 * @param prevY
	 * 	The previous Y-value, which will be greater than MIN_DRAWABLE_VALUE
	 * @param x
	 * 	The current X-value, which will be greater than MIN_DRAWABLE_VALUE,
	 * 	and greater than or equal to prevX
	 * @param y
	 * 	The current Y-value, which will be greater than MIN_DRAWABLE_VALUE
	 * @param rawDataPoint
	 * 	The raw {@link PlottablePoint}
	 * @see #MIN_DRAWABLE_VALUE
	 */
	protected abstract void paintDataPoint(final BoundedDrawingBox drawing,
			final double prevX,
			final double prevY,
			final double x,
			final double y,
			final PlottablePoint rawDataPoint);

	/**
	 * Draws a single point on the graph, in highlighted style.
	 *
	 * <p>This is called by {@link #paint()} after all data points have been
	 * painted, and the parameter is the data point closest to the mouse.
	 * Note that this means that, by the time this method is called, point
	 * has already been drawn.</p>
	 *
	 * <p>This draws a larger dot at point, although of course a subclass
	 * implementation does not have to follow that lead.</p>
	 *
	 * @param drawing
	 * 	The {@link BoundedDrawingBox BoundedDrawingBox} that should constrain
	 * 	the drawing.  Forwarding graphics calls through drawing will ensure
	 * 	that everything draws up to the edge of the viewing window but no
	 * 	farther
	 * @param x
	 * 	The X-position of the point to draw, in screen pixels
	 * @param y
	 * 	The Y-position of the point to draw, in screen pixels
	 */
	protected abstract void paintHighlightedPoint(final BoundedDrawingBox drawing,
			double x, double y);

	private void paintComment(final BoundedDrawingBox drawing,
			final PlottablePoint highlightedPoint, double x, double y) {
		int ix = (int)x;
		int iy = (int)y;

		if (drawComments && highlightedPoint.hasComment()) {
			// create the panel, but display it offscreen so we can measure
			// its preferred width
			commentPanel = new PopupPanel();
			commentPanel.add(new Label(highlightedPoint.getComment()));
			commentPanel.setPopupPosition(-10000, -10000);
			commentPanel.show();
			final int preferredCommentPanelWidth = commentPanel.getOffsetWidth();
			commentPanel.hide();

			// compute the actual panel width by taking the minimum of the comment
			// panel's preferred width, the width of the drawing region, and the
			// PREFERRED_MAX_COMMENT_WIDTH.
			final int desiredPanelWidth =
				(int)Math.min(preferredCommentPanelWidth,
						Math.min(drawing.getWidth(),
								PREFERRED_MAX_COMMENT_WIDTH));

			// set the panel to the corrected width
			final int actualPanelWidth;
			if (desiredPanelWidth != preferredCommentPanelWidth) {
				commentPanel.setWidth(String.valueOf(desiredPanelWidth) + "px");
				commentPanel.show();

				// unfortunately, setting the width doesn't take borders and such
				// into account, so we need read the width again and then adjust
				// accordingly
				final int widthPlusExtra = commentPanel.getOffsetWidth();
				commentPanel.hide();

				commentPanel.setWidth(
						String.valueOf(desiredPanelWidth - (widthPlusExtra - desiredPanelWidth))
						+ "px");
				commentPanel.show();

				actualPanelWidth = commentPanel.getOffsetWidth();
			} else {
				actualPanelWidth = preferredCommentPanelWidth;
			}

			// now, if the actual panel width is less than the comment panel's
			// preferred width, then the height must have changed so we need to
			// redisplay the panel to determine its new height.
			commentPanel.show();
			final int actualPanelHeight = commentPanel.getOffsetHeight();

			// Now that we know the actual height and width of the comment panel,
			// we can determine where to place the panel horizontally and
			// vertically.  The general strategy is to try to center the panel
			// horizontally above the point (we favor placement above the point
			// so that the mouse pointer doesn't occlude the comment).  For
			// horizontal placement, if the panel can't be centered with respect
			// to the point, then just shift it left or right enough so that it
			// fits within the bounds of the drawing region.  For vertical
			// placement, if the panel can't be placed above the point, then
			// place it below.

			final int actualPanelLeft;
			final int desiredPanelLeft = ix - actualPanelWidth / 2;
			if (desiredPanelLeft < drawing.getTopLeft().getIntX()) {
				actualPanelLeft = drawing.getTopLeft().getIntX();
			} else if ((desiredPanelLeft + actualPanelWidth) > drawing.getBottomRight().getIntX()) {
				actualPanelLeft = drawing.getBottomRight().getIntX() - actualPanelWidth;
			} else {
				actualPanelLeft = desiredPanelLeft;
			}

			final int actualPanelTop;
			final int desiredPanelTop =
				(int)(iy - actualPanelHeight - HIGHLIGHTED_DOT_RADIUS);
			if (desiredPanelTop < drawing.getTopLeft().getIntY()) {
				// place the panel below the point since there's not
				// enough room to place it above
				actualPanelTop = (int)(iy + HIGHLIGHTED_DOT_RADIUS);
			} else {
				actualPanelTop = desiredPanelTop;
			}

			// get the top-left coords of the canvas so we can offset the panel position
			final Element nativeCanvasElement = drawing.getCanvas().getNativeCanvasElement();
			final int canvasLeft = nativeCanvasElement.getAbsoluteLeft();
			final int canvasTop = nativeCanvasElement.getAbsoluteTop();

			// set the panel's position--these are in absolute page coordinates,
			// so we need to offset it by the canvas's absolute position.
			commentPanel.setPopupPosition(actualPanelLeft + canvasLeft,
					actualPanelTop + canvasTop);

			// show the panel
			commentPanel.show();
		}
	}

	private void hideComment() {
		if (commentPanel != null) {
			commentPanel.hide();
			commentPanel = null;
		}
	}

	protected GraphAxis getAxis(JavaScriptObject nativeAxis) {
		Dynamic djso = nativeAxis.cast();
		return djso.get("__backingAxis");
	}
}
