package org.bodytrack.client;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.math.Vector2;

// TODO: implement date
public class GraphAxis {
	public double major_tick_min_spacing_pixels = 100;
    public double major_tick_width_pixels = 6;

    public double minor_tick_min_spacing_pixels = 20;
    public double minor_tick_width_pixels = 3;

	private double min, max;
	private Basis basis;
	private Vector2 begin;
	private double width, length, scale;
	
	GraphAxis(double min, double max, Basis basis, double width) {
		this.min = min;
		this.max = max;
		this.basis = basis;
		this.width = width;
	}
	
	public void layout(Vector2 begin, double length) {
		this.begin = begin;
		this.length = length;
		this.scale = length / (this.max - this.min);
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
	    
		renderer.drawLineSegment(project2D(this.min), project2D(this.max));
		

	    double majorTickSize = computeTickSize(major_tick_min_spacing_pixels);
	    renderTicks(renderer, majorTickSize, major_tick_width_pixels);

	    double minorTickSize = computeTickSize(minor_tick_min_spacing_pixels);
	    renderTicks(renderer, minorTickSize, minor_tick_width_pixels);
	    	
		renderer.stroke();		    
	}
	
	private void renderTicks(DirectShapeRenderer renderer, double tickValue,
			double tickWidthPixels) {
		double epsilon = 1e-10;
		for (double y = Math.ceil(this.min / tickValue) * tickValue;
				y <= (Math.floor(this.max / tickValue) + epsilon) * tickValue;
				y += tickValue) {
			GWT.log("GraphAxis.render_ticks: tick at " + String.valueOf(y));
	
			renderer.drawLineSegment(project2D(y),	project2D(y).add(this.basis.x.scale(tickWidthPixels)));
		}
	}

	public double computeTickSize(double min_pixels) {
		double min_delta = (this.max-this.min) * (min_pixels/this.length);
		double min_delta_mantissa = min_delta / Math.pow(10, Math.floor(Math.log10(min_delta)));
		// Round min_delta up to nearest (1,2,5)
		double actual_delta_mantissa;
		if (min_delta_mantissa > 5) {
			actual_delta_mantissa = 10;
		} else if (min_delta_mantissa > 2) {
			actual_delta_mantissa = 5;
		} else if (min_delta_mantissa > 1) {
			actual_delta_mantissa = 2;
		} else {
			actual_delta_mantissa = 1;
		}
		return min_delta * (actual_delta_mantissa / min_delta_mantissa);
	}

	public double getWidth() {
		return width;
	}
}
