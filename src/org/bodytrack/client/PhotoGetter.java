package org.bodytrack.client;

import java.util.Comparator;

import org.bodytrack.client.NativeObjectSet.EqualsHashcodeProvider;
import org.bodytrack.client.PhotoSeriesPlot.PhotoAlertable;
import org.bodytrack.client.PlottablePoint.DateComparator;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;

/**
 * A class to download a single photo and return the appropriate
 * graphics both before and after the photo is downloaded.  This
 * class also handles resizing images as necessary.
 *
 * <p>A future implementation of this class will be able to handle
 * multiple photo sizes, and automatically decide, when asked for
 * a photo of a certain size, whether to download a new photo or
 * simply to scale the current photo.  To implement this, we will
 * need to change img from a single JavaScript Image object to a
 * list of Image objects.  For now, though, this just
 * handles single photo downloads.</p>
 *
 * <p>This class also maintains the logic for building photo
 * download URLs, given the appropriate information.</p>
 */

// TODO: Add a PhotoManager class to ensure that no photo is ever downloaded
// twice (even if two photo plots point to the same channel), with some logic
// for dropping photos from the cache after the cache is too full (possibly use
// reference counting to make sure that the cache doesn't drop photos that
// a plot is using)

public final class PhotoGetter extends JavaScriptObject implements Comparable<PhotoGetter> {
    public static final EqualsHashcodeProvider<PhotoGetter> EQUALS_HASHCODE =
            new PhotoGetterEqualsHashcodeProvider();

    private static final Comparator<Double> DATE_COMPARATOR = new DateComparator();

    /* Overlay types always have protected zero-arg constructors. */
    protected PhotoGetter() { }

    public static PhotoGetter buildDummyPhotoGetter(final int userId,
            final PhotoDescription desc) {
        return buildPhotoGetter(userId, desc.getId(), true, desc.getBeginDate(),
                desc.getCount(), desc.getUrl(), desc.getThumbnails(), null);
    }

    public static PhotoGetter buildPhotoGetter(final int userId,
            final PhotoDescription desc,
            final PhotoAlertable callback) {
        return buildPhotoGetter(userId, desc.getId(), false, desc.getBeginDate(),
                desc.getCount(), desc.getUrl(), desc.getThumbnails(), callback);
    }

    /**
     * Creates a new PhotoGetter.
     *
     * @param isDummy
     * 	If this is <code>true</code>, the PhotoGetter is created normally
     * 	except that no image will ever actually be loaded from the server,
     * 	meaning that callback is never called
     * @param count
     * 	The count field to store on the newly created object.  The count
     * 	field does not in any way affect how the photo is downloaded or drawn
     * @param callback
     * 	The object that will get a callback whenever the photo loads
     * 	or an error occurs.  If this is <code>null</code>, no exception
     * 	will occur, and callback will simply be ignored.
     * @return
     * 	A new {@link PhotoGetter} that will download the specified image
     * 	on a future call to {@link #download()}.
     */
    private static native PhotoGetter buildPhotoGetter(final int userId,
            final int imageId,
            final boolean isDummy,
            final double time,
            final int count,
            final String url,
            final Dynamic[] thumbnailInfos,
            final PhotoAlertable callback) /*-{
        // We would prefer to use Alertable over PhotoAlertable for callback, but GWT
        // fails to compile this JSNI when callback is declared as an Alertable

        var getter = {};
        getter.userId = userId;
        getter.imageId = imageId;
        getter.isDummy = isDummy;
        getter.time = time;
        getter.count = count;
        getter.callback = callback;

        // The length that getter.urls, getter.images, and several other properties will have
        var length = thumbnailInfos.length + 1; // Add 1 for the full-size image

        // Properties in array form, with the last element holding data about the full-size
        // image and all other elements holding data for the thumbnails.  Each of these
        // arrays has length equal to the length variable.
        getter.urls = thumbnailInfos.map(function (info) { return info.url; });
        getter.urls.push(url);
        getter.imageLoaded = getter.urls.map(function (_) { return false; });
        getter.loadFailed = getter.urls.map(function (_) { return false; });
        getter.loadStarted = getter.urls.map(function (_) { return false; });
        getter.widths = thumbnailInfos.map(function (info) { return info.width; });
        getter.widths.push(-1);
        getter.heights = thumbnailInfos.map(function (info) { return info.height; });
        getter.heights.push(-1);
        getter.images = getter.urls.map(function (_) { return new Image(); });

        return getter;
    }-*/;

    public native int getUserId() /*-{
        return this.userId;
    }-*/;

    public native int getImageId() /*-{
        return this.imageId;
    }-*/;

    public native double getTime() /*-{
        return this.time;
    }-*/;

    /**
     * Returns the count parameter used to initialize this {@link PhotoGetter}
     */
    public native int getCount() /*-{
        return this.count;
    }-*/;

    public native void setCount(final int count) /*-{
        this.count = count;
    }-*/;

    /**
     * Returns <code>true</code> if and only if this {@link PhotoGetter} has
     * started loading at least one image of any size.
     */
    public native boolean loadStarted() /*-{
        return this.loadStarted.some(function (elem, _, __) { return !!elem; });
    }-*/;

    /**
     * Returns <code>true</code> if the requested image has loaded in at least one size,
     * <code>false</code> otherwise.
     */
    public native boolean imageLoaded() /*-{
        return this.imageLoaded.some(function (elem, _, __) { return !!elem; });
    }-*/;

    /**
     * Returns the best aspect ratio (ratio of width to height) of the image.
     *
     * <p>
     * The best aspect ratio of the image is taken from the full-size image if that image
     * has been loaded, or the largest thumbnail otherwise (the thumbnail sizes are known
     * at the creation of a new {@link PhotoGetter}, but the full-size image size is not
     * known until the image itself has been loaded).  If the full-size image has not been
     * loaded and there are no thumbnails, returns <code>1.0</code>.
     * </p>
     */
    public native double getAspectRatio() /*-{
        var max = function (array) {
            return array.reduce(
                    function (prev, curr, _, __) { return curr > prev ? curr : prev; },
                    -Number.MAX_VALUE);
        };

        // Assuming that all elements of this.widths and this.heights refer to the same
        // photo, the aspect ratios will all be the same within rounding error, so the
        // largest width and the largest height will correspond to the same thumbnail
        // or to the full-size image
        var width = max(this.widths);
        var height = max(this.heights);

        if (width <= 0 || height <= 0) {
            return 1.0;
        }

        return width / height;
    }-*/;

    /**
     * Begins downloading the full-size image or thumbnail closest to the
     * specified height.
     *
     * <p>
     * If any thumbnails are available, this downloads only thumbnails and never
     * downloads the full-size image.  If no thumbnails are available, this
     * downloads the full-size image when appropriate.
     * </p>
     *
     * @param preferredHeight
     *  The preferred height of the image to download
     * @param minScale
     *  This method will only initiate a download if there are no photos
     *  already begun to be downloaded at size greater than or equal to
     *  <pre>preferredHeight * minScale</pre>
     * @param maxScale
     *  This method will only initiate a download if there are no photos
     *  already begun to be downloaded at size less than or equal to
     *  <pre>preferredHeight * maxScale</pre>
     * @return
     *  <code>true</code> if and only if calling this method actually
     * 	started a download
     */
    public boolean downloadIfNecessary(final double preferredHeight,
            final double minScale,
            final double maxScale) {
        final int bestIdx = chooseDownloadIndex(preferredHeight, minScale, maxScale);
        if (bestIdx < 0)
            return false;

        downloadImage(bestIdx);
        return true;
    }

    // Helper function for downloadIfNecessary, which chooses the index of the photo to
    // download, returning -1 if no photo should be downloaded
    private native int chooseDownloadIndex(final double preferredHeight,
            final double minScale,
            final double maxScale) /*-{
        if (this.isDummy || this.imageId < 0)
            return -1;

        var minHeight = preferredHeight * minScale;
        var maxHeight = preferredHeight * maxScale;
        var bestIdx = -1; // Index of the height closest to preferredHeight
        var bestDelta = Number.MAX_VALUE;

        // Don't want to touch the full-size image with this loop, so skip last item
        for (var i = 0; i < this.images.length - 1; i++) {
            if (this.heights[i] < minHeight || this.heights[i] > maxHeight)
                continue;
            if (this.loadStarted[i])
                return -1;
            var delta = Math.abs(this.heights[i] - preferredHeight);
            if (bestIdx < 0 || delta < bestDelta) {
                bestIdx = i;
                bestDelta = delta;
            }
        }

        // Don't download full-size image if any thumbnails are available
        var anyThumbnails = this.images.length > 1;
        if (anyThumbnails) {
            if (bestIdx < 0) { // Didn't find any photos to download
                // If any downloads have started, we don't initiate a download
                if (this.imageLoaded.some(function (elem, _, __) { return !!elem; }))
                    return -1;
                // Else we download some thumbnail, at least
                return 0;
            }

            return bestIdx;
        }

        return this.loadStarted[0] ? -1 : 0;
    }-*/;

    // Helper function for downloadIfNecessary, which initiates a download on the image at the
    // specified index, keeping the bookkeeping information in this.loadStarted, this.loadFailed,
    // and this.imageLoaded correct
    private native void downloadImage(int idx) /*-{
        var getter = this;
        // If we used this keyword inside callbacks, would get wrong this because any
        // JavaScript function defines a new constructor

        this.images[idx].onload = function() {
            getter.imageLoaded[idx] = true;
            getter.loadFailed[idx] = false;

            if (getter.images[idx].width && getter.images[idx].width > 0)
                getter.widths[idx] = getter.images[idx].width;

            if (getter.images[idx].height && getter.images[idx].height > 0)
                getter.heights[idx] = getter.images[idx].height;

            if (!!getter.callback) {
                // In Java-like style:
                // getter.callback.onSuccess(getter);
                getter.callback.@org.bodytrack.client.PhotoSeriesPlot.PhotoAlertable::onSuccess(Lorg/bodytrack/client/PhotoGetter;)(getter);
            }
        };
        this.images[idx].onerror = function() {
            if (!getter.imageLoaded[idx])
                getter.loadFailed[idx] = true;

            if (!!getter.callback) {
                // In Java-like style:
                // getter.callback.onFailure(getter);
                getter.callback.@org.bodytrack.client.PhotoSeriesPlot.PhotoAlertable::onFailure(Lorg/bodytrack/client/PhotoGetter;)(getter);
            }
        };

        this.loadStarted[idx] = true;
        this.images[idx].src = this.urls[idx]; // Actually initiate download
    }-*/;

    /**
     * A handy shortcut to the native drawImageBounded method
     *
     * <p>
     * This gets the position, width, and height of bounds, and uses
     * that information to call the other
     * {@link #drawImageBounded(String, double, double, double, double,
     * double, double, double, double) drawImageBounded} with the correct
     * parameters.
     * </p>
     *
     * @param canvas
     * 	The canvas we should use to draw the image
     * @param x
     * 	The X-position of the <em>center</em> of the image, in pixels
     * 	from the left edge of the canvas
     * @param y
     * 	The Y-position of the <em>center</em> of the image, in pixels
     * 	from the top edge of the canvas
     * @param width
     * 	The width of the image
     * @param height
     * 	The height of the image
     * @param bounds
     * 	A {@link BoundedDrawingBox} with the bounds that we should
     * 	use to constrain the image drawing
     * @return
     * 	<code>true</code> if and only if the image was successfully drawn,
     * 	meaning that {@link #imageLoaded() imageLoaded} returns
     * 	<code>true</code> and that canvas is a valid HTML canvas.  Note that
     * 	this does <em>not</em> return <code>false</code> if everything else
     * 	is fine but the image is outside the bounding box; a caller can
     * 	check for this using arithmetic, so we do not alert a caller to
     * 	that event
     */
    public boolean drawImageBounded(Element canvas, double x, double y,
            double width, double height, BoundedDrawingBox bounds) {
        Vector2 topLeft = bounds.getTopLeft();

        return drawImageBounded(canvas, x, y, width, height,
                topLeft.getX(),
                topLeft.getY(),
                bounds.getWidth(),
                bounds.getHeight());
    }

    /**
     * Draws the image with the specified <strong>center</strong> location
     * and dimensions, and only inside the specified box.
     *
     * <p>This is exactly like
     * {@link #drawImage(String, double, double, double, double) drawImage},
     * except that it also takes parameters for the bounds of the region
     * where we might draw the image.</p>
     *
     * @param canvas
     * 	The canvas we should use to draw the image
     * @param x
     * 	The X-position of the <em>center</em> of the image, in pixels
     * 	from the left edge of the canvas
     * @param y
     * 	The Y-position of the <em>center</em> of the image, in pixels
     * 	from the top edge of the canvas
     * @param width
     * 	The width of the image
     * @param height
     * 	The height of the image
     * @param minX
     * 	The minimum X-value that is within the bounds
     * @param minY
     * 	The minimum Y-value that is within the bounds
     * @param boundsWidth
     * 	The width of the clipping region i.e. the width of the region
     * 	in which we will draw an image
     * @param boundsHeight
     * 	The height of the clipping region i.e. the height of the
     * 	region in which we will draw an image
     * @return
     * 	<code>true</code> if and only if the image was successfully drawn,
     * 	meaning that {@link #imageLoaded()} returns <code>true</code> and
     * 	that canvas is a valid HTML canvas.  Note that this does <em>not</em>
     * 	return <code>false</code> if everything else is fine but the image
     * 	is outside the bounding box; a caller can check for that using
     * 	arithmetic, so this method does not alert a caller to that event
     */
    public native boolean drawImageBounded(Element canvas, double x, double y,
            double width, double height, double minX, double minY,
            double boundsWidth, double boundsHeight) /*-{
        var largestLoadedIdx = -1;
        for (var i = 0; i < this.imageLoaded.length; i++) {
            if (this.imageLoaded[i]) {
                if (largestLoadedIdx < 0 || this.heights[i] > this.heights[largestLoadedIdx]) {
                    largestLoadedIdx = i;
                }
            }
        }

        // Same as drawImage, except with clipping also enabled
        if (largestLoadedIdx < 0) return false;
        if (!(canvas && canvas.getContext)) return false;

        var ctx = canvas.getContext('2d');
        if (!ctx) return false;

        ctx.save();
        ctx.beginPath();
        ctx.rect(minX, minY, boundsWidth, boundsHeight);
        ctx.closePath();
        ctx.clip();

        var img = this.images[largestLoadedIdx];
        ctx.drawImage(img, x - width / 2, y - height / 2, width, height);

        ctx.restore();

        return true;
    }-*/;

    // Compare first by floor of time, then by user ID, then by image ID
    @Override
    public int compareTo(PhotoGetter other) {
        if (other == null)
            return 1;

        if (getUserId() == other.getUserId()
                && getImageId() == other.getImageId())
            return 0;

        return DATE_COMPARATOR.compare(getTime(), other.getTime());
    }

    private static class PhotoGetterEqualsHashcodeProvider
            implements EqualsHashcodeProvider<PhotoGetter> {

        @Override
        public boolean equals(PhotoGetter obj1, PhotoGetter obj2) {
            if (obj1 == null || obj2 == null)
                return obj1 == null && obj2 == null;

            return (obj1.getUserId() == obj2.getUserId())
                    && (obj1.getImageId() == obj2.getImageId());
        }

        @Override
        public int hashCode(PhotoGetter obj) {
            if (obj == null)
                return 0;

            return (obj.getUserId() << 16) + obj.getImageId();
        }
    }
}
