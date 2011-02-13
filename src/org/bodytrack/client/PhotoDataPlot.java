package org.bodytrack.client;

import gwt.g2d.client.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to show photos on a
 * {@link org.bodytrack.client.GraphWidget GraphWidget}.
 */
public class PhotoDataPlot extends DataPlot {
	private final int userId;

	// Images tells us which images are associated with each PlottablePoint
	// we have to draw
	private final Map<PlottablePoint, Set<PhotoGetter>> images;

	private final PhotoAlertable loadListener;
	private final Map<PhotoGetter, Integer> loadingText;

	private Set<PhotoGetter> highlightedImages;

	// Used so we only need to move a pointer to indicate that we have
	// no highlighted images
	private static final Set<PhotoGetter> EMPTY_HIGHLIGHTED_IMAGES_SET =
		new HashSet<PhotoGetter>();

	private static final double IMAGE_Y_VALUE =
		PhotoGraphAxis.PHOTO_CENTER_LOCATION;

	/**
	 * Ratio of heights between highlighted image and regular image.
	 */
	private static final double HIGHLIGHTED_SIZE_RATIO = 1.2;

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

		images = new HashMap<PlottablePoint, Set<PhotoGetter>>();
		loadListener = new PhotoAlertable();
		loadingText = new HashMap<PhotoGetter, Integer>();
		highlightedImages = new HashSet<PhotoGetter>();
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
			if (! images.containsKey(pos)) {
				Set<PhotoGetter> newValue = new HashSet<PhotoGetter>();
				newValue.add(loadPhoto(userId, desc.getId()));
				images.put(pos, newValue);
			} else {
				Set<PhotoGetter> value = images.get(pos);

				boolean haveDesc = false;

				for (PhotoGetter photo: value) {
					if (photo.getImageId() == desc.getId()
							&& photo.getUserId() == userId) {
						haveDesc = true;
						break;
					}
				}

				if (! haveDesc)
					value.add(loadPhoto(userId, desc.getId()));
			}

			// Now handle the PlottablePoint we just generated
			result.add(pos);
		}

		return result;
	}

	/**
	 * Loads the specified photo, and adds loading text to the
	 * container.
	 *
	 * @param userId
	 * 		the ID of the current user
	 * @param photoId
	 * 		the ID of the photo
	 * @return
	 * 		the {@link org.bodytrack.client.PhotoGetter PhotoGetter}
	 * 		that can be used to draw the photo we just requested
	 */
	private PhotoGetter loadPhoto(int userId, int photoId) {
		PhotoGetter photo = PhotoGetter.buildPhotoGetter(userId,
			photoId, loadListener);

		loadingText.put(photo,
			getContainer().addLoadingMessage(photo.getUrl()));

		return photo;
	}

	/**
	 * Removes loading text for the photo from the container
	 * in which this draws itself.
	 *
	 * @param photo
	 * 		the photo that has just loaded (or failed)
	 * @return
	 * 		<tt>true</tt> if we actually remove text from the
	 * 		container, <tt>false</tt> if there was no text to
	 * 		begin with for this particular image
	 */
	private boolean removePhotoLoadingText(PhotoGetter photo) {
		boolean contains = loadingText.containsKey(photo);

		if (contains) {
			int msgId = loadingText.get(photo);
			getContainer().removeLoadingMessage(msgId);
			loadingText.remove(photo);
		}

		return contains;
	}

	/**
	 * Draws the images at the specified point.
	 *
	 * <p>Although we handle edge points and regular points in the same
	 * way in this class, we still need to draw all the images, so this
	 * does exactly the same thing that {@link
	 * #paintDataPoint(BoundedDrawingBox, double, double, double, double)
	 * paintDataPoint} does.</p>
	 *
	 * @param drawing
	 * 		the bounding box that constrains where photos will draw
	 * @param x
	 * 		the X-value (in pixels) at which we draw the image
	 * @param y
	 * 		the Y-value (in pixels) at which we draw the image
	 */
	@Override
	protected void paintEdgePoint(BoundedDrawingBox drawing, double x,
			double y) {
		drawAllImagesAtPoint(drawing, x, y);
	}

	/**
	 * Draws the images that are matched with x.
	 *
	 * <p>This does nothing except draw the images matched with x, ignoring
	 * all other parameters, since the Y-values on our points are just
	 * dummy values anyway, and since we don't draw lines between successive
	 * points.</p>
	 *
	 * @param drawing
	 * 		the bounding box that constrains where photos will draw
	 * @param prevX
	 * 		ignored
	 * @param prevY
	 * 		ignored
	 * @param x
	 * 		the X-value (in pixels) at which we draw the image
	 * @param y
	 * 		the Y-value (in pixels) at which we draw the image
	 */
	@Override
	protected void paintDataPoint(BoundedDrawingBox drawing, double prevX,
			double prevY, double x, double y) {
		drawAllImagesAtPoint(drawing, x, y);
	}

	/**
	 * Helper method that actually implements <tt>paintEdgePoint</tt>
	 * and <tt>paintDataPoint</tt>.
	 *
	 * @param drawing
	 * 		the bounding box that constrains where photos will draw
	 * @param x
	 * 		the X-value (in pixels) at which we draw the image
	 * @param y
	 * 		the Y-value (in pixels) at which we draw the image
	 */
	private void drawAllImagesAtPoint(BoundedDrawingBox drawing, double x,
			double y) {
		// We stored data in images under the logical X-value (time), not
		// under a pixel value
		double unprojectedX = getXAxis().unproject(new Vector2(x, y));

		Set<PhotoGetter> photos = images.get(
			new PlottablePoint(unprojectedX, IMAGE_Y_VALUE));
		if (photos == null || photos.size() == 0)
			// This shouldn't ever occur
			return;

		for (PhotoGetter photo: photos)
			drawPhoto(drawing, x, y, photo);
	}

	/**
	 * Draws a single photo at the specified X position.
	 *
	 * @param drawing
	 * 		the <tt>BoundedDrawingBox</tt> we use to draw only in
	 * 		bounds
	 * @param x
	 * 		the X-value (in pixels) at which the center of the image
	 * 		should be drawn
	 * @param y
	 * 		the Y-value (in pixels) at which the center of the image
	 * 		should be drawn
	 * @param photo
	 * 		the photo to draw at point x
	 */
	private void drawPhoto(BoundedDrawingBox drawing, double x,
			double y, PhotoGetter photo) {
		// Get the dimensions on photo
		double originalWidth = photo.getOriginalWidth();
		double originalHeight = photo.getOriginalHeight();

		double widthToHeight = ((double) originalWidth) / originalHeight;

		// Get the correct dimensions for image
		double height = Math.round(getPhotoHeight());
		if (highlightedImages.contains(photo)) // Handle highlighting
			height *= HIGHLIGHTED_SIZE_RATIO;
		double width = Math.round(height * widthToHeight);

		// Now draw the image itself, not allowing it to overflow onto
		// the axes
		photo.drawImageClipped(GraphWidget.DEFAULT_GRAPHER_ID, x, y, width,
			height, drawing);

		// Note that the borders are drawn after the image is - this is
		// so the image doesn't obscure the borders

		double xMin = x - (width / 2.0);
		double xMax = x + (width / 2.0);
		double yMin = y - (height / 2.0);
		double yMax = y + (height / 2.0);

		// Draw a border around the image
		drawing.drawLineSegment(xMin, yMin, xMin, yMax);
		drawing.drawLineSegment(xMin, yMax, xMax, yMax);
		drawing.drawLineSegment(xMax, yMax, xMax, yMin);
		drawing.drawLineSegment(xMax, yMin, xMin, yMin);
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
		return Math.abs(yAxis.project2D(0).getY() -
			yAxis.project2D(PhotoGraphAxis.PHOTO_HEIGHT).getY());
	}

	/**
	 * The same as {@link DataPlot#highlightIfNear(Vector2, double)},
	 * except that this counts threshold as a percentage of image height.
	 * rather than as a number of pixels.
	 *
	 * <p>It is important to remember that threshold is a radius, so
	 * threshold should always be in the range [0, 50) - otherwise, the
	 * threshold will definitely be bigger than the image.</p>
	 *
	 * <p>This also figures out which photo should be highlighted for
	 * the user.</p>
	 *
	 * @inheritDoc
	 */
	// TODO: write a new version of closest() that cares only about
	// X-values and handles Y-values more intelligently
	@Override
	public boolean highlightIfNear(Vector2 pos, double threshold) {
		PlottablePoint point = closest(pos,
			threshold * getPhotoHeight() / 100);

		if (point != null) {
			highlightedImages = images.get(point);
			highlight();
		} else
			// We need to clear highlightedImages ourselves
			highlightedImages = EMPTY_HIGHLIGHTED_IMAGES_SET;

		return point != null;
	}

	/**
	 * An {@link org.bodytrack.client.Alertable Alertable} implementation
	 * that is specific to photo loading.
	 */
	public final class PhotoAlertable implements Alertable<PhotoGetter> {
		/**
		 * Called every time a new image loads.
		 *
		 * @param photo
		 * 		the <tt>PhotoGetter</tt> that just successfully loaded
		 * 		its image
		 */
		@Override
		public void onSuccess(PhotoGetter photo) {
			removePhotoLoadingText(photo);

			getContainer().paint();
		}

		/**
		 * Called every time a new image fails to load.
		 *
		 * <p>This does not attempt to reload the image, since it is
		 * assumed that the image must not exist on the server if
		 * we are getting an error.</p>
		 *
		 * @param photo
		 * 		the <tt>PhotoGetter</tt> that just encountered an error
		 */
		@Override
		public void onFailure(PhotoGetter photo) {
			// Don't do anything if this is a spurious error after
			// a successful load (should never happen)
			if (photo.imageLoaded())
				return;

			removePhotoLoadingText(photo);

			getContainer().paint();
		}
	}
}
