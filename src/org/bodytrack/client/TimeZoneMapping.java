package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

public class TimeZoneMapping extends JavaScriptObject {
	
	protected TimeZoneMapping(){}
	
	public final native TimeZoneSegment[] getTimeZones()/*-{
		return this.timeZones;
	}-*/;

}
