package org.bodytrack.client;

import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.event.Event;
import com.google.gwt.user.client.ui.RootPanel;

public class MapPlotContainer extends BasePlotContainer {
	
	private RootPanel placeholderElement;
	private int width, height;
	private MapWidget mapWidget;
	
	public MapPlotContainer(String placeholderElementId){
		if (placeholderElementId == null) {
			throw new NullPointerException("The placeholder element ID cannot be null");
		}
		placeholderElement = RootPanel.get(placeholderElementId);
		width = placeholderElement.getElement().getClientWidth();
		height = placeholderElement.getElement().getClientHeight();
		
		MapOptions options = new MapOptions();
		options.setMapTypeId(new MapTypeId().getHybrid());
		options.setZoom(8);
	    options.setCenter(new LatLng(39.509, -98.434));
		options.setDraggable(true);
		options.setNavigationControl(true);
		options.setMapTypeControl(true);
		options.setScrollwheel(true);
		mapWidget = new MapWidget(options);
		mapWidget.setSize("100%", "100%");
		placeholderElement.add(mapWidget);
		LatLng f;
	}

	@Override
	public void setSize(int widthInPixels, int heightInPixels, int newPaintEventId) {
		placeholderElement.setSize(widthInPixels + "px", widthInPixels + "px");
		Event.trigger(mapWidget.getMap(), "resize");
	}

	@Override
	public void paint(int eventId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paint() {
		// TODO Auto-generated method stub
		
	}

}
