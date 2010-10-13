package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single set of data, along with its associated axes.
 * 
 * Has the ability to draw itself and its axes on a
 * {@link org.bodytrack.client.Canvas Canvas} object, and to update
 * the positions of its dots based on the zoom level.  Also, if the
 * zoom level changes enough, the {@link #zoom(double, double) zoom()}
 * method will automatically fetch the data from the server via Ajax
 * and redraw the data whenever the data comes in from the server.
 */
public class DataPlot {
	private GraphWidget container;
	private GraphAxis xAxis;
	private GraphAxis yAxis;
	private Canvas canvas;
	
	// Values related to getting new values from the server
	private String baseUrl;
	private List<GrapherTile> pendingData;
	
	// Determining whether or not we should retrieve more data from
	// the server
	private int currentLevel;
	
	private static final double LOWER_BOUND_FACTOR = 1.0 / Math.sqrt(2);
	private static final double UPPER_BOUND_FACTOR = Math.sqrt(2);
	
	/**
	 * Main constructor for the DataPlot object.
	 * 
	 * The parameter url is the trickiest to get right.  This parameter
	 * should be the <strong>beginning</strong> (the text up to, but
	 * not including, the &lt;level&gt;.&lt;offset&gt;.json part of the
	 * URL to fetch) of the URL which will be used to get more data.
	 * Note that this <strong>must</strong> be a trusted BodyTrack
	 * URL.  As described in the documentation for
	 * {@link org.bodytrack.client.GrapherTile#retrieveTile(String, List)
	 *  GrapherTile.retriveTile()}, an untrusted connection could allow
	 * unauthorized access to all of a user's data.
	 * 
	 * @param container
	 * 		the {@link org.bodytrack.client.GraphWidget GraphWidget} on
	 * 		which this DataPlot will draw itself and its axes
	 * @param xAxis
	 * 		the X-axis along which this data set will be aligned when
	 * 		drawn.  Usually this is a
	 * 		{@link org.bodytrack.client.TimeGraphAxis TimeGraphAxis}
	 * @param yAxis
	 * 		the Y-axis along which this data set will be aligned when
	 * 		drawn
	 * @param url
	 * 		the beginning of the URL for fetching this data with Ajax
	 * 		calls
	 * @param level
	 * 		the current zoom level at which a 
	 * @throws NullPointerException
	 * 		if container or url is <tt>null</tt>.  If xAxis or yAxis is
	 * 		<tt>null</tt>, then a new axis will be created and
	 * 		registered with container
	 */
	public DataPlot(GraphWidget container, GraphAxis xAxis, GraphAxis yAxis,
			String url, int level) {
		if (container == null || url == null)
			throw new NullPointerException(
				"Cannot have a null container or url");
		
		this.container = container;
		// TODO: check for null on xAxis and yAxis
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		
		baseUrl = url;
		
		canvas = new Canvas(this.container);
		pendingData = new ArrayList<GrapherTile>();
	}
	
	public void zoom(double factor, double about) {
		xAxis.zoom(factor, about);
		yAxis.zoom(factor, about);
		
		// TODO: keep track of current width of xAxis, and call
		// fetchFromServer if necessary
		
	}
	
	private void fetchFromServer(int level, int offset) {
		String url = baseUrl + level + "." + offset + ".json";
		
		GrapherTile.retrieveTile(url, pendingData);
	}
	
	/**
	 * Paints this DataPlot on the stored GraphWidget.
	 */
	public void paint() {
		// TODO: keep track of current data, and always check for values
		// in pendingData
		
	}
}
