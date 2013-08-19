package org.bodytrack.client;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public final class StyleDescription extends JavaScriptObject {
   // JavaScript overlay types always have protected, no-arg constructors
   protected StyleDescription() {
   }

   public native JsArray<StyleType> getStyleTypes() /*-{
      return this['styles'];
   }-*/;

   public native CommentsDescription getCommentsDescription() /*-{
      return this['comments'];
   }-*/;

   public native HighlightDescription getHighlightDescription() /*-{
      return this['highlight'];
   }-*/;

   public boolean willShowComments() {
      final CommentsDescription commentsDescription = getCommentsDescription();
      return commentsDescription != null && commentsDescription.willShow();
   }
   
   public native TimespanStyles getTimespanStyles()/*-{
   		return this.timespanStyles;
   }-*/;

   public static final class StyleType extends JavaScriptObject {
      // JavaScript overlay types always have protected, no-arg constructors
      protected StyleType() {
      }

      public native String getType() /*-{
         return this['type'];
      }-*/;

      public native boolean willShow() /*-{
         // default to true if undefined
         return (typeof this['show'] === 'undefined') || !!(this['show']);
      }-*/;

      /**
       * Returns a {@link Color} built from the value of the <code>color</code>
       * field, if one exists and its value is parseable.
       *
       * @param defaultColor
       * 	The color to return if this style has no <code>color</code> field or
       * 	the field has a value that can't be parsed by
       * 	{@link ColorUtils#buildColor(String)}
       * @return
       * 	The {@link Color} object that this style's <code>color</code> field holds,
       * 	if such a field exists, or <code>defaultColor</code> if this style either has no
       * 	such field or has a color that {@link ColorUtils#buildColor(String)}
       * 	can't parse.
       */
      public CssColor getColor(final CssColor defaultColor) {
         return getColorField("color", defaultColor);
      }

      /**
       * Returns a {@link Color} built from the value of the <code>fillColor</code>
       * field, if one exists and its value is parseable.
       *
       * @param defaultColor
       * 	The color to return if this style has no <code>fillColor</code> field or
       * 	the field has a value that can't be parsed by
       * 	{@link ColorUtils#buildColor(String)}
       * @return
       * 	The {@link Color} object that this style's <code>fillColor</code> field holds,
       * 	if such a field exists, or <code>defaultColor</code> if this style either has no
       * 	such field or has a color that {@link ColorUtils#buildColor(String)}
       * 	can't parse.
       */
      public CssColor getFillColor(final CssColor defaultColor) {
         return getColorField("fillColor", defaultColor);
      }

      /**
       * Returns a {@link Color} built from the value of the field specified
       * by the <code>fieldName</code> parameter, if such a field exists and its
       * value is parseable.
       *
       * @param defaultColor
       * 	The color to return if this style does not have a field with
       * 	a name matching the value of the <code>fieldName</code> parameter	or
       * 	the field has a value that can't be parsed by
       * 	{@link ColorUtils#buildColor(String)}
       * @return
       * 	The {@link Color} object held by the field specified by the
       * 	<code>fieldName</code> parameter, if such a field exists, or
       * 	<code>defaultColor</code> if this style either has no
       * 	such field or has a color that {@link ColorUtils#buildColor(String)}
       * 	can't parse.
       */
      public CssColor getColorField(final String fieldName, final CssColor defaultColor) {
         final CssColor color = ColorUtils.getColor(this.<String>getValue(fieldName), defaultColor);
         return color;
      }

      /**
       * Returns the value of the <code>lineWidth</code> field, if such
       * a field exists, otherwise returns the given <code>defaultLineWidth</code>.
       */
      public double getLineWidth(final double defaultLineWidth) {
         return getDoubleValue("lineWidth", defaultLineWidth);
      }

      /** Returns whether the style should be filled.  Defaults to <code>true</code> if undefined. */
      public native boolean willFill() /*-{
         // default to true if undefined
         return (typeof this['fill'] === 'undefined') || !!(this['fill']);
      }-*/;

      public native <T> T getValue(final String fieldName) /*-{
         return this[fieldName];
      }-*/;

      public native double getDoubleValue(final String fieldName, final double defaultValue) /*-{
         return (typeof this[fieldName] === 'undefined') ? defaultValue : this[fieldName];
      }-*/;
   }

   public static final class CommentsDescription extends JavaScriptObject {
      // JavaScript overlay types always have protected, no-arg constructors
      protected CommentsDescription() {
      }

      public native boolean willShow() /*-{
         // default to true if undefined
         return (typeof this['show'] === 'undefined') || !!(this['show']);
      }-*/;

      /**
       * Returns the CSS class name applied to comment container elements.  If not defined, returns <code>null</code>.
       */
      public String getCommentContainerCssClass() {
         return getStringValue("commentContainerCssClass");
      }

      /**
       * Returns the CSS class name applied to comment elements.  If not defined, returns <code>null</code>.
       */
      public String getCommentCssClass() {
         return getStringValue("commentCssClass");
      }

      /**
       * Returns the value of the given <code>fieldName</code>.  If not defined, returns <code>null</code>.
       */
      private native String getStringValue(final String fieldName) /*-{
         return (typeof this[fieldName] === 'undefined') ? null : this[fieldName];
      }-*/;

      /**
       * Returns the value of the <code>verticalMargin</code> field, if such
       * a field exists, otherwise returns the given <code>defaultVerticalMargin</code>.
       */
      public double getVerticalMargin(final double defaultVerticalMargin) {
         return getDoubleValue("verticalMargin", defaultVerticalMargin);
      }

      private native double getDoubleValue(final String fieldName, final double defaultValue) /*-{
         return (typeof this[fieldName] === 'undefined') ? defaultValue : this[fieldName];
      }-*/;

      public native JsArray<StyleType> getStyleTypes() /*-{
         return this['styles'];
      }-*/;
   }
   
   public static final class TimespanStyles extends JavaScriptObject{
	   
	   protected TimespanStyles(){}
	   
	   public native TimespanStyle getDefault()/*-{
	   		return this.defaultStyle;
	   }-*/;
	   
	   public native TimespanStyle getStyle(String value, TimespanStyle pointStyle)/*-{
	  		var d = this.defaultStyle;
	  		var style = {};
	  		if (this.values != null)
	  			var style = this.values[value];
	  		if (style == null) style = {};
	  		if (pointStyle == null) pointStyle = {};
	  		var result = {};
	  		for (var member in d){
	  			result[member] = d[member];
	  		}
	  		for (var member in style){
	  			result[member] = style[member];
	  		}
	  		for (var member in pointStyle){
	  			result[member] = pointStyle[member];
	  		}
	  		return result;	
  	   }-*/;
   }
   
   public static final class TimespanStyle extends JavaScriptObject{
	   
	   protected TimespanStyle(){}
	   
	   public native double getTop()/*-{
	   		return this.top;
	   }-*/;
	   public native double getBottom()/*-{
	  		return this.bottom;
	  }-*/;
	   
	   public native double getBorderWidth()/*-{
 			return this.borderWidth;
		}-*/;
	   
	   private native String get(String element)/*-{
	  		return this[element];
	  }-*/;
	   
	   public CssColor getFillColor(){
		   String fillColor = get("fillColor");
		   if (fillColor == null)
			   return null;
		   return CssColor.make(fillColor);
	   }
	   
	   public CssColor getBorderColor(){
		   String borderColor = get("borderColor");
		   if (borderColor == null)
			   return null;
		   return CssColor.make(borderColor);
	   }
   }

   public static final class HighlightDescription extends JavaScriptObject {
      // JavaScript overlay types always have protected, no-arg constructors
      protected HighlightDescription() {
      }

      /**
       * Returns the value of the <code>lineWidth</code> field, if defined, otherwise returns <code>null</code>.
       */
      public Double getLineWidth() {
         if (isLineWidthDefined()) {
            return getDoubleValue("lineWidth");
         }
         return null;
      }

      private native double getDoubleValue(final String fieldName) /*-{
         return this[fieldName];
      }-*/;

      private native boolean isLineWidthDefined() /*-{
         return (typeof this["lineWidth"] !== 'undefined');
      }-*/;

      public native JsArray<StyleType> getStyleTypes() /*-{
         return this['styles'];
      }-*/;
   }
}
