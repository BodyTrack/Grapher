package org.bodytrack.client;

import gwt.g2d.client.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.ImageElement;

/**
 * A class to show photos on a
 * {@link org.bodytrack.client.GraphWidget GraphWidget}.
 */
public class PhotoDataPlot extends DataPlot {
	private final int userId;

	// Images tells us which images are associated with each PlottablePoint
	// we have to draw
	// TODO: Handle multiple images in a second (this version is problematic
	// because, if two images have times within a second of each other, they
	// will crowd out each other in the map)
	private final Map<PlottablePoint, PhotoGetter> images;

	// TODO: Will be used when we implement highlighting
	private PhotoGetter highlightedImage;

	private static final double IMAGE_Y_VALUE =
		PhotoGraphAxis.PHOTO_CENTER_LOCATION;

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
	 * 		the base URL for retrieving JSON descriptions of images,
	 * 		<em>NOT</em> for getting the images themselves
	 * @param userId
	 * 		the user ID of the current user
	 * @param minLevel
	 * 		the minimum level to which the user will be allowed to zoom
	 * @throws NullPointerException
	 * 		if any parameter is <tt>null</tt>
	 * @see DataPlot#DataPlot(GraphWidget, GraphAxis, GraphAxis, String,
	 * 		int, gwt.g2d.client.graphics.Color)
	 */
	public PhotoDataPlot(GraphWidget container, GraphAxis xAxis,
			PhotoGraphAxis yAxis, String url, int userId, int minLevel) {
		super(container, xAxis, yAxis, url, minLevel,
			Canvas.DEFAULT_COLOR);
		// Note that we know that our Y-axis is a PhotoGraphAxis,
		// so we will be able to cast freely later on

		this.userId = userId;
		images = new HashMap<PlottablePoint, PhotoGetter>();

		highlightedImage = null;
	}

	/**
	 * Returns the points that will form the centers of the images.
	 *
	 * <p>This also has the hidden side effect of starting to load
	 * any unseen images in tile, thus making our drawing possible.
	 * There is some subtlety here: the call to this method populates
	 * the set of available images whenever called, which happens
	 * to be every time the points are needed in
	 * {@link org.bodytrack.client.DataPlot DataPlot} methods.  The
	 * methods {@link #paintDataPoint(BoundedDrawingBox, double,
	 * double, double, double)} and
	 * {@link #highlightIfNear(Vector2, double)} expect this images to
	 * be filled, which is always the case whenever they use elements
	 * of images, just by the way the code is written.</p>
	 */
	@Override
	protected List<PlottablePoint> getDataPoints(GrapherTile tile) {
		// There is no data in the tile
		if (tile.getPhotoDescriptions() == null)
			return new ArrayList<PlottablePoint>();

		List<PhotoDescription> descs = tile.getPhotoDescriptions();
		List<PlottablePoint> result = new ArrayList<PlottablePoint>();

		for (PhotoDescription desc: descs) {
			PlottablePoint pos = new PlottablePoint(
				desc.getBeginDate(), IMAGE_Y_VALUE);

			// This depends on the fact that equality of PlottablePoint
			// objects is based on equality of the floors of the times
			// of the objects (this is robust to small variations in
			// even the same object)
			if (! images.containsKey(pos))
				images.put(pos, new PhotoGetter(userId, desc.getId()));

			result.add(pos);
		}

		return result;
	}

	/**
	 * Draws nothing, since we handle edge points and normal points in
	 * the same way in this class.
	 */
	@Override
	protected void paintEdgePoint(BoundedDrawingBox drawing, double x,
			double y) { }

	/**
	 * Draws the images that are matched with x.
	 *
	 * <p>This does nothing except draw the images matched with x, ignoring
	 * all other parameters, since the Y-values on our points are just
	 * dummy values anyway, and since we don't draw lines between successive
	 * points.</p>
	 */
	@Override
	protected void paintDataPoint(BoundedDrawingBox drawing, double prevX,
			double prevY, double x, double y) {
		PhotoGetter photo = images.get(new PlottablePoint(x, y));
		if (photo == null)
			// This shouldn't ever occur
			return;

		// TODO: If photo == highlightedImage, we need to draw at a
		// different size

		ImageElement imageElem = photo.getElement();

		// Get the current dimensions on elem
		int originalWidth = imageElem.getWidth();
		int originalHeight = imageElem.getHeight();

		double widthToHeight = ((double) originalWidth) / originalHeight;

		// Get the correct dimensions for image
		int height = (int) Math.round(getPhotoHeight());
		int width = (int) Math.round(height * widthToHeight);

		// Set the proper dimensions on elem, maintaining aspect ratio
		imageElem.setWidth(width);
		imageElem.setHeight(height);

		// Now get the corner positions of the image (in pixels)
		double xMin = x - (width / 2.0);
		double xMax = x + (width / 2.0);
		double yMin = y - (height / 2.0);
		double yMax = y + (height / 2.0);

		// Draw a border around the image
		drawing.drawLineSegment(xMin, yMin, xMin, yMax);
		drawing.drawLineSegment(xMin, yMax, xMax, yMax);
		drawing.drawLineSegment(xMax, yMax, xMax, yMin);
		drawing.drawLineSegment(xMax, yMin, xMin, yMin);

		// Now draw the image itself
		getCanvas().getSurface().drawImage(imageElem,
			new Vector2(xMin, yMin));
	}

	/**
	 * Returns the height the photo should take up, in pixels.
	 *
	 * @return
	 * 		the height the photo should take up, in pixels
	 */
	private double getPhotoHeight() {
		GraphAxis yAxis = getYAxis();

		// Note that 0 has a lower Y-value than PHOTO_HEIGHT, since
		// higher values have smaller Y-values in pixels
		return yAxis.project2D(0).getY() -
			yAxis.project2D(PhotoGraphAxis.PHOTO_HEIGHT).getY();
	}

	/**
	 * Exactly the same as {@link DataPlot#highlightIfNear(Vector2, double)},
	 * except that this also figures out <em>which</em> photo should be
	 * highlighted for the user.
	 */
	@Override
	public boolean highlightIfNear(Vector2 pos, double threshold) {
		PlottablePoint point = closest(pos, threshold);
		if (point != null) {
			highlightedImage = images.get(point);
			highlight();
		}

		return point != null;
	}
}
