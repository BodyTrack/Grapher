package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;

/**
 * <p>
 * <code>LollipopDataPlot</code> renders data points as "lollipops", which
 * are dots with a line from the min y value to the data point.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class LollipopDataPlot extends DotDataPlot {

   public LollipopDataPlot(final JavaScriptObject container,
                      final JavaScriptObject xAxis,
                      final JavaScriptObject yAxis,
                      final Channel channel,
                      final int minLevel,
                      final Color color) {
      super(container, xAxis, yAxis, channel, minLevel, color);
   }

   @Override
   protected void paintEdgePoint(final BoundedDrawingBox drawing,
                                 final GrapherTile tile,
                                 final double x,
                                 final double y,
                                 final PlottablePoint rawDataPoint) {

      super.paintEdgePoint(drawing, tile, x, y, rawDataPoint);

      final Canvas canvas = drawing.getCanvas();
      final Surface surface = canvas.getSurface();
      final DirectShapeRenderer renderer = canvas.getRenderer();
      final GraphAxis yAxis = getYAxis();

      surface.setFillStyle(getColor());

      // The Y-value in pixels corresponding to the lowest point to draw on the rectangle
      final double minDrawY = yAxis.project2D(0).getY();

      // Draw a line
      final double oldLineWidth = surface.getLineWidth();
      surface.setLineWidth(isHighlighted()
         ? AbstractPlotRenderer.HIGHLIGHT_STROKE_WIDTH
         : AbstractPlotRenderer.NORMAL_STROKE_WIDTH);

      renderer.beginPath();
      renderer.moveTo(x, minDrawY);
      renderer.drawLineTo(x, y);
      renderer.closePath();

      renderer.stroke();
      surface.setLineWidth(oldLineWidth);
   }
}
