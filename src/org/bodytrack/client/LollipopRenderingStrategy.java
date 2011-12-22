package org.bodytrack.client;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

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
      final Surface surface = canvas.getSurface();
      final DirectShapeRenderer renderer = canvas.getRenderer();

      // The Y-value in pixels corresponding to the lowest point to draw on the rectangle
      final double minDrawY = yAxis.project2D(0).getY();

      // Draw a line
      final double oldLineWidth = surface.getLineWidth();
      surface.setLineWidth(isAnyPointHighlighted
                           ? HIGHLIGHT_STROKE_WIDTH
                           : getLineWidth());

      renderer.beginPath();
      renderer.moveTo(x, minDrawY);
      renderer.drawLineTo(x, y);
      renderer.closePath();

      renderer.stroke();
      surface.setLineWidth(oldLineWidth);
   }
}
