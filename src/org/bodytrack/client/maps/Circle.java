package org.bodytrack.client.maps;

import com.google.gwt.dom.client.Element;
import com.google.gwt.maps.client.MapPane;
import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

public class Circle extends Overlay { //circle implementaion for v2 api, should replicate v3 counterpart

	private Element svgElement, circleElement;
	
	private Widget widget;
	
	private LatLng center;
	private double radius;
	private String color;
	private double opacity;
	private MapWidget map;

	private MapPane pane;
	
	private class SVGWidget extends AbsolutePanel{
		public SVGWidget(Element e){
			super((com.google.gwt.user.client.Element) svgElement);
		}
	}
	
	public void setRadius(double radius){
		this.radius = radius;
		calculatePosition();
	}
	
	public void setCenter(LatLng center){
		this.center = center;
		calculatePosition();
	}
	
	private void updateStyle(){
		circleElement.setAttribute("style", "fill:" + color + ";fill-opacity:" + opacity + ";");
	}
	
	public Circle(LatLng center, double radius, String color, double opacity){
		svgElement = PolyLine.createSVGElement("svg");
		circleElement = PolyLine.createSVGElement("circle");
		svgElement.insertFirst(circleElement);
		this.center = center;
		this.radius = radius;
		this.color = color;
		this.opacity = opacity;
		widget = new SVGWidget(svgElement);
		updateStyle();
	}
	
	private void calculatePosition(){
		if (map == null)
			return;
		Point c = map.convertLatLngToDivPixel(center);
		double longitude = center.getLongitude() + radius * 360 / 6378100 / 2 / Math.PI;;
		while (longitude > 360)
			longitude -= 180;
		while (longitude < -360)
			longitude += 180;
		Point c2 = map.convertLatLngToDivPixel(LatLng.newInstance(center.getLatitude(),longitude));
		int dX = c.getX() - c2.getX();
		int dY = c.getY() - c2.getY();
		double radius = Math.sqrt(dX * dX + dY * dY);
		circleElement.setAttribute("cx", (radius) + "");
		circleElement.setAttribute("cy", (radius) + "");
		circleElement.setAttribute("r", radius + "");
		pane.setWidgetPosition(widget, (int) (c.getX() - radius), (int) (c.getY() - radius));
		widget.setSize(radius * 2 + "px", radius * 2 + "px");
		
	}
	
	@Override
	protected Overlay copy() {
		return new Circle(center, radius, color, opacity);
	}

	@Override
	protected void initialize(MapWidget map) {
		this.map = map;
		pane = map.getPane(MapPaneType.MAP_PANE);
		pane.add(widget);
		calculatePosition();
	}

	@Override
	protected void redraw(boolean force) {
		if (!force)
			return;
		calculatePosition();
	}

	@Override
	protected void remove() {
		widget.removeFromParent();
	}

}
