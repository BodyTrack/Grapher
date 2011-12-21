package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SeriesPlotRenderingStrategy {

   /** The width at which a normal line is drawn. */
   int NORMAL_STROKE_WIDTH = 1;

   /** The width at which a highlighted line is drawn. */
   int HIGHLIGHT_STROKE_WIDTH = 3;

   /** The radius to use when drawing a dot on the grapher. */
   double DOT_RADIUS = 0.5;

   /** The radius to use when drawing a highlighted dot on the grapher. */
   double HIGHLIGHTED_DOT_RADIUS = 4;

   /** Default {@link Color} for lines and strokes. */
   Color DEFAULT_STROKE_COLOR = Canvas.BLACK;

   /** Default {@link Color} for filled areas. */
   Color DEFAULT_FILL_COLOR = Canvas.BLACK;

   /**
    * Called by {@link SeriesPlotRenderer#render(Canvas, BoundedDrawingBox, Iterable, GraphAxis, GraphAxis, PlottablePoint)}
    * immediately before rendering of a style begins, to allow implementations to prepare for rendering.
    *
    * @param canvas
    *    The canvas upon which rendering will take place.
    * @param isAnyPointHighlighted
    *    whether any point is currently highlighted
    */
   void beforeRender(Canvas canvas, boolean isAnyPointHighlighted);

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
                       boolean isAnyPointHighlighted,
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
    *      @see SeriesPlotRenderer#MIN_DRAWABLE_VALUE
    */
   void paintDataPoint(BoundedDrawingBox drawing,
                       GrapherTile tile,
                       GraphAxis xAxis,
                       GraphAxis yAxis,
                       boolean isAnyPointHighlighted,
                       double prevX,
                       double prevY,
                       double x,
                       double y,
                       PlottablePoint rawDataPoint);

   /**
    * Called by {@link SeriesPlotRenderer#render(Canvas, BoundedDrawingBox, Iterable, GraphAxis, GraphAxis, PlottablePoint)}
    * immediately after rendering of a style begins, to allow implementations to clean up after rendering.
    *
    * @param canvas
    *    The canvas upon which rendering will take place.
    */
   void afterRender(Canvas canvas);
}