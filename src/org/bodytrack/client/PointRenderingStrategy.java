package org.bodytrack.client;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class PointRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy implements DataPointRenderingStrategy {
   private static final int DEFAULT_RADIUS = 3;
   private final double radius;

   public PointRenderingStrategy(final StyleDescription.StyleType styleType,
                                 final Double theHighlightLineWidth) {
      super(styleType, theHighlightLineWidth);
      radius = styleType.getDoubleValue("radius", DEFAULT_RADIUS);
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
      paintPoint(drawing, xAxis, yAxis, x, y, rawDataPoint, highlightedPoint);
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
      paintPoint(drawing, xAxis, yAxis, x, y, rawDataPoint, highlightedPoint);
   }

   /** Returns the radius of the dot. */
   protected final double getRadius() {
      return radius;
   }
}