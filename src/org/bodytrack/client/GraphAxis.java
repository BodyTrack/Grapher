package org.bodytrack.client;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.RootPanel;
import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.TextAlign;
import gwt.g2d.client.graphics.TextBaseline;
import gwt.g2d.client.math.Vector2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class GraphAxis {

   public interface EventListener {
      void onAxisChange(final String eventId);
   }

   /**
    * A point that signals to the {@link #highlight(PlottablePoint)}
    * method that there should be no visible highlighted point, just
    * a darkening of the axis
    */
   public static final PlottablePoint DARKEN_AXIS_ONLY = new PlottablePoint(Long.MIN_VALUE, Long.MIN_VALUE);

   protected static final Color NORMAL_COLOR = Canvas.DARK_GRAY;
   protected static final Color HIGHLIGHTED_COLOR = Canvas.BLACK;
   protected static final Color HIGHLIGHTED_POINT_COLOR = Canvas.RED;
   protected static final double HIGHLIGHTED_POINT_LINE_WIDTH = 3;
   protected static final double HIGHLIGHTED_POINT_LINE_LENGTH = 15;

   static final int JUSTIFY_MIN = 0;
   static final int JUSTIFY_MED = 1;
   static final int JUSTIFY_MAX = 2;

   public double majorTickMinSpacingPixels = 50;
   public double majorTickWidthPixels = 8;

   public double minorTickMinSpacingPixels = 10;
   public double minorTickWidthPixels = 3;

   protected double min;
   protected double max;

   protected boolean hasMinRange = false;
   protected double minRange = -1e+100;

   protected boolean hasMaxRange = false;
   protected double maxRange = 1e+100;

   private final Canvas drawingCanvas;
   protected Basis basis;
   private Vector2 begin;
   private double width;
   protected double length;
   private double scale;
   private BBox bounds;

   private final boolean isXAxis;

   // For determining whether to highlight this GraphAxis
   private PlottablePoint highlightedPoint; // null if this isn't highlighted

   private final String divName;

   private final Set<EventListener> eventListeners = new HashSet<EventListener>();

   private Vector2 mouseDragLastPos;

   private String previousPaintEventId = null;

   public GraphAxis(final String divName,
                    final double min,
                    final double max,
                    final Basis basis,
                    final double width,
                    final boolean isXAxis) {
      this.divName = divName;
      if (basis == null) {
         throw new NullPointerException("Null basis");
      }
      if (min > max) {
         throw new IllegalArgumentException("Axis min is greater than max");
      }

      if (divName != null) {
         final RootPanel div = RootPanel.get(divName);
         final Surface drawingSurface = new Surface(div.getOffsetWidth(), div.getOffsetHeight());
         div.add(drawingSurface);

         drawingSurface.addMouseWheelHandler(new BaseMouseWheelHandler() {
            @Override
            protected void handleMouseWheelEvent(final MouseWheelEvent event) {
               final Vector2 pos = new Vector2(event.getX(), event.getY());
               final double zoomFactor = Math.pow(getMouseWheelZoomRate(), event.getDeltaY());

               zoom(zoomFactor, unproject(pos), UUID.uuid());
            }
         });

         drawingSurface.addMouseDownHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(final MouseDownEvent event) {
               mouseDragLastPos = new Vector2(event.getX(), event.getY());
            }
         });

         drawingSurface.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
               // ignore mouse moves if the mouse button isn't being held down
               if (mouseDragLastPos != null) {
                  final Vector2 pos = new Vector2(event.getX(), event.getY());
                  drag(mouseDragLastPos, pos, UUID.uuid());
                  mouseDragLastPos = pos;
               }
            }
         });

         drawingSurface.addMouseUpHandler(new MouseUpHandler() {
            @Override
            public void onMouseUp(final MouseUpEvent event) {
               mouseDragLastPos = null;
            }
         });

         drawingSurface.addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(final MouseOutEvent event) {
               mouseDragLastPos = null;

               paint(UUID.uuid());
            }
         });

         drawingCanvas = Canvas.buildCanvas(drawingSurface);
      } else {
         drawingCanvas = null;
      }

      this.min = min;
      this.max = max;
      this.basis = basis;
      this.width = width;
      this.isXAxis = isXAxis;

      highlightedPoint = null;
   }

   public final void addEventListener(final EventListener listener) {
      if (listener != null) {
         eventListeners.add(listener);
      }
   }

   public final void removeEventListener(final EventListener listener) {
      if (listener != null) {
         eventListeners.remove(listener);
      }
   }

   public void layout() {
      if (drawingCanvas == null) {
         return;
      }

      final double axisLength;
      final Vector2 beginVector;
      if (isXAxis) {
         axisLength = (double)drawingCanvas.getSurface().getWidth();
         beginVector = Vector2.ZERO;
      } else {
         final double elementHeight = drawingCanvas.getSurface().getHeight();
         axisLength = elementHeight;
         beginVector = new Vector2(0, elementHeight);
      }

      layout(beginVector, axisLength);
   }

   private void layout(final Vector2 begin, final double length) {
      this.begin = begin;
      this.length = length;
      rescale();
   }

   private void rescale() {
      this.scale = length / (this.max - this.min);
      final Vector2 vWidth = new Vector2(basis.x.scale(this.width));
      final Vector2 vLength = new Vector2(basis.y.scale(this.length));
      final Vector2 end = this.begin.add(vWidth).add(vLength);
      final Vector2 boundsMin = new Vector2(Math.min(begin.getX(), end.getX()),
                                            Math.min(begin.getY(), end.getY()));
      final Vector2 boundsMax = new Vector2(Math.max(begin.getX(), end.getX()),
                                            Math.max(begin.getY(), end.getY()));
      bounds = new BBox(boundsMin, boundsMax);
   }

   private double project1D(final double value) {
      return (value - this.min) * scale;
   }

   /**
    * Returns a Vector2 with the correct X- or Y-coordinate (X-coordinate
    * if this is an X-axis, Y-coordinate if this is a Y-axis) for
    * drawing a point.
    *
    * @param value
    * 		the value the user sees on the axis
    * @return
    * 		a {@link Vector2 Vector2} with one
    * 		coordinate correct for drawing the specified value relative
    * 		to this axis
    */
   public Vector2 project2D(double value) {
      return begin.add(basis.y.scale(project1D(value)));
   }

   public double unproject(Vector2 point) {
      return this.min + (point.subtract(begin).dot(basis.y) / scale);
   }

   /**
    * Marks this GraphAxis as highlighted.
    *
    * @param point
    * 		the point to highlight.  If this is <tt>null</tt>, the
    * 		result is the same as calling {@link #unhighlight()}.
    * 		Also, if the special value {@link #DARKEN_AXIS_ONLY}
    * 		is passed in as point, no line is drawn, but the
    * 		axis is still darkened
    */
   public void highlight(PlottablePoint point) {
      highlightedPoint = point;
   }

   /**
    * Marks this GraphAxis as not highlighted.
    */
   public void unhighlight() {
      highlightedPoint = null;
   }

   /**
    * Returns <tt>true</tt> if and only if this GraphAxis is marked as
    * highlighted.
    *
    * @return
    * 		<tt>true</tt> if and only if {@link #highlight()} has been
    * 		called since this GraphAxis was constructed, and
    * 		{@link #unhighlight()} has not been called since the last
    * 		call to highlight
    */
   public boolean isHighlighted() {
      return highlightedPoint != null;
   }

   /**
    * Returns the most recently set highlighted point, as set in
    * the {@link #highlight(PlottablePoint) highlight} method.
    *
    * <p>This is intended for subclass use only.  However, if a
    * subclass overrides this, it should also override
    * {@link #highlight(PlottablePoint)} and {@link #isHighlighted()}.</p>
    *
    * @return
    * 		the highlighted point for this axis
    */
   protected PlottablePoint getHighlightedPoint() {
      return highlightedPoint;
   }

   public void paint(final String newPaintEventId) {
      // guard against redundant paints
      if (previousPaintEventId == null || !previousPaintEventId.equals(newPaintEventId)) {
         previousPaintEventId = newPaintEventId;

         if (drawingCanvas == null) {
            return;
         }

         drawingCanvas.getSurface().clear();

         // Pick the color to use, based on highlighting status
         if (isHighlighted()) {
            drawingCanvas.getSurface().setStrokeStyle(HIGHLIGHTED_COLOR);
         } else {
            drawingCanvas.getSurface().setStrokeStyle(NORMAL_COLOR);
         }

         drawingCanvas.getRenderer().beginPath();
         drawingCanvas.getRenderer().drawLineSegment(project2D(this.min), project2D(this.max));

         final double majorTickSize = computeTickSize(majorTickMinSpacingPixels);
         renderTicks(0, majorTickSize, null, drawingCanvas, majorTickWidthPixels, new DefaultLabelFormatter());
         //renderTickLabels(surface, majorTickSize, majorTickWidthPixels+3);

         final double minorTickSize = computeTickSize(minorTickMinSpacingPixels);
         renderTicks(0, minorTickSize, null, drawingCanvas, minorTickWidthPixels, null);

         drawingCanvas.getRenderer().stroke();

         renderHighlight(drawingCanvas, highlightedPoint);

         // Clean up after ourselves
         drawingCanvas.getSurface().setStrokeStyle(Canvas.DEFAULT_COLOR);
      }
   }

   /**
    * Draws a colored line for point, if that is on this axis.
    *
    * <p>Note that this does absolutely nothing unless this
    * axis is highlighted.</p>
    *
    * <p>This is designed to be overridden by subclasses.</p>
    *
    * @param canvas
    * 		the {@link org.bodytrack.client.Canvas Canvas} we can
    * 		use to perform all rendering operations
    * @param point
    * 		the point to render on the axis
    */
   protected void renderHighlight(Canvas canvas,
                                  PlottablePoint point) {
      if (!isHighlighted()) {
         return;
      }

      if (point.equals(DARKEN_AXIS_ONLY)) {
         return;
      }

      DirectShapeRenderer renderer = canvas.getRenderer();

      canvas.getSurface().save();
      double oldLineWidth = canvas.getSurface().getLineWidth();
      canvas.getSurface().setLineWidth(HIGHLIGHTED_POINT_LINE_WIDTH);
      canvas.getSurface().setStrokeStyle(HIGHLIGHTED_POINT_COLOR);

      renderer.beginPath();

      if (isXAxis) {
         double time = point.getDate();

         if (time >= min && time <= max) {
            // top of the line to draw
            Vector2 top = project2D(time);
            renderer.drawLineSegment(top,
                                     new Vector2(top.getX(),
                                                 top.getY() + HIGHLIGHTED_POINT_LINE_LENGTH));
         }
      } else {
         double value = point.getValue();
         if (value >= min && value <= max) {
            // left edge of the line to draw
            Vector2 left = project2D(value);
            double size = Math.min(HIGHLIGHTED_POINT_LINE_LENGTH,
                                   getWidth());
            renderer.drawLineSegment(left,
                                     new Vector2(left.getX() + size, left.getY()));
         }
      }

      renderer.stroke();

      // Clean up after ourselves
      canvas.getSurface().restore();
      canvas.getSurface().setLineWidth(oldLineWidth);
   }

   public double getMin() {
      return min;
   }

   public double getMax() {
      return max;
   }

   public boolean isXAxis() {
      return isXAxis;
   }

   static abstract class LabelFormatter {
      abstract String format(double value);
   }

   static class DefaultLabelFormatter extends LabelFormatter {
      String format(double value) {
         String label = NumberFormat.getDecimalFormat().format(value);

         if (label.equals("-0")) {
            label = "0";
         }

         return label;
      }
   }

   static class TickGenerator {
      private double tickSize;
      private double offset;
      private double currentTick = 0.0;

      TickGenerator(double tickSize, double offset) {
         this.tickSize = tickSize;
         this.offset = offset;
      }

      double nextTick(double min) {
         currentTick = closestTick(min - tickSize);

         while (currentTick < min) {
            advanceTick();
         }

         return currentTick;
      }

      double nextTick() {
         advanceTick();
         return currentTick;
      }

      void advanceTick() {
         currentTick = closestTick(currentTick + tickSize);
      }

      double closestTick(double val) {
         return Math.round((val - offset) / tickSize) * tickSize
                + offset;
      }
   }

   /**
    * Wrapper class for TickGenerator that allows iteration.
    */
   static class IterableTickGenerator implements Iterable<Double> {
      private TickGenerator gen;
      private double maxValue;
      private double minValue;

      /**
       * Builds a new IterableTickGenerator wrapping the specified
       * TickGenerator.
       *
       * @param gen
       * 		the TickGenerator this IterableTickGenerator should wrap
       * @param minValue
       * 		the minimum (starting) value of a tick
       * @param maxValue
       * 		the maximum value of a tick this should return in
       * 		iteration
       * @throws NullPointerException
       * 		if gen is <tt>null</tt>
       */
      public IterableTickGenerator(TickGenerator gen, double minValue,
                                   double maxValue) {
         if (gen == null) {
            throw new NullPointerException(
                  "TickGenerator cannot be null");
         }

         this.gen = gen;
         this.minValue = minValue;
         this.maxValue = maxValue;
      }

      @Override
      public Iterator<Double> iterator() {
         return new TickGeneratorIterator();
      }

      private class TickGeneratorIterator implements Iterator<Double> {
         private double currTick;
         private double nextTick;

         /**
          * Primes the ticks to return.
          */
         public TickGeneratorIterator() {
            currTick = 0.0;
            nextTick = gen.nextTick(minValue);
         }

         /**
          * Returns <tt>true</tt> iff the next tick returned from
          * {@link #next()} would be less than or equal to maxValue
          * (which was passed into the IterableTickGenerator constructor).
          *
          * @return
          * 		<tt>true</tt> iff the next tick would be
          * 		less than or equal to maxValue (which was passed
          * 		into the IterableTickGenerator constructor)
          */
         @Override
         public boolean hasNext() {
            return nextTick <= maxValue;
         }

         /**
          * Returns the next tick.
          *
          * @throws NoSuchElementException
          * 		if there are no more ticks available (users of this class
          * 		can test for this by calling
          * 		{@link TickGeneratorIterator#hasNext() hasNext()}
          * @return
          * 		a Double obtained by calling nextTick() on the
          * 		TickGenerator specified in the constructor to this
          * 		IterableTickGenerator
          */
         @Override
         public Double next() {
            if (!hasNext()) {
               throw new NoSuchElementException("No more ticks to show");
            }

            currTick = nextTick;

            nextTick = gen.nextTick();

            return currTick;
         }

         /**
          * Always throws a
          * {@linkplain java.lang.UnsupportedOperationException}
          *
          * @throws UnsupportedOperationException
          * 		always
          */
         @Override
         public void remove() {
            throw new UnsupportedOperationException(
                  "Cannot remove from this iterator");
         }
      }
   }

   protected double setupText(Surface surface, int justify) {
      boolean textParallelToAxis =
            (Math.abs(basis.x.getX()) < Math.abs(basis.x.getY()));
      double labelOffsetPixels = 0;

      if (textParallelToAxis) {
         final TextAlign[] align =
               {TextAlign.LEFT, TextAlign.CENTER, TextAlign.RIGHT};

         surface.setTextAlign(align[justify]);
         surface.setTextBaseline(TextBaseline.TOP);
      } else {
         surface.setTextAlign(TextAlign.LEFT);

         final TextBaseline[] baseline =
               {TextBaseline.BOTTOM, TextBaseline.MIDDLE, TextBaseline.TOP};

         surface.setTextBaseline(baseline[justify]);
         labelOffsetPixels = 3;
      }

      return labelOffsetPixels;
   }

	protected void renderTicks(double offsetPixels,
			double tickSize,
			TickGenerator tickGen,
			Canvas canvas,
			double tickWidthPixels,
			LabelFormatter formatter) {
		if (tickGen == null) {
			tickGen = new TickGenerator(tickSize, 0);
		}

		double labelOffsetPixels = formatter == null ? 0
				: setupText(canvas.getSurface(), JUSTIFY_MED)
				+ offsetPixels + tickWidthPixels;

		IterableTickGenerator gen =
			new IterableTickGenerator(tickGen, this.min, this.max);

		for (double tick : gen) {
			renderTick(canvas.getRenderer(), tick,
					offsetPixels + tickWidthPixels);

			if (formatter != null) {
				renderTickLabel(canvas.getSurface(), tick,
						labelOffsetPixels, formatter);
			}
		}
	}

   protected void renderTicksRangeLabelInline(double offsetPixels,
                                              double tickSize, TickGenerator tickGen, Canvas canvas,
                                              double tickWidthPixels, LabelFormatter formatter) {
      Surface surface = canvas.getSurface();
      DirectShapeRenderer renderer = canvas.getRenderer();

      if (tickGen == null) {
         tickGen = new TickGenerator(tickSize, 0);
      }

      double labelOffsetPixels = setupText(surface, JUSTIFY_MED)
                                 + offsetPixels;

      double tick = tickGen.nextTick(this.min);

      if (tick > this.max) {
         // No ticks are visible
         // Draw one inline label in the middle
         renderTickLabel(surface, (this.min + this.max) / 2.0,
                         labelOffsetPixels, formatter);
         return;
      }

      int pixelMargin = 100;  // TODO: fix this
      if (project1D(tick) >= pixelMargin) {
         // Draw label for before first tick, justified to the minimum
         // of axis (left or bottom)
         setupText(surface, JUSTIFY_MIN);
         renderTickLabel(surface, this.min, labelOffsetPixels, formatter);
         setupText(surface, JUSTIFY_MED);
      }

      while (true) {
         renderTick(renderer, tick, tickWidthPixels + offsetPixels);
         double nextTick = tickGen.nextTick();

         if (nextTick > this.max) {
            break;
         }

         renderTickLabel(surface, (tick + nextTick) / 2.0,
                         labelOffsetPixels, formatter);
         tick = nextTick;
      }

      if (length - project1D(tick) >= pixelMargin) {
         // Draw label for after last tick, justified to maximum of
         // axis (right or top)

         setupText(surface, JUSTIFY_MAX);
         renderTickLabel(surface, this.max, labelOffsetPixels, formatter);
      }
   }

   protected void renderTicksRangeLabel(double offsetPixels, double tickSize,
                                        TickGenerator tickGen, TickGenerator endOfRangeGenerator,
                                        Canvas canvas, double tickWidthPixels, LabelFormatter formatter) {
      Surface surface = canvas.getSurface();
      DirectShapeRenderer renderer = canvas.getRenderer();

      if (tickGen == null) {
         tickGen = new TickGenerator(tickSize, 0);
      }

      double labelOffsetPixels = setupText(surface, JUSTIFY_MED)
                                 + offsetPixels + tickWidthPixels;

      double minTick = tickGen.nextTick(this.min - tickSize * 1.5);

      while (minTick <= this.max) {
         if (this.min <= minTick) {
            renderTick(renderer, minTick, tickWidthPixels + offsetPixels);
         }

         endOfRangeGenerator.nextTick(minTick);
         double maxTick = endOfRangeGenerator.nextTick();

         if (this.min <= maxTick && maxTick <= this.max) {
            renderTick(renderer, maxTick, tickWidthPixels + offsetPixels);
         }

         if (this.min <= minTick && maxTick <= this.max) {
            renderTickLabel(surface, (minTick + maxTick) / 2.0,
                            labelOffsetPixels, formatter);
         }

         minTick = tickGen.nextTick();
      }
   }

	protected void renderTick(final DirectShapeRenderer renderer,
			final double tick,
			final double tickWidthPixels) {
		final Vector2 fromPosition = project2D(tick);
		final Vector2 toPosition = fromPosition.add(this.basis.x.scale(tickWidthPixels));
		renderer.drawLineSegment(fromPosition, toPosition);
	}

   protected void renderTickLabel(Surface surface, double tick,
                                  double labelOffsetPixels, LabelFormatter formatter) {
      renderTickLabel(surface, tick, labelOffsetPixels,
                      formatter.format(tick));
   }

   protected void renderTickLabel(Surface surface, double y,
                                  double labelOffsetPixels, String label) {
      // .setFont("italic 30px sans-serif")
      surface.fillText(label,
                       project2D(y).add(this.basis.x.scale(labelOffsetPixels)));
   }

   public double computeTickSize(double minPixels) {
      return computeTickSize(minPixels, 1.0);
   }

   public double computeTickSize(double minPixels, double unitSize) {
      double minDelta = (this.max - this.min)
                        * (minPixels / this.length) / unitSize;
      double minDeltaMantissa = minDelta
                                / Math.pow(10, Math.floor(Math.log10(minDelta)));

      // Round minDelta up to nearest (1,2,5)
      double actualDeltaMantissa;
      if (minDeltaMantissa > 5) {
         actualDeltaMantissa = 10;
      } else if (minDeltaMantissa > 2) {
         actualDeltaMantissa = 5;
      } else if (minDeltaMantissa > 1) {
         actualDeltaMantissa = 2;
      } else {
         actualDeltaMantissa = 1;
      }

      return minDelta * (actualDeltaMantissa / minDeltaMantissa);
   }

   public double getWidth() {
      return width;
   }

   public Canvas getDrawingCanvas() {
      return drawingCanvas;
   }

   public boolean contains(Vector2 pos) {
      return bounds.contains(pos);
   }

   protected void uncheckedTranslate(double motion) {
      this.min += motion;
      this.max += motion;
   }

   protected void clampToRange() {
      // First, try to translate to put in range
      uncheckedTranslate(Math.max(0, minRange - this.min));
      uncheckedTranslate(Math.min(0, maxRange - this.max));

      // Second, truncate to range
      if (hasMinRange) {
         this.min = Math.max(this.min, minRange);
      }
      if (hasMaxRange) {
         this.max = Math.min(this.max, maxRange);
      }
   }

   public void zoom(final double factor, final double about, final String eventId) {
      this.min = about + factor * (this.min - about);
      this.max = about + factor * (this.max - about);
      clampToRange();
      rescale();

      // notify event listeners that the axis has changed
      publishAxisChangeEvent(eventId);
   }

   private void publishAxisChangeEvent(final String eventId) {
      for (final EventListener listener : eventListeners) {
         listener.onAxisChange(eventId);
      }
   }

   public void drag(final Vector2 from, final Vector2 to, final String eventId) {
      final double motion = unproject(from) - unproject(to);
      uncheckedDrag(motion, eventId);
   }

   /**
    * Drags this axis by motion, which is expressed in terms of
    * the units for this axis.
    *
    * @param motion
    * 		the amount to move this axis in logical units (seconds
    * 		for an axis representing time, other values for another
    * 		axis), not in screen pixels
    */
   private void uncheckedDrag(final double motion, final String eventId) {
      uncheckedTranslate(motion);
      clampToRange();
      rescale();

      // notify event listeners that the axis has changed
      publishAxisChangeEvent(eventId);
   }

   /**
    * Replaces the bounds of this axis with the specified min and max.
    *
    * @param newMin
    * 		the new min value for this axis
    * @param newMax
    * 		the new max value for this axis
    * @throws IllegalArgumentException
    * 		if newMax > newMin
    */
   public void replaceBounds(final double newMin, final double newMax) {
      if (newMin > newMax) {
         throw new IllegalArgumentException("Can't have backwards axis bounds");
      }

      final double oldMin = getMin();
      final double oldMax = getMax();

      final String eventId = UUID.uuid();

      // Zoom in place to the right factor
      zoom((newMax - newMin) / (oldMax - oldMin), (oldMin + oldMax) / 2, eventId);

      // Now translate
      uncheckedDrag(newMin - getMin(), eventId);
   }
}
