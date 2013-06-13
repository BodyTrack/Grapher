package org.bodytrack.client;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SquareRenderingStrategy extends PointRenderingStrategy {
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
      drawing.drawSquare(x, y, getRadius());
   }
}
