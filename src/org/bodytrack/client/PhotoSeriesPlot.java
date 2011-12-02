package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;
import gwt.g2d.client.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to show photos on a {@link PlotContainer}.
 */
public class PhotoSeriesPlot extends BaseSeriesPlot {
   /**
    * If the difference in height between the current height, in pixels,
    * and the value of previousHeight is greater than
    * OVERLAP_RESET_THRESH_H, then we have to drop the overlap
    * cache.
    */
   private static final double OVERLAP_RESET_THRESH_H = 5;

   /**
    * If the quotient of the X-axis width, in seconds, and previousWidth
    * differs from 1 by more than OVERLAP_RESET_THRESH_W,
    * then we have to drop the overlap cache.
    */
   private static final double OVERLAP_RESET_THRESH_W = 0.05;

   private static final double IMAGE_Y_VALUE =
         PhotoGraphAxis.PHOTO_CENTER_LOCATION;

   /**
    * Ratio of heights between highlighted image and regular image.
    */
   private static final double HIGHLIGHTED_SIZE_RATIO = 1.2;

   private static final double PHOTO_HIGHLIGHT_DISTANCE_THRESHOLD = 10;

   private final int userId;

   // Images tells us which images are associated with each PlottablePoint
   // we have to draw
   private final Map<PlottablePoint, Set<PhotoGetter>> images;

   private final PhotoAlertable loadListener;
   // TODO: Add this back in?
   // private final Map<PhotoGetter, Integer> loadingText;

   private Set<PhotoGetter> highlightedImages;

   // Map from photo P to set of photos that overlap P, which
   // makes this map somewhat redundant (information contained
   // in two places whenever two photos overlap) but fast
   private final Map<PhotoGetter, Set<PhotoGetter>> overlap;
   private double previousHeight;
   // Since overlap is only valid at a given height (in pixels)
   private double previousWidth;
   // Since overlap is only valid at a X-axis width (in seconds)

   private String previousPaintEventId = null;

   private final PhotoRenderer normalRenderer;
   private final PhotoRenderer highlightRenderer;

   /**
    * Initializes a new PhotoSeriesPlot.
    *
    * @param datasource
    * 		a native JavaScript function which can be used to retrieve
    * 		information about photos, NOT the photos themselves
    * @param nativeXAxis
    * 		the X-axis along which this data set will be aligned when drawn
    * @param nativeYAxis
    * 		the Y-axis along which this data set will be aligned when drawn
    * @param minLevel
    * 		the minimum level to which the user will be allowed to zoom
    * @param userId
    * 		the user ID of the current user
    * @throws NullPointerException
    * 		if any parameter is <tt>null</tt>
    */
   public PhotoSeriesPlot(final JavaScriptObject datasource,
                          final JavaScriptObject nativeXAxis,
                          final JavaScriptObject nativeYAxis,
                          final int minLevel,
                          final int userId) {
      super(datasource, nativeXAxis, nativeYAxis, minLevel);

      this.userId = userId;

      images = new HashMap<PlottablePoint, Set<PhotoGetter>>();
      loadListener = new PhotoAlertable();
      // loadingText = new HashMap<PhotoGetter, Integer>();
      highlightedImages = new HashSet<PhotoGetter>();

      overlap = new HashMap<PhotoGetter, Set<PhotoGetter>>();
      previousHeight = 1e-10;
      previousWidth = 1e-10;

      this.normalRenderer = new PhotoRenderer(false, true);
      this.highlightRenderer = new PhotoRenderer(true, false);   // Only the normal renderer needs to draw comments
   }

   /**
    * Loads the specified photo, and adds loading text to the
    * container.
    *
    * @param userId
    * 		the ID of the current user
    * @param photoId
    * 		the ID of the photo
    * @param time
    * 		the timestamp on the photo, which is used to decide where to
    * 		put it on the X-axis
    * @return
    * 		the {@link PhotoGetter}
    * 		that can be used to draw the photo we just requested
    */
   private PhotoGetter loadPhoto(final int userId, final int photoId, final double time) {
      final PhotoGetter photo = PhotoGetter.buildPhotoGetter(userId,
                                                             photoId, time, loadListener);

      // TODO: Put loading text back on?
      // loadingText.put(photo,
      // 	getContainer().addLoadingMessage(photo.getUrl()));

      addOverlaps(photo, getPhotoHeight());

      return photo;
   }

   /**
    * Adds all photos that overlap with photo to the overlap instance
    * variable.
    *
    * <p>This expects - and maintains - the invariants that, for any
    * photo in images, there is a corresponding non-<tt>null</tt>
    * entry in overlap.  This is an acceptable expectation, since this
    * is a private method that deals only with private data.</p>
    *
    * @param photo
    * 		the photo to check for overlapping
    * @param height
    * 		the height at which that photo will be drawn by default,
    * 		as returned by {@link #getPhotoHeight()}
    */
   private void addOverlaps(final PhotoGetter photo, final double height) {
      final double time = photo.getTime();

      final Set<PhotoGetter> overlapping;
      if (overlap.containsKey(photo)) {
         overlapping = overlap.get(photo);
      } else {
         overlapping = new HashSet<PhotoGetter>();
         overlap.put(photo, overlapping);
      }

      for (final Set<PhotoGetter> second : images.values()) {
         for (final PhotoGetter otherPhoto : second) {
            if (photo != otherPhoto &&
                otherPhoto.getTime() <= time &&
                overlaps(photo, otherPhoto, height)) {
               overlapping.add(otherPhoto);
               overlap.get(otherPhoto).add(photo);
            }
         }
      }
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
   private boolean removePhotoLoadingText(final PhotoGetter photo) {
      // TODO: Add this back in?
      /*
        boolean contains = loadingText.containsKey(photo);

        if (contains) {
           int msgId = loadingText.get(photo);
           getContainer().removeLoadingMessage(msgId);
           loadingText.remove(photo);
        }

        return contains;
        */
      return true;
   }

   @Override
   protected void beforeRender(final Canvas canvas, final BoundedDrawingBox drawing) {
      canvas.getSurface().setStrokeStyle(Canvas.DEFAULT_COLOR);
   }

   @Override
   protected HighlightableRenderer getRenderer() {
      return isHighlighted() ? highlightRenderer : normalRenderer;
   }

   @Override
   protected void afterRender(final Canvas canvas, final BoundedDrawingBox drawing) {
      // Clean up after ourselves
      canvas.getSurface().setStrokeStyle(Canvas.DEFAULT_COLOR);
   }


   /**
    * Returns the points that will form the centers of the images.
    *
    * <p>This also has the hidden side effect of starting to load
    * any unseen images in tile, thus making our drawing possible.
    * There is some subtlety here: the call to this method populates
    * the set of available images whenever called, which happens
    * to be every time the points are needed in
    * {@link DataSeriesPlot} methods.  The
    * methods {@link PhotoRenderer#paintDataPoint(BoundedDrawingBox, double, double, double, double, PlottablePoint)} and
    * {@link Plot#highlightIfNear(Vector2)} expect this images to
    * be filled, which is always the case whenever they use elements
    * of images, just by the way the code is written.</p>
    */
   private List<PlottablePoint> getDataPoints(final GrapherTile tile) {
      // There is no data in the tile
      if (tile.getPhotoDescriptions() == null) {
         return new ArrayList<PlottablePoint>();
      }

      final List<PhotoDescription> descs = tile.getPhotoDescriptions();
      final List<PlottablePoint> result = new ArrayList<PlottablePoint>();

      for (final PhotoDescription desc : descs) {
         final PlottablePoint pos = new PlottablePoint(desc.getBeginDate(), IMAGE_Y_VALUE);

         // This depends on the fact that equality of PlottablePoint
         // objects is based on equality of the floors of the times
         // of the objects (this is robust to small variations in
         // even the same object)
         if (images.containsKey(pos)) {
            final Set<PhotoGetter> value = images.get(pos);

            boolean haveDesc = false;

            for (final PhotoGetter photo : value) {
               if (photo.getImageId() == desc.getId()
                   && photo.getUserId() == userId) {
                  haveDesc = true;
                  break;
               }
            }

            if (!haveDesc) {
               value.add(loadPhoto(userId, desc.getId(),
                                   desc.getBeginDate()));
            }
         } else {
            final Set<PhotoGetter> newValue = new HashSet<PhotoGetter>();
            final PhotoGetter photoGetter = loadPhoto(userId, desc.getId(), desc.getBeginDate());
            newValue.add(photoGetter);
            images.put(pos, newValue);
         }

         // Now handle the PlottablePoint we just generated
         result.add(pos);
      }

      return result;
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
    * 		ignored
    */
   private void drawAllImagesAtPoint(final BoundedDrawingBox drawing, final double x, final double y) {
      // We stored data in images under the logical X-value (time), not
      // under a pixel value
      final double photoTime = getXAxis().unproject(new Vector2(x, y));

      final Set<PhotoGetter> photos = images.get(new PlottablePoint(photoTime, IMAGE_Y_VALUE));
      if (photos == null) {
         return; // This shouldn't ever occur
      }

      for (final PhotoGetter photo : photos) {
         drawPhoto(drawing, x, y, photo);
      }
   }

   /**
    * Draws a single photo at the specified X position.
    *
    * <p>This method worries about fit, making sure that the photo
    * will not overlap anything.  Then, this calls
    * {@link #renderPhoto(BoundedDrawingBox, double, double,
    * double, double, PhotoGetter)} to show the photo on the page.
    * As such, x and y are taken as polite suggestions rather than
    * hard absolutes, since we are interested in not having the
    * photos overlap.</p>
    *
    * @param drawing
    * 		the <tt>BoundedDrawingBox</tt> we use to draw only in
    * 		bounds
    * @param x
    * 		the X-value (in pixels) at which the center of the image
    * 		should be drawn
    * @param y
    * 		ignored
    * @param photo
    * 		the photo to draw at point (x, y)
    */
   private void drawPhoto(final BoundedDrawingBox drawing,
                          final double x,
                          final double y,
                          final PhotoGetter photo) {
      // Get the correct dimensions for image
      double height = getHeight(photo);

      double photoY = y;
      if (overlap.get(photo).size() > 0) {
         // There is overlap, so we draw photos with even IDs on
         // top and photos with odd IDs on the bottom

         // TODO: Possibly rename getPhotoHeight() to
         // getFullPhotoHeight(), and change getPhotoHeight() to
         // return the height of the shrunken photo
         photoY = getPhotoY(photo);
         height /= 2.0;
      }

      final double width = getWidth(photo, height);
      renderPhoto(drawing, x, photoY, width, height, photo);
   }

   /**
    * Returns the full-size height of photo.
    *
    * <p>This checks for highlighting status, and handles that correctly
    * as well.  To get the default photo height, call
    * {@link #getPhotoHeight()}.</p>
    *
    * @param photo
    * 		the photo for which we want to get the height
    * @return
    * 		the height at which photo should be drawn, if at full size
    */
   private double getHeight(final PhotoGetter photo) {
      double height = getPhotoHeight();
      if (highlightedImages.contains(photo)) // Handle highlighting
      {
         height *= HIGHLIGHTED_SIZE_RATIO;
      }

      return height;
   }

   /**
    * Finds the width at which photo should be drawn, if we are working
    * at the specified height.
    *
    * @param photo
    * 		the photo for which we want the width
    * @param height
    * 		the height at which photo will be drawn
    * @return
    * 		the width at which photo should be drawn, maintaining the
    * 		aspect ratio of photo
    */
   private double getWidth(final PhotoGetter photo, final double height) {
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
    * 		the first photo that may or may not overlap
    * @param photo2
    * 		the second photo that may or may not overlap
    * @param height
    * 		the height at which these photos will be drawn by default,
    * 		as returned by {@link #getPhotoHeight()}
    * @return
    * 		<tt>true</tt> if and only if photo1 and photo2 would overlap
    * 		when drawn at full size, when not highlighted
    */
   private boolean overlaps(final PhotoGetter photo1,
                            final PhotoGetter photo2,
                            final double height) {
      final double width1 = getWidth(photo1, height);
      final double width2 = getWidth(photo2, height);

      final double x1 = getPhotoX(photo1);
      final double x2 = getPhotoX(photo2);

      return 2 * Math.abs(x1 - x2) < width1 + width2;
   }

   /**
    * Returns the X-value, in pixels, at which the specified photo should
    * be drawn.
    *
    * @param photo
    * 		the photo to place on the X-axis
    * @return
    * 		the X-value, in pixels, at which we should draw the center of
    * 		photo
    */
   private double getPhotoX(final PhotoGetter photo) {
      return getXAxis().project2D(photo.getTime()).getX();
   }

   /**
    * Returns the Y-value, in pixels, at which the specified photo
    * should be drawn.
    *
    * <p>This implements all the logic for spreading out photos along
    * the Y-axis, and should be used for both drawing and
    * highlighting.</p>
    *
    * @param photo
    * 		the photo to place on the Y-axis
    * @return
    * 		the Y-value, in pixels, at which we should draw the center
    * 		of photo
    */
   private double getPhotoY(final PhotoGetter photo) {
      double y = getYAxis().project2D(
            PhotoGraphAxis.PHOTO_CENTER_LOCATION).getY();

      if (overlap.get(photo).size() > 0) {
         final double photoHeight = getPhotoHeight();

         if (photo.getImageId() % 2 == 0) {
            y -= photoHeight / 4.0;
         } else {
            y += photoHeight / 4.0;
         }
      }

      return y;
   }

   /**
    * Actually draws the specified photo to the canvas, without
    * worrying about fit.
    *
    * @param drawing
    * 		the <tt>BoundedDrawingBox</tt> we use to draw only in
    * 		bounds
    * @param x
    * 		the X-value (in pixels) at which the center of the image
    * 		will be drawn
    * @param y
    * 		the Y-value (in pixels) at which the center of the image
    * 		will be drawn
    * @param width
    * 		the width (in pixels) at which the image will be drawn
    * @param height
    * 		the height (in pixels) at which the image will be drawn
    * @param photo
    * 		the photo to draw at point (x, y)
    */
   private void renderPhoto(final BoundedDrawingBox drawing,
                            final double x,
                            final double y,
                            final double width,
                            final double height,
                            final PhotoGetter photo) {
      // Now draw the image itself, not allowing it to overflow onto
      // the axes
      photo.drawImageBounded(drawing.getCanvas().getNativeCanvasElement(),
                             x, y, width, height, drawing);

      // Note that the borders are drawn after the image is, so the image
      // doesn't obscure the borders

      final double xMin = x - (width / 2.0);
      final double xMax = x + (width / 2.0);
      final double yMin = y - (height / 2.0);
      final double yMax = y + (height / 2.0);

      // Draw a border around the image
      drawing.drawLineSegment(xMin, yMin, xMin, yMax);
      drawing.drawLineSegment(xMin, yMax, xMax, yMax);
      drawing.drawLineSegment(xMax, yMax, xMax, yMin);
      drawing.drawLineSegment(xMax, yMin, xMin, yMin);
   }

   /**
    * Returns the height the photo should take up, in pixels.
    *
    * <p>This also has the side effect of dropping and refilling the cache
    * in the overlap instance variable whenever there is too much change
    * from the previous version.</p>
    *
    * @return
    * 		the height the photo should take up, in pixels
    */
   private double getPhotoHeight() {
      final GraphAxis yAxis = getYAxis();

      // Note that 0 has a lower Y-value than PHOTO_HEIGHT, since
      // higher values in logical units have smaller Y-values in pixels
      final double height = yAxis.project2D(0).getY() -
                            yAxis.project2D(PhotoGraphAxis.PHOTO_HEIGHT).getY();

      final double xAxisWidth = getXAxis().getMax() - getXAxis().getMin();
      final double widthRatio = previousWidth / xAxisWidth;

      // TODO: Smarter caching of overlap info, so we only add and drop
      // values when we need to do so.  Also, maybe use old cache as
      // starting point whenever we refresh the cache.
      if (Math.abs(height - previousHeight) > OVERLAP_RESET_THRESH_H
          || Math.abs(1 - widthRatio) > OVERLAP_RESET_THRESH_W) {
         resetOverlapCache(height);
         previousHeight = height;
         previousWidth = xAxisWidth;
      }

      return height;
   }

   /**
    * Resets the overlap instance variable.
    *
    * @param height
    * 		the default height used to draw images
    */
   private void resetOverlapCache(final double height) {
      overlap.clear();

      for (final Set<PhotoGetter> second : images.values()) {
         for (final PhotoGetter photo : second) {
            overlap.put(photo, new HashSet<PhotoGetter>());
         }
      }
      for (final Set<PhotoGetter> second : images.values()) {
         for (final PhotoGetter photo : second) {
            addOverlaps(photo, height);
         }
      }
   }

   /**
    * The same as {@link Plot#highlightIfNear(Vector2)},
    * except that this counts threshold as a percentage of image height.
    * rather than as a number of pixels.
    *
    * <p>It is important to remember that threshold is a radius, so
    * threshold should always be in the range [0, 50) - otherwise, the
    * threshold will definitely be bigger than the image.</p>
    *
    * <p>This also figures out which photo should be highlighted for
    * the user.</p>
    */
   @Override
   public boolean highlightIfNear(final Vector2 pos) {
      highlightedImages = closeImages(pos, PHOTO_HIGHLIGHT_DISTANCE_THRESHOLD * getPhotoHeight() / 100);

      final boolean haveImages = highlightedImages.size() > 0;

      if (haveImages) {
         highlight();
      }

      return haveImages;
   }

   /**
    * A method, similar to {@link DataSeriesPlot#closest(Vector2, double)}, that
    * finds the images with centers within threshold pixels of pos.
    *
    * @param pos
    * 		the current mouse position
    * @param threshold
    * 		the maximum number of pixels an image must be from pos in
    * 		order to be highlighted
    * @return
    * 		a <tt>Set</tt> of images that should be highlighted, based
    * 		on the fact that the mouse is at pos
    */
   private Set<PhotoGetter> closeImages(final Vector2 pos, final double threshold) {
      final Set<PhotoGetter> result = new HashSet<PhotoGetter>();

      // Precompute some values
      final double xAxisMinValue = getXAxis().getMin();
      final double xAxisMaxValue = getXAxis().getMax();
      final double thresholdSq = threshold * threshold;

      // TODO: Something better than 2 loops just to walk through all photos
      for (final Set<PhotoGetter> second : images.values()) {
         for (final PhotoGetter photo : second) {
            final double time = photo.getTime();

            // Don't bother with photos that are out of bounds
            if (time < xAxisMinValue || time > xAxisMaxValue) {
               continue;
            }

            // Both these values are in pixels
            final double photoX = getPhotoX(photo);
            final double photoY = getPhotoY(photo);

            final Vector2 photoPos = new Vector2(photoX, photoY);
            if (pos.distanceSquared(photoPos) < thresholdSq) {
               result.add(photo);
            }
         }
      }

      return result;
   }

   private final class PhotoRenderer extends AbstractPlotRenderer {
      private PhotoRenderer(final boolean highlighted, final boolean drawComments) {
         super(highlighted, drawComments);
      }

      /**
       * Draws the images at the specified point.
       *
       * <p>Although we handle edge points and regular points in the same way in this class, we still need to draw all the
       * images, so this does exactly the same thing that
       * {@link PhotoRenderer#paintDataPoint(BoundedDrawingBox, double, double, double, double, PlottablePoint)}
       * does.</p>
       *
       * @param drawing
       * 		the bounding box that constrains where photos will draw
       * @param tile
       *       the tile from which the data point to be drawn was obtained
       * @param x
       * 		the X-value (in pixels) at which we draw the image
       * @param y
       * 		ignored
       * @param rawDataPoint
       * 		the raw {@link PlottablePoint}
       */
      @Override
      protected void paintEdgePoint(final BoundedDrawingBox drawing,
                                    final GrapherTile tile,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
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
       * 		ignored
       * @param rawDataPoint
       * 		the raw {@link PlottablePoint}
       */
      @Override
      protected void paintDataPoint(final BoundedDrawingBox drawing,
                                    final double prevX,
                                    final double prevY,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
         drawAllImagesAtPoint(drawing, x, y);
      }

      /**
       * Implemented here as a no-op, since we don't need highlighted points to look different.
       */
      @Override
      protected void paintHighlightedPoint(final BoundedDrawingBox drawing,
                                           final double x,
                                           final double y) {
         // no need to do anything
      }

      @Override
      protected List<PlottablePoint> getDataPoints(final GrapherTile tile) {
         return PhotoSeriesPlot.this.getDataPoints(tile);
      }
   }

   /**
    * An {@link Alertable} implementation
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
      public void onSuccess(final PhotoGetter photo) {
         removePhotoLoadingText(photo);

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
       * 		the <tt>PhotoGetter</tt> that just encountered an error
       */
      @Override
      public void onFailure(final PhotoGetter photo) {
         // Don't do anything if this is a spurious error after
         // a successful load (should never happen)
         if (photo.imageLoaded()) {
            return;
         }

         removePhotoLoadingText(photo);

         signalRepaintOfPlotContainer();
      }
   }
}
