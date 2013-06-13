package org.bodytrack.client;


/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SeriesPlotRenderingStrategy extends RenderingStrategy {

   /** The radius to use when drawing a dot on the grapher. */
   double DOT_RADIUS = 0.5;

   /**
    * Paints a left edge point for a segment of the plot
    *
    * <p>Note that this is only called for the left edge of a plot segment.
    * This particular implementation draws a small dot, although a subclass
    * implementation does not have to do the same.  Note that all x and y
    * values are assumed to be in terms of pixels, not logical values on the
    * axes.</p>
    *
    * @param drawing
    * 	The {@link BoundedDrawingBox} that should constrain the drawing.
    * 	Forwarding graphics calls through drawing will ensure that everything
    * 	draws up to the edge of the viewing window but no farther
    * @param tile
    * 	The tile from which the data point to be drawn was obtained
    * @param xAxis the xAxis
    * @param yAxis the yAxis
    * @param isAnyPointHighlighted whether any point is currently highlighted
    * @param x
    * 	The X-coordinate of the point to draw
    * @param y
    * 	The Y-coordinate of the point to draw
    * @param rawDataPoint
    * 	The raw {@link PlottablePoint}
    */
   void paintEdgePoint(BoundedDrawingBox drawing,
                       GrapherTile tile,
                       GraphAxis xAxis,
                       GraphAxis yAxis,
                       PlottablePoint highlightedPoint,
                       double x,
                       double y,
                       PlottablePoint rawDataPoint);

   /**
    * Draws a single data point on the graph.
    *
    * <p>Note that this method has as a precondition that {@code prevX < x}.
    * Note that all x and y values are assumed to be in terms of pixels.</p>
    *
    *
    * @param drawing
    * 	The {@link BoundedDrawingBox} that should constrain the drawing.
    * 	Forwarding graphics calls through drawing will ensure that everything
    * 	draws up to the edge of the viewing window but no farther
    * @param tile
    * 	The tile from which the data point to be drawn was obtained
    * @param xAxis the xAxis
    * @param yAxis the yAxis
    * @param isAnyPointHighlighted whether any point is currently highlighted
    * @param prevX
    * 	The previous X-value, which is expected to be greater than
    * 	MIN_DRAWABLE_VALUE
    * @param prevY
    * 	The previous Y-value, which is expected to be greater than
    * 	MIN_DRAWABLE_VALUE
    * @param x
    * 	The current X-value, which is expected to be greater than
    * 	MIN_DRAWABLE_VALUE, and greater than or equal to prevX
    * @param y
    * 	The current Y-value, which is expected to be greater than
    * 	MIN_DRAWABLE_VALUE
    * @param rawDataPoint
    * 	The raw {@link PlottablePoint}
    * @see SeriesPlotRenderer#MIN_DRAWABLE_VALUE
    */
   void paintDataPoint(BoundedDrawingBox drawing,
                       GrapherTile tile,
                       GraphAxis xAxis,
                       GraphAxis yAxis,
                       PlottablePoint highlightedPoint,
                       double prevX,
                       double prevY,
                       double x,
                       double y,
                       PlottablePoint rawDataPoint);
}