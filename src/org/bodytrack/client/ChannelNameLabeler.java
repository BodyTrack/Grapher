package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.KnownColor;
import gwt.g2d.client.math.Vector2;

/**
 * Draws sections to the right of the Y-axes in order to show the names
 * of channels.
 */
public class ChannelNameLabeler {
	private static final Color DEVICE_NAME_BACKGROUND_COLOR =
		KnownColor.LIGHT_GREY;
	private static final Color CHANNEL_NAME_BACKGROUND_COLOR = KnownColor.GRAY;
	private static final Color TEXT_COLOR = KnownColor.WHITE;

	private static final double DEVICE_NAME_RIGHT_MARGIN = 3;
	private static final double CHANNEL_NAME_PROPORTION = 0.3;
	private static final double CHANNEL_NAME_MIN_WIDTH = 10;
	public static final double DEVICE_NAME_CORNER_SIZE = 8;
	public static final double CHANNEL_NAME_CORNER_SIZE = 5;

	private final ChannelManager channelMgr;
	private final double labelerWidth;

	public ChannelNameLabeler(ChannelManager mgr, double width) {
		channelMgr = mgr;
		labelerWidth = width;
	}

	public double getWidth() {
		return labelerWidth;
	}

	public void paint(Canvas canvas) {
		paintDeviceLabels(canvas);
		paintChannelLabels(canvas);
	}

	private void paintDeviceLabels(Canvas canvas) {
		List<Integer> breaks = getDeviceBreaks();

		for (int i = 0; i < breaks.size() - 1; i++) {
			int beginIndex = breaks.get(i);
			int endIndex = breaks.get(i + 1) - 1;
			paintDeviceLabel(canvas, beginIndex, endIndex);
		}

		if (breaks.size() > 0) {
			// In this case, we know channelMgr.getYAxes() is nonempty
			int beginIndex = breaks.get(breaks.size() - 1);
			int endIndex = channelMgr.getYAxes().size() - 1;
			paintDeviceLabel(canvas, beginIndex, endIndex);
		}
	}

	private void paintDeviceLabel(Canvas canvas, int axisIndex1,
			int axisIndex2) {
		int smallerAxisIndex = Math.min(axisIndex1, axisIndex2);
		int largerAxisIndex = Math.max(axisIndex1, axisIndex2);
		GraphAxis beginAxis = channelMgr.getYAxes().get(largerAxisIndex);
		GraphAxis endAxis = channelMgr.getYAxes().get(smallerAxisIndex);
		String label = getDeviceName(beginAxis);

		Vector2 begin = beginAxis.getMaxPoint();
		Vector2 end = endAxis.getMinPoint();
		double height = end.subtract(begin).getY();

		// Need to paint the device label
		canvas.setFillStyle(DEVICE_NAME_BACKGROUND_COLOR);
		paintLabelBackground(canvas,
			begin.add(Vector2.UNIT_X.scale(beginAxis.getWidth())),
			labelerWidth - DEVICE_NAME_RIGHT_MARGIN,
			height,
			DEVICE_NAME_CORNER_SIZE);
		canvas.setFillStyle(TEXT_COLOR);
		// TODO: canvas.getSurface().strokeText(...)
	}

	private List<Integer> getDeviceBreaks() {
		List<Integer> breaks = new ArrayList<Integer>();
		List<GraphAxis> yAxes = channelMgr.getYAxes();

		String prevDeviceName = null;

		for (int i = 0; i < yAxes.size(); i++) {
			GraphAxis yAxis = yAxes.get(i);
			String deviceName = getDeviceName(yAxis);
			if (deviceName == null || !deviceName.equals(prevDeviceName)) {
				breaks.add(i);
				prevDeviceName = deviceName;
			}
		}

		return breaks;
	}

	private String getDeviceName(GraphAxis yAxis) {
		List<DataPlot> matchingPlots = channelMgr.getYAxisMap().get(yAxis);
		if (matchingPlots != null && matchingPlots.size() > 0)
			return matchingPlots.get(0).getDeviceName();
		return null;
	}

	private void paintChannelLabels(Canvas canvas) {
		// Loop through the axes from top to bottom
		for (GraphAxis yAxis: channelMgr.getYAxes()) {
			Vector2 channelBegin = yAxis.getMaxPoint();
			Vector2 channelEnd = yAxis.getMinPoint();
			double channelHeight = channelEnd.subtract(channelBegin).getY();

			// Paint the channel label
			canvas.setFillStyle(CHANNEL_NAME_BACKGROUND_COLOR);
			double channelNameWidth = Math.max(CHANNEL_NAME_MIN_WIDTH,
				CHANNEL_NAME_PROPORTION * labelerWidth);
			paintLabelBackground(canvas,
				channelBegin.add(Vector2.UNIT_X.scale(yAxis.getWidth())),
				channelNameWidth,
				channelHeight,
				CHANNEL_NAME_CORNER_SIZE);
			canvas.setFillStyle(TEXT_COLOR);
			// TODO: canvas.getSurface().strokeText(...)
		}
	}

	private String getChannelName(GraphAxis yAxis) {
		List<DataPlot> matchingPlots = channelMgr.getYAxisMap().get(yAxis);
		if (matchingPlots != null && matchingPlots.size() > 0)
			return matchingPlots.get(0).getChannelName();
		return null;
	}

	/**
	 * Paints a rectangle with two rounded corners
	 *
	 * @param canvas
	 * 		The canvas on which to draw the rectangle
	 * @param begin
	 * 		The coordinates of the top left corner of the rectangle to draw
	 * @param width
	 * 		The width of the rectangle
	 * @param height
	 * 		The height of the rectangle
	 * @param cornerSize
	 * 		The size of the rounded corners to draw.  It is required that
	 * 		<code>cornerSize < width</code> and that
	 * 		<code>cornerSize < 2 * height</code>.
	 */
	private void paintLabelBackground(Canvas canvas, Vector2 begin,
			double width, double height, double cornerSize) {
		Vector2 topRight = begin.add(Vector2.UNIT_X.scale(width));
		Vector2 botRight = topRight.add(Vector2.UNIT_Y.scale(height));

		canvas.getRenderer()
			.beginPath() // Draw the main rectangle
			.drawRect(begin, width - cornerSize, height)
			.closePath()
			.fill()
			.beginPath() // Draw the rectangle between the arcs
			.drawRect(topRight.add(new Vector2(-cornerSize, cornerSize)),
				cornerSize,
				height - (2 * cornerSize))
			.closePath()
			.fill()
			.beginPath() // Draw the top-right arc
			.moveTo(topRight.subtract(Vector2.UNIT_X.scale(cornerSize)))
			.drawArc(topRight.add(new Vector2(-cornerSize, cornerSize)),
				cornerSize,
				0.0,
				- Math.PI / 2.0,
				true)
			.closePath()
			.fill()
			.beginPath() // Draw the bottom-right arc
			.moveTo(topRight.add(Vector2.UNIT_Y.scale(height - cornerSize)))
			.drawArc(botRight.subtract(new Vector2(cornerSize, cornerSize)),
				cornerSize,
				Math.PI / 2.0,
				0.0,
				true)
			.closePath()
			.fill();
	}

	/**
	 * Tells whether the axes for this labeler are in the most compact order
	 * possible.
	 *
	 * <p>That is to say, returns <code>true</code> if and only if the Y-axes
	 * on the {@link ChannelManager} associated with this object are in an order
	 * such that the least number of device labels is required.  Note that,
	 * except in the trivial cases of 0 or 1 Y-axes on the entire graph, there
	 * are always at least two such orderings.</p>
	 *
	 * @return
	 * 		<code>true</code> if and only if a call to {@link #paint(Canvas)}
	 * 		will draw the fewest possible number of device labels
	 */
	public boolean areAxesInCompactOrder() {
		// TODO: IMPLEMENT
		return false;
	}
}
