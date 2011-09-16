package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.KnownColor;
import gwt.g2d.client.graphics.TextAlign;
import gwt.g2d.client.math.Vector2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Draws sections to the right of the Y-axes in order to show the names
 * of channels.
 */
public class ChannelNameLabeler {
	private static final Color DEVICE_NAME_BACKGROUND_COLOR =
		KnownColor.LIGHT_GREY;
	private static final Color CHANNEL_NAME_BACKGROUND_COLOR = KnownColor.GRAY;
	private static final Color TEXT_COLOR = KnownColor.WHITE;

	private static final double CHANNEL_NAME_PROPORTION = 0.3;
	private static final double CHANNEL_NAME_MIN_WIDTH = 10;
	private static final double DEVICE_NAME_CORNER_SIZE = 8;
	private static final double CHANNEL_NAME_CORNER_SIZE = 5;

	private static final double BUBBLE_WIDTH = 15;
	private static final double BUBBLE_DIAMETER = 10;

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
		// Save old data to be restored later
		TextAlign oldTextAlign = canvas.getSurface().getTextAlign();

		// Change settings
		canvas.getSurface().setTextAlign(TextAlign.CENTER);

		// Actually do the work
		paintDeviceLabels(canvas);
		paintChannelLabels(canvas);

		// Restore old settings
		canvas.getSurface().setTextAlign(oldTextAlign);
		canvas.setFillStyle(Canvas.DEFAULT_COLOR);
	}

	private void paintDeviceLabels(Canvas canvas) {
		List<IntStringPair> breaks = getDeviceBreaks();

		for (int i = 0; i < breaks.size() - 1; i++) {
			int beginIndex = breaks.get(i).getNumber();
			int endIndex = breaks.get(i + 1).getNumber() - 1;
			paintDeviceLabel(canvas, beginIndex, endIndex,
				breaks.get(i).getString());
		}

		if (breaks.size() > 0) {
			// In this case, we know channelMgr.getYAxes() is nonempty
			IntStringPair beginPair = breaks.get(breaks.size() - 1);
			int beginIndex = beginPair.getNumber();
			int endIndex = channelMgr.getYAxes().size() - 1;
			paintDeviceLabel(canvas, beginIndex, endIndex,
				beginPair.getString());
		}
	}

	private void paintDeviceLabel(Canvas canvas, int axisIndex1,
			int axisIndex2, String label) {
		int smallerAxisIndex = Math.min(axisIndex1, axisIndex2);
		int largerAxisIndex = Math.max(axisIndex1, axisIndex2);
		GraphAxis beginAxis = channelMgr.getYAxes().get(largerAxisIndex);
		GraphAxis endAxis = channelMgr.getYAxes().get(smallerAxisIndex);

		Vector2 begin = beginAxis.getMaxPoint();
		Vector2 end = endAxis.getMinPoint();
		double height = end.subtract(begin).getY();

		canvas.setFillStyle(DEVICE_NAME_BACKGROUND_COLOR);
		paintLabelBackground(canvas,
			begin.add(Vector2.UNIT_X.scale(beginAxis.getWidth())),
			labelerWidth,
			height,
			DEVICE_NAME_CORNER_SIZE);

		// Now paint the label itself
		double channelNameWidth = Math.max(CHANNEL_NAME_MIN_WIDTH,
			labelerWidth * CHANNEL_NAME_PROPORTION);
		Vector2 labelStartingPoint = begin
			.add(end.subtract(begin).scale(0.5)) // Vertically in the middle
			.add(Vector2.UNIT_X.scale( // Horizontally in the middle
				beginAxis.getWidth() + channelNameWidth
					+ (labelerWidth - channelNameWidth) / 2.0));
		double availableWidth = labelerWidth - channelNameWidth - BUBBLE_WIDTH;
		label = makeValidLabel(canvas, label, availableWidth);
		canvas.setFillStyle(TEXT_COLOR);
		canvas.getSurface().fillText(label, labelStartingPoint, availableWidth);
	}

	private List<IntStringPair> getDeviceBreaks() {
		List<IntStringPair> breaks = new ArrayList<IntStringPair>();
		List<GraphAxis> yAxes = channelMgr.getYAxes();

		String prevDeviceName = null;

		for (int i = 0; i < yAxes.size(); i++) {
			GraphAxis yAxis = yAxes.get(i);
			String deviceName = getDeviceName(yAxis);
			if (deviceName == null || !deviceName.equals(prevDeviceName)) {
				breaks.add(new IntStringPair(i, deviceName));
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
			Vector2 begin = yAxis.getMaxPoint();
			Vector2 end = yAxis.getMinPoint();
			double height = end.subtract(begin).getY();
			String label = getChannelName(yAxis);

			canvas.setFillStyle(CHANNEL_NAME_BACKGROUND_COLOR);
			double channelNameWidth = Math.max(CHANNEL_NAME_MIN_WIDTH,
				CHANNEL_NAME_PROPORTION * labelerWidth);
			paintLabelBackground(canvas,
				begin.add(Vector2.UNIT_X.scale(yAxis.getWidth())),
				channelNameWidth,
				height,
				CHANNEL_NAME_CORNER_SIZE);

			// Now paint the label itself
			double textX =
				begin.getX() + yAxis.getWidth() + channelNameWidth / 2.0;
			double textY = begin.add(end.subtract(begin).scale(0.5)).getY();
			label = makeValidLabel(canvas, label, height);
			canvas.setFillStyle(TEXT_COLOR);
			canvas.getSurface()
				.rotateCcw(Math.PI / 2.0)
				.fillText(label,
					- textY, // The inversion is because of the rotation,
					textX,   // which changes both X and Y coordinates
					height)
				.rotate(Math.PI / 2.0);
		}
	}

	private String getChannelName(GraphAxis yAxis) {
		List<DataPlot> matchingPlots = channelMgr.getYAxisMap().get(yAxis);
		if (matchingPlots != null && matchingPlots.size() > 0)
			return matchingPlots.get(0).getChannelName();
		return null;
	}

	private String makeValidLabel(Canvas canvas, String label,
			double availableWidth) {
		if (canvas.measureText(label) <= availableWidth)
			return label;

		int idx = label.length() - 1;
		StringBuilder sb = new StringBuilder(label);
		sb.append("...");
		while (canvas.measureText(sb.toString()) > availableWidth) {
			sb.deleteCharAt(idx);
			idx--;
		}

		return sb.toString();
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
		List<IntStringPair> breaks = getDeviceBreaks();

		Set<String> labels = new HashSet<String>();
		for (IntStringPair pair: breaks)
			labels.add(pair.getString());

		// The axes are in compact order if each label would appear
		// exactly once
		return labels.size() == breaks.size();
	}

	/**
	 * An immutable (int, string) tuple type.
	 */
	private static class IntStringPair {
		private int n;
		private String str;

		public IntStringPair(int n, String str) {
			this.n = n;
			this.str = str;
		}

		public int getNumber() {
			return n;
		}

		public String getString() {
			return str;
		}
	}
}
