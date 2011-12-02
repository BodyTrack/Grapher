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
   private final TileLoader tileLoader;

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

   private String previousPaintEventId = null;

   /**
    *
    * @param datasource
    * 		a native JavaScript function which can be used to retrieve tiles
    * @param nativeXAxis
    * 		the X-axis along which this data set will be aligned when drawn
    * @param nativeYAxis
    * 		the Y-axis along which this data set will be aligned when drawn
    * @param minLevel
    * 		the minimum level to which the user will be allowed to zoom
    * @throws NullPointerException
    * 		if datasource, nativeXAxis, or nativeYAxis is <code>null</code>
    * @throws IllegalArgumentException
    * 		if xAxis is really a Y-axis, or if yAxis is really a Y-axis
    */
   protected BaseSeriesPlot(final JavaScriptObject datasource,
                            final JavaScriptObject nativeXAxis,
                            final JavaScriptObject nativeYAxis,
                            final int minLevel) {
      if (datasource == null || nativeXAxis == null || nativeYAxis == null) {
         throw new NullPointerException("Cannot have a null datasource or axis");
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

      this.tileLoader = new StandardTileLoader(minLevel, datasource, xAxis);
      tileLoader.addEventListener(
            new TileLoader.EventListener() {
               @Override
               public void handleLoadSuccess() {
                  signalRepaintOfPlotContainer();
               }

               @Override
               public void handleLoadFailure() {
                  signalRepaintOfPlotContainer();
               }
            }
      );
      tileLoader.checkForFetch(computeCurrentLevel());

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

   /**
    * Called before the {@link #paint(Canvas, String)} method calls
    * {@link SeriesPlotRenderer#render(BoundedDrawingBox, Iterable, GraphAxis, GraphAxis, PlottablePoint)},
    * to allow implementations to prepare for rendering.
    *
    * @param canvas
    *    The canvas upon which rendering will take place.
    * @param drawing
    * 	The {@link BoundedDrawingBox} that should constrain the drawing.
    * 	Forwarding graphics calls through drawing will ensure that everything
    * 	draws up to the edge of the viewing window but no farther
    */
   protected abstract void beforeRender(final Canvas canvas, final BoundedDrawingBox drawing);

   /**
    * Get the appropriate renderer required for painting.
    */
   protected abstract SeriesPlotRenderer getRenderer();

   /**
    * Paints this plot in its {@link PlotContainer}.
    *
    * <p>Does not draw the axes associated with this plot.</p>
    */
   @Override
   public final void paint(final Canvas canvas, final String newPaintEventId) {
      // guard against redundant paints
      if (previousPaintEventId == null || !previousPaintEventId.equals(newPaintEventId)) {
         previousPaintEventId = newPaintEventId;

         final BoundedDrawingBox drawing = getDrawingBounds(canvas);
         
         beforeRender(canvas, drawing);

         final SeriesPlotRenderer renderer = getRenderer();
         renderer.render(drawing,
                         tileLoader.getBestResolutionTiles(computeCurrentLevel()),
                         getXAxis(),
                         getYAxis(),
                         getHighlightedPoint());

         // Clean up after ourselves
         afterRender(canvas, drawing);

         // Make sure we shouldn't get any more info from the server
         tileLoader.checkForFetch(computeCurrentLevel());
      }
   }

   /**
    * Called after the {@link #paint(Canvas, String)} method calls
    * {@link SeriesPlotRenderer#render(BoundedDrawingBox, Iterable, GraphAxis, GraphAxis, PlottablePoint)},
    * to allow implementations to clean up after rendering.
    *
    * @param canvas
    *    The canvas upon which rendering took place.
    * @param drawing
    * 	The {@link BoundedDrawingBox} that should constrain the drawing.
    * 	Forwarding graphics calls through drawing will ensure that everything
    * 	draws up to the edge of the viewing window but no farther
    */
   protected abstract void afterRender(final Canvas canvas, final BoundedDrawingBox drawing);

   /** Causes the containing {@link PlotContainer} to paint itself. */
   protected final void signalRepaintOfPlotContainer(){
      if (plotContainer != null) {
         plotContainer.paint();
      }
   }

   /**
    * Builds and returns a new {@link BoundedDrawingBox
    * BoundedDrawingBox} that constrains drawing to the viewing window.
    *
    * @return
    * 		a <tt>BoundedDrawingBox</tt> that will only allow drawing
    * 		within the axes
    */
   protected final BoundedDrawingBox getDrawingBounds(final Canvas canvas) {
      final double minX = getXAxis().project2D(getXAxis().getMin()).getX();
      final double maxX = getXAxis().project2D(getXAxis().getMax()).getX();

      // Although minY and maxY appear to be switched, this is actually
      // the correct way to define these variables, since we draw the
      // Y-axis from bottom to top but pixel values increase from top
      // to bottom.  Thus, the max Y-value is associated with the min
      // axis value, and vice versa.
      final double minY = getYAxis().project2D(getYAxis().getMax()).getY();
      final double maxY = getYAxis().project2D(getYAxis().getMin()).getY();

      return new BoundedDrawingBox(canvas, minX, minY, maxX, maxY);
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

   public final void setHighlightedPoint(final PlottablePoint highlightedPoint) {
      this.highlightedPoint = highlightedPoint;
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
    * <p>If {@link #setHighlightedPoint(PlottablePoint)} has been called since the constructor
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
   private int computeCurrentLevel() {
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

   protected final GrapherTile getBestResolutionTileAt(final double time){
      return tileLoader.getBestResolutionTileAt(time, computeCurrentLevel());
   }

}
