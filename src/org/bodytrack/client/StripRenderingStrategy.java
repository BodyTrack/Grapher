package org.bodytrack.client;

import com.google.gwt.canvas.dom.client.CssColor;

public class StripRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy {

   // The alpha value used when drawing rectangles for strip chart plots.
   private static final double NORMAL_ALPHA = 1.0;
   private static final String FIELD_RANGED_COLORS = "rangedColors";
   private static final String FIELD_STRIP_WIDTH_SECS = "stripWidthSecs";
   private static final String FIELD_STRIP_POSITION = "stripPosition";
   private static final String STRIP_POSITION_CENTER = "center";

   private final RangedColors rangedColors;
   private final Double stripWidthSecs;
   private final boolean isStripCentered;

   public StripRenderingStrategy(final StyleDescription.StyleType styleType,
                                 final Double highlightLineWidth) {
      super(styleType, highlightLineWidth);
      rangedColors = new RangedColors(styleType.<String>getValue(FIELD_RANGED_COLORS));

      if (styleType.isDefined(FIELD_STRIP_WIDTH_SECS)) {
         Double stripWidthSecsNum;
         try {
            final Object stripWidthSecsStr = styleType.getValue(FIELD_STRIP_WIDTH_SECS);
            stripWidthSecsNum = Double.parseDouble(stripWidthSecsStr.toString());
         }
         catch (final Exception e) {
            Log.debug("Failed to parse the stripWidthSecs: " + e);
            stripWidthSecsNum = null;
         }
         stripWidthSecs = stripWidthSecsNum;
      } else {
         stripWidthSecs = null;
      }

      if (styleType.isDefined(FIELD_STRIP_POSITION)) {
         boolean willCenterStrip = false;
         try {
            final Object stripPositionObj = styleType.getValue(FIELD_STRIP_POSITION);
            if (stripPositionObj != null) {
               willCenterStrip = stripPositionObj.toString().trim().toLowerCase().startsWith(STRIP_POSITION_CENTER);
            }
         }
         catch (final Exception e) {
            Log.debug("Failed to parse the stripPosition: " + e);
            willCenterStrip = false;
         }
         isStripCentered = willCenterStrip;
      } else {
         isStripCentered = false;
      }

      Log.debug("StripRenderingStrategy: isStripCentered=[" + isStripCentered + "]");
      Log.debug("StripRenderingStrategy: stripWidthSecs=[" + stripWidthSecs + "]");
   }

   @Override
   public final void paintDataPoint(final BoundedDrawingBox drawing,
                                    final GrapherTile tile,
                                    final GraphAxis xAxis,
                                    final GraphAxis yAxis,
                                    final PlottablePoint highlightedPoint,
                                    final double prevX,
                                    final double prevY,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
      paintEdgePoint(drawing, tile, xAxis, yAxis, highlightedPoint, x, y, rawDataPoint);
   }

   private double getStripWidth(final GrapherTile tile) {
      return (stripWidthSecs == null) ? tile.getPlottableTile().getSampleWidth() : stripWidthSecs;
   }

   @Override
   public final void paintEdgePoint(final BoundedDrawingBox drawing,
                                    final GrapherTile tile,
                                    final GraphAxis xAxis,
                                    final GraphAxis yAxis,
                                    final PlottablePoint highlightedPoint,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {

      final double stripWidth = getStripWidth(tile);

      final double leftX;
      final double rightX;
      if (isStripCentered) {
         final double stripHalfWidth = stripWidth / 2;
         leftX = xAxis.project2D(rawDataPoint.getDate() - stripHalfWidth).getX();
         rightX = Math.max(leftX + 1, xAxis.project2D(rawDataPoint.getDate() + stripHalfWidth).getX());
      } else {
         leftX = xAxis.project2D(rawDataPoint.getDate()).getX();
         rightX = Math.max(leftX + 1, xAxis.project2D(rawDataPoint.getDate() + stripWidth).getX());
      }

      // draw the rectangle
      final CssColor color = rangedColors.getColorForValue(rawDataPoint.getValue());
      if (color != null) {
         drawRectangle(drawing.getCanvas(), yAxis, color, leftX, rightX);
      }
   }

   /**
    * Draws a rectangle with the specified corners, stretching down to 0.
    */
   private void drawRectangle(final GrapherCanvas canvas,
                              final GraphAxis yAxis,
                              final CssColor color,
                              final double leftX,
                              final double rightX) {

      // Get the bottom and top Y values
      final double bottomY = yAxis.project2D(yAxis.getMin()).getY();
      final double topY = yAxis.project2D(yAxis.getMax()).getY();

      // Draw the Zeo plot with the specified color
      canvas.setGlobalAlpha(NORMAL_ALPHA);
      canvas.setFillStyle(color);

      // Fill rectangle, without outline. Round to nearest pixels and
      // offset by half a pixel, so that we're always completely
      // filling pixels. Otherwise antialiasing will cause us to
      // paint partial pixels, which will make the graph fade on the
      // edges of the rectangles.
      canvas.fillRectangle(Math.round(leftX) + .5,
                           Math.round(topY) + .5,
                           Math.round(rightX) - Math.round(leftX),
                           Math.round(bottomY) - Math.round(topY));
   }
}
