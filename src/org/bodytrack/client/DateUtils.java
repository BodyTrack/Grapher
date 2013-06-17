package org.bodytrack.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class DateUtils {
	
	public static final String DATE_TIME_FORMAT_STRING =
			   "EEE MMM dd yyyy, HH:mm:ss.SSS";
	
	private static final DateTimeFormat DATE_TIME_FORMAT =
			   DateTimeFormat.getFormat(DATE_TIME_FORMAT_STRING);
	
	public static String getDateAsString(double date){
		return DATE_TIME_FORMAT.format(new Date((long)(date * 1000)));
	}

}
