package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

import com.google.gwt.core.client.JavaScriptObject;

public final class StyleDescription extends JavaScriptObject {
	// JavaScript overlay types always have protected, no-arg constructors
	protected StyleDescription() {}

	public native String getColorName() /*-{
		return this.color;
	}-*/;

	public native JavaScriptObject getLines() /*-{
		return this.lines;
	}-*/;

	public native JavaScriptObject getPoints() /*-{
		return this.points;
	}-*/;

	public native JavaScriptObject getBars() /*-{
		return this.bars;
	}-*/;

	public native String getDeviceName() /*-{
		return this.device_name;
	}-*/;

	public native String getChannelName() /*-{
		return this.channel_name;
	}-*/;

	/**
	 * Builds up an actual {@link Color} object from the return value
	 * of {@link #getColorName()}
	 *
	 * @return
	 * 	The {@link Color} object that this style holds, if there is
	 * 	such a color, or <code>null</code> if this style either has no
	 * 	color or has a color that {@link ColorUtils#buildColor(String)}
	 * 	can't parse
	 */
	public Color getColor() {
		String colorName = getColorName();

		return colorName != null ? ColorUtils.buildColor(colorName) : null;
	}

	public Color getColor(Color defaultColor) {
		Color trueColor = getColor();
		return (trueColor != null) ? trueColor : defaultColor;
	}
}
