package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.math.Vector2;

public class GraphWidget extends Surface {
	private List<GraphAxis> xAxes = new ArrayList<GraphAxis>();
	private List<GraphAxis> yAxes = new ArrayList<GraphAxis>();
	private List<GraphAxis> allAxes = new ArrayList<GraphAxis>();
	
	private List<DataPlot> dataPlots;

	private int width, height;
	private int axisMargin;
	private int graphMargin = 5;

	private int graphWidth, graphHeight;

	private GraphAxis mouseDragAxis;
	private Vector2 mouseDragLastPos;

	GraphWidget(int width, int height, int axisMargin) {
		super(width, height);
		this.width = width;
		this.height = height;
		this.axisMargin = axisMargin;
		
		dataPlots = new ArrayList<DataPlot>();

		this.addMouseWheelHandler(new MouseWheelHandler() {
			@Override
			public void onMouseWheel(MouseWheelEvent event) {
				handleMouseWheelEvent(event);
			}
		});

		this.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				handleMouseDownEvent(event);
			}
		});

		this.addMouseMoveHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				handleMouseMoveEvent(event);
			}
		});

		this.addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				handleMouseUpEvent(event);
			}
		});

		this.addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				handleMouseOutEvent(event);
			}
		});
	}

	GraphAxis findAxis(Vector2 pos) {
		for (GraphAxis axis: allAxes) {
			if (axis.contains(pos))
				return axis;
		}

		return null;
	}

	private void handleMouseWheelEvent(MouseWheelEvent event) {
		Vector2 eventLoc = new Vector2(event.getX(), event.getY());
		GraphAxis axis = findAxis(eventLoc);
		if (axis != null) {
			double zoomFactor = Math.pow(1.003, event.getDeltaY());
			double zoomAbout = axis.unproject(eventLoc);
			axis.zoom(zoomFactor, zoomAbout);
			paint();
		}
	}

	private void handleMouseDownEvent(MouseDownEvent event) {
		Vector2 pos = new Vector2(event.getX(), event.getY());
		GraphAxis axis = findAxis(pos);

		if (axis != null) {
			mouseDragAxis = axis;
			mouseDragLastPos = pos;
		}
	}

	private void handleMouseMoveEvent(MouseMoveEvent event) {
		Vector2 pos = new Vector2(event.getX(), event.getY());
		if (mouseDragAxis != null) {
			mouseDragAxis.drag(mouseDragLastPos, pos);
			mouseDragLastPos = pos;
			paint();
		}
	}

	private void handleMouseUpEvent(MouseUpEvent event) {
		if (mouseDragAxis != null) {
			mouseDragAxis = null;
		}
	}

	private void handleMouseOutEvent(MouseOutEvent event) {
		if (mouseDragAxis != null) {
			mouseDragAxis = null;
		}
	}

	private void layout() {
		int xAxesWidth = calculateAxesWidth(xAxes);
		int yAxesWidth = calculateAxesWidth(yAxes);
		graphWidth = width - graphMargin - yAxesWidth;
		graphHeight = height - graphMargin - xAxesWidth;
		Vector2 xAxesBegin = new Vector2(graphMargin,
			graphHeight + graphMargin);
		layoutAxes(xAxes, graphWidth, xAxesBegin, Basis.xDownYRight);

		Vector2 yAxesBegin = new Vector2(graphWidth+graphMargin,
			graphHeight + graphMargin);
		layoutAxes(yAxes, graphHeight, yAxesBegin, Basis.xRightYUp);
	}

	private int calculateAxesWidth(List<GraphAxis> axes) {
		int ret = 0;

		for (int i=0; i < axes.size(); i++) {
			ret += axes.get(i).getWidth();

			if (i > 0)
				ret += axisMargin;
		}

		return ret;
	}

	private void layoutAxes(List<GraphAxis> axes, double length,
			Vector2 begin, Basis basis) {
		Vector2 offset = begin;

		for (GraphAxis axis: axes) {
			axis.layout(offset, length);

			offset = offset.add(
					basis.x.scale(axisMargin + axis.getWidth()));
		}
	}

	public void paint() {
		layout();
		this.clear();
		//this.clearRectangle(0,0,400,400);
		//this.setFillStyle(new Color(
		//		(int) (Random.nextDouble() * 255), 128, 128));
		//this.fillRectangle(0,0,500,500);
		this.save();
		this.translate(.5, .5);

		for (GraphAxis axis: allAxes)
			axis.paint(this);

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
		if (xAxes.contains(graphAxis))
			return;

		xAxes.add(graphAxis);
		allAxes.add(graphAxis);
	}

	public void addYAxis(GraphAxis graphAxis) {
		if (yAxes.contains(graphAxis))
			return;

		yAxes.add(graphAxis);
		allAxes.add(graphAxis);
	}
	
	/**
	 * Adds plot to the list of data plots to be drawn.
	 * 
	 * Note that a plot can only be added once to this internal list.
	 * 
	 * @param plot
	 * 		the plot to add to the list of plots to be drawn
	 */
	public void addDataPlot(DataPlot plot) {
		if (! dataPlots.contains(plot))
			dataPlots.add(plot);
	}
	
	/**
	 * Removes plot from the list of data plots to be drawn.
	 * 
	 * @param plot
	 * 		the plot to remove from the list of plots to be drawn
	 */
	public void removeDataPlot(DataPlot plot) {
		dataPlots.remove(plot);
	}
}
