package org.bodytrack.client;

/**
 * A class holding a date and value, which makes the value easy to graph.
 * 
 * The date is stored as a <tt>double</tt>, and the value
 * is stored as a <tt>double</tt>.  Objects of this class are mutable,
 * and therefore not thread-safe.  However, since GWT compiles Java code
 * to JavaScript, which does not support threading, thread-safety is not
 * an issue.
 */
public class PlottablePoint {
	private double myDate;
	private double myValue;
	
	/**
	 * Saves copies of date and value in this PlottablePoint.
	 * 
	 * @param date
	 * 		the date for this PlottablePoint, represented as the number
	 * 		of seconds since 1/1/1970 (the epoch)
	 * @param value
	 * 		the value of this PlottablePoint
	 */
	public PlottablePoint(double date, double value) {
		myDate = date;
		myValue = value;
	}
	
	/**
	 * Returns the date for this PlottablePoint.
	 * 
	 * @return
	 * 		the date for this PlottablePoint
	 */
	public double getDate() {
		return myDate;
	}

	/**
	 * Sets the date of this PlottablePoint.
	 * 
	 * @param date
	 * 		the date to set for this PlottablePoint
	 */
	public void setDate(double date) {
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
