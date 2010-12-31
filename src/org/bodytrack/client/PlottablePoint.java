package org.bodytrack.client;

/**
 * A class holding a date and value, which makes the value easy to graph.
 * 
 * <p>The date is stored as a <tt>double</tt>, and the value is stored as a
 * <tt>double</tt>.  Once created, objects of this class are designed to
 * be immutable, although in JavaScript all objects are mutable, and are
 * subject to change if an attacker can exploit a cross-site scripting
 * vulnerability.</p>
 */
public final class PlottablePoint {
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
	 * Returns the value of this PlottablePoint.
	 * 
	 * @return
	 * 		the value of this PlottablePoint
	 */
	public double getValue() {
		return myValue;
	}
}
