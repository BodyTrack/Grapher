package org.bodytrack.client;

public class LollipopRenderingStrategy extends DotRenderingStrategy {
   public LollipopRenderingStrategy(final StyleDescription.StyleType styleType,
                                    final boolean willShowComments) {
      super(styleType, willShowComments);
   }

   @Override
   protected void paintDot(final BoundedDrawingBox drawing,
                           final GraphAxis xAxis,
                           final GraphAxis yAxis,
                           final boolean isAnyPointHighlighted,
                           final double x,
                           final double y,
                           final PlottablePoint rawDataPoint) {
      super.paintDot(drawing, xAxis, yAxis, isAnyPointHighlighted, x, y, rawDataPoint);

      final Canvas canvas = drawing.getCanvas();

      // The Y-value in pixels corresponding to the lowest point to draw on the rectangle
      final double minDrawY = yAxis.project2D(0).getY();

      // Draw a line
      final double oldLineWidth = canvas.getLineWidth();
      canvas.setLineWidth(isAnyPointHighlighted
                          ? HIGHLIGHT_STROKE_WIDTH
                          : getLineWidth());

      canvas.beginPath()
         .moveTo(x, minDrawY)
         .drawLineTo(x, y)
         .closePath();

      canvas.stroke();
      canvas.setLineWidth(oldLineWidth);
   }
}
