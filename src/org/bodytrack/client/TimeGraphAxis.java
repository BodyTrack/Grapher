package org.bodytrack.client;


import java.util.Calendar;
import java.util.Date;

import com.google.gwt.core.client.GWT;
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
	
	
	public double computeTimeTickSize(double minPixels) {
		double minDelta = (this.max-this.min) * (minPixels/this.length);
		final double tickSizes[] = {
				3600*12, 3600*6, 3600*3, 3600*1, // 12, 6, 3, 1 hours
				60*30, 60*15, 60*5, 60*1,        // 30, 15, 5, 1 mins
				30, 15, 5, 1                     // 30, 15, 5, 1 seconds
		};
		if (minDelta < 1) return computeTickSize(minPixels);
		if (minDelta > 3600*12) return computeTickSize(minPixels, 86400) * 86400;
		for (int i = 0; i < tickSizes.length; i++) {
			if (minDelta >= tickSizes[i]) return tickSizes[i];
		}
		return 1;
	}
	
	public void paint(Surface surface) {
		DirectShapeRenderer renderer = new DirectShapeRenderer(surface);
	    renderer.beginPath();
		renderer.drawLineSegment(project2D(this.min), project2D(this.max));
		double epsilon = 1e-10;
		
		double inlineYearPixels = 30;
		double inlineMonthPixels = 30;
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
		if (timeMajorTickSize <= 3600 * 12 + epsilon) {
			// Show time ticks if we get at least one tick per 12 hours

			renderDateTicks(renderer, timeMajorTickSize, SECOND, majorTickWidthPixels);
			renderDateTickLabels(surface, timeMajorTickSize, SECOND, majorTickWidthPixels);
			
			double timeMinorTickSize = computeTimeTickSize(timePixels / 5);
			renderDateTicks(renderer, timeMinorTickSize, SECOND, minorTickWidthPixels);
		}
		
		
		
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
		
	    if (false) {
	    	double majorTickSize = computeTickSize(majorTickMinSpacingPixels);
	    	renderTicks(renderer, majorTickSize, majorTickWidthPixels);
	    	renderTickLabels(surface, majorTickSize, majorTickWidthPixels+3);

	    	double minorTickSize = computeTickSize(minorTickMinSpacingPixels);
	    	renderTicks(renderer, minorTickSize, minorTickWidthPixels);
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


