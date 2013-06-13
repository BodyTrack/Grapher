package org.bodytrack.client;

public class CircleRenderingStrategy extends PointRenderingStrategy {

   public CircleRenderingStrategy(final StyleDescription.StyleType styleType,
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
      drawing.drawCircle(x, y, getRadius());
   }
}
