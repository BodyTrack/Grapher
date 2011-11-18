package org.bodytrack.client;

import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;

/**
 * <p>
 * <code>BaseMouseWheelHandler</code> is an abstract {@link MouseWheelHandler} which handles things such as preventing
 * event propagation and setting the proper zoom rate depending on which browser is being used.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class BaseMouseWheelHandler implements MouseWheelHandler {

   private static final double MOUSE_WHEEL_ZOOM_RATE_MAC = 1.003;
   private static final double MOUSE_WHEEL_ZOOM_RATE_PC = 1.1;

   private final double mouseWheelZoomRate = shouldZoomMac()
                                             ? MOUSE_WHEEL_ZOOM_RATE_MAC
                                             : MOUSE_WHEEL_ZOOM_RATE_PC;

   /**
    * Tells whether this application should use the Mac scroll wheel ratio.
    *
    * <p>Checks the <tt>navigator.platform</tt> property in JavaScript to
    * determine if this code is on a Mac or not, and returns <tt>true</tt>
    * iff the best guess is Mac.  If this property cannot be read, returns
    * <tt>false</tt>.</p>
    *
    * <p>However, there is a twist: Google Chrome and Firefox seem to zoom
    * Windows-style, regardless of platform.  Thus, this checks for
    * Safari, and only returns <tt>true</tt> if the browser appears to be
    * Safari on the Mac.</p>
    *
    * @return
    * 		<tt>true</tt> if and only if the grapher should zoom Mac-style
    */
   @SuppressWarnings({"NoopMethodInAbstractClass"})
   private static native boolean shouldZoomMac() /*-{
      // Don't do anything unless navigator.platform is available
      if (! ($wnd.navigator && $wnd.navigator.platform)) {
         return false;
      }

      var isSafari = false;

      // Safari zooms Mac-style, but Chrome and Firefox zoom
      // Windows-style on the Mac
      if ($wnd.navigator.vendor) {
         // Chrome has vendor "Google Inc.", Safari has vendor
         // "Apple Computer Inc.", and Firefox 3.5, at least,
         // appears to have no navigator.vendor

         isSafari = $wnd.navigator.vendor.indexOf("Apple Computer") >= 0;
      }

      return isSafari && !!$wnd.navigator.platform.match(/.*mac/i);
   }-*/;

   /**
    * Delegates to {@link #handleMouseWheelEvent(MouseWheelEvent)} and then stops event propagation.
    */
   @Override
   public final void onMouseWheel(final MouseWheelEvent event) {
      handleMouseWheelEvent(event);

      // Stops scrolling meant for the widget from moving the
      // browser's scroll bar
      event.preventDefault();
      event.stopPropagation();
   }

   /** Returns the mouse wheel zoom rate appropriate for the current browser. */
   public final double getMouseWheelZoomRate() {
      return mouseWheelZoomRate;
   }

   protected abstract void handleMouseWheelEvent(final MouseWheelEvent event);
}
