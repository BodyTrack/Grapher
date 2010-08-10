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
	
	private final int YEAR=1;
	private final int MONTH=2;
	private final int DAY=3;
	private final int SECOND=4;
	
	final long secondsInHour = 3600;
	final long secondsInDay = secondsInHour * 24;
	final long secondsInWeek = secondsInDay * 7;
	final long secondsInYear = (long)Math.round(secondsInDay * 365.24);
	final long secondsInMonth = (long)Math.round(secondsInYear / 12.);
	
	
	public double computeTimeTickSize(double minPixels) {
		double minDelta = (this.max-this.min) * (minPixels/this.length);
		final double tickSizes[] = {
				1, 5, 15, 30,                     // 1, 5, 15, 30 seconds
				60*1, 60*5, 60*15, 60*30,         // 1, 5, 15, 30 mins
				3600*1, 3600*3, 3600*6, 3600*12,   // 1, 3, 6, 12 hours
				secondsInDay,
				secondsInWeek,
				secondsInMonth,
				secondsInYear
		};
		if (minDelta < 1) return computeTickSize(minPixels);
		for (int i = 0; i < tickSizes.length; i++) {
			if (tickSizes[i] >= minDelta) return tickSizes[i];
		}
		return 1;
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
		
		double inlineYearPixels = 30;
		double inlineMonthPixels = 30;
		double dayPixels = 50;
		double inlineDayTickWidthPixels = 40;
		double timePixels = 50;
		double secondsInDay = 86400;
		double secondsInYear = secondsInDay * 365;
		double secondsInMonth = secondsInYear / 12;

		// Year out of line
		// Year inline
		// Month out of line ; Year inline 
		// Month inline ; Year inline 
		// DOW DOM out of line ; Month inline ; Year inline
		// DOW DOM inline ; Month inline ; Year inline
		// HH:MM[:SS[.NNN]] on tick ; DOW DOM inline ;  Month inline ; Year inline 

		
		double timeMajorTickSize = computeTimeTickSize(timePixels);
		if (timeMajorTickSize <= 3600*12 + epsilon) {
			renderTicks(timeMajorTickSize, createDateTickGenerator(timeMajorTickSize), surface, renderer, majorTickWidthPixels, new TimeLabelFormatter());

			double timeMinorTickSize = computeTimeTickSize(timePixels / 5);
			renderTicks(timeMinorTickSize, createDateTickGenerator(timeMinorTickSize), surface, renderer, minorTickWidthPixels, null);
		}
		
		double dayMajorTickSize = Math.max(secondsInDay, computeTimeTickSize(dayPixels));
		if (dayMajorTickSize <= secondsInWeek) {
			renderTicks(dayMajorTickSize, createDateTickGenerator(dayMajorTickSize), surface, renderer, inlineDayTickWidthPixels, new DayLabelFormatter());
			double dayMinorTickSize = Math.max(secondsInDay, computeTimeTickSize(dayPixels/7.));
			renderTicks(dayMinorTickSize, createDateTickGenerator(dayMinorTickSize), surface, renderer, minorTickWidthPixels, null);
		}
		
		if (false) {
		// Months
		double monthTickSize = computeTickSize(inlineMonthPixels, secondsInMonth);
		if (monthTickSize <= 1 - epsilon) {
			
		}
		
		
		// Years
		double yearTickSize = computeTickSize(inlineYearPixels, secondsInYear);
		GWT.log("yearTickSize=" + yearTickSize);
		
		
		if (yearTickSize >= 1 - epsilon) {
			boolean in_line = (yearTickSize < 1 + epsilon);
			// Draw years out of line
			// TODO: draw ticks with (deltaYear, deltaMonth, deltaDay, deltaHour)
			// deltaMin, deltaSec?
			double majorTickSize = yearTickSize;
			
			double m = in_line ? majorTickWidthPixels * 2 : majorTickWidthPixels;
			renderDateTicks(renderer, majorTickSize, YEAR, m);
			renderDateTicks(renderer, majorTickSize, YEAR, m, 1, 0);
			double l = in_line ? minorTickWidthPixels : majorTickWidthPixels;
			renderDateTickLabels(surface, majorTickSize, YEAR, l, 0, 6);
			
			double minorTickSize = computeTickSize(inlineYearPixels/5, secondsInYear);
			minorTickSize = Math.max(1, minorTickSize);
			renderDateTicks(renderer, minorTickSize, YEAR, minorTickWidthPixels);
			renderDateTicks(renderer, minorTickSize, YEAR, minorTickWidthPixels, 1, 0);
			
			
		} else {
			GWT.log("years not out of line");
		}
		}
		
	    if (false) {
	    	double majorTickSize = computeTickSize(majorTickMinSpacingPixels);
	    	//renderTicks(renderer, majorTickSize, majorTickWidthPixels);
	    	//renderTickLabels(surface, majorTickSize, majorTickWidthPixels+3);

	    	double minorTickSize = computeTickSize(minorTickMinSpacingPixels);
	    	//renderTicks(renderer, minorTickSize, minorTickWidthPixels);
	    }
	    
		renderer.stroke();
	}

	private void renderDateTicks(DirectShapeRenderer renderer, double tickValue, int tickUnit, double tickWidthPixels) {
		renderDateTicks(renderer, tickValue, tickUnit, tickWidthPixels, 0, 0);
	}

	private void renderDateTicks(DirectShapeRenderer renderer, double tickValue, int tickUnit, double tickWidthPixels, double unitOffset, double subUnitOffset) {
		// Compute location of first tick
		Date d = new Date((long) (this.min * 1000));
		double epsilon = 1e-10;
		
		switch (tickUnit) {
		case YEAR:
			d.setMonth((int)Math.round(subUnitOffset));  d.setDate(1);
			d.setYear((int)Math.round(Math.ceil((d.getYear()+1900-unitOffset) / tickValue - epsilon) * tickValue + unitOffset - 1900));
			break;
		case SECOND:
			// Round down to nearest second
			d.setTime(d.getTime() / 1000 * 1000);
			break;
		default:
			Window.alert("renderDateTicks: bad unit");
			return;
		}
		
		//GWT.log("ticks " + tickWidthPixels + " " + (d.getYear()+1900) + " " + tickValue);
		
		for (; true; advanceDate(d, tickUnit, tickValue)) {
			double y = d.getTime()/1000.;
			if (y < this.min) continue; 
			if (y > this.max) break;
			//GWT.log("renderDateTick " + d);
			renderer.drawLineSegment(project2D(y),project2D(y).add(this.basis.x.scale(tickWidthPixels)));
		}
		
	}

	private void renderDateTickLabels(Surface surface, double tickValue, int tickUnit, double tickWidthPixels) {
		renderDateTickLabels(surface, tickValue, tickUnit, tickWidthPixels);
	}
	
	private void renderDateTickLabels(Surface surface, double tickValue, int tickUnit, double tickWidthPixels, double unitOffset, double subUnitOffset) {
    	surface.setTextAlign(TextAlign.CENTER);
    	surface.setTextBaseline(TextBaseline.TOP);

    	// Compute location of first tick
		Date d = new Date((long) (this.min * 1000));
		
		if (tickUnit == YEAR) {
			d.setMonth((int)subUnitOffset);  d.setDate(1);
			d.setYear((int)Math.round((Math.floor((d.getYear()-unitOffset) / tickValue) * tickValue + unitOffset)));
		}
		
		for (; true; advanceDate(d, tickUnit, tickValue)) {
			double y = d.getTime()/1000.;
			//GWT.log("renderDateTickLabels " + d);
			if (y < this.min) continue; 
			if (y > this.max) break;
			String label="";
			if (tickUnit == YEAR) label = String.valueOf(d.getYear()+1900);
			renderTickLabel(surface, y, tickWidthPixels, label);
		}
		
	}

	
	private void advanceDate(Date d, int unit, double value) {
		if (unit == YEAR) {
			d.setYear(d.getYear()+(int)Math.round(value));
		}
		
	}

}


