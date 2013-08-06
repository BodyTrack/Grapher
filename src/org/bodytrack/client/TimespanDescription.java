package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

public class TimespanDescription extends JavaScriptObject {
	
	protected TimespanDescription(){}
	
	public static native TimespanDescription buildDescription(String json) /*-{
		return JSON.parse(json);
	}-*/;
	
	public final native double getStart() /*-{
		return this.start;
	}-*/;
	
	public final native double getEnd() /*-{
		return this.end;
	}-*/;
	
	
}
