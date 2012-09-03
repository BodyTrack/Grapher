package org.bodytrack.client;

import com.google.gwt.i18n.client.TimeZone;

public class TimeZoneSegmentWrapper {
	private String name;
	private boolean usesDST;
	private double offset;
	private double start;
	private double end;
	private TimeZone timeZone;

	public TimeZoneSegmentWrapper(TimeZoneSegment segment) {
		name = segment.getTimeZoneName();
		usesDST = segment.usesDST();
		offset = segment.getOffset();
		start = segment.getStart();
		end = segment.getEnd();
		timeZone = segment.getTimeZone();
	}

	public String getTimeZoneName() {
		return name;
	}

	public double getOffset() {
		return offset;
	}

	public boolean usesDST() {
		return usesDST;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}
}
