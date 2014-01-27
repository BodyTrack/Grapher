package org.bodytrack.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bodytrack.client.DataPointListener.TriggerAction;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Represents a single set of data, along with references to its
 * associated axes.
 *
 * <p>
 * Has the ability to draw itself and its axes on a
 * {@link GrapherCanvas} object, and to update
 * the positions of its dots based on the zoom level.  Also, if the
 * zoom level or position of the X-axis changes enough, this class will
 * automatically fetch the data from the server via Ajax and redraw
 * the data whenever it comes in from the server.
 * </p>
 *
 * <p>
 * A class that wishes to inherit this class can override
 * {@link DataSeriesPlot#paintAllDataPoints}, but the easiest way to modify
 * functionality it to override {@link DataSeriesPlot#paintDataPoint} and
 * {@link DataSeriesPlot#paintEdgePoint(BoundedDrawingBox, GrapherTile, double, double, PlottablePoint)}.
 * These two functions are responsible for painting a single point on
 * this DataSeriesPlot.  This (parent) class will automatically handle
 * highlighting, zooming, and the Ajax calls for pulling extra data
 * from the server.
 * </p>
 *
 * <p>
 * A class that wishes to inherit this class may also wish to
 * override {@link DataSeriesPlot#getDataPoints(GrapherTile)}, which
 * determines the points that {@link DataSeriesPlot#paintAllDataPoints}
 * will draw, and the order in which paintAllDataPoints will draw them.
 * </p>
 */
public class DataSeriesPlot extends BaseSeriesPlot {

    private static final double HIGHLIGHT_DISTANCE_THRESHOLD = 5;
    private final SeriesPlotRenderer renderer;

    public static DataSeriesPlot getDataSeriesPlot(final JavaScriptObject nativePlot) {
        final Dynamic dynPlot = nativePlot.cast();
        return dynPlot.get("__backingPlot");
    }

    /**
     * Main constructor for the {@link DataSeriesPlot} object.
     *
     * @param datasource
     *  A native JavaScript function which can be used to retrieve tiles
     * @param nativeXAxis
     *  The X-axis along which this data set will be aligned when drawn
     * @param nativeYAxis
     *  The Y-axis along which this data set will be aligned when drawn
     * @param styleJson
     *  The JSON style
     * @param localDisplay
     *  <code>true</code> if the data from the server should be shifted to
     *  pretend that all time offsets are local offsets, and <code>false</code>
     *  if the data from the server should be displayed as is
     * @throws NullPointerException
     *  If datasource, nativeXAxis, nativeYAxis, or styleJson is <code>null</code>
     * @throws IllegalArgumentException
     *  If xAxis is really a Y-axis, or if yAxis is really an X-axis
     */
    public DataSeriesPlot(final JavaScriptObject datasource,
            final JavaScriptObject nativeXAxis,
            final JavaScriptObject nativeYAxis,
            final JavaScriptObject styleJson,
            final boolean localDisplay) {
        // The superclass constructor checks for null in its parameters
        super(datasource, nativeXAxis, nativeYAxis, localDisplay);
        this.renderer = new DataSeriesPlotRenderer(styleJson.<StyleDescription>cast());
    }

    @Override
    protected SeriesPlotRenderer getRenderer() {
        return renderer;
    }

    /**
     * Returns the ordered list of points this DataSeriesPlot should draw.
     *
     * <p>
     * It is acceptable, and not considered an error, if this or a subclass
     * implementation returns <code>null</code>.  Such a return should simply
     * be taken as a sign that the specified tile contains no data points
     * that should be drawn.
     * </p>
     *
     * @param tile
     *  The {@link GrapherTile GrapherTile} from which to pull the data points
     * @return
     *  A list of {@link PlottablePoint} objects to be drawn
     */
    protected List<PlottablePoint> getDataPoints(final GrapherTile tile) {
        return tile.getDataPoints();
    }

    /**
     * Returns a PlottablePoint if and only if there is a point, part of
     * this DataSeriesPlot, within threshold pixels of pos.  Otherwise, returns
     * <code>null</code>.
     *
     * <p></p>
     * This actually builds a square of 2 * threshold pixels on each
     * side, centered at pos, and checks if there is a data point within
     * that square, but that is a minor detail that should not affect
     * the workings of this method.
     * </p>
     *
     * @param pos
     *  The mouse position from which to check proximity to a data point
     * @param threshold
     *  The maximum distance pos can be from a data point to be
     *  considered &quot;near&quot; to it
     * @return
     *  <code>null</code> if there is no point within threshold pixels
     *  of pos, or one of the points, if there is such a point
     * @throws IllegalArgumentException
     *  If threshold is negative
     */
    private PlottablePoint closest(final Vector2 pos, final double threshold) {
        if (threshold < 0)
            throw new IllegalArgumentException("Cannot work with a negative distance");

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

        final GrapherTile bestTileMinTime = getBestResolutionTileAt(minTime);
        final GrapherTile bestTileMaxTime = getBestResolutionTileAt(maxTime);

        final PlottablePoint closest = getClosestPoint(bestTileMinTime,
                minTime, maxTime, minValue, maxValue, centerTime, centerValue);

        // pos is right on the border between two tiles (TODO: should this be an .equals() comparison instead?)
        if (bestTileMinTime != bestTileMaxTime) {
            // This is unlikely but possible, especially if threshold
            // is large

            final PlottablePoint closestMaxTime = getClosestPoint(
                    bestTileMaxTime, minTime, maxTime, minValue,
                    maxValue, centerTime, centerValue);

            final double distClosestSq = getDistanceSquared(closest,
                    centerTime, centerValue);
            final double distClosestMaxTimeSq =
                    getDistanceSquared(closestMaxTime, centerTime, centerValue);

            if (distClosestMaxTimeSq < distClosestSq)
                return closestMaxTime;
        }

        return closest;
    }
    
    public void onClick(final Vector2 pos) { 
    	if (highlightedPoint != null && getXAxis() != null){
    		getXAxis().setCursorPosition(highlightedPoint.getDate());    
    		publishDataPoint(highlightedPoint, TriggerAction.CLICK);
    	}
    	
    }
    
    public void doCursorClick(){
    	PlottablePoint cursorPoint = getCursorHighlightedPoint();
    	if (cursorPoint != null){
    		publishDataPoint(cursorPoint, TriggerAction.CLICK);
    	}
    }

    /**
     * Helper method for {@link DataSeriesPlot#closest(Vector2, double)}.
     *
     * @param tile
     *  The {@link GrapherTile} in which to search for the closest point
     * @param minTime
     *  The minimum time at which we consider points
     * @param maxTime
     *  The maximum time at which we consider points
     * @param minValue
     *  The minimum value of a point for us to consider it
     * @param maxValue
     *  The maximum value of a point at which we will consider it
     * @param centerTime
     *  The time to which we will try to make our point close
     * @param centerValue
     *  The value to which we will try to make our point close
     * @return
     * 	The point closest to (centerTime, centerValue)
     * 	in <code>getDataPoints(tile)</code>, as long as that point is within the
     * 	square determined by (minTime, minValue) and (maxTime, maxValue) and
     * 	visible to the user.  If there is no such point, returns <code>null</code>
     */
    private PlottablePoint getClosestPoint(final GrapherTile tile,
            final double minTime,
            final double maxTime,
            final double minValue,
            final double maxValue,
            final double centerTime,
            final double centerValue) {
        if (tile == null)
            return null;

        final List<PlottablePoint> points = getDataPoints(tile);

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
                        centerTime, centerValue);

                if (distanceSq < shortestDistanceSq) {
                    closest = point;
                    shortestDistanceSq = distanceSq;
                }
            }
        }

        return closest;
    }

    private double getDistanceSquared(final PlottablePoint point,
            final double time,
            final double value) {
        if (point == null)
            return Double.MAX_VALUE;

        final double pointTime = point.getDate();
        final double pointValue = point.getValue();

        return (time - pointTime) * (time - pointTime)
                + (value - pointValue) * (value - pointValue);
    }

    /**
     * Highlights this {@link DataSeriesPlot} if and only if it contains a
     * point within threshold pixels of pos.
     *
     * <p>
     * Note that this does <strong>not</strong> unhighlight this
     * {@link DataSeriesPlot} if there is no point within threshold pixels of
     * pos.  A subclass may also change the measurement unit on threshold
     * (the unit is pixels here), as long as that fact is clearly
     * documented.
     * </p>
     *
     * @param pos
     *  The position at which the mouse is hovering, and from which we want
     *  to derive our highlighting
     * @return
     *  <code>true</code> if and only if this highlights the axes
     */
    @Override
    public boolean highlightIfNear(final Vector2 pos) {
    	
        setHighlightedPoint(closest(pos, HIGHLIGHT_DISTANCE_THRESHOLD));
        publishHighlightedValue();
        return isHighlighted();
    }
    
    private native void callAfterload(final JavaScriptObject afterload, final JavaScriptObject arg)/*-{
    	if (typeof afterload == "function")
    		afterload(arg);
    	
    }-*/;

    public final JavaScriptObject getStatistics(final double xMin, final double xMax,
            final JsArrayString fieldnames, final JavaScriptObject afterload) {
        if (xMin > xMax) {
            return JavaScriptObject.createObject();
        }
        
        

        final boolean dataPending = checkForData(xMin, xMax,
                new TileLoader.EventListener() {
                    @Override
                    public void handleLoadSuccess() {
                        callAfterload(afterload,calculateStatistics(xMin, xMax, fieldnames));
                    }

                    @Override
                    public void handleLoadFailure() {
                        callAfterload(afterload,null);
                    }
                });

        final Dynamic result = (Dynamic)calculateStatistics(xMin, xMax, fieldnames);
        result.set("data_pending", JsUtils.convertBoolean(dataPending));
        if (!dataPending){
        	callAfterload(afterload,result);        	
        }
        
        return result;
    }

    private JavaScriptObject calculateStatistics(final double xMin, final double xMax,
            final JsArrayString fieldnames) {
        final Map<String,Object> stats = getStatsForTimespan(xMin, xMax);
        
		return fillStatisticsDictionary(stats);
    }

    private native JavaScriptObject fillStatisticsDictionary(Map<String,Object> stats) /*-{
    	
    	var result = {};
    	result.count = stats.@java.util.Map::get(Ljava/lang/Object;)("count").@java.lang.Integer::intValue()();
    	result.has_data = result.count != 0;
    	if (result.has_data){
    		result.y_min = stats.@java.util.Map::get(Ljava/lang/Object;)("yMin").@java.lang.Double::doubleValue()();
    		result.y_max = stats.@java.util.Map::get(Ljava/lang/Object;)("yMax").@java.lang.Double::doubleValue()();
    	} 
    	
    	var sideChannelCounts = stats.@java.util.Map::get(Ljava/lang/Object;)("sideChannelCounts");
    	var sideChannelKeys = sideChannelCounts.@java.util.Map::keySet()();
    	sideChannelKeys = sideChannelKeys.@java.util.Set::toArray()();
    	result.sideChannelCounts = {};
    	for (var i = 0, li = sideChannelKeys.length; i < li; i++){
    		result.sideChannelCounts[sideChannelKeys[i]] = sideChannelCounts.@java.util.Map::get(Ljava/lang/Object;)(sideChannelKeys[i]).@java.lang.Integer::intValue()();
    	}
    	
    	
        return result;
    }-*/;

    private Map<String,Object> getStatsForTimespan(final double xMin, final double xMax) {
        final List<GrapherTile> tiles = getTileLoader().getBestResolutionTiles(xMin, xMax);
        
        Map<String,Object> stats = new HashMap<String,Object>();
        Map<String,Integer> extraCounts = new HashMap<String,Integer>();

        double yMin = Double.MAX_VALUE;
        double yMax = -Double.MIN_VALUE;
        int count = 0;

        for (final GrapherTile tile : tiles) {
            for (final PlottablePoint pt : getDataPoints(tile)) {
            	
                final double time = pt.getDate();
                if (time < xMin || time > xMax) {
                    continue;
                }
                final double val = pt.getValue();
                if (val < SeriesPlotRenderer.MIN_DRAWABLE_VALUE || Double.isInfinite(val)) {
                    continue;
                }
                count += pt.getCount();
                if (val < yMin) {
                    yMin = val;
                }
                if (val > yMax) {
                    yMax = val;
                }
                if (pt.getComment() != null){
                	Integer value = extraCounts.get("comment");
                	if (value == null) value = 0;
                	extraCounts.put("comment", value + pt.getCount());
                }
                for (String sideChannel : pt.getSideChannelNames()){
                	Integer value = extraCounts.get(sideChannel);
                	if (value == null) value = 0;
                	extraCounts.put(sideChannel,value + pt.getCount());
                }
            }
        }
        
        stats.put("yMin", yMin);
        stats.put("yMax", yMax);
        stats.put("count", count);
        stats.put("sideChannelCounts",extraCounts);

        return stats;
    }
}
