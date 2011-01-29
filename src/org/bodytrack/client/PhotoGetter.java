package org.bodytrack.client;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;

/**
 * A class to download a single photo and return the appropriate
 * graphics both before and after the photo is downloaded.  This
 * class also handles resizing images as necessary.
 *
 * <p>A future implementation of this class will be able to handle
 * multiple photo sizes, and automatically decide, when asked for
 * a photo of a certain size, whether to download a new photo or
 * simply to scale the current photo.  To implement this, we will
 * need to change img from a single <tt>Image</tt> object to a list
 * of <tt>Image</tt> objects.  For now, though, this just
 * handles single photo downloads.</p>
 *
 * <p>This class also maintains the logic for building photo
 * download URLs, given the appropriate information.</p>
 */
public final class PhotoGetter {
	/**
	 * At least for now, we always download images at size
	 * DEFAULT_IMAGE_WIDTH and do not use other image sizes.
	 */
	public static final int DEFAULT_WIDTH = 300;

	private static final Image DEFAULT_IMAGE = new Image();
	private static final int DEFAULT_IMAGE_HEIGHT = DEFAULT_WIDTH * 3 / 2;
	static {
		DEFAULT_IMAGE.setWidth(DEFAULT_WIDTH + "px");
		DEFAULT_IMAGE.setHeight(DEFAULT_IMAGE_HEIGHT + "px");
	}

	private final Image img;
	private final String baseUrl;
	private final PhotoGetterHandler eventHandler;

	private boolean imageLoaded;
	private boolean loadFailed;

	public PhotoGetter(int userId, int imageId) {
		img = new Image();

		// You can find a photo at
		// the URL /users/:user_id/logphotos/:id.:width.jpg
		// Note that baseUrl leaves of the :width.jpg portion of
		// the URL
		baseUrl = "/users/" + userId + "/logphotos/" + imageId + ".";

		eventHandler = new PhotoGetterHandler();
		img.addLoadHandler(eventHandler);
		img.addErrorHandler(eventHandler);

		imageLoaded = false;
		loadFailed = false;

		String url = baseUrl + DEFAULT_WIDTH + ".jpg";
		img.setUrl(url);
	}

	/**
	 * Returns an ImageElement of width DEFAULT_IMAGE_WIDTH.
	 *
	 * @return
	 * 		the image with the ID specified in the constructor,
	 * 		if that image is available and has loaded.  If not
	 * 		available yet, this will return an ImageElement
	 * 		for DEFAULT_IMAGE.
	 */
	public ImageElement getElement() {
		if (imageLoaded)
			// This is safe because img is an Image object
			// that must contain the data we want
			return ImageElement.as(img.getElement());

		return ImageElement.as(DEFAULT_IMAGE.getElement());
	}

	/**
	 * Returns <tt>true</tt> if the requested image has loaded,
	 * <tt>false</tt> otherwise.
	 *
	 * @return
	 * 		<tt>true</tt> if and only if the requested image
	 * 		has loaded and is available from
	 * 		{@link #getElement()}
	 */
	public boolean imageLoaded() {
		return imageLoaded;
	}

	/**
	 * Returns <tt>true</tt> if and only if the attempt to load the
	 * image failed.
	 *
	 * @return
	 * 		<tt>true</tt> if the image load encountered an error, and
	 * 		the image has failed to load.  If the image loads despite
	 * 		an error, this will return <tt>false</tt>
	 */
	public boolean loadFailed() {
		return loadFailed;
	}

	/**
	 * A class that hides details of our event handling from users of
	 * the PhotoGetter class and allows a clean set of exported
	 * functions.
	 */
	private class PhotoGetterHandler implements LoadHandler, ErrorHandler {
		/**
		 * Callback whenever the image loads successfully.
		 *
		 * @param event
		 * 		the <tt>LoadEvent</tt> that informs us about
		 * 		the loaded image
		 * 		information about the error
		 */
		@Override
		public void onLoad(LoadEvent event) {
			imageLoaded = true;
			loadFailed = false; // This is for the unlikely event
				// in which we get an image after an error of some kind
		}

		/**
		 * Callback whenever there is an error loading the image.
		 *
		 * @param event
		 * 		the <tt>ErrorEvent</tt> thrown our way to give
		 * 		information about the error
		 */
		@Override
		public void onError(ErrorEvent event) {
			if (! imageLoaded) // We disregard errors once we have success
				loadFailed = true;
		}
	}
}
