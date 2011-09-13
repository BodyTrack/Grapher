package org.bodytrack.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Publishes information to the page outside of the Grapher widget.  This
 * information is available outside of GWT and offers a consistent interface
 * to the rest of the page, regardless of how GWT compiles this widget.
 *
 * <p>This class deals with the public API between the GWT application
 * and the rest of the webpage.  This is through the window.grapher variable,
 * which contains several functions that the rest of the page can use.</p>
 *
 * <p>There are performance reasons to use a function rather than a global
 * variable to allow the webpage to access grapher information.  Calling
 * the window.grapher.getCurrentView function from external JavaScript
 * is expensive, but the overall cost isn't too bad as long as the function
 * is called infrequently.  On the other hand, updating a global variable
 * on handled events like scrolling is much more expensive, again as long as
 * we expect relatively infrequent access to this information.  If we find
 * frequent access (several times per second) to the current view, we will
 * have to change the implementation to a global variable that updates on
 * any change in the grapher.</p>
 */
public final class InfoPublisher implements ChannelChangedListener {

	private static InfoPublisher instance = null;
	private int uidCounter = 0;
	private Map<Integer, JavaScriptObject> listenerFunctions;

	private InfoPublisher() {
		listenerFunctions = new HashMap<Integer, JavaScriptObject>();
	}

	public static InfoPublisher getInstance() {
		if (instance == null)
			instance = new InfoPublisher();

		return instance;
	}

	/**
	 * Initializes the window.grapher variable.
	 */
	private static native void initialize(ViewSwitchWidget viewSwitcher, InfoPublisher instance) /*-{
		// In Java-like syntax:
		// graphWidget = viewSwitcher.getGraphWidget();
		var graphWidget = viewSwitcher.@org.bodytrack.client.ViewSwitchWidget::getGraphWidget()();

		$wnd.grapher = {};

		$wnd.grapher.getCurrentView = function() {
			// In Java-like syntax:
			// return viewSwitcher.getCurrentSavableView();
			return viewSwitcher.@org.bodytrack.client.ViewSwitchWidget::getCurrentSavableView()();
		};

		$wnd.grapher.addChannel = function(deviceName, channelName) {
			// In Java-like syntax:
			// graphWidget.addDataPlotAsync(deviceName, channelName);
			graphWidget.@org.bodytrack.client.GraphWidget::addDataPlotAsync(Ljava/lang/String;Ljava/lang/String;)(deviceName, channelName);
			return true;
		};

		$wnd.grapher.removeChannel = function(deviceName, channelName) {
			// In Java-like syntax:
			// graphWidget.removeDataPlot(deviceName, channelName);
			graphWidget.@org.bodytrack.client.GraphWidget::removeDataPlot(Ljava/lang/String;Ljava/lang/String;)(deviceName, channelName);
			return true;
		};

		$wnd.grapher.addChannelWithBounds = function(deviceName, channelName, minValue, maxValue) {
			graphWidget.@org.bodytrack.client.GraphWidget::addDataPlotAsync(Ljava/lang/String;Ljava/lang/String;DD)(deviceName, channelName, minValue, maxValue);
			return true;
		}

		$wnd.grapher.hasChannel = function(deviceName, channelName) {
			return graphWidget.@org.bodytrack.client.GraphWidget::hasDataPlot(Ljava/lang/String;Ljava/lang/String;)(deviceName, channelName);
		}

		$wnd.grapher.addChannelChangeListener = function(listenerFunction) {
			return instance.@org.bodytrack.client.InfoPublisher::addJavaScriptListener(Lcom/google/gwt/core/client/JavaScriptObject;)(listenerFunction);
		}

		$wnd.grapher.removeChannelChangeListener = function(listenerId) {
			instance.@org.bodytrack.client.InfoPublisher::removeJavaScriptListener(I)(listenerId);
		}
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
				+ "initialize the window.grapher variable");
		widget.getGraphWidget().getChannelManager().addChannelListener(getInstance());
		initialize(widget, getInstance());
	}

	public int addJavaScriptListener(JavaScriptObject listener) {
		int id = uidCounter;
		listenerFunctions.put(id, listener);
		uidCounter++;
		return id;
	}

	public void removeJavaScriptListener(int id) {
		listenerFunctions.remove(id);
	}

	private native void notifyChannelChanged(JavaScriptObject listenerFunction, String deviceName, String channelName, boolean added) /*-{
		listenerFunction(deviceName,channelName,added);
	}-*/;

	@Override
	public void channelAdded(String deviceName, String channelName) {
		for (JavaScriptObject function : listenerFunctions.values())
			notifyChannelChanged(function,deviceName,channelName,true);
	}

	@Override
	public void channelRemoved(String deviceName, String channelName) {
		for (JavaScriptObject function : listenerFunctions.values())
			notifyChannelChanged(function,deviceName,channelName,false);
	}
}
