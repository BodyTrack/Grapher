package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.TextAlign;
import gwt.g2d.client.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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

public class GraphWidget extends Surface {
	public static final String DEFAULT_GRAPHER_ID = "bodytrack-gwt-viewer";

	/**
	 * The default loading message for this widget to show.
	 */
	public static final String DEFAULT_LOADING_MESSAGE = "Loading...";

	private static final double MOUSE_WHEEL_ZOOM_RATE_MAC = 1.003;
	private static final double MOUSE_WHEEL_ZOOM_RATE_PC = 1.1;

	private static final double HIGHLIGHT_DISTANCE_THRESHOLD = 5;
	private static final double PHOTO_HIGHLIGHT_DISTANCE_THRESH = 10;

	private static final int PAINT_TWICE_DELAY = 10;

	private static final int INITIAL_MESSAGE_ID = 1;
	private static final Color LOADING_MSG_COLOR = Canvas.DARK_GRAY;
	private static final int LOADING_MSG_X_MARGIN = 5;
	private static final int LOADING_MSG_Y_MARGIN = 5;
	private static final int TEXT_HEIGHT = 12;

	private List<DataPlot> dataPlots;

	/* xAxes and yAxes provide the reverse mapping from
	 * dataPlots.get(i).getXAxis() and dataPlots.get(i).getYAxis().
	 * They map from axes to sets of data plots associated with
	 * those axes
	 */
	private final Map<GraphAxis, List<DataPlot>> xAxes;
	private final Map<GraphAxis, List<DataPlot>> yAxes;

	private int nextMessageId;
	private final List<MessageIdPair> loadingMessages;

	private final int width, height;
	private final int axisMargin;
	private final int graphMargin = 5;

	private GraphAxis mouseDragAxis;
	private Vector2 mouseDragLastPos;

	// Once a GraphWidget object is instantiated, this doesn't change
	private final double mouseWheelZoomRate;

	public GraphWidget(int width, int height, int axisMargin) {
		super(width, height);
		this.width = width;
		this.height = height;
		this.axisMargin = axisMargin;

		dataPlots = new ArrayList<DataPlot>();
		xAxes = new HashMap<GraphAxis, List<DataPlot>>();
		yAxes = new HashMap<GraphAxis, List<DataPlot>>();

		nextMessageId = INITIAL_MESSAGE_ID;
		loadingMessages = new ArrayList<MessageIdPair>();

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
	 * 		<tt>true</tt> if and only if the grapher should zoom
	 * 		Mac-style
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
			// "Apple Computer Inc.", and Firefox 3.5 appears
			// to have no navigator.vendor

			isSafari =
				$wnd.navigator.vendor.indexOf("Apple Computer") >= 0;
		}

		return isSafari && !!$wnd.navigator.platform.match(/.*mac/i);
	}-*/;

	private GraphAxis findAxis(Vector2 pos) {
		for (GraphAxis axis: xAxes.keySet()) {
			if (axis.contains(pos))
				return axis;
		}

		for (GraphAxis axis: yAxes.keySet()) {
			if (axis.contains(pos))
				return axis;
		}

		return null;
	}

	private void handleMouseWheelEvent(MouseWheelEvent event) {
		Vector2 eventLoc = new Vector2(event.getX(), event.getY());
		GraphAxis axis = findAxis(eventLoc);

		double zoomFactor = Math.pow(mouseWheelZoomRate,
			event.getDeltaY());

		if (axis != null) {
			boolean canZoomIn = true;

			// Enforce minimum zoom: if any axis allows zooming,
			// the user is able to zoom on the X-axis
			if (xAxes.containsKey(axis)) {
				canZoomIn = false;

				for (DataPlot plot: xAxes.get(axis))
					if (plot.shouldZoomIn())
						canZoomIn = true;
			}

			if (zoomFactor >= 1 || canZoomIn) {
				double zoomAbout = axis.unproject(eventLoc);
				axis.zoom(zoomFactor, zoomAbout);
			}
		} else {
			// We are in the main viewing area

			boolean highlighted = false;
			Set<DataPlot> highlightedPlots = new HashSet<DataPlot>();

			for (DataPlot plot: dataPlots) {
				if (plot.isHighlighted()) {
					highlighted = true;
					highlightedPlots.add(plot);
				}
			}

			if (highlighted) {
				// Only move the axes for highlighted data plot(s)
				for (DataPlot plot: highlightedPlots) {
					GraphAxis yAxis = plot.getYAxis();
					double zoomAbout = yAxis.unproject(eventLoc);
					yAxis.zoom(zoomFactor, zoomAbout);
				}
			} else {
				// Zoom all Y-axes
				for (GraphAxis yAxis: yAxes.keySet()) {
					double zoomAbout = yAxis.unproject(eventLoc);
					yAxis.zoom(zoomFactor, zoomAbout);
				}
			}
		}

		paint();
	}

	private void handleMouseDownEvent(MouseDownEvent event) {
		Vector2 pos = new Vector2(event.getX(), event.getY());

		mouseDragAxis = findAxis(pos);
		mouseDragLastPos = pos;

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
			for (DataPlot plot: dataPlots)
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

				for (GraphAxis xAxis: xAxes.keySet())
					xAxis.drag(mouseDragLastPos, pos);

				for (GraphAxis yAxis: yAxes.keySet())
					yAxis.drag(mouseDragLastPos, pos);
			}

			mouseDragLastPos = pos;
		} else {
			// We are not dragging anything, so we just update the
			// highlighting on the data plots and axes

			for (DataPlot plot: dataPlots) {
				plot.unhighlight();

				double distanceThreshold = (plot instanceof PhotoDataPlot)
					? PHOTO_HIGHLIGHT_DISTANCE_THRESH
					: HIGHLIGHT_DISTANCE_THRESHOLD;
				plot.highlightIfNear(pos, distanceThreshold);
			}

			// Now we handle highlighting of the axes
			setAxisHighlighting(xAxes);
			setAxisHighlighting(yAxes);
		}

		paint();
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
		mouseDragAxis = null;
		mouseDragLastPos = null;

		paint();
	}

	private void handleMouseOutEvent(MouseOutEvent event) {
		mouseDragAxis = null;
		mouseDragLastPos = null;

		// Ensure that all data plots are unhighlighted, as are
		// all axes

		for (DataPlot plot: dataPlots)
			plot.unhighlight();

		for (GraphAxis axis: xAxes.keySet())
			axis.unhighlight();

		for (GraphAxis axis: yAxes.keySet())
			axis.unhighlight();

		paint();
	}

	private void layout() {
		int xAxesWidth = calculateAxesWidth(xAxes.keySet());
		int yAxesWidth = calculateAxesWidth(yAxes.keySet());
		int graphWidth = width - graphMargin - yAxesWidth;
		int graphHeight = height - graphMargin - xAxesWidth;
		Vector2 xAxesBegin = new Vector2(graphMargin,
			graphHeight + graphMargin);
		layoutAxes(xAxes.keySet(), graphWidth, xAxesBegin,
			Basis.xDownYRight);

		Vector2 yAxesBegin = new Vector2(graphWidth+graphMargin,
			graphHeight + graphMargin);
		layoutAxes(yAxes.keySet(), graphHeight, yAxesBegin,
			Basis.xRightYUp);
	}

	private int calculateAxesWidth(Set<GraphAxis> axes) {
		if (axes.size() == 0)
			return 0;

		int ret = -axisMargin;
		// Don't want to add the margin an extra time for the first
		// axis, so we subtract once initially, and we know that, by
		// the return statement, ret will be positive because we
		// checked whether axes contains any elements

		for (GraphAxis axis: axes)
			ret += axis.getWidth() + axisMargin;

		return ret;
	}

	private void layoutAxes(Set<GraphAxis> axes, double length,
			Vector2 begin, Basis basis) {
		Vector2 offset = begin;

		for (GraphAxis axis: axes) {
			axis.layout(offset, length);

			offset = offset.add(
					basis.x.scale(axisMargin + axis.getWidth()));
		}
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
			String msg = loadingMessages.get(0).getMessage();

			showLoadingMessage(msg);
		}

		// Draw the axes
		for (GraphAxis xAxis: xAxes.keySet())
			xAxis.paint(this);

		for (GraphAxis yAxis: yAxes.keySet())
			yAxis.paint(this);

		// Now draw the data
		for (DataPlot plot: dataPlots)
			plot.paint();

		this.restore();
	}

	private void showLoadingMessage(String msg) {
		// Save old data to be restored later
		TextAlign oldTextAlign = getTextAlign();

		// Change settings
		setTextAlign(TextAlign.LEFT);
		setStrokeStyle(LOADING_MSG_COLOR);

		// Actually write the text
		int bottom = height - LOADING_MSG_Y_MARGIN;
		int textTop = bottom - TEXT_HEIGHT;
		strokeText(msg, LOADING_MSG_X_MARGIN, textTop);

		// Restore old settings
		setTextAlign(oldTextAlign);
		setStrokeStyle(Canvas.DEFAULT_COLOR);
	}

	/**
	 * Returns <tt>true</tt> if and only if this widget holds a
	 * <strong>reference</strong> to axis.
	 *
	 * In other words, this will not return <tt>true</tt> unless
	 * this GraphWidget contains the exact GraphAxis object axis,
	 * regardless of whether this contains an axis identical
	 * to (but with a different memory location from) axis.
	 *
	 * @param axis
	 * 		the {@link org.bodytrack.client.GraphAxis GraphAxis}
	 * 		for which to look
	 * @return
	 * 		<tt>true</tt> iff this GraphWidget holds a reference
	 * 		to axis
	 */
	public boolean refersToAxis(GraphAxis axis) {
		return xAxes.containsKey(axis) || yAxes.containsKey(axis);
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
		if (plot == null)
			throw new NullPointerException("Cannot add a null DataPlot");

		if (! dataPlots.contains(plot))
			dataPlots.add(plot);
		else
			return;

		// TODO: Check for bug if the same axis is both an X-axis
		// and a Y-axis, which should never happen in reality

		if (! xAxes.containsKey(plot.getXAxis())) {
			List<DataPlot> axisList = new ArrayList<DataPlot>();
			axisList.add(plot);
			xAxes.put(plot.getXAxis(), axisList);
		} else
			xAxes.get(plot.getXAxis()).add(plot);

		if (! yAxes.containsKey(plot.getYAxis())) {
			List<DataPlot> axisList = new ArrayList<DataPlot>();
			axisList.add(plot);
			yAxes.put(plot.getYAxis(), axisList);
		} else
			yAxes.get(plot.getYAxis()).add(plot);
	}
	
	/**
	 * Removes plot from the list of data plots to be drawn.
	 * 
	 * @param plot
	 * 		the plot to remove from the list of plots to be drawn
	 * @throws NullPointerException
	 * 		if plot is <tt>null</tt>
	 * @throws NoSuchElementException
	 * 		if plot is not one of the
	 * 		{@link org.bodytrack.client.DataPlot DataPlot} objects
	 * 		referenced by this GraphWidget
	 */
	public void removeDataPlot(DataPlot plot) {
		if (plot == null)
			throw new NullPointerException("Cannot remove null DataPlot");

		if (! dataPlots.contains(plot))
			throw new NoSuchElementException("Cannot remove DataPlot "
				+ "that is not used in this GraphWidget");

		GraphAxis xAxis = plot.getXAxis();
		GraphAxis yAxis = plot.getYAxis();

		dataPlots.remove(plot);

		if (xAxes.get(xAxis).size() > 1)
			xAxes.get(xAxis).remove(plot);
		else
			xAxes.remove(xAxis);

		if (yAxes.get(yAxis).size() > 1)
			yAxes.get(yAxis).remove(plot);
		else
			yAxes.remove(yAxis);
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
	 * work.</p>
	 *
	 * @param msg
	 * 		the message to display whenever there are no older
	 * 		messages left in the queue
	 * @return
	 * 		an integer ID that can be used to remove msg whenever
	 * 		it should no longer appear
	 * @see #removeLoadingMessage(int)
	 */
	public int addLoadingMessage(String msg) {
		int id = nextMessageId;
		nextMessageId++;

		loadingMessages.add(new MessageIdPair(id, msg));

		return id;
	}

	/**
	 * Removes the specified message from the queue of messages
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
	public String removeLoadingMessage(int messageId) {
		// Since IDs are assigned sequentially, starting at
		// INITIAL_MESSAGE_ID, this allows us to avoid a search
		// in some cases
		if (messageId < INITIAL_MESSAGE_ID || messageId >= nextMessageId)
			return null;

		Iterator<MessageIdPair> it = loadingMessages.iterator();

		String result = null;

		while (it.hasNext()) {
			MessageIdPair curr = it.next();

			if (result == null && curr.getId() == messageId) {
				result = curr.getMessage();
				it.remove();
			}
		}

		return result;
	}

	private static class MessageIdPair implements Comparable<MessageIdPair> {
		private final int id;
		private final String msg;

		public MessageIdPair(int id, String msg) {
			this.id = id;
			this.msg = msg;
		}

		public int getId() {
			return id;
		}

		public String getMessage() {
			return msg;
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (! (o instanceof MessageIdPair))
				return false;
			MessageIdPair other = (MessageIdPair) o;
			return compareTo(other) == 0;
		}

		@Override
		public int compareTo(MessageIdPair other) {
			return id - other.id;
		}
	}
}
