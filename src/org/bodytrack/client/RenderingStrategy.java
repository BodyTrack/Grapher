package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface RenderingStrategy {
   /** The width at which a normal line is drawn. */
   int DEFAULT_STROKE_WIDTH = 1;

   /** Default {@link Color} for lines and strokes. */
   Color DEFAULT_STROKE_COLOR = Canvas.BLACK;

   /** Default {@link Color} for filled areas. */
   Color DEFAULT_FILL_COLOR = Canvas.BLACK;

   /**
    * Called immediately before rendering begins, to allow implementations to prepare for rendering.
    *
    * @param canvas
    *    The canvas upon which rendering will take place.
    * @param isAnyPointHighlighted
    *    whether any point is currently highlighted
    */
   void beforeRender(Canvas canvas, boolean isAnyPointHighlighted);

   /**
    * Called immediately after rendering of a style ends, to allow implementations to clean up after rendering.
    *
    * @param canvas
    *    The canvas upon which rendering took place.
    */
   void afterRender(Canvas canvas);
}