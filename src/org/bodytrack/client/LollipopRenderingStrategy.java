package org.bodytrack.client;

public class LollipopRenderingStrategy extends DotRenderingStrategy {
   public LollipopRenderingStrategy(final StyleDescription.StyleType styleType,
                                    final boolean willShowComments) {
      super(styleType, willShowComments);
   }

   @Override
   protected void paintDot(final BoundedDrawingBox drawing,
                           final GraphAxis xAxis,
                           final GraphAxis yAxis,
                           final boolean isAnyPointHighlighted,
                           final double x,
                           final double y,
                           final PlottablePoint rawDataPoint) {

      // The Y-value in pixels corresponding to the lowest point to draw on the lollipop stick
      final double minDrawY = yAxis.project2D(0).getY();

      // The Y-value in pixels corresponding to the highest point to draw on the lollipop stick
      final double maxDrawY = y + getDesiredDotRadius(rawDataPoint);

      // Don't draw the stick if the circle part of the lollipop would occlude the stick
      if (minDrawY > maxDrawY) {
         drawing.drawLineSegment(x, minDrawY, x, maxDrawY);
      }

      super.paintDot(drawing, xAxis, yAxis, isAnyPointHighlighted, x, y, rawDataPoint);
   }
}
