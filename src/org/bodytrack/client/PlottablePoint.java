package org.bodytrack.client;

import java.util.Date;

/**
 * A class holding a date and value, which makes the value easy to graph.
 * 
 * The date is stored as a {@linkplain java.util.Date}, and the value
 * is stored as a <tt>double</tt>.  Objects of this class are mutable,
 * and therefore not thread-safe.  However, since GWT compiles Java code
 * to JavaScript, which does not support threading, thread-safety is not
 * an issue.
 */
public class PlottablePoint {
	private Date myDate;
	private double myValue;
	
	/**
	 * Saves a reference to date and a copy of value in this PlottablePoint.
	 * 
	 * @param date
	 * @param value
	 */
	public PlottablePoint(Date date, double value) {
		myDate = date;
		myValue = value;
	}
	
	/**
	 * Returns the date for this PlottablePoint.
	 * 
	 * @return
	 * 		the date for this PlottablePoint
	 */
	public Date getDate() {
		return myDate;
	}

	/**
	 * Sets the date of this PlottablePoint.
	 * 
	 * @param date
	 * 		the date to set for this PlottablePoint
	 */
	public void setDate(Date date) {
		myDate = date;
	}

	/**
	 * Returns the value of this PlottablePoint.
	 * 
	 * @return
	 * 		the value of this PlottablePoint
	 */
	public double getValue() {
		return myValue;
	}

	/**
	 * Sets the value of this PlottablePoint.
	 * 
	 * @param value
	 * 		the value to set for this PlottablePoint
	 */
	public void setMyValue(double value) {
		myValue = value;
	}
}
