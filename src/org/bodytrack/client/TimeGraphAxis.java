package org.bodytrack.client;

import java.util.Date;

import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.i18n.client.NumberFormat;

@SuppressWarnings("deprecation")
public class TimeGraphAxis extends GraphAxis {

	private final TimeZoneMap timeZoneMap;

	public TimeGraphAxis(String divName, double min, double max, Basis basis,
			double width, boolean isXAxis) {
		super(divName, min, max, basis, width, isXAxis);
		minRange = -21474836400.0;
		maxRange = 21474836400.0;
		hasMinRange = hasMaxRange = true;
		timeZoneMap = new BrowserTimeZoneMap();
	}

	@Override
	public TimeZoneMap getTimeZoneMap() {
		return timeZoneMap;
	}

	private final long secondsInHour = 3600;
	private final long secondsInDay = secondsInHour * 24;
	private final long secondsInWeek = secondsInDay * 7;
	private final long secondsInYear = 31556926;
	private final long secondsInDecade = secondsInYear * 10;
	private final long secondsInCentury = secondsInDecade * 10;
	private final long secondsInMonth = (long)Math.round(secondsInYear / 12.);

	private final double timeTickSizes[][] = {
			{1, 5, 15, 30},                                            // 1, 5, 15, 30 seconds
			{60*1, 60*5, 60*15, 60*30},                                // 1, 5, 15, 30 mins
			{3600*1, 3600*3, 3600*6, 3600*12},                         // 1, 3, 6, 12 hours
			{secondsInDay},                                            // 1 day
			{secondsInWeek},                                           // 1 weeks
			{secondsInMonth, secondsInMonth * 3, secondsInMonth * 6},  // 1, 3, 6 months
			{secondsInYear}                                            // 1 year
	};

	private int previousPaintEventId = 0;

	private double computeTimeTickSize(double minPixels) {
		//double minDelta = Math.max(minTickSize,
		// (this.max - this.min) * (minPixels / this.length));
		double minDelta = (this.max - this.min) * (minPixels / this.length);

		if (minDelta < 1)
			return computeTickSize(minPixels);

		for (int unit = 0; unit < timeTickSizes.length; unit++)
			for (int i = 0; i < timeTickSizes[unit].length; i++)
				if (timeTickSizes[unit][i] >= minDelta)
					return timeTickSizes[unit][i];

		return computeTickSize(minPixels, secondsInYear) * secondsInYear;
	}

	// Like computeTimeTickSize, but constrain the minimum size of the tick like so:
	// Select minorTickSize to be of the same units as majorTickSize, unless
	//   majorTickSize is the minimum value of the unit (e.g. 1 second, 1 minute,
	//   1 hour), in which case select minorTickSize from the next smaller unit
	private double computeTimeMinorTickSize(double minPixels, double majorTickSize) {
		// Find unit matching majorTickSize
		double epsilon = 1e-10;
		if (majorTickSize <= 1 + epsilon) return computeTickSize(minPixels);
		for (int unit = 0; unit < timeTickSizes.length; unit++) {
			for (int i = 0; i < timeTickSizes[unit].length; i++) {
				if (majorTickSize <= timeTickSizes[unit][i]) {
					double minTickSize;
					if (i == 0) {
						// Major tick is minimum value of unit;  OK to use
						// next lower unit
						minTickSize = timeTickSizes[unit-1][0];
					} else {
						// Don't go smaller than major tick's unit
						minTickSize = timeTickSizes[unit][0];
					}
					return Math.max(minTickSize, computeTimeTickSize(minPixels));
				}
			}
		}
		return computeTimeTickSize(minPixels);
	}

	private class TimeLabelFormatter extends LabelFormatter {
		String format(double time) {
			// Compute time, rounded to nearest microsecond, then truncated
			// to second only
			double whole = Math.floor(time + (.5/1000000.));
			// Compute fractional second in microseconds, rounded to nearest
			int microseconds = (int) Math.round(1000000 * (time - whole));

			Date d = new Date((long) (whole*1000.));
			
			String ret = NumberFormat.getFormat("00").format(d.getHours())
				+ ":" +	NumberFormat.getFormat("00").format(d.getMinutes());
			
			int seconds = d.getSeconds();
			if (seconds != 0 || microseconds != 0) {
				ret += ":" + NumberFormat.getFormat("00").format(seconds);
				if (microseconds != 0) {
					ret += "."
						+ NumberFormat.getFormat("000000").format(microseconds);
					// Remove trailing zeros
					ret = ret.replaceFirst("0+$", "");
				}
			}

			return ret;
		}
	}
	
	final static String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
	final static String[] verboseDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	
	final static String[] months = {"Jan", "Feb", "Mar", "Apr", "May",
			"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	final static String[] verboseMonths = {"January", "February", "March", "April", "May",
		"June", "July", "August", "September", "October", "November", "December"};
	
	private class VerboseDateLabelFormatter extends LabelFormatter{
		private LabelFormatter dayFormatter = new VerboseDayLabelFormatter();
		private LabelFormatter monthFormatter = new VerboseMonthLabelFormatter(true);
		
		String format(double time){
			return dayFormatter.format(time) + " " + monthFormatter.format(time);
		}
		
	}
	
	private class DateLabelFormatter extends LabelFormatter{
		private LabelFormatter dayFormatter = new DayLabelFormatter();
		private LabelFormatter monthFormatter = new MonthLabelFormatter(true);
		
		String format(double time){
			return dayFormatter.format(time) + " " + monthFormatter.format(time);
		}
		
	}
	
	private class DateNumberLabelFormatter extends LabelFormatter{
		String format(double time) {
			Date d = new Date((long) Math.round(time*1000.));
			String ret = String.valueOf(d.getDate());
			return ret;
		}		
	}
	
	
	private class DayLabelFormatter extends LabelFormatter {
		private LabelFormatter dateNumberFormatter = new DateNumberLabelFormatter();
		
		String format(double time) {
			Date d = new Date((long) Math.round(time*1000.));
			String ret = days[d.getDay()] + " " + dateNumberFormatter.format(time);
			return ret;
		}
	}
	
	private class VerboseDayLabelFormatter extends LabelFormatter {
		String format(double time) {
			Date d = new Date((long) Math.round(time*1000.));
			String ret = verboseDays[d.getDay()] + " " + d.getDate();
			return ret;
		}
	}

	private class MonthLabelFormatter extends LabelFormatter {
		
		private boolean includeYear;
		
		public MonthLabelFormatter(boolean includeYear){
			this.includeYear = includeYear;
		}
		
		private LabelFormatter YearLabelFormatter = new YearLabelFormatter();
		
		String format(double time) {
			Date d = new Date((long) Math.round(time * 1000.0));
			return months[d.getMonth()] + (includeYear ? " " + YearLabelFormatter.format(time) : "");
		}
	}	
	
	private class VerboseMonthLabelFormatter extends LabelFormatter {
	
		private boolean includeYear;
	
		public VerboseMonthLabelFormatter(boolean includeYear){
			this.includeYear = includeYear;
		}
	
		private LabelFormatter YearLabelFormatter = new YearLabelFormatter();
		
		String format(double time) {
			Date d = new Date((long) Math.round(time * 1000.0));
			return verboseMonths[d.getMonth()] + (includeYear ? " " + YearLabelFormatter.format(time) : "");
		}
	}	

	private class YearLabelFormatter extends LabelFormatter {
		String format(double time) {
			Date d = new Date((long) Math.round(time * 1000.0));
			return String.valueOf(d.getYear() + 1900);
		}
	}	
	
	private class YearSmallLabelFormatter extends LabelFormatter {
		String format(double time) {
			Date d = new Date((long) Math.round(time * 1000.0));
			int year = d.getYear() % 100;
			if (year < 0){
				year += 100;				
			}
			return (year < 10 ? "'0" : "'") + year;
		}
	}	
	
	private class DecadeLabelFormatter extends LabelFormatter {
		String format(double time) {
			Date d = new Date((long) Math.round(time * 1000.0));
			int decadeStart = (d.getYear() + 1900) / 10 * 10;
			int decadeEnd = decadeStart + 9;
			return decadeStart + " - " + decadeEnd;
		}
	}
	
	private class DecadeSmallLabelFormatter extends LabelFormatter {
		String format(double time) {
			Date d = new Date((long) Math.round(time * 1000.0));
			int decadeStart = (d.getYear() % 100);
			if (decadeStart < 0)
				decadeStart += 100;
			decadeStart = decadeStart / 10 * 10;
			return (decadeStart == 0 ? "'0" : "'") + decadeStart + "s";
		}
	}
	
	private class CenturyLabelFormatter extends LabelFormatter {
		String format(double time) {
			Date d = new Date((long) Math.round(time * 1000.0));
			int centuryStart = (d.getYear() + 1900) / 100 * 100;
			int centuryEnd = centuryStart + 99;
			return centuryStart + " - " + centuryEnd;
		}
	}	
	
	private class CenturySmallLabelFormatter extends LabelFormatter {
		String format(double time) {
			Date d = new Date((long) Math.round(time * 1000.0));
			int centuryStart = (d.getYear() + 1900) / 100 * 100;
			return centuryStart + "s";
		}
	}	

	private TickGenerator createDateTickGenerator(double tickSize) {
		int nHours = (int) Math.round(tickSize / secondsInHour);
		if (nHours <= 1) return null;
		if (nHours < 24) return new HourTickGenerator(nHours);
		int nDays = (int) Math.round(tickSize / secondsInDay);
		if (nDays == 1) return new DayTickGenerator();
		int nWeeks = (int) Math.round(tickSize / secondsInWeek);
		if (nWeeks == 1) return new WeekTickGenerator();
		int nMonths = (int) Math.round(tickSize / secondsInMonth);
		if (nMonths < 12) return new MonthTickGenerator(nMonths);
		int nYears = (int) Math.round(tickSize / secondsInYear);

		return new YearTickGenerator(nYears);
	}

	private double closestDay(double time) {
		Date timeDate = new Date(((long)time) * 1000);
		double hour = timeDate.getHours() + timeDate.getMinutes() / 30.0
			+ timeDate.getSeconds() / 1800.;
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

	private class YearTickGenerator extends MonthTickGenerator {
		YearTickGenerator(int tickSizeYears) {
			super(12 * tickSizeYears);
		}
	}

	private class MonthTickGenerator extends TickGenerator {
		private int tickSizeMonths;

		MonthTickGenerator(int tickSizeMonths) {
			super(secondsInMonth * tickSizeMonths, 0);
			this.tickSizeMonths = tickSizeMonths;
		}

		private int divideFloor(int numerator, int divisor) {
			if (numerator < 0) {
				return -(((Math.abs(divisor) - numerator - 1)) / divisor);
			} else {
				return numerator / divisor;
			}
		}

		double closestTick(double time) {
			Date timeDate = new Date((long) (time*1000));

			double monthsSince1970 = timeDate.getYear() * 12
			+ timeDate.getMonth()
			+ (timeDate.getDate() * secondsInDay / secondsInMonth);

			int tickMonthsSince1970 =
				(int) Math.round(monthsSince1970 / tickSizeMonths)
				* tickSizeMonths;

			int tickYear = divideFloor(tickMonthsSince1970, 12);
			int tickMonth = tickMonthsSince1970 - tickYear * 12; 
			Date tickDate = new Date(tickYear, tickMonth, 1);

			return Math.round(tickDate.getTime() / 1000);
		}
	}

	private class WeekTickGenerator extends TickGenerator {
		WeekTickGenerator() {
			super(secondsInWeek, 0);
		}

		double closestTick(double time) {
			Date timeDate = new Date((long) (time*1000));

			double day = ((60 * (60 * timeDate.getSeconds()) + timeDate.getMinutes())
					+ timeDate.getHours()) / 24.;
			int daysSinceMonday = timeDate.getDay() - 1;

			if (daysSinceMonday < 0)
				daysSinceMonday += 7;

			day += daysSinceMonday;

			if (day >= 3.5) {
				return closestDay(time + secondsInDay * (7 - day));
			} else {
				return closestDay(time - secondsInDay * day);
			}
		}
	}

	private class DayTickGenerator extends TickGenerator {
		DayTickGenerator() {
			super(secondsInDay, 0);
		}

		double closestTick(double time) {
			return closestDay(time);
		}		
	}

	private class HourTickGenerator extends TickGenerator {
		private int tickSizeHours;
		HourTickGenerator(int tickSizeHours) {
			super(tickSizeHours * secondsInHour, 0);
			this.tickSizeHours = tickSizeHours;
		}
		double closestTick(double time) {
			Date timeDate = new Date((long) (time * 1000));
			double hour = timeDate.getHours() + timeDate.getMinutes() / 30.0
			+ timeDate.getSeconds() / 1800.0;

			int closestHour =
				(int) Math.round(hour / tickSizeHours) * tickSizeHours;
			if (closestHour == 24) {
				// Midnight of next day.  Advance time and return closest
				// beginning of day
				return closestDay(time + (24-hour) * secondsInHour);
			} else {
				timeDate.setHours(closestHour);
			}

			// Remove minutes and seconds
			timeDate.setMinutes(0);
			timeDate.setSeconds(0);
			double epsilon = 1e-10;
			// Return time in seconds, truncating fractional second
			return Math.floor(timeDate.getTime() / 1000 + epsilon);
		}
	}
	
	public String getCursorPositionString(){
		if (getCursorPosition() == null)
			return null;
		return DateUtils.getDateAsString(getCursorPosition());
	}

	@Override
	public void paint(final int newPaintEventId) {
		// guard against redundant paints
		if (previousPaintEventId != newPaintEventId) {
			previousPaintEventId = newPaintEventId;

			paint();
		}
	}

	private void paint() {
		GrapherCanvas canvas = getDrawingCanvas();
		if (canvas == null)
			return;

		canvas.clear();

		// Pick the color to use, based on highlighting status
		if (isHighlighted())
			canvas.setStrokeStyle(HIGHLIGHTED_COLOR);
		else
			canvas.setStrokeStyle(NORMAL_COLOR);

		
		int height = canvas.getHeight();
		
		canvas.beginPath();
		canvas.drawLineSegment(project2D(this.min).add(this.basis.x.scale(height)), project2D(this.max).add(this.basis.x.scale(height)));
		canvas.drawLineSegment(project2D(this.min).add(this.basis.x.scale(height/2)), project2D(this.max).add(this.basis.x.scale(height/2)));
		double epsilon = 1e-10;

		// Year out of line
		// Year inline
		// Month out of line ; Year inline 
		// Month inline ; Year inline 
		// DOW DOM out of line ; Month inline ; Year inline
		// DOW DOM inline ; Month inline ; Year inline
		// HH:MM[:SS[.NNN]] on tick ; DOW DOM inline ;  Month inline ; Year inline 


		double timeMajorPixels = 50;
		double timeMajorNoLabelPixels = 10;
		double timeMinorPixels = 5;
		double timeMajorTickSize = computeTimeTickSize(timeMajorPixels);
		double timeMajorNoLabelTickSize =
			computeTimeTickSize(timeMajorNoLabelPixels);

		

		double topLabelPixelOffset = 2;
		
		
		double dayMajorTickSize = secondsInDay /*Math.max(secondsInDay,
				computeTimeTickSize(dayMajorPixels))*/;
		double dayMajorTickWidth = computeTickWidth(dayMajorTickSize);
		
		double monthMajorTickSize = secondsInMonth;
		double monthMajorTickWidth = computeTickWidth(monthMajorTickSize);
		
		double yearMajorTickSize = secondsInYear;
		double yearMajorTickWidth = computeTickWidth(yearMajorTickSize);
		
		double decadeMajorTickSize = secondsInDecade;
		double decadeMajorTickWidth = computeTickWidth(decadeMajorTickSize);
		
		double centuryMajorTickSize = secondsInCentury;
		double centuryMajorTickWidth = computeTickWidth(centuryMajorTickSize);
		
		if (dayMajorTickWidth >= 80){
			
			LabelFormatter formatter;
			
			if (dayMajorTickWidth >= 120){
				formatter = new VerboseDateLabelFormatter();
			}
			else{
				formatter = new DateLabelFormatter();				
			}
			
			renderTicksRangeLabelInline(topLabelPixelOffset, dayMajorTickSize,
					createDateTickGenerator(dayMajorTickSize), canvas,
					height, formatter);
			
			
			if (timeMajorNoLabelTickSize <= 3600*12 + epsilon) {
					renderTicks(0, timeMajorTickSize,
							createDateTickGenerator(timeMajorTickSize), canvas,
							majorTickWidthPixels, null);
					
					renderLabels(height / 2, timeMajorTickSize,
							createDateTickGenerator(timeMajorTickSize), canvas,
							0, new TimeLabelFormatter());

				double timeMinorTickSize =
					computeTimeMinorTickSize(timeMinorPixels, timeMajorTickSize);

				renderTicks(0, timeMinorTickSize,
						createDateTickGenerator(timeMinorTickSize), canvas,
						minorTickWidthPixels, null);
			}
		}
		else if (monthMajorTickWidth >= 150){
			renderTicks(0, dayMajorTickSize,
					createDateTickGenerator(dayMajorTickSize), canvas,
					majorTickWidthPixels, null);
			
			LabelFormatter dayFormatter = null;
			
			if (dayMajorTickWidth >= 40){
				dayFormatter = new DayLabelFormatter();
			}
			else if (dayMajorTickWidth >= 15){
				dayFormatter = new DateNumberLabelFormatter();
			}
			renderRangeLabelInline(height / 2, dayMajorTickSize,
					createDateTickGenerator(dayMajorTickSize), canvas,
					0, dayFormatter);
			
			renderTicksRangeLabelInline(topLabelPixelOffset, monthMajorTickSize,
					createDateTickGenerator(monthMajorTickSize), canvas,
					height, new VerboseMonthLabelFormatter(true));
						
		}
		else if (yearMajorTickWidth >= 80){
			renderTicks(0, monthMajorTickSize,
					createDateTickGenerator(monthMajorTickSize), canvas,
					majorTickWidthPixels, null);
			
			LabelFormatter monthFormatter = null;
			
			if (monthMajorTickWidth >= 55){
				monthFormatter = new VerboseMonthLabelFormatter(false);
			}
			else if (monthMajorTickWidth >= 20){
				monthFormatter = new MonthLabelFormatter(false);				
			}
			
			renderRangeLabelInline(height / 2, monthMajorTickSize,
					createDateTickGenerator(monthMajorTickSize), canvas,
					0, monthFormatter);
			
			renderTicksRangeLabelInline(topLabelPixelOffset, yearMajorTickSize,
					createDateTickGenerator(yearMajorTickSize), canvas,
					height, new YearLabelFormatter());
			
		}
		else if (decadeMajorTickWidth >= 150){
			renderTicks(0, yearMajorTickSize,
					createDateTickGenerator(yearMajorTickSize), canvas,
					majorTickWidthPixels, null);
			
			LabelFormatter yearFormatter = null;
			
			
			
			if (yearMajorTickWidth >= 30)
				yearFormatter = new YearLabelFormatter();
			else if (yearMajorTickWidth >= 15)
				yearFormatter = new YearSmallLabelFormatter();
			
			renderRangeLabelInline(height / 2, yearMajorTickSize,
					createDateTickGenerator(yearMajorTickSize), canvas,
					0, yearFormatter);
			
			renderTicksRangeLabelInline(topLabelPixelOffset, decadeMajorTickSize,
					createDateTickGenerator(decadeMajorTickSize), canvas,
					height, new DecadeLabelFormatter());
			
		}
		else{
			renderTicks(0, decadeMajorTickSize,
					createDateTickGenerator(decadeMajorTickSize), canvas,
					majorTickWidthPixels, null);
			
			LabelFormatter decadeFormatter = null;
			
			if (decadeMajorTickWidth >= 65){
				decadeFormatter = new DecadeLabelFormatter();
			}
			else if (decadeMajorTickWidth >= 25){
				decadeFormatter = new DecadeSmallLabelFormatter();
			}
			
			renderRangeLabelInline(height / 2, decadeMajorTickSize,
					createDateTickGenerator(decadeMajorTickSize), canvas,
					0, decadeFormatter);
			
			LabelFormatter centuryFormatter = new CenturySmallLabelFormatter();
			if (centuryMajorTickWidth >= 65){
				centuryFormatter = new CenturyLabelFormatter();
			}
			
			renderTicksRangeLabelInline(topLabelPixelOffset, centuryMajorTickSize,
					createDateTickGenerator(centuryMajorTickSize), canvas,
					height, centuryFormatter);
			
		}

		canvas.stroke();
		
		

		renderHighlight(canvas, getHighlightedPoint());
		
		renderCursor(canvas);

		// Clean up after ourselves
		canvas.setStrokeStyle(GrapherCanvas.DEFAULT_COLOR);
	}
}
