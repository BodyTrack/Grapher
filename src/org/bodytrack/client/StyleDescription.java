package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;

public final class StyleDescription extends JavaScriptObject {
	// JavaScript overlay types always have protected, no-arg constructors
	protected StyleDescription() {}

	public native String getColorName() /*-{
		return this.color;
	}-*/;

	public native LinesDescription getLines() /*-{
		return this.lines;
	}-*/;

	public native PointsDescription getPoints() /*-{
		return this.points;
	}-*/;

	public native BarsDescription getBars() /*-{
		return this.bars;
	}-*/;

	public native CommentsDescription getComments() /*-{
		return this.comments;
	}-*/;

	public native String getDeviceName() /*-{
		return this.device_name;
	}-*/;

	public native String getChannelName() /*-{
		return this.channel_name;
	}-*/;

	// TODO: Remove this function, since it is perfectly permissible
	// for a style to have multiple types (bars and lines, for instance)
	public native String getType() /*-{
		return this.type;
	}-*/;

	/**
	 * Builds up an actual {@link Color} object from the return value
	 * of {@link #getColorName()}
	 *
	 * @param defaultColor
	 * 	The color to return if this style has no color or has a color that
	 * 	can't be parsed by {@link ColorUtils#buildColor(String)}
	 * @return
	 * 	The {@link Color} object that this style holds, if there is
	 * 	such a color, or defaultColor if this style either has no
	 * 	color or has a color that {@link ColorUtils#buildColor(String)}
	 * 	can't parse
	 */
	public Color getColor(Color defaultColor) {
		return ColorUtils.getColor(getColorName(), defaultColor);
	}

	public static final class LinesDescription extends JavaScriptObject {
		// JavaScript overlay types always have protected, no-arg constructors
		protected LinesDescription() {}

		public native boolean show() /*-{
			return !!(this.show);
		}-*/;

		public native double getLineWidth() /*-{
			return this.lineWidth;
		}-*/;

		public native boolean fill() /*-{
			return !!(this.fill);
		}-*/;

		public native String getColorName() /*-{
			return this.fillColor;
		}-*/;
	}

	public static final class PointsDescription extends JavaScriptObject {
		// JavaScript overlay types always have protected, no-arg constructors
		protected PointsDescription() {}

		public native boolean show() /*-{
			return !!(this.show);
		}-*/;

		public native double getLineWidth() /*-{
			return this.lineWidth;
		}-*/;

		public native double getRadius() /*-{
			return this.radius;
		}-*/;

		public native boolean fill() /*-{
			return !!(this.fill);
		}-*/;

		public native String getColorName() /*-{
			return this.fillColor;
		}-*/;
	}

	public static final class BarsDescription extends JavaScriptObject {
		// JavaScript overlay types always have protected, no-arg constructors
		protected BarsDescription() {}

		public native boolean show() /*-{
			return !!(this.show);
		}-*/;

		public native double getLineWidth() /*-{
			return this.lineWidth;
		}-*/;

		public native double getRadius() /*-{
			return this.radius;
		}-*/;

		public native boolean fill() /*-{
			return !!(this.fill);
		}-*/;

		public native boolean hasColor() /*-{
			return !!(this.fillColor);
		}-*/;

		public native boolean isSingleColor() /*-{
			// For information on how to test object type, see
			// the ECMAScript standard at
			// http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-262.pdf
			// on page 33
			return !!(this.fillColor)
				&& (Object.prototype.toString.call(this.fillColor) !== '[object Array]');
		}-*/;

		// Meaningless unless isSingleColor() returns true
		public native String getColor() /*-{
			return !!(this.fillColor);
		}-*/;

		// Meaningless unless isSingleColor() returns false
		public native JsArrayMixed getRawColors() /*-{
			return this.fillColor;
		}-*/;

		// TODO: Add a method convert the output of getRawColors() to
		// something Java can use to draw bars
	}

	public static final class CommentsDescription extends JavaScriptObject {
		// JavaScript overlay types always have protected, no-arg constructors
		protected CommentsDescription() {}

		public native boolean show() /*-{
			return !!(this.show);
		}-*/;
	}
}
