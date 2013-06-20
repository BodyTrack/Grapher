package org.bodytrack.client;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;

/**
 * <p>
 * <code>BaseMouseWheelHandler</code> is an abstract {@link MouseWheelHandler} which
 * handles things such as preventing event propagation and setting the proper zoom
 * rate depending on which browser is being used.
 * </p>
 *
 * <p>
 * This deals with the rather hairy task of normalizing mouse wheel deltas across
 * browsers.  Some other implementations and pieces of useful information are available
 * at <a href="https://github.com/brandonaaron/jquery-mousewheel/">jQuery mousewheel</a>,
 * <a href="http://stackoverflow.com/questions/5527601/normalizing-mousewheel-speed-across-browsers">
 * StackOverflow question 5527601</a>, and
 * <a href="http://code.google.com/p/closure-library/source/browse/trunk/closure/goog/events/mousewheelhandler.js">
 * Google closure library</a>.
 * </p>
 *
 * <p>
 * In some cases, this tests the user agent string of the browser.  See
 * <a href="http://www.webkit.org/blog/1580/">the WebKit blog</a> for
 * more on the Safari UA, and see
 * <a href="https://developer.mozilla.org/en/Gecko_user_agent_string_reference">MDN</a>
 * for more on the Firefox UA.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class BaseMouseWheelHandler implements MouseWheelHandler {
	protected static final double MOUSE_WHEEL_ZOOM_RATE = 1.1;

	@SuppressWarnings("unused") // Actually used in native code
	private static final double MOUSE_SCALE_FACTOR = getMouseScaleFactor();

	private static native int getMouseScaleFactor() /*-{
		// Don't do anything unless navigator.platform is available
		if (!($wnd.navigator && $wnd.navigator.platform)) {
			return 1;
		}

		if (!!$wnd.navigator.platform.match(/.*mac/i)) {
			// Mac

			// Safari zooms Mac-style, but Chrome and Firefox zoom
			// Windows-style on the Mac
			if ($wnd.navigator.vendor) {
				// Chrome has vendor "Google Inc.", Safari has vendor
				// "Apple Computer Inc.", and Firefox 3.5, at least,
				// appears to have no navigator.vendor

				if ($wnd.navigator.vendor.indexOf("Apple Computer") >= 0) {
					var ua = $wnd.navigator.userAgent;
					if (!!ua) {
						var match = ua.match(/Version\/(\d+)\.(\d+)\.(\d+)/i);
						if (!!match) {
							var major = parseInt(match[1]);
							var minor = parseInt(match[2]);
							var point = parseInt(match[3]);
							if (major < 5 || (major == 5 && minor < 1)) {
								return 360;
							}
						}
					}
					return 120;
				}
			}
		}
		// Else we're on Linux or Windows

		// The default that seems to work for most browsers and most platforms
		return 40;
	}-*/;

	/**
	 * Delegates to {@link #handleMouseWheelEvent(MouseWheelEvent)} and then
	 * stops event propagation
	 */
	@Override
	public final void onMouseWheel(final MouseWheelEvent event) {
		handleMouseWheelEvent(event, getWheelDelta(event.getNativeEvent()));

		// Stops scrolling meant for the widget from moving the
		// browser's scroll bar
		event.preventDefault();
		event.stopPropagation();
	}

	private native WheelEvent getWheelDelta(final NativeEvent event) /*-{
		var MOUSE_SCALE_FACTOR = @org.bodytrack.client.BaseMouseWheelHandler::MOUSE_SCALE_FACTOR;
		
		var delta = 0, deltaX = 0, deltaY = 0, deltaZ = 0;

		// Based roughly on the jQeury mousehweel plugin (recalling
		// that MOUSE_SCALE_FACTOR = 40 except on a Mac)
		// Also, this flips signs from jQuery mousewheel in order to match GWT behavior
		if (event.wheelDeltaX != null) {
			deltaX = -event.wheelDeltaX / MOUSE_SCALE_FACTOR;
		}
		if (event.wheelDeltaY != null) {
			deltaY = -event.wheelDeltaY / MOUSE_SCALE_FACTOR;
		}
		if (event.wheelDeltaZ != null) {
			deltaZ = -event.wheelDeltaZ / MOUSE_SCALE_FACTOR;
		}
		if (event.wheelDelta != null) {
			delta = -event.wheelDelta / MOUSE_SCALE_FACTOR;
		}
		else if (event.detail != null) {
			delta = event.detail;
		}
		return @org.bodytrack.client.BaseMouseWheelHandler.WheelEvent::new(DDDD)(delta,deltaX,deltaY,deltaZ);
	}-*/;
	
	public static class WheelEvent{
		private double delta;
		private double deltaX;
		private double deltaY;
		private double deltaZ;
		
		public WheelEvent(double delta, double deltaX, double deltaY, double deltaZ){
			this.delta = delta;
			this.deltaX = deltaX;
			this.deltaY = deltaY;
			this.deltaZ = deltaZ;
			if (deltaX == 0 && deltaY == 0 && deltaZ == 0){
				this.deltaX = this.deltaY = this.deltaZ = this.delta;
			}
		}
		
		public double getDelta(){
			return delta;
		}
		
		public double getDeltaX(){
			return deltaX;
		}
		
		public double getDeltaY(){
			return deltaY;
		}
		
		public double getDeltaZ(){
			return deltaZ;
		}
	}

	protected abstract void handleMouseWheelEvent(final MouseWheelEvent event,
			final WheelEvent wheelDelta);
}
