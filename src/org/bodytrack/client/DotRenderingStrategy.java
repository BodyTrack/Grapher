package org.bodytrack.client;

public class DotRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy implements PointRenderingStrategy {
   private static final int DEFAULT_DOT_RADIUS = 3;

   private final double radius;

   public DotRenderingStrategy(final StyleDescription.StyleType styleType) {
      super(styleType);
      radius = styleType.getDoubleValue("radius", DEFAULT_DOT_RADIUS);
   }

   @Override
   public final void paintEdgePoint(final BoundedDrawingBox drawing,
                                    final GrapherTile tile,
                                    final GraphAxis xAxis,
                                    final GraphAxis yAxis,
                                    final boolean isAnyPointHighlighted,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
      paintPoint(drawing, xAxis, yAxis, x, y, rawDataPoint);
   }

   @Override
   public final void paintDataPoint(final BoundedDrawingBox drawing,
                                    final GrapherTile tile,
                                    final GraphAxis xAxis,
                                    final GraphAxis yAxis,
                                    final boolean isAnyPointHighlighted,
                                    final double prevX,
                                    final double prevY,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
      paintPoint(drawing, xAxis, yAxis, x, y, rawDataPoint);
   }

   @Override
   public void paintPoint(final BoundedDrawingBox drawing,
                          final GraphAxis xAxis,
                          final GraphAxis yAxis,
                          final double x,
                          final double y,
                          final PlottablePoint rawDataPoint) {
      if (willFill()) {
         drawing.drawFilledCircle(x, y, radius);
      } else {
         drawing.drawCircle(x, y, radius);
      }
   }

   /** Returns the radius of the dot. */
   protected final double getRadius() {
      return radius;
   }
}
