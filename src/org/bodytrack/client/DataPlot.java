package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.math.Vector2;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * Represents a single set of data, along with references to its
 * associated axes.
 *
 * <p>Has the ability to draw itself and its axes on a
 * {@link Canvas} object, and to update
 * the positions of its dots based on the zoom level.  Also, if the
 * zoom level or position of the X-axis changes enough, this class will
 * automatically fetch the data from the server via Ajax and redraw
 * the data whenever it comes in from the server.</p>
 *
 * <p>A class that wishes to inherit this class can override
 * {@link DataPlot#paintAllDataPoints}, but the easiest way to modify
 * functionality it to override {@link DataPlot#paintDataPoint} and
 * {@link DataPlot#paintEdgePoint(BoundedDrawingBox, GrapherTile, double, double, PlottablePoint)}.
 * These two functions are responsible for painting a single point on
 * this DataPlot.  This (parent) class will automatically handle
 * highlighting, zooming, and the Ajax calls for pulling extra data
 * from the server.</p>
 *
 * <p>A classes that wishes to inherit this class may also wish to
 * override {@link DataPlot#getDataPoints(GrapherTile)}, which
 * determines the points that {@link DataPlot#paintAllDataPoints}
 * will draw, and the order in which paintAllDataPoints will draw
 * them.</p>
 */
public class DataPlot implements Alertable<GrapherTile> {
   /**
    * We never re-request a URL with MAX_REQUESTS_PER_URL or more failures
    * in a row.
    */
   private static final int MAX_REQUESTS_PER_URL = 5;

   /**
    * Used to speed up the log2 method.
    */
   private static final double LN_2 = Math.log(2);

   private GraphWidget containingGraphWidget = null;

   private final JavaScriptObject datasource;
   private JavaScriptObject xAxis;
   private JavaScriptObject yAxis;
   private final HighlightableRenderer normalRenderer;
   private final HighlightableRenderer highlightRenderer;

   private final int minLevel;
   private Color color;

   private boolean shouldZoomIn;

   // Values related to getting new values from the server (the data will be pulled in with the checkForFetch call)
   private final List<GrapherTile> currentData = new ArrayList<GrapherTile>();
   private final Set<TileDescription> pendingDescriptions = new HashSet<TileDescription>();
   private final Map<String, Integer> pendingUrls = new HashMap<String, Integer>();
   private final List<GrapherTile> pendingData = new ArrayList<GrapherTile>();

   // Determining whether or not we should retrieve more data from
   // the server
   private int currentLevel;
   private int currentMinOffset;
   private int currentMaxOffset;

   // If highlightedPoint is null, then this should not be highlighted.
   // Otherwise, this is the point to highlight on the axes
   private PlottablePoint highlightedPoint;

   private final GraphAxis.EventListener graphAxisEventListener = new GraphAxis.EventListener() {
      @Override
      public void onAxisChange(final String eventId) {
         if (DataPlot.this.containingGraphWidget != null) {
            DataPlot.this.containingGraphWidget.paint(eventId);
         }
      }
   };

   private String previousPaintEventId = null;

   /**
    * Main constructor for the DataPlot object.
    *
    * <p>The parameter url is the trickiest to get right.  This parameter
    * should be the <strong>beginning</strong> (the text up to, but
    * not including, the &lt;level&gt;.&lt;offset&gt;.json part of the
    * URL to fetch) of the URL which will be used to get more data.
    * Note that this <strong>must</strong> be a trusted BodyTrack
    * URL.  As described in the documentation for
    * {@link GrapherTile#retrieveTile(JavaScriptObject, int, int, Alertable)},
    * an untrusted connection could allow
    * unauthorized access to all of a user's data.</p>
    *
    * @param datasource
    * 		a native JavaScript function which can be used to retrieve tiles
    * @param nativeXAxis
    * 		the X-axis along which this data set will be aligned when drawn
    * @param nativeYAxis
    * 		the Y-axis along which this data set will be aligned when drawn
    * @param minLevel
    * 		the minimum level to which the user will be allowed to zoom
    * @param color
    * 		the color in which to draw these data points (note that
    * 		this does not affect the color of the axes)
    * @throws NullPointerException
    * 		if datasource, xAxis, yAxis, url, or color is <tt>null</tt>
    * @throws IllegalArgumentException
    * 		if xAxis is really a Y-axis, or if yAxis is really an X-axis
    */
   public DataPlot(final JavaScriptObject datasource,
                   final JavaScriptObject nativeXAxis,
                   final JavaScriptObject nativeYAxis,
                   final int minLevel,
                   final Color color) {
      this(datasource, nativeXAxis, nativeYAxis,
         minLevel, color, new LineRenderer(false, true),
         new LineRenderer(true, false));
         // Only the normal renderer needs to draw comments
   }

   public DataPlot(final JavaScriptObject datasource,
                   final JavaScriptObject nativeXAxis,
                   final JavaScriptObject nativeYAxis,
                   final int minLevel,
                   final Color color,
                   final HighlightableRenderer normalRenderer,
                   final HighlightableRenderer highlightRenderer) {
      if (datasource == null || nativeXAxis == null
          || nativeYAxis == null || color == null) {
         throw new NullPointerException(
               "Cannot have a null datasource, axis, or color");
      }

      this.xAxis = nativeXAxis;
      this.yAxis = nativeYAxis;
      if (!getXAxis().isXAxis()) {
         throw new IllegalArgumentException("X-axis must be horizontal");
      }
      if (getYAxis().isXAxis()) {
         throw new IllegalArgumentException("Y-axis must be vertical");
      }

      // register self as an event listener to the axes...
      registerGraphAxisEventListener(getXAxis());
      registerGraphAxisEventListener(getYAxis());

      this.datasource = datasource;
      shouldZoomIn = true;
      this.minLevel = minLevel;

      this.color = color;
      this.normalRenderer = normalRenderer;
      this.highlightRenderer = highlightRenderer;

      currentLevel = Integer.MIN_VALUE;
      currentMinOffset = Integer.MAX_VALUE;
      currentMaxOffset = Integer.MIN_VALUE;

      highlightedPoint = null;

      shouldZoomIn = checkForFetch();
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

   public static DataPlot getDataPlot(JavaScriptObject nativePlot) {
      final Dynamic dynPlot = nativePlot.cast();
      return dynPlot.get("__backingPlot");
   }

   /** Sets the {@link GraphWidget} which contains this <code>DataPlot</code>. */
   public final void registerGraphWidget(final GraphWidget graphWidget) {
      this.containingGraphWidget = graphWidget;
   }

   /**
    * Unregisters the given {@link GraphWidget} from this <code>DataPlot</code> if and only if the given
    * {@link GraphWidget} is not <code>null</code> and is currently registered with this <code>DataPlot</code>. That is,
    * if this <code>DataPlot</code> is already associated with a {@link GraphWidget} other than the given one, then
    * nothing happens.
    */
   public final void unregisterGraphWidget(final GraphWidget graphWidget) {
      if (graphWidget != null && graphWidget.equals(this.containingGraphWidget)) {
         this.containingGraphWidget = null;
      }
   }

   /**
    * Returns <tt>true</tt> if and only if the X-axis is allowed to
    * zoom in farther, based on the zoom policy of this DataPlot.
    *
    * @return
    * 		<tt>true</tt> if the X-axis should be allowed to
    * 		zoom in more, <tt>false</tt> otherwise
    */
   public boolean shouldZoomIn() {
      return shouldZoomIn;
   }

   /**
    * Returns the color at which this <tt>DataPlot</tt> is drawn.
    *
    * @return
    * 		the color used to draw this <tt>DataPlot</tt>
    */
   public Color getColor() {
      return color;
   }

   /**
    * Sets the color at which this <tt>DataPlot</tt> is drawn.
    *
    * @param newColor
    * 		the color that should be used to draw this <tt>DataPlot</tt>
    * @throws NullPointerException
    * 		if newColor is <tt>null</tt>
    */
   public void setColor(final Color newColor) {
      if (newColor == null) {
         throw new NullPointerException(
               "Cannot use null color to draw plot");
      }

      color = newColor;
      if (containingGraphWidget != null) {
         containingGraphWidget.paint();
      }
   }

   /**
    * Checks for and performs a fetch for data from the server if
    * necessary.
    *
    * @return
    * 		<tt>true</tt> if the user should be allowed to zoom past
    * 		this point, <tt>false</tt> if the user shouldn't be allowed
    * 		to zoom past this point
    */
   private boolean checkForFetch() {
      final int correctLevel = computeCurrentLevel();
      final int correctMinOffset = computeMinOffset(correctLevel);
      final int correctMaxOffset = computeMaxOffset(correctLevel);

      if (correctLevel != currentLevel) {
         for (int i = correctMinOffset; i <= correctMaxOffset; i++) {
            fetchFromServer(correctLevel, i);
         }
      } else if (correctMinOffset < currentMinOffset) {
         fetchFromServer(correctLevel, correctMinOffset);
      } else if (correctMaxOffset > currentMaxOffset) {
         fetchFromServer(correctLevel, correctMaxOffset);
      }

      // This way we don't fetch the same data multiple times
      currentLevel = correctLevel;
      currentMinOffset = correctMinOffset;
      currentMaxOffset = correctMaxOffset;

      return correctLevel > minLevel;
   }

   /**
    * Fetches the specified tile from the server.
    *
    * Note that this checks the pendingDescriptions instance variable
    * to determine if this tile has already been requested.  If so,
    * does not request anything from the server.
    *
    * @param level
    * 		the level of the tile to fetch
    * @param offset
    * 		the offset of the tile to fetch
    */
   private void fetchFromServer(final int level, final int offset) {
      final TileDescription desc = new TileDescription(level, offset);

      // Ensures we don't fetch the same tile twice unnecessarily
      if (pendingDescriptions.contains(desc)) {
         return;
      }

      final String tileKey = level + "." + offset;

      // Make sure we don't fetch this again unnecessarily
      pendingDescriptions.add(desc);
      pendingUrls.put(tileKey, 0);

      GrapherTile.retrieveTile(datasource, level, offset, this);
   }

   /**
    * Called every time a new tile loads.
    *
    * @param tile
    * 		the <tt>GrapherTile</tt> representing the tile that loaded
    */
   @Override
   public void onSuccess(final GrapherTile tile) {
      final String tileKey = tile.getLevel() + "." + tile.getOffset();

      pendingData.add(tile);

      if (pendingUrls.containsKey(tileKey)) {
         pendingUrls.remove(tileKey);
      }

      checkForNewData();

      if (containingGraphWidget != null) {
         containingGraphWidget.paint();
      }
   }

   /**
    * Called every time a tile load fails.
    *
    * <p>Tries to re-request the tile.</p>
    *
    * @param tile
    * 		the <tt>GrapherTile</tt> representing the tile that failed
    * 		to load
    */
   @Override
   public void onFailure(final GrapherTile tile) {
      final int level = tile.getLevel();
      final int offset = tile.getOffset();
      final String tileKey = level + "." + offset;

      if (pendingUrls.containsKey(tileKey)) {
         final int oldValue = pendingUrls.get(tileKey);
         if (oldValue > MAX_REQUESTS_PER_URL) {
            return;
         }

         pendingUrls.remove(tileKey);
         pendingUrls.put(tileKey, oldValue + 1);
      } else {
         pendingUrls.put(tileKey, 1);
      }

      GrapherTile.retrieveTile(datasource, level, offset, this);

      if (containingGraphWidget != null) {
         containingGraphWidget.paint();
      }
   }

   /**
    * Paints this DataPlot on the stored GraphWidget.
    *
    * <p>Does not draw the axes associated with this DataPlot.</p>
    *
    * <p>Note that it is <strong>not</strong> recommended that a subclass
    * override this method.  Instead, it is recommended that a subclass
    * override the {@link #paintAllDataPoints} method.</p>
    */
   public void paint(final Canvas canvas, final String newPaintEventId) {
      // guard against redundant paints
      if (previousPaintEventId == null || !previousPaintEventId.equals(newPaintEventId)) {
         previousPaintEventId = newPaintEventId;

         // Draw data points
         canvas.getSurface().setStrokeStyle(color);
         final BoundedDrawingBox drawing = getDrawingBounds(canvas);

         HighlightableRenderer renderer =
            isHighlighted() ? highlightRenderer : normalRenderer;
         renderer.setHighlightedPoint(getHighlightedPoint());
         renderer.render(drawing, getBestResolutionTiles(), xAxis, yAxis);
         renderer.setHighlightedPoint(null);

         // Clean up after ourselves
         canvas.getSurface().setStrokeStyle(Canvas.DEFAULT_COLOR);

         // Make sure we shouldn't get any more info from the server
         shouldZoomIn = checkForFetch();
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
   private BoundedDrawingBox getDrawingBounds(Canvas canvas) {
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
    * Checks to see if we have received data from the server
    */
   private void checkForNewData() {
      if (pendingData.size() > 0) {
         // Pull all the data out of pendingData
         for (final GrapherTile tile : pendingData) {
            if (tile == null) {
               continue;
            }

            currentData.add(tile);

            // Make sure we don't still mark this as pending
            pendingDescriptions.remove(tile.getDescription());
         }

         pendingData.clear();
      }
   }

   /**
    * Returns the ordered list of points this DataPlot should draw
    * in {@link #paintAllDataPoints}.
    *
    * <p>It is acceptable, and not considered an error, if this or a subclass
    * implementation returns <tt>null</tt>.  Such a return should simply
    * be taken as a sign that the specified tile contains no data points
    * that paintAllDataPoints should draw.</p>
    *
    * @param tile
    * 		the {@link GrapherTile GrapherTile}
    * 		from which to pull the data points
    * @return
    * 		a list of
    * 		{@link PlottablePoint PlottablePoint}
    * 		objects to be drawn by paintAllDataPoints
    */
   protected List<PlottablePoint> getDataPoints(final GrapherTile tile) {
      return tile.getDataPoints();
   }

   /**
    * Returns a sorted list of all best resolution tiles available.
    *
    * @return
    * 		a sorted list of all the best resolution tiles in
    * 		currentData
    */
   private List<GrapherTile> getBestResolutionTiles() {

      final List<GrapherTile> best = new ArrayList<GrapherTile>();

      // When minTime and maxTime are used in calculations, they are
      // used to make the calculations scale-independent
      final double minTime = getXAxis().getMin();
      final double maxTime = getXAxis().getMax();

      double maxCoveredTime = minTime;

      final int bestLevel = computeCurrentLevel();

      final double timespan = maxTime - minTime;
      while (maxCoveredTime <= maxTime) {
         final GrapherTile bestAtCurrTime = getBestResolutionTileAt(maxCoveredTime + timespan * 1e-3, bestLevel);
         // We need to move a little to the right of the current time
         // so we don't get the same tile twice

         if (bestAtCurrTime == null) {
            maxCoveredTime += timespan * 1e-2;
         } else {
            best.add(bestAtCurrTime);

            maxCoveredTime = bestAtCurrTime.getDescription().getMaxTime();
         }
      }

      return best;
   }

   /**
    * Returns the best-resolution tile that covers the specified
    * point.
    *
    * @param time
    * 		the time which must be covered by the tile
    * @param bestLevel
    * 		the level to which we want the returned tile to be close
    * @return
    * 		the best-resolution (lowest-level) tile which has min value
    * 		less than or equal to time, and max value greater than or
    * 		equal to time, or <tt>null</tt> if no such tile exists
    */
   private GrapherTile getBestResolutionTileAt(final double time, final int bestLevel) {
      GrapherTile best = null;
      TileDescription bestDesc = null;

      for (final GrapherTile tile : currentData) {
         final TileDescription desc = tile.getDescription();

         if (desc.getMinTime() > time || desc.getMaxTime() < time) {
            continue;
         }

         if (best == null) {
            best = tile;
            bestDesc = desc;
         } else if (Math.abs(desc.getLevel() - bestLevel) <
                    Math.abs(bestDesc.getLevel() - bestLevel)) {
            best = tile;
            bestDesc = desc;
         } else if (Math.abs(desc.getLevel() - bestLevel) ==
                    Math.abs(bestDesc.getLevel() - bestLevel)) {
            if (desc.getLevel() < bestDesc.getLevel()) {
               best = tile;
               bestDesc = desc;
            }
         }
      }

      return best;
   }

   /**
    * Returns the X-Axis for this DataPlot.
    *
    * @return
    * 		the X-axis for this DataPlot
    */
   public GraphAxis getXAxis() {
      Dynamic djso = xAxis.cast();
      return djso.get("__backingAxis");
   }

   public JavaScriptObject getNativeXAxis() {
      return xAxis;
   }

   /**
    * Sets the X-axis for this <tt>DataPlot</tt>.
    *
    * <p>This is only intended to be used within this package.
    * In almost all cases, there is no need for this method.</p>
    *
    * @param axis
    * 		the new X-axis to use
    * @throws NullPointerException
    * 		if axis is <tt>null</tt>
    */
   void setXAxis(final JavaScriptObject axis) {
      if (axis == null) {
         throw new NullPointerException("Cannot use null X-axis");
      }

      // unregister self from the existing axis
      unregisterGraphAxisEventListener(getXAxis());

      xAxis = axis;

      // now register self with this new axis
      registerGraphAxisEventListener(getXAxis());
   }

   /**
    * Returns the Y-Axis for this DataPlot.
    *
    * @return
    * 		the Y-axis for this DataPlot
    */
   public GraphAxis getYAxis() {
      Dynamic djso = yAxis.cast();
      return djso.get("__backingAxis");
   }

   public JavaScriptObject getNativeYAxis() {
      return yAxis;
   }

   /**
    * Sets the Y-axis for this <tt>DataPlot</tt>.
    *
    * <p>This is only intended to be used within this package.
    * In almost all cases, there is no need for this method.</p>
    *
    * @param axis
    * 		the new Y-axis to use
    */
   void setYAxis(final JavaScriptObject axis) {
      if (axis == null) {
         throw new NullPointerException("Cannot use null Y-axis");
      }

      // unregister self from the existing axis
      unregisterGraphAxisEventListener(getYAxis());

      yAxis = axis;

      // now register self with this new axis
      registerGraphAxisEventListener(getYAxis());
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
    * Computes the floor of the log (base 2) of x.
    *
    * @param x
    * 		the value for which we want to take the log
    * @return
    * 		the floor of the log (base 2) of x
    */
   private static int log2(final double x) {
      if (x <= 0) {
         return Integer.MIN_VALUE;
      }

      return (int)Math.floor((Math.log(x) / LN_2));
   }

   /**
    * Returns the offset at which the left edge of the X-axis is operating.
    *
    * Returns the offset of the tile in which the minimum value
    * of the X-axis is found.
    *
    * @param level
    * 		the level at which we assume we are operating when calculating
    * 		offsets
    * @return
    * 		the current offset of the X-axis, based on level
    * 		and the private variable xAxis
    */
   private int computeMinOffset(final int level) {
      final double min = getXAxis().getMin();

      final double tileWidth = getTileWidth(level);

      // Tile offset computation
      return (int)(min / tileWidth);
   }

   /**
    * Returns the offset at which the right edge of the X-axis is operating.
    *
    * Returns the offset of the tile in which the maximum value
    * of the X-axis is found.
    *
    * @param level
    * 		the level at which we assume we are operating when calculating
    * 		offsets
    * @return
    * 		the current offset of the X-axis, based on level
    * 		and the private variable xAxis
    */
   private int computeMaxOffset(final int level) {
      final double max = getXAxis().getMax();

      final double tileWidth = getTileWidth(level);

      // Tile number computation
      return (int)(max / tileWidth);
   }

   /**
    * Returns the width of a single tile.
    *
    * @param level
    * 		the level of the tile for which we will find the width
    * @return
    * 		the width of a tile at the given level
    */
   private static double getTileWidth(final int level) {
      return (new TileDescription(level, 0)).getTileWidth();
   }

   /**
    * Returns a PlottablePoint if and only if there is a point, part of
    * this DataPlot, within threshold pixels of pos.  Otherwise, returns
    * <tt>null</tt>.
    *
    * This actually builds a square of 2 * threshold pixels on each
    * side, centered at pos, and checks if there is a data point within
    * that square, but that is a minor detail that should not affect
    * the workings of this method.
    *
    * @param pos
    *		the mouse position from which to check proximity to a data
    *		point
    * @param threshold
    * 		the maximum distance pos can be from a data point to be
    * 		considered &quot;near&quot; to it
    * @return
    * 		<tt>null</tt> if there is no point within threshold pixels
    * 		of pos, or one of the points, if there is such a point
    * @throws IllegalArgumentException
    * 		if threshold is negative
    */
   public PlottablePoint closest(final Vector2 pos, final double threshold) {
      if (threshold < 0) {
         throw new IllegalArgumentException(
               "Cannot work with a negative distance");
      }

      final double x = pos.getX();
      final double y = pos.getY();

      // Build a square for checking location
      final Vector2 topLeft = new Vector2(x - threshold, y - threshold);
      final Vector2 bottomRight = new Vector2(x + threshold, y + threshold);

      // Now convert that square into a square of times and values
      final double minTime = getXAxis().unproject(topLeft);
      final double maxTime = getXAxis().unproject(bottomRight);
      final double minValue = getYAxis().unproject(bottomRight);
      final double maxValue = getYAxis().unproject(topLeft);

      final double centerTime = getXAxis().unproject(pos);
      final double centerValue = getXAxis().unproject(pos);

      // Don't even bother trying to highlight if the mouse is out of
      // bounds
      if (maxTime < getXAxis().getMin()
          || minTime > getXAxis().getMax()
          || maxValue < getYAxis().getMin()
          || minValue > getYAxis().getMax()) {
         return null;
      }

      // Get the tiles to check
      final int correctLevel = computeCurrentLevel();

      final GrapherTile bestTileMinTime =
            getBestResolutionTileAt(minTime, correctLevel);
      final GrapherTile bestTileMaxTime =
            getBestResolutionTileAt(maxTime, correctLevel);

      final PlottablePoint closest = getClosestPoint(bestTileMinTime,
                                                     minTime,
                                                     maxTime,
                                                     minValue,
                                                     maxValue,
                                                     centerTime,
                                                     centerValue);

      // pos is right on the border between two tiles
      if (bestTileMinTime != bestTileMaxTime) {
         // TODO: should this be an .equals() comparison instead?
         // This is unlikely but possible, especially if threshold
         // is large

         final PlottablePoint closestMaxTime = getClosestPoint(
               bestTileMaxTime, minTime, maxTime, minValue,
               maxValue, centerTime, centerValue);

         final double distClosestSq = getDistanceSquared(closest,
                                                         centerTime,
                                                         centerValue);
         final double distClosestMaxTimeSq =
               getDistanceSquared(closestMaxTime, centerTime, centerValue);

         if (distClosestMaxTimeSq < distClosestSq) {
            return closestMaxTime;
         }
      }

      return closest;
   }

   /**
    * Helper method for {@link DataPlot#closest(Vector2, double)}.
    *
    * This method has a lot of similar parameters, which is normally
    * poor style, but it is an internal helper method, so this is
    * OK.
    *
    * @param tile
    * 		the {@link GrapherTile GrapherTile}
    * 		in which to search for the closest point
    * @param minTime
    * 		the minimum time at which we consider points
    * @param maxTime
    * 		the maximum time at which we consider points
    * @param minValue
    * 		the minimum value of a point for us to consider it
    * @param maxValue
    * 		the maximum value of a point at which we will consider it
    * @param centerTime
    * 		the time to which we will try to make our point close
    * @param centerValue
    * 		the value to which we will try to make our point close
    * @return
    * 		the point closest to (centerTime, centerValue)
    * 		in getDataPoints(tile), as long as that point is within the
    * 		square determined by (minTime, minValue) and
    * 		(maxTime, maxValue) and visible to the user.  If there is no
    * 		such point, returns <tt>null</tt>
    */
   private PlottablePoint getClosestPoint(final GrapherTile tile,
                                          final double minTime,
                                          final double maxTime,
                                          final double minValue,
                                          final double maxValue,
                                          final double centerTime,
                                          final double centerValue) {
      if (tile == null) {
         return null;
      }

      final List<PlottablePoint> points = getDataPoints(tile);
      if (points == null) {
         return null;
      }

      PlottablePoint closest = null;
      double shortestDistanceSq = Double.MAX_VALUE;
      for (final PlottablePoint point : points) {
         final double time = point.getDate();
         final double val = point.getValue();

         // Only check for proximity to points we can see
         if (time < getXAxis().getMin() || time > getXAxis().getMax()) {
            continue;
         }
         if (val < getYAxis().getMin() || val > getYAxis().getMax()) {
            continue;
         }

         // Only check for proximity to points within the desired
         // range
         if (time >= minTime && time <= maxTime
             && val >= minValue && val <= maxValue) {

            // If we don't have a value for closest, any point
            // in the specified range is closer
            if (closest == null) {
               closest = point;
               continue;
            }

            // Compute the square of the distance to pos
            final double distanceSq = getDistanceSquared(point,
                                                         centerTime,
                                                         centerValue);

            if (distanceSq < shortestDistanceSq) {
               closest = point;
               shortestDistanceSq = distanceSq;
            }
         }
      }

      return closest;
   }

   /**
    * Returns the square of the distance from point to (time, value).
    *
    * @param point
    * 		the first of the two points
    * @param time
    * 		the time for the second point
    * @param value
    * 		the distance for the second point
    * @return
    * 		the square of the distance from point to (time, value), or
    * 		{@link Double#MAX_VALUE} if point is <tt>null</tt>
    */
   private double getDistanceSquared(final PlottablePoint point,
                                     final double time,
                                     final double value) {
      if (point == null) {
         return Double.MAX_VALUE;
      }

      final double pointTime = point.getDate();
      final double pointValue = point.getValue();

      return (time - pointTime) * (time - pointTime)
             + (value - pointValue) * (value - pointValue);
   }

   /**
    * Highlights this DataPlot in future
    * {@link DataPlot#paint() paint} calls.
    *
    * <p>Note that this does not highlight the axes associated with this
    * DataPlot.</p>
    */
   public void highlight() {
      highlightedPoint = AbstractPlotRenderer.HIGHLIGHTED_NO_SINGLE_POINT;
   }

   /**
    * Stops highlighting this DataPlot.
    *
    * <p>Note that this does not affect the highlighting status on the
    * axes associated with this DataPlot.</p>
    */
   public void unhighlight() {
      highlightedPoint = null;
   }

   /**
    * Tells whether or not this DataPlot is highlighted.
    *
    * <p>If {@link #highlight()} has been called since the constructor
    * and since the last call to {@link #unhighlight()}, returns
    * <tt>true</tt>.  Otherwise, returns <tt>false</tt>.</p>
    *
    * @return
    * 		<tt>true</tt> if and only if this DataPlot is highlighted
    */
   public boolean isHighlighted() {
      return highlightedPoint != null;
   }

   /**
    * Highlights this <tt>DataPlot</tt> if and only if it contains a
    * point within threshold pixels of pos.
    *
    * <p>Also, if this data plot should be highlighted, this publishes
    * the highlighted data plot's value to the container widget, as long
    * as such a preference was indicated when this <tt>DataPlot</tt>
    * was created, using the publishValueOnHighlight constructor parameter.
    * On the other hand, if this data plot is currently highlighted but
    * should be unhighlighted, this removes the published value.  If a
    * subclass would like to change the formatting of the published
    * value, it should accomplish that by overriding
    * {@link #getDataLabel(PlottablePoint)}.</p>
    *
    * <p>Note that this does <strong>not</strong> unhighlight this
    * <tt>DataPlot</tt> if there is no point within threshold pixels of
    * pos.  A subclass may also change the measurement unit on threshold
    * (the unit is pixels here), as long as that fact is clearly
    * documented.</p>
    *
    * @param pos
    * 		the position at which the mouse is hovering, and from which
    * 		we want to derive our highlighting
    * @param threshold
    * 		the maximum distance the mouse can be from a point, while
    * 		still causing the highlighting effects
    * @return
    * 		<tt>true</tt> if and only if this highlights the axes
    * @throws IllegalArgumentException
    * 		if threshold is negative
    */
   public boolean highlightIfNear(final Vector2 pos, final double threshold) {
      highlightedPoint = closest(pos, threshold);
      return isHighlighted();
   }

   /**
    * Returns a label for the specified point.
    *
    * <p>This implementation takes the value of p out to three
    * significant digits and returns that value.  However, subclass
    * implementations might behave differently.</p>
    *
    * <p>This is designed to be overridden by subclasses that wish
    * to change the default behavior.  However, there are a few
    * requirements for subclass implementations, which unfortunately
    * cannot be expressed in code.  A subclass implementation of
    * this method must always return a non-<tt>null</tt> label in
    * finite (preferably very short) time, and must never throw
    * an exception.</p>
    *
    * @param p
    * 		the point for which to return a data label
    * @return
    * 		a data label to be displayed for p
    */
   protected String getDataLabel(final PlottablePoint p) {
      final double value = p.getValue();
      final double absValue = Math.abs(value);

      final String timeString = getTimeString(p.getDate()) + "   ";

      if (absValue == 0.0) // Rare, but possible
      {
         return timeString + "0.0";
      }

      if (absValue < 1e-3 || absValue > 1e7) {
         return timeString
                + NumberFormat.getScientificFormat().format(value);
      }

      return timeString
             + NumberFormat.getFormat("###,##0.0##").format(value);
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
            GWT.log("DataPlot.getTimeString(): Unexpected number of "
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

   /**
    * Returns the highlighted point maintained by this <tt>DataPlot</tt>.
    *
    * @return
    * 		the highlighted point this <tt>DataPlot</tt> keeps, or
    * 		<tt>null</tt> if there is no highlighted point
    */
   public PlottablePoint getHighlightedPoint() {
      return highlightedPoint;
   }
}
