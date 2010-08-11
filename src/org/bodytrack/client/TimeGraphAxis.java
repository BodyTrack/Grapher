package org.bodytrack.client;


import java.util.Calendar;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.TextAlign;
import gwt.g2d.client.graphics.TextBaseline;

public class TimeGraphAxis extends GraphAxis {
	TimeGraphAxis(double min, double max, Basis basis, double width) {
		super(min, max, basis, width);
	}
	
	final long secondsInHour = 3600;
	final long secondsInDay = secondsInHour * 24;
	final long secondsInWeek = secondsInDay * 7;
	final long secondsInYear = (long)Math.round(secondsInDay * 365.24);
	final long secondsInMonth = (long)Math.round(secondsInYear / 12.);
	
	final double timeTickSizes[][] = {
			{1, 5, 15, 30},                     // 1, 5, 15, 30 seconds
			{60*1, 60*5, 60*15, 60*30},         // 1, 5, 15, 30 mins
			{3600*1, 3600*3, 3600*6, 3600*12},   // 1, 3, 6, 12 hours
			{secondsInDay},
			{secondsInWeek},
			{secondsInMonth},
			{secondsInYear}
	};
	
	public double computeTimeTickSize(double minPixels) {
		return computeTimeTickSize(minPixels, 0.);
	}
	
	public double computeTimeTickSize(double minPixels, double minTickSize) {
		double minDelta = Math.max(minTickSize, (this.max-this.min) * (minPixels/this.length));
		if (minDelta < 1) return computeTickSize(minPixels);
		for (int unit = 0; unit < timeTickSizes.length; unit++) {
			for (int i = 0; i < timeTickSizes[unit].length; i++) {
				if (timeTickSizes[unit][i] >= minDelta) return timeTickSizes[unit][i];
			}
		}
		return 1;
	}
	
	// Like computeTimeTickSize, but constrain the minimum size of the tick like so:
	// Select minorTickSize to be of the same units as majorTickSize, unless
	//   majorTickSize is the minimum value of the unit (e.g. 1 second, 1 minute, 1 hour), in which case
	//   select minorTickSize from the next smaller unit
	public double computeTimeMinorTickSize(double minPixels, double majorTickSize) {
		// Find unit matching majorTickSize
		double epsilon = 1e-10;
		if (majorTickSize <= 1 + epsilon) return computeTickSize(minPixels);
		for (int unit = 0; unit < timeTickSizes.length; unit++) {
			for (int i = 0; i < timeTickSizes[unit].length; i++) {
				if (majorTickSize <= timeTickSizes[unit][i]) {
					double minTickSize;
					if (i == 0) {
						// Major tick is minimum value of unit;  OK to use next lower unit
						minTickSize = timeTickSizes[unit-1][0];
					} else {
						// Don't go smaller than major tick's unit
						minTickSize = timeTickSizes[unit][0];
					}
					return computeTimeTickSize(minPixels, minTickSize);
				}
			}
		}
		return computeTimeTickSize(minPixels);
	}

	
	class TimeLabelFormatter extends LabelFormatter {
		String format(double time) {
			// Compute time, rounded to nearest microsecond, then truncated to second only
			double whole = Math.floor(time+(.5/1000000.));
			// Compute fractional second in microseconds, rounded to nearest
			int microseconds = (int)Math.round(1000000 * (time - whole));
			
			Date d = new Date((long) (whole*1000.));
			
			String ret = NumberFormat.getFormat("00").format(d.getHours()) + ":" +
			             NumberFormat.getFormat("00").format(d.getMinutes());
			
			int seconds = d.getSeconds();
			if (seconds != 0 || microseconds != 0) {
				ret += ":" + NumberFormat.getFormat("00").format(seconds);
				if (microseconds != 0) {
					ret += "." + NumberFormat.getFormat("000000").format(microseconds);
					// Remove trailing zeros
					ret = ret.replaceFirst("0+$", "");
				}
			}
			
			return ret;
		}
	}
	
	class DayLabelFormatter extends LabelFormatter {
		final String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
		String format(double time) {
			Date d = new Date((long) Math.round(time*1000.));
			String ret = days[d.getDay()] + " " + d.getDate();
			return ret;
		}
	}	

	TickGenerator createDateTickGenerator(double tickSize) {
		int nHours = (int) Math.round(tickSize / secondsInHour);
		if (nHours <= 1) return null;
		if (nHours < 24) return new HourTickGenerator(nHours);
		int nDays = (int) Math.round(tickSize / secondsInDay);
		if (nDays == 1) return new DayTickGenerator();
		int nWeeks = (int) Math.round(tickSize / secondsInWeek);
		if (nWeeks == 1) return new WeekTickGenerator();
//		int nMonths = (int) Math.round(tickSize / secondsInMonth);
//		if (nMonths < 12) return new MonthTickGenerator(nMonths);
//		int nYears = (int) Math.round(tickSize / secondsInYear);
		return null;
	}
	

	double closestDay(double time) {
		Date timeDate = new Date((long) (time*1000));
		double hour = timeDate.getHours() + timeDate.getMinutes() / 30. + timeDate.getSeconds() / 1800.;
		if (hour >= 12) {
			// Advance day by moving to one min before midnight
			timeDate.setHours(23);
			timeDate.setMinutes(59);
			timeDate.setSeconds(59);
			// Advance 12 hours
			timeDate.setTime(timeDate.getTime() + secondsInHour * 12 * 1000);
		}
		// Truncate to beginning of day
		timeDate.setHours(0);
		timeDate.setMinutes(0);
		timeDate.setSeconds(0);
		double epsilon = 1e-10;
		// Return time in seconds, truncating fractional second
		double ret=Math.floor(timeDate.getTime() / 1000 + epsilon);
		return ret;			
	}

	class WeekTickGenerator extends TickGenerator {
		WeekTickGenerator() {
			super(secondsInWeek, 0);
		}

		double closestTick(double time) {
			Date timeDate = new Date((long) (time*1000));
			
			double day = ((60*(60*timeDate.getSeconds()) + timeDate.getMinutes()) + timeDate.getHours()) / 24.;
			int daysSinceMonday = timeDate.getDay()-1;
			if (daysSinceMonday < 0) daysSinceMonday += 7;
			day += daysSinceMonday;
			
			if (day >= 3.5) {
				return closestDay(time + secondsInDay * (7 - day));
			} else {
				return closestDay(time - secondsInDay * day);
			}
		}
	}
	
	class DayTickGenerator extends TickGenerator {
		DayTickGenerator() {
			super(secondsInDay, 0);
		}

		double closestTick(double time) {
			return closestDay(time);
		}		
	}
	
	class HourTickGenerator extends TickGenerator {
		private int tickSizeHours;
		HourTickGenerator(int tickSizeHours) {
			super(tickSizeHours * secondsInHour, 0);
			this.tickSizeHours = tickSizeHours;
		}
		double closestTick(double time) {
			Date timeDate = new Date((long) (time*1000));
			double hour = timeDate.getHours() + timeDate.getMinutes() / 30. + timeDate.getSeconds() / 1800.;
			int closestHour = (int)Math.round(hour / tickSizeHours) * tickSizeHours;
			if (closestHour == 24) {
				// Midnight of next day
				// Advance day by moving to one min before midnight
				timeDate.setHours(23);
				timeDate.setMinutes(59);
				timeDate.setSeconds(59);
				// Advance 12 hours
				timeDate.setTime(timeDate.getTime() + 86400 * 500);
				// Move back to zero hour, beginning of day
				timeDate.setHours(0);
			} else {
				timeDate.setHours(closestHour);
			}
			// Remove minutes and seconds
			timeDate.setMinutes(0);
			timeDate.setSeconds(0);
			double epsilon = 1e-10;
			// Return time in seconds, truncating fractional second
			double ret=Math.floor(timeDate.getTime() / 1000 + epsilon);
			return ret;
		}
	}
	
	public void paint(Surface surface) {
		DirectShapeRenderer renderer = new DirectShapeRenderer(surface);
	    renderer.beginPath();
		renderer.drawLineSegment(project2D(this.min), project2D(this.max));
		double epsilon = 1e-10;
		
		// Year out of line
		// Year inline
		// Month out of line ; Year inline 
		// Month inline ; Year inline 
		// DOW DOM out of line ; Month inline ; Year inline
		// DOW DOM inline ; Month inline ; Year inline
		// HH:MM[:SS[.NNN]] on tick ; DOW DOM inline ;  Month inline ; Year inline 

		double pixelOffset = 0;
		
		double timeMajorPixels = 50;
		double timeMajorNoLabelPixels = 10;
		double timeMinorPixels = 5;
		double timeMajorTickSize = computeTimeTickSize(timeMajorPixels);
		double timeMajorNoLabelTickSize = computeTimeTickSize(timeMajorNoLabelPixels);
		if (timeMajorNoLabelTickSize <= 3600*12 + epsilon) {
			double timeLabelHeight;
			if (timeMajorTickSize <= 3600*12 + epsilon) {
				renderTicks(pixelOffset, timeMajorTickSize, createDateTickGenerator(timeMajorTickSize), surface, renderer, majorTickWidthPixels, new TimeLabelFormatter());
				timeLabelHeight = 10;
			} else {
				timeMajorTickSize = 3600*12;
				renderTicks(pixelOffset, timeMajorTickSize, createDateTickGenerator(timeMajorTickSize), surface, renderer, majorTickWidthPixels, null);
				timeLabelHeight = 0;
			}
			double timeMinorTickSize = computeTimeMinorTickSize(timeMinorPixels, timeMajorTickSize);
			//double timeMinorTickSize = computeTimeTickSize(timeMinorPixels);
			renderTicks(pixelOffset, timeMinorTickSize, createDateTickGenerator(timeMinorTickSize), surface, renderer, minorTickWidthPixels, null);
			pixelOffset += 12 + timeLabelHeight;
		}		
		
		double dayPixels = 50;
		double inlineDayTickWidthPixels = 15;
		double dayMajorTickSize = Math.max(secondsInDay, computeTimeTickSize(dayPixels));
		if (dayMajorTickSize == secondsInDay) {
			renderTicksInlineLabels(pixelOffset, dayMajorTickSize, createDateTickGenerator(dayMajorTickSize), surface, renderer, inlineDayTickWidthPixels, new DayLabelFormatter());
			double dayMinorTickSize = Math.max(secondsInDay, computeTimeTickSize(dayPixels/7.));
			renderTicks(pixelOffset, dayMinorTickSize, createDateTickGenerator(dayMinorTickSize), surface, renderer, minorTickWidthPixels, null);
			pixelOffset += inlineDayTickWidthPixels;
		}
		
		renderer.stroke();
	}


}


