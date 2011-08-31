package org.bodytrack.client;

import com.google.gwt.core.client.GWT;
import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

import java.util.ArrayList;
import java.util.List;

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
      super(container, xAxis, yAxis, deviceName, channelName,
            url, minLevel, Canvas.DEFAULT_COLOR, true);
      // Doesn't make sense to publish data values as 1, 2, 3, 4,
      // at least until we have a way to provide more description
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
    * Returns the ordered list of points this DataPlot should draw
    * in {@link DataPlot#paintAllDataPoints}.
    *
    * This method processes all the points returned by
    * {@code tile.getDataPoints()}, so that all the points (except the
    * first in each group) represent top right corners of bars.  This
    * is necessary because the
    * {@link DataPlot#paintDataPoint(BoundedDrawingBox, double, double, double, double, PlottablePoint)}
    * method uses the top right corner of a bar when drawing a point.
    *
    * @param tile
    * 		the {@link GrapherTile GrapherTile}
    * 		from which to pull the data points
    * @return
    * 		a list of
    * 		{@link PlottablePoint PlottablePoint}
    * 		objects to be drawn by paintAllDataPoints, which may be
    * 		empty (this will be the case if no sample width is available
    * 		for tile)
    */
   @Override
   protected List<PlottablePoint> getDataPoints(final GrapherTile tile) {
      final List<PlottablePoint> points = tile.getDataPoints();

      final List<PlottablePoint> transformedPoints =
            new ArrayList<PlottablePoint>();

      final double width = tile.getPlottableTile().getSampleWidth();
      if (width <= 0) {
         return new ArrayList<PlottablePoint>();
      }

      // An optimizing compiler should do this anyway, but we want
      // to make sure we are safe
      final double halfWidth = width / 2.0;

      PlottablePoint prev = null;

      for (final PlottablePoint point : points) {
         final double time = point.getDate();
         final double value = point.getValue();

         if (value < MIN_DRAWABLE_VALUE) {
            prev = null;
            continue;
         }

         if (prev == null) {
            // Left edge
            // Add two points - the two edges for the first bar

            transformedPoints.add(
                  new PlottablePoint(time - halfWidth, value));
            transformedPoints.add(
                  new PlottablePoint(time + halfWidth, value));
         }
         else {
            // Not on the left edge
            transformedPoints.add(
                  new PlottablePoint(time + halfWidth, value));
         }

         prev = point;
      }

      return transformedPoints;
   }

   /**
    * Implemented here as a no-op, since we handle the edges properly
    * in {@link DataPlot#paintDataPoint(BoundedDrawingBox, double, double, double, double, PlottablePoint)}.
    */
   @Override
   protected void paintEdgePoint(final BoundedDrawingBox drawing, final double x,
                                 final double y) {
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
    * This is the most important way in which this class modifies
    * its behavior from the &quot;default&quot; of the parent DataPlot
    * class.
    */
   @Override
   protected void paintDataPoint(final BoundedDrawingBox drawing, final double prevX, final double prevY, final double x, final double y, final PlottablePoint rawDataPoint) {
      drawRectangle(getColor(rawDataPoint.getValue()), prevX, prevY, x, y);
   }

   /**
    * Draws a rectangle with the specified corners, stretching down
    * to 0.
    *
    * @param color
    * 		the color to use for the interior of the rectangle.  The
    * 		borders of the rectangle will be drawn in the color already
    * 		set by the paint method.  If the color is <code>null</code>
    * 	   this method does nothing.
    * @param prevX
    * 		the X-value (in pixels) for the left edge of the rectangle
    * @param prevY
    * 		unused
    * @param x
    * 		the X-value (in pixels) for the right edge of the rectangle
    * @param y
    * 		the Y-value (in pixels) for the top of the rectangle
    */
   private void drawRectangle(final Color color, final double prevX, final double prevY, final double x, final double y) {
      if (color == null) {
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
      final double leftX = prevX;
      final double rightX = x;
      final double bottomY = minDrawY;
      final double topY = y;

      final boolean highlighted = isHighlighted();

      // Draw the Zeo plot with the specified color
      surface.setGlobalAlpha(highlighted
                             ? HIGHLIGHTED_ALPHA : NORMAL_ALPHA);
      surface.setFillStyle(color);

      // Draw rectangles.  We go clockwise, starting at the top left corner
      renderer.beginPath();
      renderer.moveTo(leftX, topY);
      renderer.drawLineTo(rightX, topY);
      renderer.drawLineTo(rightX, bottomY);
      renderer.drawLineTo(leftX, bottomY);
      renderer.drawLineTo(leftX, topY);
      renderer.closePath();
      renderer.fill();

      // Draw lines around rectangles
      surface.setGlobalAlpha(Canvas.DEFAULT_ALPHA);
      surface.setFillStyle(Canvas.DEFAULT_COLOR);

      final double oldLineWidth = surface.getLineWidth();
      surface.setLineWidth(highlighted
                           ? HIGHLIGHT_STROKE_WIDTH : NORMAL_STROKE_WIDTH);

      renderer.stroke();

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
    * 		the color to use to draw the bar for that value (may be null)
    */
   private Color getColor(final double value) {
      final int val = (int)Math.round(value);

      final ZeoState zeoState = ZeoState.findByValue(val);

      if (zeoState != null) {
         return zeoState.getColor();
      }

      GWT.log("ZeoDataPlot.getColor(): unexpected value: " + val);
      return null;
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
