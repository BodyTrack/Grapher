package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import org.bodytrack.client.StyleDescription.CommentsDescription;
import org.bodytrack.client.StyleDescription.HighlightDescription;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Draws lines for the default DataSeriesPlot view
 *
 * <p>A BaseSeriesPlotRenderer can render a highlighted point, as set by the
 * {@link Plot#setHighlightedPoint(PlottablePoint)} method.  If this value is
 * not null, that point is drawn at a larger radius whenever
 * {@link SeriesPlotRenderer#render(GrapherCanvas, BoundedDrawingBox, Iterable, GraphAxis, GraphAxis, PlottablePoint)}
 * is called.  Hidden from the user of this
 * object is a mutable comment panel, which may be visible or invisible at
 * different points in time.  This mutable comment panel is never initialized
 * and never shown if the drawComments parameter to the constructor is passed
 * in as false.</p>
 */
public abstract class BaseSeriesPlotRenderer implements SeriesPlotRenderer {

	/**
	 * The default vertical distance between a data point and the comment container.
	 */
	private static final double DEFAULT_COMMENT_VERTICAL_MARGIN = 4;

	/**
	 * The preferred width in pixels of a comment popup panel. The comment
	 * panel's actual width will be the minimum of this value, the drawing
	 * width, and the preferred width of the comment.
	 */
	private static final int PREFERRED_MAX_COMMENT_WIDTH = 600;

	private PopupPanel commentPanel;
	private boolean willShowComments = false;
	private double commentVerticalMargin = DEFAULT_COMMENT_VERTICAL_MARGIN;
	private String commentContainerCssClass = null;
	private String commentCssClass = null;
	private final List<SeriesPlotRenderingStrategy> plotRenderingStrategies =
		new ArrayList<SeriesPlotRenderingStrategy>();
	private final List<DataPointRenderingStrategy> highlightRenderingStrategies =
		new ArrayList<DataPointRenderingStrategy>();
	private final List<DataPointRenderingStrategy> commentRenderingStrategies =
		new ArrayList<DataPointRenderingStrategy>();
	private final List<DataIndependentRenderingStrategy> dataIndependentStrategies =
		new ArrayList<DataIndependentRenderingStrategy>();

	/**
	 * Creates a new BaseSeriesPlotRenderer object
	 *
	 * @param styleDescription the {@link StyleDescription} used for rendering the plot
	 */
	public BaseSeriesPlotRenderer(final StyleDescription styleDescription) {
		setStyleDescription(styleDescription);
	}

	public final void setStyleDescription(final StyleDescription styleDescription) {
		willShowComments = false;
		commentVerticalMargin = DEFAULT_COMMENT_VERTICAL_MARGIN;
		commentContainerCssClass = null;
		commentCssClass = null;
		plotRenderingStrategies.clear();
		highlightRenderingStrategies.clear();
		commentRenderingStrategies.clear();

		if (styleDescription != null) {
			willShowComments = styleDescription.willShowComments();

			final HighlightDescription highlightDescription =
				styleDescription.getHighlightDescription();
			final Double highlightLineWidth =
				(highlightDescription == null)
					? null
					: highlightDescription.getLineWidth();

			final List<SeriesPlotRenderingStrategy> newPlotRenderingStrategies =
				buildSeriesPlotRenderingStrategies(styleDescription.getStyleTypes(),
						highlightLineWidth);
			if (newPlotRenderingStrategies != null) {
				plotRenderingStrategies.addAll(newPlotRenderingStrategies);
			}

			if (highlightDescription != null) {
				final List<DataPointRenderingStrategy> newHighlightRenderingStrategies =
					buildPointRenderingStrategies(highlightDescription.getStyleTypes(),
							highlightLineWidth);
				if (newHighlightRenderingStrategies != null) {
					highlightRenderingStrategies.addAll(newHighlightRenderingStrategies);
				}
			}

			final CommentsDescription commentsDescription =
				styleDescription.getCommentsDescription();
			if (commentsDescription != null) {
				commentVerticalMargin = commentsDescription.getVerticalMargin(
						DEFAULT_COMMENT_VERTICAL_MARGIN);
				commentContainerCssClass = commentsDescription.getCommentContainerCssClass();
				commentCssClass = commentsDescription.getCommentCssClass();
				final List<DataPointRenderingStrategy> newCommentRenderingStrategies =
					buildPointRenderingStrategies(commentsDescription.getStyleTypes(),
							highlightLineWidth);
				if (newCommentRenderingStrategies != null) {
					commentRenderingStrategies.addAll(newCommentRenderingStrategies);
				}
			}
		}

		// Mandatory midnight lines
		dataIndependentStrategies.add(new MidnightLineRenderingStrategy());
	}

	protected abstract List<SeriesPlotRenderingStrategy> buildSeriesPlotRenderingStrategies(
			final JsArray<StyleDescription.StyleType> styleTypes,
			final Double highlightLineWidth);

	protected abstract List<DataPointRenderingStrategy> buildPointRenderingStrategies(
			final JsArray<StyleDescription.StyleType> styleTypes,
			final Double highlightLineWidth);

	@Override
	public final void render(final GrapherCanvas canvas,
			final BoundedDrawingBox drawing,
			final Iterable<GrapherTile> tiles,
			final GraphAxis xAxis,
			final GraphAxis yAxis,
			final PlottablePoint highlightedPoint) {
		final boolean isAnyPointHighlighted = highlightedPoint != null;

		renderDataIndependentStrategies(canvas, drawing, xAxis, yAxis,
				isAnyPointHighlighted);
		renderPlotStrategies(canvas, drawing, tiles, xAxis, yAxis,
				isAnyPointHighlighted);
		renderHighlightedPointsAndComments(canvas, drawing, tiles, xAxis, yAxis,
				highlightedPoint);
	}

	private void renderDataIndependentStrategies(final GrapherCanvas canvas,
			final BoundedDrawingBox drawing,
			final GraphAxis xAxis,
			final GraphAxis yAxis,
			final boolean isAnyPointHighlighted) {
		for (final DataIndependentRenderingStrategy strategy: dataIndependentStrategies) {
			strategy.beforeRender(canvas, drawing, isAnyPointHighlighted);
			strategy.render(drawing, xAxis, yAxis);
			strategy.afterRender(canvas, drawing);
		}
	}

	public void renderPlotStrategies(final GrapherCanvas canvas,
			final BoundedDrawingBox drawing,
			final Iterable<GrapherTile> tiles,
			final GraphAxis xAxis,
			final GraphAxis yAxis,
			final boolean isAnyPointHighlighted) {
		for (final SeriesPlotRenderingStrategy renderingStrategy: plotRenderingStrategies) {
			renderingStrategy.beforeRender(canvas, drawing, isAnyPointHighlighted);
			renderPlotTiles(renderingStrategy, canvas, drawing, tiles,
					xAxis, yAxis, isAnyPointHighlighted);
			renderingStrategy.afterRender(canvas, drawing);
		}
	}

	private void renderPlotTiles(final SeriesPlotRenderingStrategy renderingStrategy,
			final GrapherCanvas canvas,
			final BoundedDrawingBox drawing,
			final Iterable<GrapherTile> tiles,
			final GraphAxis xAxis,
			final GraphAxis yAxis,
			final boolean isAnyPointHighlighted) {
		double prevX = -Double.MAX_VALUE;
		double prevY = -Double.MAX_VALUE;
		double prevValue = -Double.MAX_VALUE;

		for (final GrapherTile tile: tiles) {
			for (final PlottablePoint point: getDataPoints(tile)) {
				if (point.getValue() < MIN_DRAWABLE_VALUE)
					continue;
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
						renderingStrategy.paintDataPoint(drawing, tile, xAxis, yAxis,
								isAnyPointHighlighted, prevX, prevY, x, y, point);
				} else {
					renderingStrategy.paintEdgePoint(drawing, tile, xAxis, yAxis,
							isAnyPointHighlighted, x, y, point);
				}
				prevX = x;
				prevY = y;
			}
		}
	}

	private void renderHighlightedPointsAndComments(final GrapherCanvas canvas,
			final BoundedDrawingBox drawing,
			final Iterable<GrapherTile> tiles,
			final GraphAxis xAxis,
			final GraphAxis yAxis,
			final PlottablePoint highlightedPoint) {
		final boolean isAnyPointHighlighted = highlightedPoint != null;

		for (final DataPointRenderingStrategy renderingStrategy: commentRenderingStrategies) {
			for (final GrapherTile tile: tiles) {
				for (final PlottablePoint point: getDataPoints(tile)) {
					if (point.hasComment()) {
						renderingStrategy.beforeRender(canvas, drawing,
								isAnyPointHighlighted);
						renderingStrategy.paintPoint(drawing, xAxis, yAxis,
								xAxis.project2D(point.getDate()).getX(),
								yAxis.project2D(point.getValue()).getY(),
								highlightedPoint);
						renderingStrategy.afterRender(canvas, drawing);
					}
				}
			}
		}

		hideComment();

		// if there's a highlighted point, then we should render it as such and,
		// if it has a comment, also render the comment
		if (isAnyPointHighlighted) {
			// render highlight
			for (final DataPointRenderingStrategy renderingStrategy: highlightRenderingStrategies) {
				renderingStrategy.beforeRender(canvas, drawing, isAnyPointHighlighted);
				renderingStrategy.paintPoint(drawing,
						xAxis,
						yAxis,
						xAxis.project2D(highlightedPoint.getDate()).getX(),
						yAxis.project2D(highlightedPoint.getValue()).getY(),
						highlightedPoint);
				renderingStrategy.afterRender(canvas, drawing);
			}

			// finally, render the comment
			if (highlightedPoint.hasComment()) {
				paintComment(drawing, highlightedPoint,
						xAxis.project2D(highlightedPoint.getDate()).getX(),
						yAxis.project2D(highlightedPoint.getValue()).getY());
			}
		}
	}

	protected List<PlottablePoint> getDataPoints(final GrapherTile tile) {
		return tile.getDataPoints();
	}

	private void paintComment(final BoundedDrawingBox drawing,
			final PlottablePoint highlightedPoint,
			final double x,
			final double y) {
		final int ix = (int)x;
		final int iy = (int)y;

		if (willShowComments && highlightedPoint.hasComment()) {
			// create the panel, but display it offscreen so we can measure
			// its preferred width
			commentPanel = new PopupPanel();
			final Label label = new Label(highlightedPoint.getComment());
			if (commentCssClass != null) {
				label.setStylePrimaryName(commentCssClass);
			}
			if (commentContainerCssClass != null) {
				commentPanel.setStylePrimaryName(commentContainerCssClass);
			}
			commentPanel.add(label);
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
			if (desiredPanelWidth == preferredCommentPanelWidth) {
				actualPanelWidth = preferredCommentPanelWidth;
			} else {
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
				(int)(iy - actualPanelHeight - commentVerticalMargin);
			if (desiredPanelTop < drawing.getTopLeft().getIntY()) {
				// place the panel below the point since there's not
				// enough room to place it above
				actualPanelTop = (int)(iy + commentVerticalMargin);
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
}
