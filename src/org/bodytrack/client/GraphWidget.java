package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.TextAlign;
import gwt.g2d.client.math.Vector2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.Timer;

public class GraphWidget extends Surface implements ChannelChangedListener {
	/**
	 * The default loading message for this widget to show.  This class
	 * never actually uses this value, but makes it available for classes
	 * that use the loading API.
	 */
	public static final String DEFAULT_LOADING_MESSAGE = "Loading...";

	/**
	 * The maximum number of value messages that is <em>guaranteed</em>
	 * to be displayed.  It is possible that, if there are more than
	 * this many value messages, all will be displayed, but no guarantee
	 * is made.  Also, no guarantee is made as to which value messages
	 * will be displayed.
	 *
	 * <p>In order to guarantee display of a message, then, a caller of
	 * {@link #addValueMessage(String)} should ensure that the number of
	 * value messages, as returned by {@link #countValueMessages()}, is
	 * less than this.  If this is not so, messages can always be removed
	 * using {@link #removeValueMessage(int)}.</p>
	 */
	public static final int VALUE_MESSAGES_CAPACITY = 4;

	private static final double MOUSE_WHEEL_ZOOM_RATE_MAC = 1.003;
	private static final double MOUSE_WHEEL_ZOOM_RATE_PC = 1.1;

	private static final double HIGHLIGHT_DISTANCE_THRESHOLD = 5;
	private static final double PHOTO_HIGHLIGHT_DISTANCE_THRESH = 10;

	private static final int PAINT_TWICE_DELAY = 10;

	private static final int INITIAL_MESSAGE_ID = 1;
	private static final Color LOADING_MSG_COLOR = Canvas.DARK_GRAY;
	/* Removed for the EPA Apps for the Environment competition
	private static final double LOADING_MSG_X_MARGIN = 5;
	private static final double LOADING_MSG_Y_MARGIN = 3;
	 */
	private static final double VALUE_MSG_X_MARGIN = 5;
	private static final double VALUE_MSG_Y_MARGIN = 3;
	private static final double VALUE_MSG_GAP = 2;
	private static final double TEXT_HEIGHT = 12;

	private static final int NAME_LABELER_WIDTH = 100;

	private final ChannelManager channelMgr;

	// Draws the right-side name labels
	// We maintain the invariant that nameLabeler.areAxesInCompactOrder() always
	// returns true
	private final ChannelNameLabeler nameLabeler;

	// For the loading message API, which shows one message at a time
	// on the bottom left, without regard to width
	// Invariants: all IDs in loadingMessages conform to
	// INITIAL_MESSAGE_ID <= id < nextLoadingMessageId, with all IDs
	// unique, and there is never a null message string in loadingMessages
	private int nextLoadingMessageId;
	private final List<DisplayMessage> loadingMessages;

	// For the value message API, which shows multiple messages at a time
	// on the bottom right, with strict controls on size
	// Invariants: all IDs in valueMessages conform to
	// INITIAL_MESSAGE_ID <= id < nextValueMessageId, with all IDs
	// unique, and there is never a null message string in valueMessages
	private int nextValueMessageId;
	private final List<DisplayMessage> valueMessages;

	private int width, height;
	private final int axisMargin;
	private final int graphMargin = 5;

	private GraphAxis mouseDragAxis;
	private Vector2 mouseDragLastPos;
	private GraphAxis mouseDragOwningYAxis;

	// Once a GraphWidget object is instantiated, this doesn't change
	private final double mouseWheelZoomRate;

	public GraphWidget(int width, int height, int axisMargin) {
		super(width, height);
		this.width = width;
		this.height = height;
		this.axisMargin = axisMargin;

		channelMgr = new ChannelManager();
		channelMgr.addChannelListener(this);

		// TODO: Possibly allow width updating
		nameLabeler = new ChannelNameLabeler(channelMgr, NAME_LABELER_WIDTH);

		nextLoadingMessageId = INITIAL_MESSAGE_ID;
		loadingMessages = new ArrayList<DisplayMessage>();

		nextValueMessageId = INITIAL_MESSAGE_ID;
		valueMessages = new ArrayList<DisplayMessage>();

		this.addMouseWheelHandler(new MouseWheelHandler() {
			@Override
			public void onMouseWheel(MouseWheelEvent event) {
				handleMouseWheelEvent(event);

				// Stops scrolling meant for the widget from moving the
				// browser's scroll bar
				event.preventDefault();
				event.stopPropagation();
			}
		});

		this.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				handleMouseDownEvent(event);
			}
		});

		this.addMouseMoveHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				handleMouseMoveEvent(event);
			}
		});

		this.addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				handleMouseUpEvent(event);
			}
		});

		this.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				handleMouseOutEvent(event);
			}
		});

		mouseWheelZoomRate = shouldZoomMac()
			? MOUSE_WHEEL_ZOOM_RATE_MAC
			: MOUSE_WHEEL_ZOOM_RATE_PC;
	}
	
	public void setSize(int width, int height){
		super.setSize(width, height);
		this.width = width;
		this.height = height;
		paint();
	}

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
	private native boolean shouldZoomMac() /*-{
		// Don't do anything unless navigator.platform is available
		if (! ($wnd.navigator && $wnd.navigator.platform))
			return false;

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
	 * Returns the axis over which pos sits, or <tt>null</tt> if no such
	 * axis exists.
	 *
	 * @param pos
	 * 		some position, represented as a vector coming from the
	 * 		origin of the coordinate system
	 * @return
	 * 		the axis over which pos is, or <tt>null</tt> if pos is not
	 * 		on top of a vector
	 */
	private GraphAxis findAxis(Vector2 pos) {
		for (GraphAxis axis: channelMgr.getXAxes()) {
			if (axis.contains(pos))
				return axis;
		}

		for (GraphAxis axis: channelMgr.getYAxes()) {
			if (axis.contains(pos))
				return axis;
		}

		return null;
	}

	private void handleMouseWheelEvent(MouseWheelEvent event) {
		Vector2 pos = new Vector2(event.getX(), event.getY());
		GraphAxis axis = findAxis(pos);
		double zoomFactor = Math.pow(mouseWheelZoomRate, event.getDeltaY());

		Set<DataPlot> highlightedPlots = getHighlightedPlots();

		// We can be zooming exactly one of: one or more data plots, an axis,
		// and the whole viewing window
		if (highlightedPlots.size() > 0) {
			// We are mousing over at least one data plot, so we zoom
			// the associated Y-axes

			Set<GraphAxis> highlightedYAxes = new HashSet<GraphAxis>();
			for (DataPlot plot: highlightedPlots)
				highlightedYAxes.add(plot.getYAxis());
			for (GraphAxis yAxis: highlightedYAxes)
				yAxis.zoom(zoomFactor, yAxis.unproject(pos));
		} else if (mouseDragAxis != null || axis != null) {
			// Single axis

			// Zoom the correct single axis
			if (axis == null)
				axis = mouseDragAxis;

			axis.zoom(zoomFactor, axis.unproject(pos));
		} else {
			// The mouse is over the viewing window

			// Enforce minimum zoom: if any axis allows zooming,
			// the user is able to zoom on the X-axes
			boolean canZoomIn = false;

			for (DataPlot plot: channelMgr.getDataPlots())
				canZoomIn = canZoomIn || plot.shouldZoomIn();

			if (zoomFactor >= 1 || canZoomIn) {
				for (GraphAxis xAxis: channelMgr.getXAxes())
					xAxis.zoom(zoomFactor, xAxis.unproject(pos));
			}
		}

		paint();
	}

	private void handleMouseDownEvent(MouseDownEvent event) {
		Vector2 pos = new Vector2(event.getX(), event.getY());

		mouseDragAxis = findAxis(pos);
		mouseDragLastPos = pos;
		mouseDragOwningYAxis = findOwningYAxis(pos);

		paint();
	}

	private void handleMouseMoveEvent(MouseMoveEvent event) {
		Vector2 pos = new Vector2(event.getX(), event.getY());

		// We can be dragging exactly one of: an axis, one or
		// more data plots, the whole viewing window, and nothing
		if (mouseDragAxis != null) {
			// We are dragging an axis
			mouseDragAxis.drag(mouseDragLastPos, pos);
			mouseDragLastPos = pos;
		} else if (mouseDragLastPos != null) {
			// We are either dragging either one or more data plots,
			// or the whole viewing window

			Set<DataPlot> highlightedPlots = new HashSet<DataPlot>();
			for (DataPlot plot: channelMgr.getDataPlots())
				if (plot.isHighlighted())
					highlightedPlots.add(plot);

			if (highlightedPlots.size() > 0) {
				// We are dragging at least one data plot, so we drag
				// the associated axes

				Set<GraphAxis> highlightedAxes = new HashSet<GraphAxis>();
				for (DataPlot plot: highlightedPlots) {
					highlightedAxes.add(plot.getXAxis());
					highlightedAxes.add(plot.getYAxis());
				}

				for (GraphAxis axis: highlightedAxes)
					axis.drag(mouseDragLastPos, pos);
			} else {
				// We are dragging the entire viewing window, so we
				// drag all axes

				// TODO: Replace such doubled loops with a single
				// loop across all axes
				for (GraphAxis xAxis: channelMgr.getXAxes())
					xAxis.drag(mouseDragLastPos, pos);
			}

			mouseDragLastPos = pos;
		} else {
			// We are not dragging anything, so we just update the
			// highlighting on the data plots and axes

			for (DataPlot plot: channelMgr.getDataPlots()) {
				plot.unhighlight();

				double distanceThreshold = (plot instanceof PhotoDataPlot)
					? PHOTO_HIGHLIGHT_DISTANCE_THRESH
					: HIGHLIGHT_DISTANCE_THRESHOLD;
				plot.highlightIfNear(pos, distanceThreshold);
			}

			// Now we handle highlighting of the axes
			setAxisHighlighting(channelMgr.getXAxisMap());
			setAxisHighlighting(channelMgr.getYAxisMap());
		}

		paint();
	}

	/**
	 * Finds the set of highlighted {@link DataPlot} objects.
	 *
	 * @return
	 * 		the set of highlighted {@link DataPlot} objects on this widget
	 */
	private Set<DataPlot> getHighlightedPlots() {
		Set<DataPlot> highlightedPlots = new HashSet<DataPlot>();

		for (DataPlot plot: channelMgr.getDataPlots()) {
			if (plot.isHighlighted())
				highlightedPlots.add(plot);
		}

		return highlightedPlots;
	}

	/**
	 * Highlights each axis in axes.keySet() if and only if there exists
	 * some DataPlot in axes.get(axis) that is highlighted.
	 *
	 * In other words, each axis is highlighted if and only if it is
	 * associated with a DataPlot that is highlighted as well.
	 *
	 * @param axes
	 * 		the map (probably either the xAxes or yAxes instance variable)
	 * 		of axes to sets of DataPlot objects, which describes the
	 * 		relationship between data plots and axes
	 */
	private void setAxisHighlighting(Map<GraphAxis, List<DataPlot>> axes) {
		for (Entry<GraphAxis, List<DataPlot>> entry: axes.entrySet()) {
			// Highlight an axis if any one of the plots associated
			// with that axis is highlighted
			GraphAxis axis = entry.getKey();
			axis.unhighlight();

			for (DataPlot plot: entry.getValue())
				if (plot.isHighlighted()) {
					axis.highlight(plot.getHighlightedPoint());
					break;
				}
		}
	}

	private void handleMouseUpEvent(MouseUpEvent event) {
		Vector2 pos = new Vector2(event.getX(), event.getY());
		GraphAxis owningYAxis = findOwningYAxis(pos);

		// Only allow moving a plot if the user is dragging neither
		// an axis nor a plot i.e. the user is dragging white space
		if (mouseDragAxis == null && getHighlightedPlots().size() == 0) {
			if (mouseDragOwningYAxis != null && mouseDragOwningYAxis != owningYAxis) {
				List<GraphAxis> yAxes = channelMgr.getYAxes();
				int oldIndex = yAxes.indexOf(mouseDragOwningYAxis);
				int newIndex = yAxes.indexOf(owningYAxis);
				if (oldIndex >= 0 && newIndex >= 0 && oldIndex != newIndex) {
					channelMgr.moveYAxis(oldIndex, newIndex);
					if (!nameLabeler.areAxesInCompactOrder()) {
						// Undo the move if it would violate the compact
						// order invariant
						channelMgr.moveYAxis(newIndex, oldIndex);
					}
					paint();
					mouseDragOwningYAxis = owningYAxis;
				}
			}
		}

		mouseDragAxis = null;
		mouseDragLastPos = null;
		mouseDragOwningYAxis = null;

		paint();
	}

	private void handleMouseOutEvent(MouseOutEvent event) {
		mouseDragAxis = null;
		mouseDragLastPos = null;
		mouseDragOwningYAxis = null;

		// Ensure that all data plots are unhighlighted, as are
		// all axes

		for (DataPlot plot: channelMgr.getDataPlots())
			plot.unhighlight();

		for (GraphAxis axis: channelMgr.getXAxes())
			axis.unhighlight();

		for (GraphAxis axis: channelMgr.getYAxes())
			axis.unhighlight();

		paint();
	}

	private void layout() {
		int xAxesWidth = calculateAxesWidth(channelMgr.getXAxes());
		int yAxesWidth = calculateAxesWidth(channelMgr.getYAxes());
		int graphWidth = width - graphMargin - yAxesWidth
			- (int)(nameLabeler.getWidth());
		int graphHeight = height - graphMargin - xAxesWidth;
		Vector2 xAxesBegin = new Vector2(graphMargin,
			graphHeight + graphMargin);
		layoutAxes(channelMgr.getXAxes(), graphWidth, xAxesBegin,
			Basis.xDownYRight);

		Vector2 yAxesBegin = new Vector2(graphWidth + graphMargin,
			graphHeight + graphMargin);
		layoutAxes(channelMgr.getYAxes(), graphHeight, yAxesBegin,
			Basis.xRightYUp);
	}

	private int calculateAxesWidth(Iterable<GraphAxis> axes) {
		double maxWidth = 0;

		for (GraphAxis axis: axes)
			maxWidth = Math.max(axis.getWidth() + axisMargin, maxWidth);

		return (int)(Math.ceil(maxWidth));
	}

	private void layoutAxes(List<GraphAxis> axes, double length,
			Vector2 begin, Basis basis) {
		Vector2 offset = begin;

		double totalMargin = (axes.size() - 1) * axisMargin;
		double len = (length - totalMargin) / axes.size();

		for (GraphAxis axis: axes) {
			axis.layout(offset, len);

			offset = offset.add(basis.y.scale(len + axisMargin));
		}
	}

	/**
	 * Finds the Y-axis that &quot;owns&quot; the portion of the widget
	 * where pos is.  Returns <tt>null</tt> if no Y-axis owns that portion
	 * of the widget.
	 *
	 * @param pos
	 * 		the position to check
	 * @return
	 * 		the owning Y-axis, or null if no such Y-axis exists
	 */
	private GraphAxis findOwningYAxis(Vector2 pos) {
		for (GraphAxis yAxis: channelMgr.getYAxes()) {
			double value = yAxis.unproject(pos);
			if (value > yAxis.getMin() && value < yAxis.getMax())
				return yAxis;
		}

		return null;
	}

	/**
	 * Actually paints this widget twice, with the two paint operations
	 * separated by PAINT_TWICE_DELAY milliseconds.
	 */
	public void paintTwice() {
		paint();

		Timer timer = new Timer() {
			@Override
			public void run() {
				paint();
			}
		};

		timer.schedule(PAINT_TWICE_DELAY);
	}

	public void paint() {
		layout();
		this.clear();
		this.save();
		this.translate(.5, .5);

		// Draw any Loading... messages that might be requested
		if (loadingMessages.size() > 0) {
			showLoadingMessage(loadingMessages.get(0));
		}

		// Draw any value messages that might be requested
		if (valueMessages.size() > 0) {
			// We use the first (oldest) VALUE_MESSAGES_CAPACITY
			// messages in valueMessages, at least for now
			int numMessages = Math.min(VALUE_MESSAGES_CAPACITY,
				valueMessages.size());

			showValueMessages(valueMessages.subList(0, numMessages));
		}

		// Draw the axes
		for (GraphAxis xAxis: channelMgr.getXAxes())
			xAxis.paint(this);

		for (GraphAxis yAxis: channelMgr.getYAxes())
			yAxis.paint(this);

		// Draw the labels
		nameLabeler.paint(Canvas.buildCanvas(this));

		// Now draw the data
		for (DataPlot plot: channelMgr.getDataPlots())
			plot.paint();

		this.restore();
	}

	/**
	 * Shows the specified loading message.
	 *
	 * @param msg
	 * 		a message to be shown at the bottom left corner of
	 * 		the grapher.  Note that we require that msg is not
	 * 		<tt>null</tt>
	 */
	private void showLoadingMessage(DisplayMessage msg) {
		/* Removed for the EPA Apps for the Environment competition
		// Save old data to be restored later
		TextAlign oldTextAlign = getTextAlign();

		// Change settings
		setTextAlign(TextAlign.LEFT);
		setFillStyle(msg.getColor());

		// Actually write the text
		double bottom = height - LOADING_MSG_Y_MARGIN;
		double textTop = bottom - TEXT_HEIGHT;
		fillText(msg.getText(), LOADING_MSG_X_MARGIN, textTop);

		// Restore old settings
		setTextAlign(oldTextAlign);
		setFillStyle(Canvas.DEFAULT_COLOR);
		 */
	}

	/**
	 * Shows the specified value messages.
	 *
	 * @param messages
	 * 		a list of messages to be shown at the bottom right
	 * 		of the grapher.  Note that we require that messages
	 * 		is neither <tt>null</tt> nor empty, contains no
	 * 		<tt>null</tt> elements, and has length less than or
	 * 		equal to {@link #VALUE_MESSAGES_CAPACITY}.
	 */
	private void showValueMessages(List<DisplayMessage> messages) {
		// Save old data to be restored later
		TextAlign oldTextAlign = getTextAlign();

		// Change settings
		setTextAlign(TextAlign.RIGHT);

		// Actually write the text
		double bottom = height - VALUE_MSG_Y_MARGIN;
		double x = width - VALUE_MSG_X_MARGIN;
			// Right edge X-value with right text alignment

		for (DisplayMessage msg: messages) {
			setFillStyle(msg.getColor());

			double textTop = bottom - TEXT_HEIGHT;
			String text = msg.getText();

			// Find left edge, given that we know right edge
			fillText(text, x, textTop);

			// Move upwards for next loop iteration
			bottom = textTop - VALUE_MSG_GAP;
			textTop = bottom - TEXT_HEIGHT;
		}

		// Restore old settings
		setTextAlign(oldTextAlign);
		setFillStyle(Canvas.DEFAULT_COLOR);
	}

	/**
	 * Adds the specified channel to this widget, if it isn't already present.
	 *
	 * @param deviceName
	 * 		the device name for the channel to add
	 * @param channelName
	 * 		the name for the channel to add on the device
	 */
	public void addDataPlotAsync(String deviceName, String channelName) {
		final DataPlotFactory factory = DataPlotFactory.getInstance(this);
		factory.buildDataPlotAsync(deviceName, channelName,
			new Continuation<DataPlot>() {
				@Override
				public void call(DataPlot result) {
					// Equivalent to GraphWidget.this.addDataPlot(result)
					factory.getWidget().addDataPlot(result);
				}
			},
			null); // Ignore failures
	}
	
	/**
	 * Adds a specified channel to the widget and adjusts its y-bounds
	 * @param deviceName
	 * 		the device name for the channel to add
	 * @param channelName
	 * 		the name for the channel to add on the device
	 * @param minY
	 * 		the lower bound of the y-axis
	 * @param maxY
	 * 		the upper bound of the y-axis
	 */
	
	public void addDataPlotAsync(String deviceName, String channelName, final double minY, final double maxY) {
		final DataPlotFactory factory = DataPlotFactory.getInstance(this);
		factory.buildDataPlotAsync(deviceName, channelName,
			new Continuation<DataPlot>() {
				@Override
				public void call(DataPlot result) {
					// Equivalent to GraphWidget.this.addDataPlot(result)
					factory.getWidget().addDataPlot(result);
					result.getYAxis().replaceBounds(minY,maxY);
				}
			},
			null); // Ignore failures
	}
	
	/**
	 * Adjusts the y-axis bounds of a specified channel
	 * @param deviceName
	 * 		the device name for the channel
	 * @param channelName
	 * 		the name for the channel from the device
	 * @param minValue
	 * 		the new minimum value y axis
	 * @param maxValue
	 * 		the new maximum value of the y axis
	 * @return
	 * 		returns true if the channel was successfuly changed, otherwise false.
	 */
	public boolean setDataPlotYBounds(String deviceName, String channelName, double minValue, double maxValue){
		List<DataPlot> plots = channelMgr.getDataPlots();
		for (DataPlot plot : plots){
			if (plot.getDeviceName().equals(deviceName) && plot.getChannelName().equals(channelName)){
				try{
					plot.getYAxis().replaceBounds(minValue,maxValue);
					return true;
				}
				catch (Exception e){
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if the graph widget has the specified channel
	 * @param deviceName
	 * 		the device name of the channel
	 * @param channelName
	 * 		the name of the channel
	 * @return
	 * 		whether the channel exists
	 */
	
	public boolean hasDataPlot(String deviceName, String channelName){
		List<DataPlot> plots = channelMgr.getDataPlots();
		for (DataPlot plot : plots){
			if (plot.getDeviceName().equals(deviceName) && plot.getChannelName().equals(channelName)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the specified channel from this widget, if it is present.  If it isn't
	 * present, does nothing.
	 *
	 * @param deviceName
	 * 		the device name for the channel to remove
	 * @param channelName
	 * 		the name for the channel to remove on the device
	 */
	public void removeDataPlot(String deviceName, String channelName) {
		channelMgr.removeChannel(deviceName, channelName);
	}

	/**
	 * Adds plot to the list of data plots to be drawn.
	 * 
	 * Note that a plot can only be added once to this internal list.
	 * 
	 * @param plot
	 * 		the plot to add to the list of plots to be drawn
	 * @throws NullPointerException
	 * 		if plot is <tt>null</tt>
	 */
	public void addDataPlot(DataPlot plot) {
		channelMgr.addChannel(plot);
	}
	
	/**
	 * Removes plot from the list of data plots to be drawn.
	 *
	 * <p>Does nothing if plot is <tt>null</tt> or not present in
	 * this <tt>GraphWidget</tt>.</p>
	 *
	 * @param plot
	 * 		the plot to remove from the list of plots to be drawn
	 */
	public void removeDataPlot(DataPlot plot) {
		channelMgr.removeChannel(plot);
	}

	/**
	 * An <em>intra-package</em> method for retrieving the
	 * {@link org.bodytrack.client.ChannelManager ChannelManager} this
	 * class holds.
	 *
	 * <p>It is very important that this only be used by classes within
	 * this package.  This method really does break some abstraction
	 * barriers.</p>
	 *
	 * @return
	 * 		the <tt>ChannelManager</tt> this class uses
	 */
	ChannelManager getChannelManager() {
		return channelMgr;
	}

	/**
	 * Fires whenever a channel is added to this widget through the
	 * widget's {@link org.bodytrack.client.ChannelManager ChannelManager}.
	 *
	 * @param deviceName
	 * 		ignored
	 * @param channelName
	 * 		ignored
	 */
	@Override
	public void channelAdded(String deviceName, String channelName) {
		// Don't need to keep any other data in sync, since the
		// ChannelManager handles all that for us
		paint();
	}

	/**
	 * Fires whenever a channel is removed from this widget through the
	 * widget's {@link org.bodytrack.client.ChannelManager ChannelManager}.
	 *
	 * @param deviceName
	 * 		ignored
	 * @param channelName
	 * 		ignored
	 */
	@Override
	public void channelRemoved(String deviceName, String channelName) {
		// Don't need to keep any other data in sync, since the
		// ChannelManager handles all that for us
		paint();
	}

	/**
	 * Adds a loading message
	 *
	 * <p>Note that we implement a FIFO policy for displaying loading
	 * messages.  This means that messages that are posted early will
	 * display until removed.</p>
	 *
	 * <p>This system of assigning integer IDs to messages and then
	 * removing messages by ID is necessitated by the fact that data
	 * is not required to come into the page in the same order
	 * that it is requested.  A simple queue system thus doesn't
	 * work for removal purposes.</p>
	 *
	 * @param message
	 * 		the message to display whenever there are no older
	 * 		messages left in the queue
	 * @return
	 * 		an integer ID that can be used to remove message whenever
	 * 		it should no longer appear
	 * @throws NullPointerException
	 * 		if message is <tt>null</tt>
	 * @see #removeLoadingMessage(int)
	 */
	public final int addLoadingMessage(String message) {
		if (message == null)
			throw new NullPointerException(
				"Null loading message not allowed");

		int id = nextLoadingMessageId;
		nextLoadingMessageId++;

		loadingMessages.add(
			new DisplayMessage(id, message, LOADING_MSG_COLOR));

		return id;
	}

	/**
	 * Removes the specified message from the queue of loading messages
	 * to show.
	 *
	 * <p>It is guaranteed that this method will remove either 0
	 * or 1 messages from the set of messages that could be shown.</p>
	 *
	 * @param messageId
	 * 		the ID of the message to remove
	 * @return
	 * 		the string that was previously associated with messageId,
	 * 		at least if messageId is in the list.  If messageId is
	 * 		not in the list, returns <tt>null</tt>.
	 * @see #addLoadingMessage(String)
	 */
	public final String removeLoadingMessage(int messageId) {
		// Since IDs are assigned sequentially, starting at
		// INITIAL_MESSAGE_ID, this allows us to avoid a search
		// in some cases
		if (messageId < INITIAL_MESSAGE_ID ||
				messageId >= nextLoadingMessageId)
			return null;

		return removeMessage(loadingMessages, messageId);
	}

	/**
	 * Removes the message with the specified ID from messages, if
	 * such a message exists.  Otherwise, does nothing
	 *
	 * @param messages
	 * 		the list of messages from which to remove a message
	 * @param messageId
	 * 		the ID of the message to remove from messages
	 * @return
	 * 		the string message that was removed from messages, or
	 * 		<tt>null</tt> if there is no message in messages with
	 * 		the messageId
	 */
	private static String removeMessage(List<DisplayMessage> messages,
			int messageId) {
		Iterator<DisplayMessage> it = messages.iterator();

		String result = null;

		while (it.hasNext()) {
			DisplayMessage curr = it.next();

			if (curr.getId() == messageId) {
				result = curr.getText();
				it.remove();
			}
		}

		return result;
	}

	/**
	 * Adds a value message
	 *
	 * <p>Note that we try to display all value messages, and indeed
	 * guarantee that we will display all of them, as long as there
	 * are less than or equal to {@link #VALUE_MESSAGES_CAPACITY}.
	 * If there are more than this many messages, it is still possible
	 * that all the messages will be drawn, but no guarantees are made
	 * to which value messages will be drawn, whether some or all.
	 * The count of value messages is always available through a call
	 * to {@link #countValueMessages()}.</p>
	 *
	 * @param message
	 * 		the value message to display if possible
	 * @return
	 * 		an integer ID that can be used to remove message whenever
	 * 		it should no longer appear
	 * @throws NullPointerException
	 * 		if message or color is <tt>null</tt>
	 * @see #removeValueMessage(int)
	 */
	public final int addValueMessage(String message, Color color) {
		if (message == null)
			throw new NullPointerException(
				"Null value message not allowed");

		if (color == null)
			throw new NullPointerException("Null color not allowed");

		int id = nextValueMessageId;
		nextValueMessageId++;

		valueMessages.add(new DisplayMessage(id, message, color));

		return id;
	}

	/**
	 * Removes the specified message from the list of value messages
	 * to show.
	 *
	 * <p>It is guaranteed that this method will remove either 0
	 * or 1 messages from the set of messages that could be shown.</p>
	 *
	 * @param messageId
	 * 		the ID of the message to remove
	 * @return
	 * 		the string that was previously associated with messageId,
	 * 		at least if messageId is in the list.  If messageId is
	 * 		not in the list, returns <tt>null</tt>.
	 * @see #addValueMessage(String)
	 */
	public final String removeValueMessage(int messageId) {
		// Since IDs are assigned sequentially, starting at
		// INITIAL_MESSAGE_ID, this allows us to avoid a search
		// in some cases
		if (messageId < INITIAL_MESSAGE_ID ||
				messageId >= nextValueMessageId)
			return null;

		return removeMessage(valueMessages, messageId);
	}

	/**
	 * Returns the number of value messages currently held in this
	 * <tt>GraphWidget</tt>.
	 *
	 * @return
	 * 		the number of messages that have been added using
	 * 		{@link #addValueMessage(String)} but have not been
	 * 		removed using {@link #removeValueMessage(int)}
	 */
	public final int countValueMessages() {
		return valueMessages.size();
	}

	/**
	 * A class representing a message ID and loading message.
	 *
	 * <p>Note that objects of this class are immutable and therefore
	 * unconditionally thread-safe.  Or at least, in Java this holds.
	 * In JavaScript every object is mutable, and there is only one
	 * thread, so that doesn't really hold anymore.</p>
	 */
	private static final class DisplayMessage
			implements Comparable<DisplayMessage> {
		private final int id;
		private final String text;
		private final Color color;

		/**
		 * Creates a new <tt>MessageIdPair</tt> object.
		 *
		 * @param id
		 * 		the ID this object should hold
		 * @param text
		 * 		the message this object should hold
		 * @param color
		 * 		the color at which the message should be drawn
		 * @throws NullPointerException
		 * 		if either text or color is <tt>null</tt>
		 */
		public DisplayMessage(int id, String text, Color color) {
			if (text == null || color == null)
				throw new NullPointerException(
					"Null constructor parameter not allowed");

			this.id = id;
			this.text = text;
			this.color = color;
		}

		/**
		 * Returns the ID passed to this object's constructor when it
		 * was created.
		 *
		 * @return
		 * 		the ID this object holds
		 */
		public int getId() {
			return id;
		}

		/**
		 * Returns the message parameter passed to this object's
		 * constructor when it was created.
		 *
		 * @return
		 * 		the message this object holds
		 */
		public String getText() {
			return text;
		}

		/**
		 * Returns the color parameter passed to this object's
		 * constructor when it was created.
		 *
		 * @return
		 * 		the color this object holds
		 */
		public Color getColor() {
			return color;
		}

		/**
		 * Computes a hash code for this object.
		 */
		@Override
		public int hashCode() {
			return id;
		}

		/**
		 * Tells if this is logically equal to o.  Note that, if o
		 * is not a MessageIdPair, then this and o must be unequal.
		 * Note that equality is determined by ID.
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (! (o instanceof DisplayMessage))
				return false;
			DisplayMessage other = (DisplayMessage) o;
			return compareTo(other) == 0;
		}

		/**
		 * Compares this to other.  Note that comparison is by ID.
		 */
		@Override
		public int compareTo(DisplayMessage other) {
			if (id > other.id)
				return 1;
			if (id < other.id)
				return -1;
			return 0;
		}
	}
}
