package org.bodytrack.client;

public class LollipopRenderingStrategy extends CircleRenderingStrategy {
   public LollipopRenderingStrategy(final StyleDescription.StyleType styleType,
                                    final Double highlightLineWidth) {
      super(styleType, highlightLineWidth);
   }

   @Override
   public void paintPoint(final BoundedDrawingBox drawing,
                           final GraphAxis xAxis,
                           final GraphAxis yAxis,
                           final double x,
                           final double y,
                           final PlottablePoint rawDataPoint,
                           final PlottablePoint highlightedPoint) {
      // The Y-value in pixels corresponding to the lowest point to draw on the lollipop stick
      final double minDrawY = yAxis.project2D(0).getY();

      // The Y-value in pixels corresponding to the highest point to draw on the lollipop stick
      final double maxDrawY = y + getRadius();

      // Don't draw the stick if the circle part of the lollipop would occlude the stick
      if (minDrawY > maxDrawY) {
         drawing.drawLineSegment(x, minDrawY, x, maxDrawY);
      }

      super.paintPoint(drawing, xAxis, yAxis, x, y, rawDataPoint, highlightedPoint);
   }
}
