package org.bodytrack.client;

public class LineRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy {

   public LineRenderingStrategy(final StyleDescription.StyleType styleType,
                                final Double highlightLineWidth) {
      super(styleType, highlightLineWidth);
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
      drawing.drawCircle(x, y, DOT_RADIUS);
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
      drawing.drawLineSegment(prevX, prevY, x, y);
   }
}
