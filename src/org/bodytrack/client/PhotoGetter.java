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

		ImageElement defaultElem = ImageElement.as(DEFAULT_IMAGE.getElement());
		if (defaultElem != null) {
			// defaultElem should never be null
			defaultElem.setWidth(DEFAULT_WIDTH);
			defaultElem.setHeight(DEFAULT_IMAGE_HEIGHT);
		}
	}

	// Used for equality and hashing
	private final int userId;
	private final int imageId;

	// Used for grabbing image
	private final Image img;
	private final String baseUrl;
	private final PhotoGetterHandler eventHandler;

	// Data about what happened to the image
	private boolean imageLoaded;
	private boolean loadFailed;

	/**
	 * Creates a new <tt>PhotoGetter</tt> to retrieve information
	 * for the specified user.
	 *
	 * @param userId
	 * 		the user ID for the user requesting this image
	 * @param imageId
	 * 		the ID of the image we want to get
	 */
	public PhotoGetter(int userId, int imageId) {
		this.userId = userId;
		this.imageId = imageId;

		img = new Image();

		baseUrl = getBaseUrl(userId, imageId);

		eventHandler = new PhotoGetterHandler();
		img.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				eventHandler.onLoad(event);
				consoleLog(event.toDebugString());
			}

			private native void consoleLog(String debugString) /*-{
				console.log(debugString);
			}-*/;
		});
		// img.addLoadHandler(eventHandler);
		img.addErrorHandler(eventHandler);

		imageLoaded = false;
		loadFailed = false;

		String url = getUrl(baseUrl);
		img.setUrl(url);
	}

	/**
	 * Builds the baseUrl variable for an object to use for an image
	 * with the specified user ID and image ID.
	 *
	 * <p>This method, along with {@link #getUrl(String)}, encapsulates
	 * the logic for image URL production.</p>
	 *
	 * @param userId
	 * 		the ID of the user who owns the image
	 * @param imageId
	 * 		the ID of the image to download
	 * @return
	 * 		the baseUrl variable to use for getting images for the
	 * 		specified user ID and image ID
	 */
	private static String getBaseUrl(int userId, int imageId) {
		// You can find a photo at
		// the URL /users/:user_id/logphotos/:id.:width.jpg
		// Note that baseUrl leaves off the :width.jpg portion of
		// the URL
		return "/users/" + userId + "/logphotos/" + imageId + ".";
	}

	/**
	 * Builds the URL to use to get a photo of width DEFAULT_WIDTH.
	 *
	 * <p>This method, along with {@link #getBaseUrl(int, int)},
	 * encapsulates the logic for image URL production.</p>
	 *
	 * @param baseUrl
	 * 		the baseUrl value that holds the user ID and image ID
	 * 		information for the image
	 * @return
	 * 		the URL to use to get the image with width DEFAULT_WIDTH
	 */
	private static String getUrl(String baseUrl) {
		return baseUrl + DEFAULT_WIDTH + ".jpg";
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
		ImageElement elem = ImageElement.as(img.getElement());

		if (elem != null) {
			// TODO: Remove next 2 lines
			elem.setWidth(DEFAULT_WIDTH);
			elem.setHeight(DEFAULT_IMAGE_HEIGHT);

			return elem;
		}

		return ImageElement.as(DEFAULT_IMAGE.getElement());

		// OLD VERSION
		// if (imageLoaded)
			// This is safe because img is an Image object
			// that must contain the data we want
		//	return ImageElement.as(img.getElement());

		// return ImageElement.as(DEFAULT_IMAGE.getElement());
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
	 * Returns the user ID used to construct this <tt>PhotoGetter</tt>.
	 *
	 * @return
	 * 		the user ID passed to the constructor when this
	 * 		<tt>PhotoGetter</tt> was created
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Returns the image ID used to construct this <tt>PhotoGetter</tt>.
	 *
	 * @return
	 * 		the image ID passed to the constructor when this
	 * 		<tt>PhotoGetter</tt> was created
	 */
	public int getImageId() {
		return imageId;
	}

	/**
	 * Computes a hashcode for this object based on image ID and user ID.
	 *
	 * @return
	 * 		a hashcode based on the image ID and user ID passed in to
	 * 		the constructor when this object was created
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + imageId;
		result = prime * result + userId;
		return result;
	}

	/**
	 * Tells whether this and obj have the same image ID and user ID.
	 *
	 * @return
	 * 		<tt>true</tt> if and only if obj is of type <tt>PhotoGetter</tt>
	 * 		and has the same image ID and user ID (from the constructor)
	 * 		as this object does
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PhotoGetter))
			return false;
		PhotoGetter other = (PhotoGetter) obj;
		if (imageId != other.imageId)
			return false;
		if (userId != other.userId)
			return false;
		return true;
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
			alert();
			imageLoaded = true;
			loadFailed = false; // This is for the unlikely event
				// in which we get an image after an error of some kind
		}

		private native void alert() /*-{
			alert(1);
		}-*/;

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
