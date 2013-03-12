package org.bodytrack.client;

/**
 * A vertical axis for photos.
 *
 * <p>Logically, there is a scale, just as with any other
 * <tt>GraphAxis</tt>, and in this case the scale ranges from
 * <tt>INITIAL_MIN</tt> to <tt>INITIAL_MAX</tt>.  In this
 * axis, though, only part of the axis is drawn - this is the
 * portion of the axis, centered at <tt>PHOTO_CENTER_LOCATION</tt>
 * that takes up logical height <tt>PHOTO_HEIGHT</tt>.</p>
 */
public class PhotoGraphAxis extends GraphAxis {
   private static final double INITIAL_MIN = 0.0;
   private static final double INITIAL_MAX = 1.0;
   private static final Basis VERTICAL_AXIS_BASIS = Basis.xRightYUp;

   // We draw ticks to take up TICK_WIDTH_FACTOR, as a proportion
   // of total width
   private static final double TICK_WIDTH_FACTOR = 0.5;

   /**
    * The logical Y-value at which the center of a photo should be
    * placed.  Note that a call to {@link #project2D(double)} is
    * required to get the actual value in pixels where the center
    * should be drawn.
    */
   public static final double PHOTO_CENTER_LOCATION = 0.8;

   /**
    * The logical height for a photo.  Note that calls to
    * {@link #project2D(double)} are required to get the actual
    * locations of the top and bottom of the image.  Also note
    * that this is for the original photo, not for an expanded
    * size photo that the user is mousing over.
    */
   public static final double PHOTO_HEIGHT = 0.35;

   private int previousPaintEventId = 0;

   public PhotoGraphAxis(final String divName,
                         final double min,
                         final double max,
                         final Basis basis,
                         final double width,
                         final boolean isXAxis) {
      super(divName, INITIAL_MIN, INITIAL_MAX, VERTICAL_AXIS_BASIS, width, false);
   }

   /**
    * Returns the Y-value at which the center of a photo should be
    * drawn.
    *
    * @return
    * 		the Y-value at which the center of a photo should be drawn,
    * 		in pixels
    */
   public double projectPhotoCenter() {
      return projectY(PHOTO_CENTER_LOCATION);
   }

   /**
    * Returns the height of a photo in pixels.
    *
    * @return
    * 		the photo height, in pixels
    */
   public double projectPhotoHeight() {
      // Note that we count the stretched height of the axis as
      // project(min) - project(max).  This is because the min actually
      // has a greater Y-value than the max, since drawing starts from
      // the top left corner
      return PHOTO_HEIGHT * (projectY(INITIAL_MIN) - projectY(INITIAL_MAX));
   }

   /**
    * Returns the value, in pixels, at which the specified logical
    * Y-value should be drawn.
    *
    * @param value
    * 		the Y-value to change from logical to pixels
    * @return
    * 		the Y-axis location, in pixels, at which value should be
    * 		drawn
    */
   private double projectY(final double value) {
      return project2D(value).getY();
   }

   /**
    * Draws this axis.
    */
   @Override
   public void paint(final int newPaintEventId) {
      // guard against redundant paints
      if (previousPaintEventId != newPaintEventId) {
         previousPaintEventId = newPaintEventId;

         final GrapherCanvas canvas = getDrawingCanvas();
         if (canvas == null) {
            return;
         }

         canvas.clear();

         // Pick the color to use, based on highlighting status
         if (isHighlighted()) {
            canvas.setStrokeStyle(HIGHLIGHTED_COLOR);
         } else {
            canvas.setStrokeStyle(NORMAL_COLOR);
         }

         final double x = project2D(INITIAL_MIN).getX();
         final double width = getWidth();

         // Now figure out where the line should go, drawing ticks
         // as we go along
         double topValue = PHOTO_CENTER_LOCATION + PHOTO_HEIGHT / 2.0;
         double bottomValue = PHOTO_CENTER_LOCATION - PHOTO_HEIGHT / 2.0;

         // Don't draw anything at all if the line is out of bounds
         if (bottomValue > getMax() || topValue < getMin()) {
            return;
         }

         // Allow ourselves to begin drawing
         canvas.beginPath();

         if (topValue > getMax()) {
            topValue = getMax();
         } else {
            drawTick(canvas, topValue, x, width);
         }

         if (bottomValue < getMin()) {
            bottomValue = getMin();
         } else {
            drawTick(canvas, bottomValue, x, width);
         }

         // Get the values in pixels for where the line should go
         final double topY = projectY(topValue);
         final double bottomY = projectY(bottomValue);

         // Now draw the vertical line
         canvas.drawLineSegment(x, topY, x, bottomY);

         // Actually render all our ticks and lines on the canvas
         canvas.stroke();

         // Clean up after ourselves
         canvas.setStrokeStyle(GrapherCanvas.DEFAULT_COLOR);
      }
   }

   /**
    * Draws a single tick at the specified location.
    *
    * <p>Draws a tick of width {@code width * TICK_WIDTH_FACTOR}
    * with its left edge at the specified X-value, and at Y-value
    * determined by {@code projectY(value)}.  If <tt>value</tt> is
    * out of range, though (the range is determined by
    * {@linkplain GraphAxis#getMin()} and {@linkplain GraphAxis#getMax()}),
    * this method draws nothing.</p>
    *
    * <p>Note that this method does not call {@code canvas.beginPath()}
    * or {@code canvas.stroke()}.  It is up to a calling method to
    * call {@code canvas.beginPath()} before calling this method,
    * and to call {@code canvas.stroke()} after calling this method.</p>
    *
    * @param canvas
    * 		a {@link GrapherCanvas} on which the tick will be drawn
    * @param value
    * 		the location in which to draw the tick, specified
    * 		not in pixels but in terms of the
    * 		(INITIAL_MIN_VALUE, INITIAL_MAX_VALUE) value
    * 		scale this class keeps in place
    * @param x
    * 		the X-coordinate for the left edge of the tick
    * @param width
    * 		the width of this axis, <em>NOT</em> the width of
    * 		the tick
    */
   private void drawTick(final GrapherCanvas canvas,
                         final double value,
                         final double x,
                         final double width) {
      // Don't draw anything that is out of bounds
      if (getMin() > value || getMax() < value) {
         return;
      }

      final double y = projectY(value);

      final double tickWidth = TICK_WIDTH_FACTOR * width;
      final double xRight = x + tickWidth;

      canvas.drawLineSegment(x, y, xRight, y);
   }
}
