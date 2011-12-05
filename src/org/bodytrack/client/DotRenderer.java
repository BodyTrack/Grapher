package org.bodytrack.client;

public class DotRenderer extends AbstractPlotRenderer {
   private static final int DOT_RADIUS = 3;

   public DotRenderer(final boolean drawComments) {
      super(drawComments);
   }

   protected void paintDot(final BoundedDrawingBox drawing,
                           final double x,
                           final double y,
                           final boolean isHighlighted,
                           final PlottablePoint rawDataPoint) {
      if (drawing.contains(x, y)) {

         final boolean willPaintLargerDot = isHighlighted ||
                                            (isDrawingComments() &&
                                             rawDataPoint != null &&
                                             rawDataPoint.hasComment());

         drawing.drawFilledDot(x, y, willPaintLargerDot ? HIGHLIGHTED_DOT_RADIUS : DOT_RADIUS);
      }
   }

   @Override
   protected final void paintDataPoint(final BoundedDrawingBox drawing,
                                       final double prevX,
                                       final double prevY,
                                       final double x,
                                       final double y,
                                       final PlottablePoint rawDataPoint) {
      paintDot(drawing, x, y, false, rawDataPoint);
   }

   @Override
   protected final void paintEdgePoint(final BoundedDrawingBox drawing,
                                       final GrapherTile tile,
                                       final double x,
                                       final double y,
                                       final PlottablePoint rawDataPoint) {
      paintDot(drawing, x, y, false, rawDataPoint);
   }

   @Override
   protected final void paintHighlightedPoint(final BoundedDrawingBox drawing,
                                              final double x,
                                              final double y) {
      paintDot(drawing, x, y, true, null);
   }
}
