package org.bodytrack.client;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SquareRenderingStrategy extends CircleRenderingStrategy {
   public SquareRenderingStrategy(final StyleDescription.StyleType styleType,
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
      if (willFill()) {
         drawing.drawFilledSmallSquare(x, y, getRadius() * 2);
      } else {
         drawing.drawSmallSquare(x, y, getRadius() * 2);
      }
   }
}
