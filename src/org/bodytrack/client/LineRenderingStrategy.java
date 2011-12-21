package org.bodytrack.client;

public class LineRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy {

   public LineRenderingStrategy(final StyleDescription.StyleType styleType,
                                final boolean willShowComments) {
      super(styleType, willShowComments);
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
      drawing.drawDot(x, y, DOT_RADIUS);

      drawCommentDot(drawing, x, y, rawDataPoint);
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

      drawCommentDot(drawing, x, y, rawDataPoint);
   }

   private void drawCommentDot(final BoundedDrawingBox drawing,
                               final double x,
                               final double y,
                               final PlottablePoint rawDataPoint) {
      if (willShowComments() && rawDataPoint.hasComment()) {
         drawing.drawFilledDot(x, y, HIGHLIGHTED_DOT_RADIUS);
      }
   }
}
