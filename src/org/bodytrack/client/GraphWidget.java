package org.bodytrack.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.KnownColor;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.math.Vector2;

public class GraphWidget extends Surface {
	
	private ArrayList<GraphAxis> xAxes = new ArrayList<GraphAxis>();
	private ArrayList<GraphAxis> yAxes = new ArrayList<GraphAxis>();
	private ArrayList<GraphAxis> allAxes = new ArrayList<GraphAxis>();
	
	private int width, height;
	private int axisMargin;

	private int graphWidth, graphHeight;
	
	GraphWidget(int width, int height, int axisMargin) {
		super(width, height);
		this.width = width;
		this.height = height;
		this.axisMargin = axisMargin;
		
		this.addMouseWheelHandler(new MouseWheelHandler() {
			public void onMouseWheel(MouseWheelEvent event) { handleMouseWheelEvent(event); }
		});
	}

	GraphAxis findAxis(int x, int y) {
		for (int i=0; i < allAxes.size(); i++) {
			if (allAxes.get(i).contains(x,y)) return allAxes.get(i);
		}
		return null;
	}
	
	private void handleMouseWheelEvent(MouseWheelEvent event) {
		GWT.log("handle mouse wheel event");
		GraphAxis axis = findAxis(event.getX(), event.getY());
		if (axis != null) {
			double zoomFactor = Math.pow(1.001, event.getDeltaY());
			double zoomAbout = axis.unproject(new Vector2(event.getX(), event.getY()));
			axis.zoom(zoomFactor, zoomAbout);
			paint();
			GWT.log("found axes to zoom");
		} else {
			GWT.log("no axes to zoom");
		}
	}

	private void layout() {
		int xAxesWidth = calculateAxesWidth(xAxes);
		int yAxesWidth = calculateAxesWidth(yAxes);
		graphWidth = width - yAxesWidth;
		graphHeight = height - xAxesWidth;
		Vector2 xAxesBegin = new Vector2(0, graphHeight);
		layoutAxes(xAxes, graphWidth, xAxesBegin, Basis.xDownYRight);
		
		Vector2 yAxesBegin = new Vector2(graphWidth, graphHeight);
		layoutAxes(yAxes, graphHeight, yAxesBegin, Basis.xRightYUp);
	}
	
	
	private int calculateAxesWidth(ArrayList<GraphAxis> axes) {
		int ret = 0;
		for (int i=0; i < axes.size(); i++) {
			ret += axes.get(i).getWidth();
			if (i > 0) ret += axisMargin;
		}
		return ret;
	}
	
	private void layoutAxes(ArrayList<GraphAxis> axes, double length, Vector2 begin, Basis basis) {
		Vector2 offset = begin;
		for (int i=0; i < axes.size(); i++) {
			GWT.log("layout " + String.valueOf(i) + ": " + String.valueOf(offset.getX()));
			axes.get(i).layout(offset, length);
			offset = offset.add(basis.x.scale(axisMargin + axes.get(i).getWidth()));
		}
	}

	public void paint() {
		layout();
		GWT.log("clearing background to black?");
		this.clear();
		//this.clearRectangle(0,0,400,400);
		//this.setFillStyle(new Color((int) (Random.nextDouble() * 255), 128, 128));
		//this.fillRectangle(0,0,500,500);
		this.save();
		this.translate(.5, .5);
		for (int i=0; i < allAxes.size(); i++) allAxes.get(i).paint(this);
		this.restore();
		
		//DirectShapeRenderer renderer = new DirectShapeRenderer(this);
	    //renderer.beginPath();
	    
	    //for (double x = 0; x < 400; x++) {
	    //	double y = 200+200*Math.sin(x*.1);
	    //	renderer.drawLineTo(x,y);
	    //}
	    //renderer.stroke();
	}

	public void addXAxis(GraphAxis graphAxis) {
		xAxes.add(graphAxis);
		allAxes.add(graphAxis);
	}

	public void addYAxis(GraphAxis graphAxis) {
		yAxes.add(graphAxis);
		allAxes.add(graphAxis);
	}
}