package org.bodytrack.client;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface PointRenderingStrategy extends RenderingStrategy {
   /**
    * Paints a point.
    *
    * @param drawing
    * 	The {@link BoundedDrawingBox BoundedDrawingBox} that should constrain
    * 	the drawing.  Forwarding graphics calls through drawing will ensure
    * 	that everything draws up to the edge of the viewing window but no
    * 	farther
    * @param xAxis the xAxis
    * @param yAxis the yAxis
    * @param x
    * 	The X-position of the point to draw, in screen pixels
    * @param y
    * 	The Y-position of the point to draw, in screen pixels
    * @param rawDataPoint
    * 	The raw {@link PlottablePoint}
    */
   void paintPoint(final BoundedDrawingBox drawing,
                   final GraphAxis xAxis,
                   final GraphAxis yAxis,
                   final double x,
                   final double y,
                   final PlottablePoint rawDataPoint);
}