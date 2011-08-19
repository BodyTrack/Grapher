package org.bodytrack.client.maps;

import java.util.ArrayList;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class PolyLine extends AbsolutePanel {
	private ArrayList<Double> xPoints = new ArrayList<Double>();
	private ArrayList<Double> yPoints = new ArrayList<Double>();
	private ArrayList<Element> lineElements = new ArrayList<Element>();
	
	private double plusX, plusY;
	
	private static long lineSegmentCounter;
	
	private String color;
	
	public static native com.google.gwt.user.client.Element createSVGElement(String type)/*-{
		return $doc.createElementNS("http://www.w3.org/2000/svg","svg:" + type);
	}-*/;
	
	private Element svgElement;
	//smil="" xlink="" id="ele_1">
	public PolyLine(String color){
		super(createSVGElement("svg"));
		svgElement = getElement();
		this.color = color;
	}
	
	public void setAddFactor(double x, double y){
		if (plusX == x && plusY == y)
			return;
		if (xPoints.size() > 1){
			for (int i = 0; i < xPoints.size(); i++){
				xPoints.set(i,xPoints.get(i) - plusX + x);
				yPoints.set(i,yPoints.get(i) - plusY + y);
				int prev = i - 1;
				if (prev >= 0){
					lineElements.get(prev).setAttribute("x2", xPoints.get(i) + "");
					lineElements.get(prev).setAttribute("y2", yPoints.get(i)+ "");
				}
				if (i < lineElements.size()){
					lineElements.get(i).setAttribute("x1", xPoints.get(i) + "");
					lineElements.get(i).setAttribute("y1", yPoints.get(i) + "");

				}
			}
		}
		else if (xPoints.size() != 0){
			xPoints.set(0,xPoints.get(0) - plusX + x);
			yPoints.set(0,yPoints.get(0) - plusY + y);
		}
		plusX = x;
		plusY = y;
	}
	
	public void addPoint(Point p){
		addPoint(p.getX(), p.getY());
	}
	
	public void addPoint(double x, double y){
		x += plusX;
		y += plusY;
		xPoints.add(x);
		yPoints.add(y);
		if (xPoints.size() == 1)
			return;
		Element lineElement = createSVGElement("line");
		//lineElement.setAttribute("counter", ++lineSegmentCounter + "");
		lineElement.setAttribute("x1",xPoints.get(xPoints.size() - 2) + "");
		lineElement.setAttribute("y1",yPoints.get(yPoints.size() - 2) + "");
		lineElement.setAttribute("x2",x + "");
		lineElement.setAttribute("y2",y + "");
		
		lineElement.setAttribute("style", "stroke:" + color + ";stroke-width:4");
		lineElements.add(lineElement);
		Node lastEle = svgElement.getLastChild();
		if (lastEle == null)
			svgElement.insertFirst(lineElement);
		else
			svgElement.insertAfter(lineElement, lastEle);
	}
	
	public void setPoint(int index, Point p){
		setPoint(index,p.getX(),p.getY());
	}
	
	public void setPoint(int index, double x, double y){
		if (index >= xPoints.size()){
			addPoint(x,y);
			return;
		}
		x += plusX;
		y += plusY;
		xPoints.set(index, x);
		yPoints.set(index, y);
		int numLines = lineElements.size();
		int index2 = index - 1;
		if (index2 >= 0){
			lineElements.get(index2).setAttribute("x2",x+"");
			lineElements.get(index2).setAttribute("y2",y+"");
		}
		if (numLines > index){
			lineElements.get(index).setAttribute("x1",x+"");
			lineElements.get(index).setAttribute("y1",y+"");
		}
	}
	
	public void deletePoint(int index){
		xPoints.remove(index);
		yPoints.remove(index);
		int numLines = lineElements.size();
		if (numLines - 1 == index){
			lineElements.remove(numLines - 1).removeFromParent();
		}
		else if (index == 0){
			lineElements.remove(0).removeFromParent();
		}
		else{
			Element line = lineElements.remove(index);
			line.removeFromParent();
			lineElements.get(index - 1).setAttribute("x2",line.getAttribute("x2"));
			lineElements.get(index - 1).setAttribute("y2",line.getAttribute("y2"));
		}
	}

	public void setSegmentColor(int segment, String color2) {
		lineElements.get(segment).setAttribute("style", "stroke:" + color2 + ";stroke-width:4");
	}
	
	public double getX(int index){
		return xPoints.get(index) - plusX;
	}
	
	public double getY(int index){
		return yPoints.get(index) - plusY;
	}
}
