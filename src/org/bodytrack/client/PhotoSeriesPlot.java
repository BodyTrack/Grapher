package org.bodytrack.client;

import gwt.g2d.client.graphics.KnownColor;
import gwt.g2d.client.graphics.TextAlign;
import gwt.g2d.client.graphics.TextBaseline;
import gwt.g2d.client.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A class to show photos on a {@link PlotContainer}
 */
public class PhotoSeriesPlot extends BaseSeriesPlot {
	private static final double IMAGE_Y_VALUE =
		PhotoGraphAxis.PHOTO_CENTER_LOCATION;

	private static final double COUNT_CIRCLE_SIZE = 20;

	/**
	 * Ratio of heights between highlighted image and regular image
	 */
	private static final double HIGHLIGHTED_SIZE_RATIO = 1.2;

	private static final double PHOTO_HIGHLIGHT_THRESHOLD_PROPORTION = 0.10;

	private final int userId;
	private final List<PhotoGetter> images;
	private Set<PhotoGetter> highlightedImages;
	private final PhotoAlertable loadListener;
	private final SeriesPlotRenderer renderer;

	/**
	 * Initializes a new PhotoSeriesPlot
	 *
	 * @param datasource
	 * 	A native JavaScript function which can be used to retrieve
	 * 	information about photos, NOT the photos themselves
	 * @param nativeXAxis
	 * 	The X-axis along which this data set will be aligned when drawn
	 * @param nativeYAxis
	 * 	The Y-axis along which this data set will be aligned when drawn
	 * @param minLevel
	 * 	The minimum level to which the user will be allowed to zoom
	 * @param userId
	 * 	The user ID of the current user
	 * @param styleJson
	 * 	The JSON style
	 * @throws NullPointerException
	 * 	If any parameter is <tt>null</tt>
	 */
	public PhotoSeriesPlot(final JavaScriptObject datasource,
			final JavaScriptObject nativeXAxis,
			final JavaScriptObject nativeYAxis,
			final int minLevel,
			final int userId,
			final JavaScriptObject styleJson) {
		super(datasource, nativeXAxis, nativeYAxis, minLevel);

		if (styleJson == null)
			throw new NullPointerException();

		this.userId = userId;

		images = new ArrayList<PhotoGetter>();
		highlightedImages = new HashSet<PhotoGetter>();
		loadListener = new PhotoAlertable();
		renderer = new PhotoRenderer(styleJson.<StyleDescription>cast());
	}

	@Override
	protected SeriesPlotRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Returns the points that will form the centers of the images
	 *
	 * <p>This also has the hidden side effect of starting to load
	 * any unseen images in tile, thus making our drawing possible.
	 * There is some subtlety here: the call to this method populates
	 * the set of available images whenever called, which happens
	 * to be every time the points are needed for painting.</p>
	 *
	 * <p>This returns one point per photo.  Even though the rendering
	 * strategy ignores the actual coordinates of the points, this
	 * ensures that one call to
	 * {@link #drawPhoto(BoundedDrawingBox, int, PhotoGetter, Reference)}
	 * is made for each photo in the images list.</p>
	 */
	private List<PlottablePoint> getDataPoints(final GrapherTile tile) {
		if (tile.getPhotoDescriptions() != null)
			loadPhotos(tile.getPhotoDescriptions());

		return getDataPoints();
	}

	private List<PlottablePoint> getDataPoints() {
		// TODO: Possibly cache this list of immutable objects to avoid
		// creating a bunch of little objects on every repaint

		int photoCount = images.size();
		double min = getXAxis().getMin();
		double max = getXAxis().getMax();

		List<PlottablePoint> result = new ArrayList<PlottablePoint>();

		for (int i = 0; i < photoCount; i++)
			result.add(new PlottablePoint((max - min) * i + min, IMAGE_Y_VALUE));

		return result;
	}

	// TODO: Don't download all the photos
	private void loadPhotos(final List<PhotoDescription> descs) {
		for (final PhotoDescription desc: descs) {
			// This depends on the fact that equality of PlottablePoint
			// objects is based on equality of the floors of the times
			// of the objects (this is robust to small variations)
			if (Collections.binarySearch(images,
					PhotoGetter.buildDummyPhotoGetter(userId, desc)) < 0) {
				CollectionUtils.insertInOrder(images,
						PhotoGetter.buildPhotoGetter(userId, desc, loadListener));
			}
		}
	}

	private PhotoGetter drawPhoto(final BoundedDrawingBox drawing, final int idx,
			final PhotoGetter lastPhoto, final Reference<Integer> currentCount) {
		if (idx < 0 || idx > images.size())
			return lastPhoto;

		PhotoGetter photo = images.get(idx);
		if (lastPhoto == null || !overlaps(photo, lastPhoto)) {
			drawCount(drawing, lastPhoto, currentCount.get());
			drawPhoto(drawing, getPhotoX(photo), getPhotoY(), photo);
			currentCount.set(photo.getCount());
			return photo;
		}

		currentCount.set(currentCount.get() + photo.getCount());

		return lastPhoto;
	}

	// Assumes that drawing has text alignment equal to TextAlignment.CENTER,
	// and text baseline equal to TextBaseline.MIDDLE
	private void drawCount(final BoundedDrawingBox drawing,
			final PhotoGetter photo,
			final int count) {
		// TODO: Maybe draw shadow whenever count > 1
		if (drawing == null || photo == null || count <= 1)
			return;

		// Finish out other drawing before filling a circle and text
		drawing.strokeClippedPath();
		drawing.beginClippedPath();

		final double height = getPhotoHeight();

		// Center the red circle on the top right corner of the photo
		final double circleX = getPhotoX(photo) + getPhotoWidth(photo, height) / 2.0;
		final double circleY = getPhotoY() - getPhotoHeight() / 2.0;

		drawing.setFillStyle(KnownColor.RED);
		drawing.fillCircle(circleX, circleY, COUNT_CIRCLE_SIZE / 2.0);
		drawing.setFillStyle(KnownColor.WHITE);
		drawing.fillText("" + count, circleX, circleY);
		drawing.setFillStyle(Canvas.DEFAULT_COLOR);
	}

	/**
	 * Draws a single photo at the specified X position
	 *
	 * <p>This method worries about fit, making sure that the photo
	 * will not overlap anything.  Then, this calls
	 * {@link #renderPhoto(BoundedDrawingBox, double, double,
	 * double, double, PhotoGetter)} to show the photo on the page.</p>
	 *
	 * @param drawing
	 * 	The <tt>BoundedDrawingBox</tt> we use to draw only in bounds
	 * @param x
	 * 	The X-value (in pixels) at which the center of the image should
	 * 	be drawn
	 * @param y
	 * 	The Y-value (in pixels) at which the center of the image should
	 * 	be drawn
	 * @param photo
	 * 	The photo to draw at point (x, y)
	 */
	private void drawPhoto(final BoundedDrawingBox drawing,
			final double x,
			final double y,
			final PhotoGetter photo) {
		final double height = getHeight(photo);
		final double width = getPhotoWidth(photo, height);

		final double xMin = x - (width / 2.0);
		final double xMax = x + (width / 2.0);
		final double yMin = y - (height / 2.0);
		final double yMax = y + (height / 2.0);

		// TODO: Add this line for efficiency
		// if (!drawing.containsRectanglePart(xMin, yMin, width, height))
		//	return;

		// Now draw the image itself, not allowing it to overflow onto
		// the axes
		photo.drawImageBounded(drawing.getCanvas().getNativeCanvasElement(),
				x, y, width, height, drawing);

		// Note that the borders are drawn after the image is, so the image
		// doesn't obscure the borders
		drawing.drawLineSegment(xMin, yMin, xMin, yMax); // Left edge
		drawing.drawLineSegment(xMin, yMax, xMax, yMax); // Bottom edge
		drawing.drawLineSegment(xMax, yMax, xMax, yMin); // Right edge
		drawing.drawLineSegment(xMax, yMin, xMin, yMin); // Top edge
	}

	/**
	 * Returns the full-size height of photo
	 *
	 * <p>This checks for highlighting status, and handles that correctly
	 * as well.  To get the default photo height, call
	 * {@link #getPhotoHeight()}.</p>
	 *
	 * @param photo
	 * 	The photo for which we want to get the height
	 * @return
	 * 	The height at which photo should be drawn, if at full size
	 */
	private double getHeight(final PhotoGetter photo) {
		double height = getPhotoHeight();

		return highlightedImages.contains(photo)
			? height * HIGHLIGHTED_SIZE_RATIO
			: height;
	}

	/**
	 * Finds the width at which the photo should be drawn, given its height
	 *
	 * @param photo
	 * 	The photo for which we want the width
	 * @param height
	 * 	The height at which photo will be drawn
	 * @return
	 * 	The width at which photo should be drawn, maintaining the
	 * 	aspect ratio of photo
	 */
	private double getPhotoWidth(final PhotoGetter photo,
			final double height) {
		final double originalWidth = photo.getOriginalWidth();
		final double originalHeight = photo.getOriginalHeight();

		final double widthToHeight = originalWidth / originalHeight;

		return height * widthToHeight;
	}

	/**
	 * Tells whether the two photos overlap when drawn at full size
	 * but not highlighted.
	 *
	 * <p>It is not really overlapping if the only thing causing an
	 * overlap is the highlighting, so this ignores highlighting when
	 * calculating intersection.</p>
	 *
	 * @param photo1
	 * 	The first photo that may or may not overlap
	 * @param photo2
	 * 	The second photo that may or may not overlap
	 * @return
	 * 	<code>true</code> if and only if photo1 and photo2 would overlap
	 * 	when drawn at full size, when not highlighted
	 */
	private boolean overlaps(final PhotoGetter photo1,
			final PhotoGetter photo2) {
		final double height = getPhotoHeight();

		final double width1 = getPhotoWidth(photo1, height);
		final double width2 = getPhotoWidth(photo2, height);

		final double x1 = getPhotoX(photo1);
		final double x2 = getPhotoX(photo2);

		return 2 * Math.abs(x1 - x2) < width1 + width2;
	}

	/**
	 * Returns the X-value, in pixels, at which the specified photo should
	 * be drawn
	 *
	 * @param photo
	 * 	The photo to place on the X-axis
	 * @return
	 * 	The X-value, in pixels, at which we should draw the center of photo
	 */
	private double getPhotoX(final PhotoGetter photo) {
		return getXAxis().project2D(photo.getTime()).getX();
	}

	/**
	 * Returns the Y-value, in pixels, at which a photo should be drawn
	 *
	 * @return
	 * 	The Y-value in pixels at which photos should be drawn
	 */
	private double getPhotoY() {
		return getYAxis().project2D(IMAGE_Y_VALUE).getY();
	}

	/**
	 * Returns the height that a non-highlighted photo should take up, in pixels
	 *
	 * @return
	 * 	The height that a non-highlighted should take up, in pixels
	 */
	private double getPhotoHeight() {
		final GraphAxis yAxis = getYAxis();

		// Note that 0 has a lower Y-value than PHOTO_HEIGHT, since
		// higher values in logical units have smaller Y-values in pixels
		return yAxis.project2D(0).getY()
			- yAxis.project2D(PhotoGraphAxis.PHOTO_HEIGHT).getY();
	}

	// TODO: FIX HIGHLIGHTING
	@Override
	public boolean highlightIfNear(final Vector2 pos) {
		highlightedImages = getCloseImages(pos,
				PHOTO_HIGHLIGHT_THRESHOLD_PROPORTION * getPhotoHeight());

		return highlightedImages.size() > 0;
	}

	/**
	 * In much the same style as {@link DataSeriesPlot#closest(Vector2, double)},
	 * finds the images with centers within threshold pixels of pos
	 *
	 * @param pos
	 * 	The current mouse position
	 * @param threshold
	 * 	The maximum number of pixels an image must be from pos in order to be
	 * 	highlighted
	 * @return
	 * 	A <tt>Set</tt> of images that should be highlighted, based
	 * 	on the fact that the mouse is at pos
	 */
	private Set<PhotoGetter> getCloseImages(final Vector2 pos,
			final double threshold) {
		final Set<PhotoGetter> result = new HashSet<PhotoGetter>();

		// Precompute some values
		final double xAxisMinValue = getXAxis().getMin();
		final double xAxisMaxValue = getXAxis().getMax();
		final double thresholdSq = threshold * threshold;

		for (final PhotoGetter photo: images) {
			final double time = photo.getTime();

			// Don't bother with photos that are out of bounds
			if (time < xAxisMinValue || time > xAxisMaxValue) {
				continue;
			}

			// Both these values are in pixels
			final double photoX = getPhotoX(photo);
			final double photoY = PhotoGraphAxis.PHOTO_CENTER_LOCATION;

			final Vector2 photoPos = new Vector2(photoX, photoY);
			if (pos.distanceSquared(photoPos) < thresholdSq) {
				result.add(photo);
			}
		}

		return result;
	}

	private class PhotoRenderingStrategy implements SeriesPlotRenderingStrategy {
		private static final int NOT_RENDERING = -1;

		private TextAlign oldTextAlign;
		private TextBaseline oldTextBaseline;

		private int photoIndex = NOT_RENDERING;
		private PhotoGetter lastPhoto;
		private Reference<Integer> currentCount;
		private BoundedDrawingBox savedDrawing;

		@Override
		public void beforeRender(final Canvas canvas,
				final boolean isAnyPointHighlighted) {
			oldTextAlign = canvas.getTextAlign();
			oldTextBaseline = canvas.getTextBaseline();

			canvas.setStrokeStyle(Canvas.DEFAULT_COLOR);
			canvas.setTextAlign(TextAlign.CENTER);
			canvas.setTextBaseline(TextBaseline.MIDDLE);

			photoIndex = 0;
			lastPhoto = null;
			currentCount = new Reference<Integer>(0);
			savedDrawing = null;
		}

		@Override
		public final void paintEdgePoint(final BoundedDrawingBox drawing,
				final GrapherTile tile,
				final GraphAxis xAxis,
				final GraphAxis yAxis,
				final boolean isAnyPointHighlighted,
				final double x,
				final double y,
				final PlottablePoint rawDataPoint) {
			drawNextPhoto(drawing);
		}

		@Override
		public final void paintDataPoint(final BoundedDrawingBox drawing,
				final GrapherTile tile,
				final GraphAxis xAxis,
				final GraphAxis yAxis,
				final boolean isAnyPointHighlighted,
				final double prevX,
				final double prevY,
				final double x,
				final double y,
				final PlottablePoint rawDataPoint) {
			drawNextPhoto(drawing);
		}

		private void drawNextPhoto(final BoundedDrawingBox drawing) {
			lastPhoto = drawPhoto(drawing, photoIndex, lastPhoto, currentCount);
			photoIndex++;
			savedDrawing = drawing;
		}

		@Override
		public void afterRender(final Canvas canvas) {
			// One last count rendering before we finish, since drawPhoto
			// only draws the count for earlier photos
			drawCount(savedDrawing, lastPhoto, currentCount.get());

			canvas.setTextBaseline(oldTextBaseline);
			canvas.setTextAlign(oldTextAlign);
			canvas.setStrokeStyle(Canvas.DEFAULT_COLOR);

			photoIndex = NOT_RENDERING;
			lastPhoto = null;
			currentCount = null;
			savedDrawing = null;
		}
	}

	private final class PhotoRenderer extends BaseSeriesPlotRenderer {

		private PhotoRenderer(final StyleDescription styleDescription) {
			super(styleDescription);
		}

		@Override
		protected List<SeriesPlotRenderingStrategy> buildRenderingStrategies(
				final StyleDescription styleDescription) {
			// TODO: honor the style...
			final List<SeriesPlotRenderingStrategy> renderingStrategies =
				new ArrayList<SeriesPlotRenderingStrategy>();
			renderingStrategies.add(new PhotoRenderingStrategy());
			return renderingStrategies;
		}

		@Override
		protected List<PlottablePoint> getDataPoints(final GrapherTile tile) {
			return PhotoSeriesPlot.this.getDataPoints(tile);
		}
	}

	/**
	 * An {@link Alertable} implementation that is specific to photo loading
	 */
	public final class PhotoAlertable implements Alertable<PhotoGetter> {
		/**
		 * Called every time a new image loads.
		 *
		 * @param photo
		 * 	The <tt>PhotoGetter</tt> that just successfully loaded its image
		 */
		@Override
		public void onSuccess(final PhotoGetter photo) {
			signalRepaintOfPlotContainer();
		}

		/**
		 * Called every time a new image fails to load.
		 *
		 * <p>This does not attempt to reload the image, since it is
		 * assumed that the image must not exist on the server if
		 * we are getting an error.</p>
		 *
		 * @param photo
		 * 	The {@link PhotoGetter} that just encountered an error
		 */
		@Override
		public void onFailure(final PhotoGetter photo) {
			// Don't do anything if this is a spurious error after
			// a successful load (should never happen)
			if (photo.imageLoaded()) {
				Log.debug("Spurious failure for image " + photo.getImageId());
				return;
			}

			signalRepaintOfPlotContainer();
		}
	}
}
