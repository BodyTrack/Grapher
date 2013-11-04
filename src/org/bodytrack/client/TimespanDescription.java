package org.bodytrack.client;

import org.bodytrack.client.StyleDescription.TimespanStyle;

import com.google.gwt.core.client.JavaScriptObject;

public final class TimespanDescription extends JavaScriptObject {
	
	protected TimespanDescription(){}
	
	public static native TimespanDescription buildDescription(String json) /*-{
		return JSON.parse(json);
	}-*/;
	
	public native double getStart() /*-{
		return this.start;
	}-*/;
	
	public native double getEnd() /*-{
		return this.end;
	}-*/;
	
	
	public native String getValue() /*-{
		return this.value;
	}-*/;
	
	public native TimespanStyle getStyle()/*-{
		return this.style;
	}-*/;
	
}