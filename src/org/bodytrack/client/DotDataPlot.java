package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.shapes.CircleShape;
import gwt.g2d.client.math.Vector2;

/**
 * <p>
 * <code>DotDataPlot</code> renders data points as dots which are not connected by lines.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class DotDataPlot extends DataPlot {

   private static final int DOT_RADIUS = 3;

   public DotDataPlot(final JavaScriptObject datasource,
                      final JavaScriptObject nativeXAxis,
                      final JavaScriptObject nativeYAxis,
                      final Channel channel,
                      final int minLevel,
                      final Color color) {
      super(datasource, nativeXAxis, nativeYAxis, channel, minLevel, color);
   }

   @Override
   protected void paintEdgePoint(final BoundedDrawingBox drawing,
                                 final GrapherTile tile,
                                 final double x,
                                 final double y,
                                 final PlottablePoint rawDataPoint) {
      final Surface surface = drawing.getCanvas().getSurface();
      surface.setFillStyle(getColor());
      surface.fillShape(new CircleShape(new Vector2(x, y),
         rawDataPoint.hasComment() ? HIGHLIGHTED_DOT_RADIUS : DOT_RADIUS));
   }

   @Override
   protected void paintDataPoint(final BoundedDrawingBox drawing,
                                 final GrapherTile tile,
                                 final double prevX,
                                 final double prevY,
                                 final double x,
                                 final double y,
                                 final PlottablePoint rawDataPoint) {
      paintEdgePoint(drawing, tile, x, y, rawDataPoint);
   }
}
