package org.bodytrack.client;

import com.google.gwt.core.client.GWT;
import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

/**
 * Represents a data plot for Zeo data.
 *
 * Overrides the {@link DataPlot#getDataPoints}, {@link DataPlot#paintEdgePoint}, and {@link DataPlot#paintDataPoint}
 * methods of {@link DataPlot}, allowing this class to take advantage of the capabilities
 * of {@link DataPlot} without much code.
 *
 * @see DataPlot
 */
public class ZeoDataPlot extends DataPlot {
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
    * 		the {@link GraphWidget GraphWidget} on
    * 		which this ZeoDataPlot will draw itself and its axes
    * @param xAxis
    * 		the X-axis along which this data set will be aligned when
    * 		drawn.  Usually this is a
    * 		{@link TimeGraphAxis TimeGraphAxis}
    * @param yAxis
    * 		the Y-axis along which this data set will be aligned when
    * 		drawn
    * @param deviceName
    * 		the name of the device from which this channel came
    * @param channelName
    * 		the name of the channel on the device specified by deviceName
    * @param url
    * 		the beginning of the URL for fetching this data with Ajax
    * 		calls
    * @param minLevel
    * 		the minimum level to which this will zoom
    * @see DataPlot#DataPlot(GraphWidget, GraphAxis, GraphAxis, String, String, String, int, Color, boolean)
    */
   public ZeoDataPlot(final GraphWidget container, final GraphAxis xAxis,
                      final GraphAxis yAxis, final String deviceName, final String channelName,
                      final String url, final int minLevel) {
      super(container, xAxis, yAxis, deviceName, channelName, url, minLevel, Canvas.DEFAULT_COLOR, true);
   }

   /**
    * Returns the type of this plot.
    *
    * @return
    * 		a string representing the type of this plot.  For objects
    * 		of runtime type <tt>ZeoDataPlot</tt>, this will always be
    * 		equal to the string &quot;zeo&quot;
    */
   @Override
   public String getType() {
      return "zeo";
   }

   /**
    * Paints the specified data point as a translucent rectangle.
    *
    * This is the most important way in which this class modifies the behavior from its parent {@link DataPlot} class.
    */
   @Override
   protected void paintEdgePoint(final BoundedDrawingBox drawing,
                                 final GrapherTile tile,
                                 final double x,
                                 final double y,
                                 final PlottablePoint rawDataPoint) {

      // get the ZeoState
      final int val = (int)Math.round(rawDataPoint.getValue());
      final ZeoState zeoState = ZeoState.findByValue(val);

      // use the sample width to compute the left and right x values for the bar (we want the data point to be in the
      // center of the bar)
      final double sampleHalfWidth = tile.getPlottableTile().getSampleWidth() / 2;
      final double leftX = getXAxis().project2D(rawDataPoint.getDate() - sampleHalfWidth).getX();
      final double rightX = getXAxis().project2D(rawDataPoint.getDate() + sampleHalfWidth).getX();

      // draw the rectangle
      drawRectangle(zeoState, leftX, rightX, y);
   }

   /**
    * Implemented here as a no-op, since we don't need highlighted
    * points to look different.
    */
   @Override
   protected void paintHighlightedPoint(final BoundedDrawingBox drawing,
                                        final PlottablePoint point) {
   }

   /**
    * Paints the specified data point as a translucent rectangle.
    *
    * This is the most important way in which this class modifies the behavior from its parent {@link DataPlot} class.
    */
   @Override
   protected void paintDataPoint(final BoundedDrawingBox drawing,
                                 final GrapherTile tile,
                                 final double prevX,
                                 final double prevY,
                                 final double x,
                                 final double y,
                                 final PlottablePoint rawDataPoint) {

      paintEdgePoint(drawing, tile, x, y, rawDataPoint);
   }

   /**
    * Draws a rectangle with the specified corners, stretching down
    * to 0.
    *
    * @param zeoState
    * 		the ZeoState for the data point we're rendering.  If the state is <code>null</code>
    * 	   this method does nothing.
    * @param x
    * 		the X-value (in pixels) for the right edge of the rectangle
    * @param y
    * 		the Y-value (in pixels) for the top edge of the rectangle
    * @param rectHalfWidth
    * 		the half-width (in pixels) of the rectangle to be drawn
    */
   private void drawRectangle(final ZeoState zeoState, final double leftX, final double rightX, final double y) {
      if (zeoState == null) {
         return;
      }

      final GraphAxis yAxis = getYAxis();

      final Canvas canvas = getCanvas();
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
         surface.setLineWidth(highlighted ? HIGHLIGHT_STROKE_WIDTH : NORMAL_STROKE_WIDTH);

         renderer.beginPath();
         renderer.moveTo(leftX, bottomY);
         renderer.drawLineTo(rightX, bottomY);
         renderer.closePath();

         renderer.stroke();
         surface.setLineWidth(oldLineWidth);
      }
      else {
         // Draw the rectangle.  We go clockwise, starting at the top left corner
         renderer.beginPath();
         renderer.moveTo(leftX, topY);
         renderer.drawLineTo(rightX, topY);
         renderer.drawLineTo(rightX, bottomY);
         renderer.drawLineTo(leftX, bottomY);
         renderer.drawLineTo(leftX, topY);
         renderer.closePath();
         renderer.fill();
      }

      // Draw lines around rectangles, but only if the width in pixels is large enough and it's not the NO_DATA state
      final int widthInPixels = (int)Math.round(rightX - leftX);
      if (!isNoDataState && widthInPixels > 6) {
         surface.setGlobalAlpha(Canvas.DEFAULT_ALPHA);
         surface.setFillStyle(Canvas.DEFAULT_COLOR);

         final double oldLineWidth = surface.getLineWidth();
         surface.setLineWidth(highlighted ? HIGHLIGHT_STROKE_WIDTH : NORMAL_STROKE_WIDTH);
         renderer.stroke();
         surface.setLineWidth(oldLineWidth);
      }
   }

   @Override
   protected String getDataLabel(final PlottablePoint p) {
      String label = getTimeString(p.getDate());

      final int val = (int)Math.round(p.getValue());
      final ZeoState zeoState = ZeoState.findByValue(val);
      if (zeoState != null) {
         label += "   " + zeoState.getName();
      }

      return label;
   }
}
