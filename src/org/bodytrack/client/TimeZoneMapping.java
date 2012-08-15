package org.bodytrack.client;

import com.google.gwt.core.client.JavaScriptObject;

public class TimeZoneMapping extends JavaScriptObject {
	
	protected TimeZoneMapping(){}
	
	private final native TimeZoneSegment[] getTZ()/*-{
		return this.timeZones;
	}-*/;
	
	public final TimeZoneSegmentWrapper[] getTimeZones(){
		TimeZoneSegment[] segments = getTZ();
		TimeZoneSegmentWrapper[] wrappers = new TimeZoneSegmentWrapper[segments.length];
		for (int i = 0; i < segments.length; i++)
			wrappers[i] = new TimeZoneSegmentWrapper(segments[i]);
		return wrappers;		
	}

}
