package org.bodytrack.client;

/**
 * Publishes information to the page outside of the Grapher widget.  This
 * information is available outside of GWT and offers a consistent interface
 * to the rest of the page, regardless of how GWT compiles this widget.
 *
 * <p>This class deals with the public API between the GWT application
 * and the rest of the webpage.  This is through the window.getCurrentView
 * function, which returns the current view.</p>
 *
 * <p>There are performance reasons to use a function rather than a global
 * variable to allow the webpage to access grapher information.  Calling
 * the window.getCurrentView function from external JavaScript
 * is expensive, but the overall cost isn't too bad as long as the function
 * is called infrequently.  On the other hand, updating a global variable
 * on handled events like scrolling is much more expensive, again as long as
 * we expect relatively infrequent access to this information.  If we find
 * frequent access (several times per second) to the current view, we will
 * have to change the implementation to a global variable that updates on
 * any change in the grapher.</p>
 */
public final class InfoPublisher {
	/**
	 * Initializes the window.getCurrentView function.
	 */
	private static native void initialize(ViewSwitchWidget widget) /*-{
		$wnd.getCurrentView = function() {
			// In Java-like syntax:
			// return widget.getCurrentSavableView();
			return widget.@org.bodytrack.client.ViewSwitchWidget::getCurrentSavableView()();
		};
	}-*/;

	/**
	 * Sets the widget that keeps track of the current view name.
	 *
	 * <p>This should only be set once during the entire life of the page, and
	 * should be set as early as possible, so that outside JavaScript can take
	 * advantage of it.</p>
	 *
	 * @param widget
	 * 		the {@link ViewSwitchWidget} that keeps track of the current
	 * 		view name
	 * @throws NullPointerException
	 * 		if widget is <tt>null</tt>
	 */
	public static void setWidget(ViewSwitchWidget widget) {
		if (widget == null)
			throw new NullPointerException("Cannot use null widget to "
				+ "initialize the window.getCurrentView function");
		initialize(widget);
	}
}
