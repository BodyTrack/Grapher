package org.bodytrack.client;

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
		for (GraphAxis yAxis: channelMgr.getYAxes()) {
			Vector2 begin = yAxis.project2D(yAxis.getMax())
				.add(Vector2.UNIT_X.scale(yAxis.getWidth()));
			Vector2 end = yAxis.project2D(yAxis.getMin())
				.add(Vector2.UNIT_X.scale(yAxis.getWidth()));
			double height = end.subtract(begin).getY();

			// Paint the device label
			// TODO: Combine neighboring device labels into one device label
			canvas.setFillStyle(DEVICE_NAME_BACKGROUND_COLOR);
			paintLabelBackground(canvas,
				begin,
				labelerWidth - DEVICE_NAME_RIGHT_MARGIN,
				height,
				DEVICE_NAME_CORNER_SIZE);

			// Paint the channel label
			canvas.setFillStyle(CHANNEL_NAME_BACKGROUND_COLOR);
			double channelNameWidth = Math.max(CHANNEL_NAME_MIN_WIDTH,
				CHANNEL_NAME_PROPORTION * labelerWidth);
			paintLabelBackground(canvas, begin, channelNameWidth, height,
				CHANNEL_NAME_CORNER_SIZE);
		}
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
