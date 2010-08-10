package org.bodytrack.client;


import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.TextAlign;
import gwt.g2d.client.graphics.TextBaseline;
import gwt.g2d.client.math.Vector2;

//import java.util.Date;
// TODO: implement date
public class GraphAxis {
	public double majorTickMinSpacingPixels = 50;
    public double majorTickWidthPixels = 8;

    public double minorTickMinSpacingPixels = 10;
    public double minorTickWidthPixels = 3;

	protected double min;
	protected double max;
	protected Basis basis;
	private Vector2 begin;
	private double width;
	protected double length;
	private double scale;
	private BBox bounds;
	
	GraphAxis(double min, double max, Basis basis, double width) {
		this.min = min;
		this.max = max;
		this.basis = basis;
		this.width = width;
	}
	
	public void layout(Vector2 begin, double length) {
		this.begin = begin;
		this.length = length;
		rescale();
	}
	
	private void rescale() {
		this.scale = length / (this.max - this.min);
		Vector2 vWidth = new Vector2(basis.x.scale(this.width));
		Vector2 vLength = new Vector2(basis.y.scale(this.length));
		Vector2 end = this.begin.add(vWidth).add(vLength);
		bounds = new BBox(new Vector2(Math.min(begin.getX(), end.getX()),Math.min(begin.getY(), end.getY())),
				          new Vector2(Math.max(begin.getX(), end.getX()),Math.max(begin.getY(), end.getY())));				
	}
	
	public double project1D(double value) {
		return (value-this.min) * scale;
	}
	public Vector2 project2D(double value) {
		return begin.add(basis.y.scale(project1D(value)));
	}
	public double unproject(Vector2 point) {
		return this.min + (point.subtract(begin).dot(basis.y) / scale);
	}
	
	public void paint(Surface surface) {
		DirectShapeRenderer renderer = new DirectShapeRenderer(surface);
	    renderer.beginPath();
		renderer.drawLineSegment(project2D(this.min), project2D(this.max));
		

	    double majorTickSize = computeTickSize(majorTickMinSpacingPixels);
	    renderTicks(majorTickSize, null, surface, renderer, majorTickWidthPixels, new DefaultLabelFormatter());
	    //renderTickLabels(surface, majorTickSize, majorTickWidthPixels+3);

	    double minorTickSize = computeTickSize(minorTickMinSpacingPixels);
	    renderTicks(minorTickSize, null, surface, renderer, minorTickWidthPixels, null);
	    	
		renderer.stroke();
	}
	
	abstract class LabelFormatter {
		abstract String format(double value);
	}

	class DefaultLabelFormatter extends LabelFormatter {
		String format(double value) {
	       String label = NumberFormat.getDecimalFormat().format(value);
	        if (label.equals("-0")) label="0";
	        return label;
		}
	}
	
	class TickGenerator {
		private double tickSize;
		private double offset;
		private double currentTick = 0.0;
		
		TickGenerator(double tickSize, double offset) {
			this.tickSize = tickSize;
			this.offset = offset;
		}
		double nextTick(double min) {
			currentTick = closestTick(min - tickSize);
			while (currentTick < min) advanceTick();
			return currentTick;
		}
		double nextTick() {
			advanceTick();
			return currentTick;
		}
		void advanceTick() {
			currentTick = closestTick(currentTick + tickSize);
		}
		double closestTick(double val) {
			return Math.round((val-offset) / tickSize) * tickSize + offset; 
		}
	}
	
	protected void renderTicks(double tickSize, TickGenerator tickGen, Surface surface, DirectShapeRenderer renderer, double tickWidthPixels, LabelFormatter formatter) {
		if (tickGen == null) {
			tickGen = new TickGenerator(tickSize, 0);
		}
		
		int labelOffsetPixels = 0;
		if (formatter != null) {
	        boolean textParallelToAxis = (Math.abs(basis.x.getX()) < Math.abs(basis.x.getY()));
	        if (textParallelToAxis) {
	        	surface.setTextAlign(TextAlign.CENTER);
	        	surface.setTextBaseline(TextBaseline.TOP);
	        	labelOffsetPixels = 0;
	        } else {
	        	surface.setTextAlign(TextAlign.LEFT);
	        	surface.setTextBaseline(TextBaseline.MIDDLE);
	        	labelOffsetPixels = 3;
	        }
		}
		
		for (double y = tickGen.nextTick(this.min); y <= this.max; y = tickGen.nextTick()) {
			renderer.drawLineSegment(project2D(y),	project2D(y).add(this.basis.x.scale(tickWidthPixels)));
			if (formatter != null) {
				renderTickLabel(surface, y, tickWidthPixels+labelOffsetPixels, formatter);
			}
			
		}
	}
	
	protected void renderTickLabel(Surface surface, double y, double labelOffsetPixels, LabelFormatter formatter) {
		renderTickLabel(surface, y, labelOffsetPixels, formatter.format(y));
	}

	protected void renderTickLabel(Surface surface, double y, double labelOffsetPixels, String label) {
		// .setFont("italic 30px sans-serif")
        surface.fillText(label, project2D(y).add(this.basis.x.scale(labelOffsetPixels)));
	}

	public double computeTickSize(double minPixels) { return computeTickSize(minPixels, 1.0); }
	
	public double computeTickSize(double minPixels, double unitSize) {
		double minDelta = (this.max-this.min) * (minPixels/this.length) / unitSize;
		double minDeltaMantissa = minDelta / Math.pow(10, Math.floor(Math.log10(minDelta)));
		// Round minDelta up to nearest (1,2,5)
		double actualDeltaMantissa;
		if (minDeltaMantissa > 5) {
			actualDeltaMantissa = 10;
		} else if (minDeltaMantissa > 2) {
			actualDeltaMantissa = 5;
		} else if (minDeltaMantissa > 1) {
			actualDeltaMantissa = 2;
		} else {
			actualDeltaMantissa = 1;
		}
		return minDelta * (actualDeltaMantissa / minDeltaMantissa);
	}

	public double getWidth() {
		return width;
	}
	
	public boolean contains(Vector2 pos) {
		return bounds.contains(pos);
	}

	public void zoom(double factor, double about) {
		this.min = about + factor * (this.min-about);
		this.max = about + factor * (this.max-about);
		rescale();
	}

	public void drag(Vector2 from, Vector2 to) {
		double motion = unproject(from) - unproject(to);
		this.min += motion;
		this.max += motion;
		rescale();
	}
}
