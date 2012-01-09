package org.bodytrack.client;

public class DotRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy {
   private static final int DEFAULT_DOT_RADIUS = 3;

   private final double radius;

   public DotRenderingStrategy(final StyleDescription.StyleType styleType,
                               final boolean willShowComments) {
      super(styleType, willShowComments);
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
      paintDot(drawing, xAxis, yAxis, isAnyPointHighlighted, x, y, rawDataPoint);
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
      paintDot(drawing, xAxis, yAxis, isAnyPointHighlighted, x, y, rawDataPoint);
   }

   protected void paintDot(final BoundedDrawingBox drawing,
                           final GraphAxis xAxis,
                           final GraphAxis yAxis,
                           final boolean isAnyPointHighlighted,
                           final double x,
                           final double y,
                           final PlottablePoint rawDataPoint) {
      final double desiredRadius = getDesiredDotRadius(rawDataPoint);
      if (willFill()) {
         drawing.drawFilledCircle(x, y, desiredRadius);
      } else {
         drawing.drawCircle(x, y, desiredRadius);
      }
   }

   /**
    * Returns the appropriate size for the radius of the dot, depending on whether the point is highlighted, has a
    * comment, etc.
    */
   protected final double getDesiredDotRadius(final PlottablePoint rawDataPoint) {
      final boolean willPaintLargerDot = (willShowComments() &&
                                          rawDataPoint != null &&
                                          rawDataPoint.hasComment());

      return willPaintLargerDot ? HIGHLIGHTED_DOT_RADIUS : radius;
   }
}
