package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

import gwt.g2d.client.math.Vector2;

import java.util.HashSet;
import java.util.Set;

import org.bodytrack.client.DataPointListener.TriggerAction;

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
   private final int minLevel;
   private TileLoader tileLoader;

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
      if (datasource == null || nativeXAxis == null || nativeYAxis == null)
         throw new NullPointerException("Cannot have a null datasource or axis");

      this.xAxisNative = nativeXAxis;
      this.yAxisNative = nativeYAxis;
      this.xAxis = GraphAxis.getAxis(this.xAxisNative);
      this.yAxis = GraphAxis.getAxis(this.yAxisNative);

      if (!xAxis.isXAxis())
         throw new IllegalArgumentException("X-axis must be horizontal");
      if (yAxis.isXAxis())
         throw new IllegalArgumentException("Y-axis must be vertical");

      this.minLevel = minLevel;
      tileLoader = buildTileLoader(datasource);

      // register self as an event listener to the axes...
      registerGraphAxisEventListener(getXAxis());
      registerGraphAxisEventListener(getYAxis());
   }

   public final void setDatasource(final JavaScriptObject datasource) {
      // Because we are getting rid of the old tile loader, the cache
      // is automatically dropped
      tileLoader = buildTileLoader(datasource);

      // Need to replace the drawing of the old data, and replace it
      // with nothing until we reload the tiles
      signalRepaintOfPlotContainer();
   }

   // The method that should be called from within this class
   private TileLoader buildTileLoader(final JavaScriptObject datasource) {
      TileLoader loader = buildTileLoader(minLevel, datasource, xAxis);
      loader.checkForFetch(computeCurrentLevel());

      return loader;
   }

   /**
    * A hook to allow subclasses to build their tile loaders however they choose
    *
    * <p>This method builds a new {@link TileLoader}, with any necessary listeners
    * attached.  This particular implementation attaches a listener that
    * causes this plot's container to be repainted whenever any new data
    * is loaded.</p>
    *
    * <p>This is not designed to be called by subclasses, but is designed
    * to be overridden by subclasses that require some special tile loading
    * logic.  If a subclass does require some special tile loading logic,
    * it can override this method to return a {@link TileLoader} that
    * encapsulates that logic.  However, the subclass implementation does
    * <strong>not</strong> need to call {@link TileLoader#checkForFetch(int)}
    * on the newly created {@link TileLoader}, since any code to set the
    * datasource on a {@link BaseSeriesPlot} will call
    * {@link TileLoader#checkForFetch(int)} automatically just after the
    * object is created.  It is expected that a subclass implementation of
    * this method will never return <code>null</code>.</p>
    *
    * @param minLevel
    *  The minimum level to which the newly created {@link TileLoader} should
    *  load tiles
    * @param datasource
    *  The native JavaScript function that actually loads tiles
    * @param xAxis
    *  The X-axis that will allow the tile loader to determine if another
    *  tile load is required or not
    * @return
    *  A newly created {@link TileLoader} that uses the specified X-axis
    *  and minimum level to determine when to call the datasource function
    *  in order to load tiles
    */
   protected TileLoader buildTileLoader(final int minLevel,
                                        final JavaScriptObject datasource,
                                        final GraphAxis xAxis) {
      TileLoader loader = new StandardTileLoader(minLevel, datasource, xAxis);
      loader.addEventListener(new AlwaysRepaintListener());

      return loader;
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
         publishDataPoint(publishedPoint, TriggerAction.HIGHLIGHT);
      }
   }

   protected final void publishDataPoint(final PlottablePoint point,
                                         final TriggerAction action) {
      publishDataPoint(point, action, null);
   }

   protected final void publishDataPoint(final PlottablePoint point,
                                         final TriggerAction action,
                                         final JavaScriptObject info) {
      for (final DataPointListener listener: dataPointListeners)
         listener.handleDataPointUpdate(point, action, info);
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

   // Do-nothing implementation so subclasses don't have to worry about
   // handling this specialized event
   @Override
   public void onClick(final Vector2 pos) { }

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

   /**
    * A class that always repaints the plot container whenever any event is fired
    */
   protected class AlwaysRepaintListener implements TileLoader.EventListener {

      @Override
      public void handleLoadSuccess() {
         signalRepaintOfPlotContainer();
      }

      @Override
      public void handleLoadFailure() {
         signalRepaintOfPlotContainer();
      }
   }
}
