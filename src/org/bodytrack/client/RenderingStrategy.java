package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface RenderingStrategy {
   /** The width at which a normal line is drawn. */
   int DEFAULT_STROKE_WIDTH = 1;

   /** Default {@link Color} for lines and strokes. */
   Color DEFAULT_STROKE_COLOR = GrapherCanvas.DEFAULT_COLOR;

   /** Default {@link Color} for filled areas. */
   Color DEFAULT_FILL_COLOR = GrapherCanvas.DEFAULT_COLOR;

   /**
    * Called immediately before rendering begins, to allow implementations to prepare for rendering.
    *
    * @param canvas
    *    The canvas upon which rendering will take place.
    * @param drawing
    *    The drawing upon which rendering took place.
    * @param isAnyPointHighlighted
    *    Whether any point is currently highlighted.
    */
   void beforeRender(GrapherCanvas canvas, BoundedDrawingBox drawing, boolean isAnyPointHighlighted);

   /**
    * Called immediately after rendering of a style ends, to allow implementations to clean up after rendering.
    *
    * @param canvas
    *    The canvas upon which rendering took place.
    * @param drawing
    *    The drawing upon which rendering took place.
    */
   void afterRender(GrapherCanvas canvas, BoundedDrawingBox drawing);
}