package org.bodytrack.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * <code>BaseSeriesPlot</code> provides common functionality for {@link Plot} implementations.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseSeriesPlot implements Plot {
   /**
    * Whenever the {@link #highlight()} method is called, we don't know
    * which points on the axes should be highlighted, so we use this
    * value to indicate this.  As such, testing with == is OK as a test
    * for this point, since we set highlightedPoint to this exact
    * memory location whenever we don't know which point should be
    * highlighted.
    */
   private static final PlottablePoint HIGHLIGHTED_NO_SINGLE_POINT = new PlottablePoint(Double.MIN_VALUE, 0.0);

   /** Used to speed up the {@link #log2(double)} method. */
   private static final double LN_2 = Math.log(2);

   /**
    * Computes the floor of the log (base 2) of x.
    *
    * @param x the value for which we want to take the log
    * @return the floor of the log (base 2) of x
    */
   private static int log2(final double x) {
      if (x <= 0) {
         return Integer.MIN_VALUE;
      }

      return (int)Math.floor((Math.log(x) / LN_2));
   }

   private PlotContainer plotContainer = null;

   private final JavaScriptObject xAxisNative;
   private final JavaScriptObject yAxisNative;
   private final GraphAxis xAxis;
   private final GraphAxis yAxis;

   // If highlightedPoint is null, then this should not be highlighted.
   // Otherwise, this is the point to highlight on the axes
   private PlottablePoint highlightedPoint = null;

   private final GraphAxis.EventListener graphAxisEventListener = new GraphAxis.EventListener() {
      @Override
      public void onAxisChange(final String eventId) {
         if (plotContainer != null) {
            plotContainer.paint(eventId);
         }
      }
   };

   /**
    * @param nativeXAxis
    * 		the X-axis along which this data set will be aligned when drawn
    * @param nativeYAxis
    * 		the Y-axis along which this data set will be aligned when drawn
    *
    * @throws NullPointerException
    * 		if nativeXAxis or nativeYAxis is <code>null</code>
    * @throws IllegalArgumentException
    * 		if xAxis is really a Y-axis, or if yAxis is really a Y-axis
    */
   protected BaseSeriesPlot(final JavaScriptObject nativeXAxis, final JavaScriptObject nativeYAxis) {
      if (nativeXAxis == null || nativeYAxis == null) {
         throw new NullPointerException("Cannot have a null axis");
      }

      this.xAxisNative = nativeXAxis;
      this.yAxisNative = nativeYAxis;
      this.xAxis = convertNativeAxisToGraphAxis(this.xAxisNative);
      this.yAxis = convertNativeAxisToGraphAxis(this.yAxisNative);

      if (!xAxis.isXAxis()) {
         throw new IllegalArgumentException("X-axis must be horizontal");
      }
      if (yAxis.isXAxis()) {
         throw new IllegalArgumentException("Y-axis must be vertical");
      }

      // register self as an event listener to the axes...
      registerGraphAxisEventListener(getXAxis());
      registerGraphAxisEventListener(getYAxis());
   }

   private GraphAxis convertNativeAxisToGraphAxis(final JavaScriptObject nativeAxis) {
      final Dynamic djso = nativeAxis.cast();
      return djso.get("__backingAxis");
   }

   private void registerGraphAxisEventListener(final GraphAxis axis) {
      if (axis != null) {
         axis.addEventListener(graphAxisEventListener);
      }
   }

   private void unregisterGraphAxisEventListener(final GraphAxis axis) {
      if (axis != null) {
         axis.removeEventListener(graphAxisEventListener);
      }
   }

   /** Sets the {@link PlotContainer} which contains this {@link Plot}. */
   @Override
   public final void registerPlotContainer(final PlotContainer plotContainer) {
      this.plotContainer = plotContainer;
   }

   /**
    * Unregisters the given {@link PlotContainer} from this {@link Plot} if and only if the given
    * {@link PlotContainer} is not <code>null</code> and is currently registered with this {@link Plot}. That is,
    * if this {@link Plot} is already associated with a {@link PlotContainer} other than the given one, then
    * nothing happens.
    */
   @Override
   public final void unregisterPlotContainer(final PlotContainer plotContainer) {
      if (plotContainer != null && plotContainer.equals(this.plotContainer)) {
         this.plotContainer = null;
      }
   }

   /** Causes the containing {@link PlotContainer} to paint itself. */
   protected final void signalRepaintOfPlotContainer(){
      if (plotContainer != null) {
         plotContainer.paint();
      }
   }

   /**
    * Returns the X-Axis for this {@link Plot}.
    *
    * @return
    * 		the X-axis for this {@link Plot}
    */
   @Override
   public final GraphAxis getXAxis() {
      return xAxis;
   }

   public final JavaScriptObject getNativeXAxis() {
      return xAxisNative;
   }

   /**
    * Returns the Y-Axis for this {@link Plot}.
    *
    * @return
    * 		the Y-axis for this {@link Plot}
    */
   @Override
   public final GraphAxis getYAxis() {
      return yAxis;
   }

   public final JavaScriptObject getNativeYAxis() {
      return yAxisNative;
   }

   /**
    * Returns the highlighted point maintained by this {@link Plot}.
    *
    * @return
    * 		the highlighted point this {@link Plot} keeps, or
    * 		<code>null</code> if there is no highlighted point
    */
   @Override
   public final PlottablePoint getHighlightedPoint() {
      return highlightedPoint;
   }

   protected final void setHighlightedPoint(final PlottablePoint highlightedPoint) {
      this.highlightedPoint = highlightedPoint;
   }

   /**
    * Returns <code>true</code> if there is a single highlighted point.
    */
   protected final boolean isSinglePointHighlighted() {
      return highlightedPoint != null && highlightedPoint != HIGHLIGHTED_NO_SINGLE_POINT;
   }

   /**
    * Highlights this {@link Plot} in future
    * {@link Plot#paint(Canvas, String)} calls.
    *
    * <p>Note that this does not highlight the axes associated with this
    * {@link Plot}.</p>
    */
   @Override
   public final void highlight() {
      highlightedPoint = HIGHLIGHTED_NO_SINGLE_POINT;
   }

   /**
    * Stops highlighting this {@link Plot}.
    *
    * <p>Note that this does not affect the highlighting status on the
    * axes associated with this {@link Plot}.</p>
    */
   @Override
   public final void unhighlight() {
      highlightedPoint = null;
   }

   /**
    * Tells whether or not this {@link Plot} is highlighted.
    *
    * <p>If {@link #highlight()} has been called since the constructor
    * and since the last call to {@link #unhighlight()}, returns
    * <code>true</code>.  Otherwise, returns <tt>false</tt>.</p>
    *
    * @return
    * 		<code>true</code> if and only if this {@link Plot} is highlighted
    */
   @Override
   public final boolean isHighlighted() {
      return highlightedPoint != null;
   }

   /**
    * Computes the value for currentLevel based on xAxis.
    *
    * @return
    * 		the level at which xAxis is operating
    */
   protected final int computeCurrentLevel() {
      final double xAxisWidth = getXAxis().getMax() - getXAxis().getMin();
      final double dataPointWidth = xAxisWidth / GrapherTile.TILE_WIDTH;

      return log2(dataPointWidth);
   }

   /**
    * Returns a time string representing the specified time.
    *
    * <p>A caveat: time should be the number of <em>seconds</em>,
    * since the epoch.
    *
    * @param secondsSinceEpoch
    * 		the number of seconds since the epoch
    * @return
    * 		a string representation of time
    */
   protected final String getTimeString(final double secondsSinceEpoch) {
      return getTimeString((long)(secondsSinceEpoch * 1000));
   }

   /**
    * Returns a time string representing the specified time.
    *
    * <p>A caveat: time should be the number of <em>milliseconds</em>,
    * not seconds, since the epoch.  If a caller forgets to multiply
    * a time by 1000, wrong date strings (usually something
    * involving January 15, 1970) will come back.</p>
    *
    * @param time
    * 		the number of milliseconds since the epoch
    * @return
    * 		a string representation of time
    */
   private String getTimeString(final long time) {
      String formatString = "EEE MMM dd yyyy, HH:mm:ss";
      final int fractionalSecondDigits = getFractionalSecondDigits();

      // We know that fractionalSecondDigits will always be 0, 1, 2, or 3
      switch (fractionalSecondDigits) {
         case 0:
            break;
         case 1:
            formatString += ".S";
            break;
         case 2:
            formatString += ".SS";
            break;
         case 3:
            formatString += ".SSS";
            break;
         default:
            GWT.log("BaseSeriesPlot.getTimeString(): Unexpected number of "
                    + "fractionalSecondDigits: " + fractionalSecondDigits);
      }

      final DateTimeFormat format = DateTimeFormat.getFormat(formatString);
      return format.format(new Date(time));
   }

   /**
    * Computes the number of fractional second digits that should
    * appear in a displayed time string, based on the current level.
    *
    * <p>This <em>always</em> returns a nonnegative integer less than
    * or equal to 3.</p>
    *
    * @return
    * 		the number of fractional second digits that should appear
    * 		in a displayed times string
    */
   private int getFractionalSecondDigits() {
      final int level = computeCurrentLevel();
      if (level > 1) {
         return 0;
      }
      if (level == 1) {
         return 1;
      }
      if (level > -2) // 0 or -1
      {
         return 2;
      }
      return 3; // We can't get better than millisecond precision
   }
}
