package org.bodytrack.client;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws lines for the default DataSeriesPlot view
 *
 * <p>A BaseSeriesPlotRenderer can render a highlighted point, as set by the
 * {@link Plot#setHighlightedPoint(PlottablePoint)} method.  If this value is
 * not null, that point is drawn at a larger radius whenever
 * {@link SeriesPlotRenderer#render(Canvas, BoundedDrawingBox, Iterable, GraphAxis, GraphAxis, PlottablePoint)}
 * is called.  Hidden from the user of this
 * object is a mutable comment panel, which may be visible or invisible at
 * different points in time.  This mutable comment panel is never initialized
 * and never shown if the drawComments parameter to the constructor is passed
 * in as false.</p>
 */
public abstract class BaseSeriesPlotRenderer implements SeriesPlotRenderer {

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

   private PopupPanel commentPanel;
   private boolean willShowComments = false;
   private final List<SeriesPlotRenderingStrategy> renderingStrategies = new ArrayList<SeriesPlotRenderingStrategy>();

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
      renderingStrategies.clear();

      if (styleDescription != null) {
         willShowComments = styleDescription.willShowComments();

         final List<SeriesPlotRenderingStrategy> newRenderingStrategies = buildRenderingStrategies(styleDescription);
         if (newRenderingStrategies != null) {
            renderingStrategies.addAll(newRenderingStrategies);
         }
      }
   }

   protected abstract List<SeriesPlotRenderingStrategy> buildRenderingStrategies(final StyleDescription styleDescription);

   @Override
   public final void render(final Canvas canvas,
                            final BoundedDrawingBox drawing,
                            final Iterable<GrapherTile> tiles,
                            final GraphAxis xAxis,
                            final GraphAxis yAxis,
                            final PlottablePoint highlightedPoint) {
      final boolean isAnyPointHighlighted = highlightedPoint != null;

      for (final GrapherTile tile : tiles) {
         final List<PlottablePoint> dataPoints = getDataPoints(tile);

         if (dataPoints == null) {
            continue;
         }

         for (final SeriesPlotRenderingStrategy renderingStrategy : renderingStrategies) {
            renderingStrategy.beforeRender(canvas, isAnyPointHighlighted);

            drawing.beginClippedPath();

            // Putting these declarations outside the loop ensures
            // that no gaps appear between lines
            double prevX = -Double.MAX_VALUE;
            double prevY = -Double.MAX_VALUE;

            for (final PlottablePoint point : dataPoints) {
               // TODO: precompute these before iterating over the rendering strategies
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
                  renderingStrategy.paintDataPoint(drawing, tile, xAxis, yAxis, isAnyPointHighlighted, prevX, prevY, x, y, point);
               } else {
                  renderingStrategy.paintEdgePoint(drawing, tile, xAxis, yAxis, isAnyPointHighlighted, x, y, point);
               }

               prevX = x;
               prevY = y;
            }

            drawing.strokeClippedPath();

            renderingStrategy.afterRender(canvas);
         }
      }

      hideComment();
      if (isAnyPointHighlighted) {
         drawing.beginClippedPath();
         paintHighlightedPoint(drawing,
                               xAxis.project2D(highlightedPoint.getDate()).getX(),
                               yAxis.project2D(highlightedPoint.getValue()).getY(),
                               highlightedPoint);
         drawing.strokeClippedPath();
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
    * @param rawDataPoint
    * 	The raw {@link PlottablePoint}
    */
   private void paintHighlightedPoint(final BoundedDrawingBox drawing,
                                      final double x,
                                      final double y,
                                      final PlottablePoint rawDataPoint) {
      // TODO:
      Log.debug("BaseSeriesPlotRenderer.paintHighlightedPoint(): NOT YET IMPLEMENTED!");
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
}
