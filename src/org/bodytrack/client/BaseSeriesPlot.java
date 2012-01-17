package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.HashSet;
import java.util.Set;

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

   private PlottablePoint publishedPoint = null;
   private final Set<DataPointListener> dataPointListeners = new HashSet<DataPointListener>();

   // If highlightedPoint is null, then this should not be highlighted.
   // Otherwise, this is the point to highlight on the axes
   private PlottablePoint highlightedPoint = null;

   private final GraphAxis.EventListener graphAxisEventListener = new GraphAxis.EventListener() {
      @Override
      public void onAxisChange(final int eventId) {
         if (plotContainer != null) {
            plotContainer.paint(eventId);
         }
      }
   };

   private int previousPaintEventId = 0;

   /**
    *
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
      this.xAxis = GraphAxis.getAxis(this.xAxisNative);
      this.yAxis = GraphAxis.getAxis(this.yAxisNative);

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

   public final void addDataPointListener(final JavaScriptObject listener) {
      if (listener != null) {
         dataPointListeners.add(listener.<DataPointListener>cast());
      }
   }

   public final void removeDataPointListener(final JavaScriptObject listener) {
      if (listener != null) {
         dataPointListeners.remove(listener.<DataPointListener>cast());
      }
   }

   protected final void publishHighlightedValue() {
      boolean willPublish = false;

      if (isHighlighted()) {
         final PlottablePoint newPublishedPoint = highlightedPoint;
         willPublish = (publishedPoint == null && newPublishedPoint != null) ||
                       (publishedPoint != null && !publishedPoint.equals(newPublishedPoint));
         publishedPoint = newPublishedPoint;
      } else {
         if (publishedPoint != null) {
            willPublish = true;
         }
         publishedPoint = null;
      }

      if (willPublish) {
         for (final DataPointListener listener : dataPointListeners) {
            listener.handleDataPointHighlight(publishedPoint);
         }
      }
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

   protected abstract SeriesPlotRenderer getRenderer();

   /**
    * Sets the style for this {@link Plot}, and causes a repaint of the plot's {@link PlotContainer}.  Does
    * nothing if the given <code>newStyleJson</code> is <code>null</code>.
    */
   public final void setStyle(final JavaScriptObject newStyleJson) {
      if (newStyleJson != null) {
         final SeriesPlotRenderer renderer = getRenderer();
         if (renderer != null) {
            renderer.setStyleDescription(newStyleJson.<StyleDescription>cast());

            signalRepaintOfPlotContainer();
         }
      }
   }

   /**
    * Paints this plot in its {@link PlotContainer}.
    *
    * <p>Does not draw the axes associated with this plot.</p>
    */
   @Override
   public final void paint(final Canvas canvas, final int newPaintEventId) {
      final SeriesPlotRenderer renderer = getRenderer();
      if (renderer != null) {
         // guard against redundant paints
         if (previousPaintEventId != newPaintEventId) {
            previousPaintEventId = newPaintEventId;

            renderer.render(canvas,
                            getDrawingBounds(canvas),
                            tileLoader.getBestResolutionTiles(computeCurrentLevel()),
                            getXAxis(),
                            getYAxis(),
                            getHighlightedPoint());

            // Make sure we shouldn't get any more info from the server
            tileLoader.checkForFetch(computeCurrentLevel());
         }
      }
   }

   /** Causes the containing {@link PlotContainer} to paint itself. */
   protected final void signalRepaintOfPlotContainer() {
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
      publishHighlightedValue();
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

   protected final GrapherTile getBestResolutionTileAt(final double time) {
      return tileLoader.getBestResolutionTileAt(time, computeCurrentLevel());
   }
}
