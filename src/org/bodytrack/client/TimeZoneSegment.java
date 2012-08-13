package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

public class TimeZoneSegment extends JavaScriptObject {
	
	protected TimeZoneSegment(){}
	
	public final native String getTimeZoneName()/*-{
		return this.name;
	}-*/;
	
	public final native double getOffset()/*-{
		return this.offset / 1000.0;
	}-*/;
	
	public final native boolean usesDST()/*-{
		return this.usesDST;
	}-*/;
	
	public final native double getStart()/*-{
		return this.start / 1000.0;
	}-*/;
	
	public final native double getEnd()/*-{
		return this.end / 1000.0;
	}-*/;

}
