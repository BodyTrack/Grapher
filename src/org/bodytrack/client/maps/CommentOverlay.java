package org.bodytrack.client.maps;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.maps.client.MapPane;
import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;

public class CommentOverlay extends Overlay {
	private Label label;
	private LatLng position;
	
	private MapWidget map;

	private MapPane pane;
	
	private EventHandler eventHandler = new EventHandler();
	
	private boolean mouseOver = false;
	
	private String defaultZIndex = "100";
	private String hoveredZIndex = "107";
	
	public CommentOverlay(String comment, LatLng position){
		label = new Label(comment);
		label.addMouseOutHandler(eventHandler);
		label.addMouseOverHandler(eventHandler);
		DOM.setStyleAttribute(label.getElement(), "backgroundColor", "white");
		DOM.setStyleAttribute(label.getElement(), "color", "black");
		DOM.setStyleAttribute(label.getElement(),"border", "2px solid black");
		DOM.setStyleAttribute(label.getElement(),"text-align","center");
		label.setStyleName("nohighlight");
		this.position = position;
	}

	@Override
	protected Overlay copy() {
		return new ImageOverlay(label.getText(),position);
	}
	
	private void updateImageLocation(){
		if (mouseOver){
			DOM.setStyleAttribute(label.getElement(), "width", "150px");
			DOM.setStyleAttribute(label.getElement(), "opacity", "1");
			DOM.setStyleAttribute(label.getElement(), "font-size","1em");
			DOM.setStyleAttribute(label.getElement(), "zIndex", hoveredZIndex);
		}
		else{
			DOM.setStyleAttribute(label.getElement(), "width", "75px");
			DOM.setStyleAttribute(label.getElement(), "zIndex", defaultZIndex);
			DOM.setStyleAttribute(label.getElement(), "opacity", "0.5");
			DOM.setStyleAttribute(label.getElement(), "font-size","0.5em");
		}
		Point p = map.convertLatLngToDivPixel(position);
		pane.setWidgetPosition(label, p.getX() - label.getOffsetWidth() / 2, p.getY() - label.getOffsetHeight() / 2);
	}
	
	

	@Override
	protected void initialize(MapWidget map) {
		this.map = map;
		pane = map.getPane(MapPaneType.MAP_PANE);
		pane.add(label);
		updateImageLocation();
	}

	@Override
	protected void redraw(boolean force) {
		if (!force)
			return;
		updateImageLocation();
	}

	@Override
	protected void remove() {
		label.removeFromParent();
	}
	
	private class EventHandler implements MouseOverHandler, MouseOutHandler{

		@Override
		public void onMouseOut(MouseOutEvent event) {
			mouseOver = false;
			redraw(true);
		}

		@Override
		public void onMouseOver(MouseOverEvent event) {
			mouseOver = true;
			redraw(true);
		}
	}

}
