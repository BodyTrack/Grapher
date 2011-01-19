package org.bodytrack.client;

/**
 * A class to show photos on a
 * {@link org.bodytrack.client.GraphWidget GraphWidget}.
 */
public class PhotoDataPlot extends DataPlot {
	/**
	 * Initializes a new PhotoDataPlot.
	 *
	 * @param container
	 * 		the container on which we draw images for the user
	 * @param xAxis
	 * 		the X-axis that will determine the position of images
	 * 		on container
	 * @param yAxis
	 * 		the Y-axis that will determine the position of images
	 * 		on container
	 * @param url
	 * 		the base URL for retrieving images
	 * @param minLevel
	 * 		the minimum level to which the user will be allowed to zoom
	 * @throws NullPointerException
	 * 		if any parameter is <tt>null</tt>
	 * @see DataPlot#DataPlot(GraphWidget, GraphAxis, GraphAxis, String,
	 * 		int, gwt.g2d.client.graphics.Color)
	 */
	public PhotoDataPlot(GraphWidget container, GraphAxis xAxis,
			GraphAxis yAxis, String url, int minLevel) {
		super(container, xAxis, yAxis, url, minLevel,
			Canvas.DEFAULT_COLOR);
	}
}
