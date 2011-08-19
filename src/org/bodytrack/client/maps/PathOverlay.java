package org.bodytrack.client.maps;

import java.util.ArrayList;

import com.google.gwt.maps.client.MapPane;
import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;

public class PathOverlay extends Overlay {
	private PolyLine[] lines;
	private MapWidget map = null;
	private MapPane pane;
	
	private int numLines;
	
	private int minX = 0, maxX = 0, minY = 0, maxY = 0;
	
	private ArrayList<LatLng> points = new ArrayList<LatLng>();
	
	String[] colors = {"red","orange","yellow","green","blue","indigo","violet"};
	
	public PathOverlay(int numLines) {
		if (numLines < 1)
			throw new IllegalArgumentException("need at least one line");
		this.numLines = numLines;
		lines = new PolyLine[numLines];
		lines[0] = new PolyLine("red");
		for (int i = 1; i < numLines; i++)
			lines[i] = new PolyLine(colors[i - 1]);
	    }

	@Override
	protected Overlay copy() {
		PathOverlay p = new PathOverlay(numLines);
		for (LatLng point : points){
			p.addPoint(point);
		}
		return p;
	}
	
	private void updatePosition(){
		for (int i = 0; i < numLines; i++){
			pane.setWidgetPosition(lines[i], minX, minY);
			lines[i].setSize((maxX - minX + 4 * i) + "px", (maxY- minY + 4 * i) + "px");
			lines[i].setAddFactor(-minX,-minY);
		}
	}
	
	private static double[] transform(double x, double y, double theta, int distance){
		if (Double.isNaN(theta))
			return new double[]{x,y};
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin(theta);
		double newX, newY;
		
		newY = x * cosTheta + y * sinTheta;
		newX = y * cosTheta - x * sinTheta;
		
		newX += distance;
		
		x = newY * cosTheta - newX * sinTheta;
		y = newX * cosTheta + newY * sinTheta;
		return new double[]{x,y};
	}
	
	private void updatePoint(int point, boolean updatePos){
		Point p = map.convertLatLngToDivPixel(points.get(point));
		double theta = 0;
		double slope = 0;
		double prevSlope = 0;
		if (point > 0){
			double dx = p.getX() - lines[0].getX(point - 1);
			double dy = p.getY() - lines[0].getY(point - 1);
			if (dx == 0){
				if (dy == 0){
					theta = Double.NaN;
					slope = Double.NaN;
				}
				else if (dy > 0){
					theta = -Math.PI;
					slope = Double.POSITIVE_INFINITY;
				}
				else{
					theta = Math.PI;
					slope = Double.NEGATIVE_INFINITY;
				}
			}
			else{
				slope = dy / dx;
				theta = Math.atan(slope);
			}
			if (dx < 0)
				theta += Math.PI;
		}
		if (point > 1){
			double dx = lines[0].getX(point -2) - lines[0].getX(point - 1);
			double dy = lines[0].getY(point -2) - lines[0].getY(point - 1);
			if (dx == 0){
				if (dy == 0){
					prevSlope = Double.NaN;
				}
				else if (dy > 0){
					prevSlope = Double.POSITIVE_INFINITY;
				}
				else{
					prevSlope = Double.NEGATIVE_INFINITY;
				}
			}
			else{
				prevSlope = dy / dx;
			}
		}
		lines[0].setPoint(point, p);
		for (int i = 1; i < numLines; i++){
			if (point == 0){
				lines[i].setPoint(point, p);
			}
			else{
				double[] coords = transform(p.getX(), p.getY(), theta, 4*i);
				lines[i].setPoint(point, (int) coords[0], (int) coords[1]);
				if (point == 1){ //assign to point for this line
					coords = transform(lines[0].getX(0), lines[0].getY(0), theta, 4*i);
					lines[i].setPoint(0, coords[0], coords[1]);
				}
				else if (point > 1){//use intersection with old line
					if ((slope - prevSlope) > 0.1 && !Double.isNaN(slope) && !Double.isNaN(prevSlope) && !(Double.isInfinite(slope) && Double.isInfinite(prevSlope))){
						if (Double.isInfinite(slope)){
							double oldY = lines[i].getY(point - 1);
							double oldX = lines[i].getX(point - 1);
							
							double prevYInt = oldY - prevSlope * oldX;
							double newX = coords[0];
							double newY = prevSlope * newX + prevYInt;
							lines[i].setPoint(point - 1, newX, newY);
						}
						else if (Double.isInfinite(prevSlope)){
							double oldX = lines[i].getX(point - 1);
							
							double yInt = coords[1] - slope * coords[0]; 
							double newX = oldX;
							double newY = slope * newX + yInt;
							lines[i].setPoint(point - 1, newX, newY);
						}
						else{
							double yInt = coords[1] - slope * coords[0]; 
							
							double oldY = lines[i].getY(point - 1);
							double oldX = lines[i].getX(point - 1);
							
							double prevYInt = oldY - prevSlope * oldX;
							
							double newX = (prevYInt - yInt) / (slope - prevSlope);
							double newY = slope * newX + yInt;
							
							lines[i].setPoint(point - 1, newX, newY);
						}
					}
					
					/**/
				}
			}
		}
		boolean needupdate = false;
		if (p.getX() < minX){
			minX = p.getX();
			needupdate = true;
		}
		if (p.getX() > maxX){
			maxX = p.getX();
			needupdate = true;
		}
		if (p.getY() < minY){
			minY = p.getY();
			needupdate = true;
		}
		if (p.getY() > maxY){
			maxY = p.getY();
			needupdate = true;
		}
		if (needupdate && updatePos)
			updatePosition();
	}
	
	public void addPoint(LatLng point){
		points.add(point);
		if (map != null){
			updatePoint(points.size() - 1, false);
		}
		updatePosition();
	}

	@Override
	protected void initialize(MapWidget map) {
		this.map = map;
		for (int i = 0; i < points.size(); i++){
			updatePoint(i,false);
		}
		pane = map.getPane(MapPaneType.MAP_PANE);
	    for (int i = numLines - 1; i >= 0; i--){
	    	pane.add(lines[i]);
	    }
	    updatePosition();
	}

	@Override
	protected void redraw(boolean force) {
		if (!force) //cordinate system maintained
			return;
		for (int i = 0; i < points.size(); i++){
			updatePoint(i, false);
		}
		updatePosition();
	}

	@Override
	protected void remove() {
		for (int j = 0; j < numLines; j++){
			lines[j].removeFromParent();
		}
	}
	
	public LatLng[] getPoints(){
		return points.toArray(new LatLng[]{});
	}
	
	public LatLng getPoint(int index){
		try{
			return points.get(index);
		}
		catch (Exception e){
			return null;
		}
	}
	
	public void setSegmentColor(int line, int segment, String color){
		if (line < 0 || segment < 0)
			throw new IllegalArgumentException("invalid line or segment");
		lines[line].setSegmentColor(segment, color);
	}
	
	public void clearPoints(){
		if (pane != null){
			for (int j = 0; j < numLines; j++){
				lines[j].removeFromParent();
			}
		}
		lines[0] = new PolyLine("red");
		for (int i = 1; i < numLines; i++)
			lines[i] = new PolyLine(colors[i - 1]);
		if (pane != null){
			for (int i = numLines - 1; i >= 0; i--){
		    	pane.add(lines[i]);
		    }
		}
		points = new ArrayList<LatLng>();
		updatePosition();
	}

}
