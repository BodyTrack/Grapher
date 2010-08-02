package org.bodytrack.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.user.client.Window;

import gwt.g2d.client.graphics.DirectShapeRenderer;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.math.Vector2;

public class GraphWidget extends Surface {
	
	private ArrayList<GraphAxis> xAxes = new ArrayList<GraphAxis>();
	private ArrayList<GraphAxis> yAxes = new ArrayList<GraphAxis>();
	
	private int width, height;
	private int axisMargin;

	private int graphWidth, graphHeight;
	
	GraphWidget(int width, int height, int axisMargin) {
		super(width, height);
		this.width = width;
		this.height = height;
		this.axisMargin = axisMargin;
	}
	
	private void layout() {
		int xAxesWidth = calculateAxesWidth(xAxes);
		int yAxesWidth = calculateAxesWidth(yAxes);
		graphWidth = width - yAxesWidth;
		graphHeight = height - xAxesWidth;
		Vector2 xAxesBegin = new Vector2(0, graphHeight);
		layoutAxes(xAxes, graphWidth, xAxesBegin, Basis.xRightYUp);
		
		Vector2 yAxesBegin = new Vector2(graphWidth, graphHeight);
		layoutAxes(yAxes, graphHeight, yAxesBegin, Basis.xDownYRight);
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
			axes.get(i).layout(offset, length);
			offset = offset.add(basis.x.scale(axisMargin + axes.get(i).getWidth()));
		}
	}

	public void paint() {
		layout();
		this.clear();
		this.translate(.5, .5);
		for (int i=0; i < xAxes.size(); i++) xAxes.get(i).paint(this);
		for (int i=0; i < yAxes.size(); i++) yAxes.get(i).paint(this);
		
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
	}

	public void addYAxis(GraphAxis graphAxis) {
		yAxes.add(graphAxis);
	}
}