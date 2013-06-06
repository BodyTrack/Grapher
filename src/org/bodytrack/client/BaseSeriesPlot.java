package org.bodytrack.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bodytrack.client.DataPointListener.TriggerAction;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * <p>
 * <code>BaseSeriesPlot</code> provides common functionality for {@link Plot} implementations.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseSeriesPlot implements Plot {
   private PlotContainer plotContainer = null;

   private final JavaScriptObject xAxisNative;
   private final JavaScriptObject yAxisNative;
   private final GraphAxis xAxis;
   private final GraphAxis yAxis;
   private final boolean localDisplay;
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

   protected final ModalTimeZoneMap timeZoneMap;

   /**
    *
    *
    * @param datasource
    * 		a native JavaScript function which can be used to retrieve tiles
    * @param nativeXAxis
    * 		the X-axis along which this data set will be aligned when drawn
    * @param nativeYAxis
    * 		the Y-axis along which this data set will be aligned when drawn
    * @param localDisplay
    * 		<code>true</code> if the data from the server should be shifted to
    * 		pretend that all time offsets are local offsets, and <code>false</code>
    * 		if the data from the server should be displayed as is
    * @throws NullPointerException
    * 		if datasource, nativeXAxis, or nativeYAxis is <code>null</code>
    * @throws IllegalArgumentException
    * 		if xAxis is really a Y-axis, or if yAxis is really a Y-axis
    */
   protected BaseSeriesPlot(final JavaScriptObject datasource,
                            final JavaScriptObject nativeXAxis,
                            final JavaScriptObject nativeYAxis,
                            final boolean localDisplay) {
      if (datasource == null || nativeXAxis == null || nativeYAxis == null)
         throw new NullPointerException("Cannot have a null datasource or axis");

      this.xAxisNative = nativeXAxis;
      this.yAxisNative = nativeYAxis;
      this.xAxis = GraphAxis.getAxis(this.xAxisNative);
      this.yAxis = GraphAxis.getAxis(this.yAxisNative);
      this.localDisplay = localDisplay;

      if (xAxis.getTimeZoneMap() == null)
         timeZoneMap = new ModalTimeZoneMap(TimeZoneMap.IDENTITY_MAP, !localDisplay);
      else
         timeZoneMap = new ModalTimeZoneMap(xAxis.getTimeZoneMap(), !localDisplay);

      if (!xAxis.isXAxis())
         throw new IllegalArgumentException("X-axis must be horizontal");
      if (yAxis.isXAxis())
         throw new IllegalArgumentException("Y-axis must be vertical");

      tileLoader = buildTileLoader(datasource);
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
      TileLoader loader = buildTileLoader(datasource, xAxis, localDisplay);
      loader.checkForFetch();

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
   protected TileLoader buildTileLoader(final JavaScriptObject datasource,
         final GraphAxis xAxis, final boolean localDisplay) {
      TileLoader loader = new StandardTileLoader(datasource, xAxis, localDisplay);
      loader.addEventListener(new AlwaysRepaintListener());

      return loader;
   }

   protected TileLoader getTileLoader() {
      return tileLoader;
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
         final PlottablePoint newPublishedPoint = getHighlightedPoint();
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

   /**
    * Sets the {@link SeriesPlotContainer} which contains this {@link Plot}.
    *
    * <p>
    * Also registers the axes to begin notifying this plot whenever the
    * user pans or zooms.
    * </p>
    */
   @Override
   public final void registerPlotContainer(final PlotContainer plotContainer) {
      this.plotContainer = plotContainer;

      this.registerGraphAxisEventListener(getXAxis());
      this.registerGraphAxisEventListener(getYAxis());
   }

   /**
    * Unregisters the given {@link SeriesPlotContainer} from this {@link Plot} if
    * and only if the given {@link SeriesPlotContainer} is not <code>null</code> and
    * is currently registered with this {@link Plot}. That is, if this {@link Plot}
    * is already associated with a {@link SeriesPlotContainer} other than the given one,
    * then nothing happens.
    *
    * <p>
    * This also unregisters the axis listeners so that no further updates cause
    * extra tiles to be fetches from the server.
    * </p>
    */
   @Override
   public final void unregisterPlotContainer(final PlotContainer plotContainer) {
      if (plotContainer != null && plotContainer.equals(this.plotContainer)) {
         this.plotContainer = null;

         this.unregisterGraphAxisEventListener(getXAxis());
         this.unregisterGraphAxisEventListener(getYAxis());

         // TODO: Possibly set an inactive flag that disables all operations
         // until the flag is reset by calling registerPlotContainer?
      }
   }

   protected abstract SeriesPlotRenderer getRenderer();

   /**
    * Sets the style for this {@link Plot}, and causes a repaint of the plot's {@link SeriesPlotContainer}.  Does
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
    * Paints this plot in its {@link SeriesPlotContainer}.
    *
    * <p>Does not draw the axes associated with this plot.</p>
    */
   @Override
   public final void paint(final GrapherCanvas canvas, final int newPaintEventId) {
      final SeriesPlotRenderer renderer = getRenderer();
      if (renderer != null) {
         // guard against redundant paints
         if (previousPaintEventId != newPaintEventId) {
            previousPaintEventId = newPaintEventId;

            renderer.render(canvas,
                            getDrawingBounds(canvas),
                            tileLoader.getBestResolutionTiles(),
                            getXAxis(),
                            getYAxis(),
                            getHighlightedPoint());

            // Make sure we shouldn't get any more info from the server
            tileLoader.checkForFetch();
         }
      }
   }

   /** Causes the containing {@link SeriesPlotContainer} to paint itself. */
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
   protected final BoundedDrawingBox getDrawingBounds(final GrapherCanvas canvas) {
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

   // Returns true if and only if an actual server request is made
   public final boolean checkForData(final double xMin, final double xMax,
         final TileLoader.EventListener onload) {
      return tileLoader.checkForFetch(xMin, xMax, onload);
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

   private PlottablePoint prevHighlightedPoint = null;
   @Override
   public final PlottablePoint getHighlightedPoint() {
	  PlottablePoint point = null;
	  if (highlightedPoint != null || getXAxis() == null || getXAxis().getCursorPosition() == null)
		  	point = highlightedPoint;
	  else
		  point = getPointClosestToXCursor();
	  if ((prevHighlightedPoint != null && !prevHighlightedPoint.equals(point)) || (prevHighlightedPoint == null && point != null)){
		  prevHighlightedPoint = point;
		  publishHighlightedValue();
	  }		  
	  return point;
   }
   
   public PlottablePoint getPointClosestToXCursor(){
	   if (getXAxis() == null || getXAxis().getCursorPosition() == null)
		   return null;
	   return getClosestPointToXValue(getXAxis().getCursorPosition(),5);
   }
   
   public PlottablePoint getClosestPointToXValue(double xValue, double threshHold){
	   Vector2 projectedPosition = getXAxis().project2D(xValue);
	   double xMin = getXAxis().unproject(projectedPosition.add(new Vector2(-threshHold,0)));
	   double xMax = getXAxis().unproject(projectedPosition.add(new Vector2(threshHold,0)));
	   
	   double yMin = getYAxis().getMin();
	   double yMax = getYAxis().getMax();
	   
	   List<GrapherTile> tiles = getTileLoader().getBestResolutionTiles(xMin, xMax);
	   
	   PlottablePoint bestPoint = null;
	   double bestDistanceSquared = -1;
	   
	   for (GrapherTile tile : tiles){
		   for (PlottablePoint point : tile.getDataPoints()){
			   if (point.getDate() < xMin || point.getDate() > xMax || point.getValue() < yMin || point.getValue() > yMax)
				   continue;
			   double distance = xValue - point.getDate();
			   double distanceSquared = distance * distance;
			   if (bestDistanceSquared < 0 || distanceSquared < bestDistanceSquared){
				   bestPoint = point;
				   bestDistanceSquared = distanceSquared;
			   }
		   }
	   }
	   
	   return bestPoint; 
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
      return getHighlightedPoint() != null;
   }

   // Do-nothing implementation so subclasses don't have to worry about
   // handling this specialized event
   @Override
   public void onClick(final Vector2 pos) { }

   protected final GrapherTile getBestResolutionTileAt(final double time) {
      return tileLoader.getBestResolutionTileAt(time);
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
