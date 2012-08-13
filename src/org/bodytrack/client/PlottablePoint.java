package org.bodytrack.client;

import java.util.Comparator;
import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * A class holding a date and value, which makes the value easy to graph.
 *
 * <p>The date is stored as a <tt>double</tt>, the value is stored as a
 * <tt>double</tt>, and the comment is stored as a {@link String}.  Once
 * created, objects of this class are designed to
 * be immutable, although in JavaScript all objects are mutable, and are
 * subject to change if an attacker can exploit a cross-site scripting
 * vulnerability.</p>
 */
public final class PlottablePoint implements Comparable<PlottablePoint>, TimestampFormatter {

   public static final String DEFAULT_VALUE_FORMAT_STRING = "###,##0.0##";
   public static final String DATE_TIME_FORMAT_STRING =
	   "EEE MMM dd yyyy, HH:mm:ss.SSS";
   public static final DateTimeFormat DATE_TIME_FORMAT =
	   DateTimeFormat.getFormat(DATE_TIME_FORMAT_STRING);

   private static final String ZERO_VALUE_STRING = "0.0";
   private static final NumberFormat SCIENTIFIC_VALUE_FORMAT =
	   NumberFormat.getScientificFormat();
   private static final NumberFormat DEFAULT_VALUE_FORMAT =
	   NumberFormat.getFormat(DEFAULT_VALUE_FORMAT_STRING);

   private static final Comparator<Double> DATE_COMPARATOR = new DateComparator();

   private double myDate;
   private double myValue;
   private String comment;

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
      this(date, value, null);
   }

   /**
    * Saves copies of date and value in this PlottablePoint.
    *
    * @param date
    * 		the date for this PlottablePoint, represented as the number
    * 		of seconds since 1/1/1970 (the epoch)
    * @param value
    * 		the value of this PlottablePoint
    * @param comment
    * 		the comment for this PlottablePoint
    */
   public PlottablePoint(final double date, final double value, final String comment) {
      myDate = date;
      myValue = value;
      this.comment = comment;
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
    * Returns a formatted {@link String} representation of this point's date.
    *
    * @see #DATE_TIME_FORMAT_STRING
    */
   public String getDateAsString() {
      return getDateAsString(this);
   }
   
   public String getDateAsString(TimestampFormatter formatter){
	   if (formatter == null)
		   formatter = this;
	   return formatter.formatTimestamp(myDate);
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
    * Returns this point's value as a formatted string.
    */
   public String getValueAsString() {
      final double absValue = Math.abs(myValue);

      if (absValue == 0.0) // Rare, but possible
      {
         return ZERO_VALUE_STRING;
      }

      if (absValue < 1e-3 || absValue > 1e7) {
         return SCIENTIFIC_VALUE_FORMAT.format(myValue);
      }

      return DEFAULT_VALUE_FORMAT.format(myValue);
   }

   /**
    * Returns <code>true</code> if this PlottablePoint has a comment; <code>false</code> otherwise.
    */
   public boolean hasComment() {
      return comment != null;
   }

   /**
    * Returns the comment for this PlottablePoint.
    *
    * @return
    * 		the comment for this PlottablePoint
    */
   public String getComment() {
      return comment;
   }

   @Override
   public int compareTo(PlottablePoint other) {
      if (other == null)
         return 1;

      return DATE_COMPARATOR.compare(myDate, other.myDate);
   }

   /**
    * Returns a hashcode based on the date of this <tt>PlottablePoint</tt>.
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      final long temp = (long)Math.floor(myDate);
      result = prime * result + (int)(temp ^ (temp >>> 32));
      return result;
   }

   /**
    * Considers this as equal to obj iff obj is a <tt>PlottablePoint</tt>
    * object with the same date as this, disregarding any fractional
    * part.
    */
   @Override
   public boolean equals(final Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (!(obj instanceof PlottablePoint)) {
         return false;
      }

      // Two dates are equal if their floors are less than 0.5
      // apart, which is an important distinction whenever we
      // are dealing with numbers that are outside the range
      // of values representable in a long
      return
            Math.abs(Math.floor(myDate) -
                     Math.floor(((PlottablePoint)obj).myDate)) < 0.5;
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("PlottablePoint");
      sb.append("{date=").append(myDate);
      sb.append(", value=").append(myValue);
      sb.append(", comment='").append(comment).append('\'');
      sb.append('}');
      return sb.toString();
   }

   public static class DateComparator implements Comparator<Double> {
      @Override
      public int compare(Double o1, Double o2) {
         if (o1 == o2) // Also includes the (null, null) case
            return 0;
         if (o1 == null)
            return -1;
         if (o2 == null)
            return 1;

         Integer date1 = Integer.valueOf((int)Math.floor(o1.doubleValue()));
         Integer date2 = Integer.valueOf((int)(Math.floor(o2.doubleValue())));

         return date1.compareTo(date2);
      }
   }

	@Override
	public String formatTimestamp(double timestamp) {
		return DATE_TIME_FORMAT.format(new Date((long)(myDate * 1000)));
	}
}
