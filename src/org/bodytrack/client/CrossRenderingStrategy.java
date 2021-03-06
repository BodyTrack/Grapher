package org.bodytrack.client;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class CrossRenderingStrategy extends PointRenderingStrategy {
   public CrossRenderingStrategy(final StyleDescription.StyleType styleType,
                                 final Double highlightLineWidth) {
      super(styleType, highlightLineWidth);
   }

   @Override
   public void paintPoint(final BoundedDrawingBox drawing,
                          final GraphAxis xAxis,
                          final GraphAxis yAxis,
                          final double x,
                          final double y,
                          final PlottablePoint rawDataPoint) {

      final double radius = getRadius();
      final double x1 = x - radius;
      final double x2 = x + radius;
      final double y1 = y - radius;
      final double y2 = y + radius;
      drawing.drawLineSegment(x1, y1, x2, y2);
      drawing.drawLineSegment(x1, y2, x2, y1);
   }
}
