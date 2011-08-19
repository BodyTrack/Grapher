package org.bodytrack.client.maps;


import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.Image;

public class ImageOverlay extends Overlay {
	private Image img;
	private LatLng position;
	
	private MapWidget map;

	private MapPane pane;
	
	private EventHandler eventHandler = new EventHandler();
	
	private boolean mouseOver = false;
	
	private String defaultZIndex = "100";
	private String hoveredZIndex = "107";
	public ImageOverlay(String source, LatLng position){
		img = new Image(source);
		img.addMouseOutHandler(eventHandler);
		img.addMouseOverHandler(eventHandler);
		DOM.setStyleAttribute(img.getElement(), "border", "2px black solid");
		img.setStyleName("nohighlight");
		
		this.position = position;
	}

	@Override
	protected Overlay copy() {
		return new ImageOverlay(img.getUrl(),position);
	}
	
	private void updateImageLocation(){
		int width, height;
		if (mouseOver){
			width = height = 150;
			DOM.setStyleAttribute(img.getElement(), "maxWidth", "150px");
			DOM.setStyleAttribute(img.getElement(), "maxHeight", "150px");
			DOM.setStyleAttribute(img.getElement(), "zIndex", hoveredZIndex);
			DOM.setStyleAttribute(img.getElement(), "opacity", "1");
		}
		else{
			width = height = 75;
			DOM.setStyleAttribute(img.getElement(), "maxWidth", "75px");
			DOM.setStyleAttribute(img.getElement(), "maxHeight", "75px");
			DOM.setStyleAttribute(img.getElement(), "zIndex", defaultZIndex);
			DOM.setStyleAttribute(img.getElement(), "opacity", "0.5");
		}
		Point p = map.convertLatLngToDivPixel(position);
		if (img.getWidth() != 0)
			width = img.getWidth();
		if (img.getHeight() != 0)
			height = img.getHeight();
		pane.setWidgetPosition(img, p.getX() - width / 2, p.getY() - height / 2);
	}
	
	

	@Override
	protected void initialize(MapWidget map) {
		this.map = map;
		pane = map.getPane(MapPaneType.MAP_PANE);
		pane.add(img);
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
		img.removeFromParent();
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
