package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class BodyTrackWidget extends VerticalPanel {
	FlowPanel topBar = new FlowPanel();
	LoginWidget loginWidget = new LoginWidget();
	NavBar navBar = new NavBar();
	GraphWidget graphWidget;
	GraphAxis timeAxis;
	
	Widget mainPanel;
	
	BodyTrackWidget() {
		setWidth("100%");
		navBar.add("Graph", new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) { selectGraph(); }
		});
		navBar.add("Photos", new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) { selectPhotos(); }
		});
		

		topBar.add(navBar);
		DOM.setStyleAttribute(navBar.getElement(), "cssFloat", "left");
		
		//	topBar.setCellHorizontalAlignment(loginWidget, HasHorizontalAlignment.ALIGN_RIGHT);
		//topBar.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
	//	topBar.setCellHorizontalAlignment(loginWidget, HorizontalPanel.ALIGN_RIGHT)
		//DOM.setElementProperty(loginWidget.getElement(), "float", "right");		
		
		topBar.add(loginWidget);
		DOM.setStyleAttribute(loginWidget.getElement(), "cssFloat", "right");
		//topBar.setCellHorizontalAlignment(loginWidget, HorizontalPanel.ALIGN_RIGHT);
		//topBar.setCellWidth(loginWidget, "100%");
		
		this.add(topBar);
		topBar.setWidth("100%");
		
		// TODO: select between different widgets
		//flowPhotosWidget = new FlowPhotosWidget(1293861600-86400*15, 1293861600);
		//this.add(flowPhotosWidget);
	}

	void unselect() {
		if (mainPanel != null) this.remove(mainPanel);
	}
	
	private static final Color[] DATA_PLOT_COLORS = {
		Canvas.BLACK,
		Canvas.GREEN,
		Canvas.BLUE,
		Canvas.RED,
		new Color(200, 200,   0),
		new Color(  0, 200, 200),
		new Color(200,   0, 200)
	};
	
	String[] anneChannels = {
			"{\"max_value\":\"100.0\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-09T09:20:56-05:00\",\"min_value\":\"0.0\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":64,\"ch_name\":\"Humidity\",\"dev_nickname\":\"AR_Basestation\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-05T06:31:51-05:00\"}",
			"{\"max_value\":\"255.0\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-05T06:31:51-05:00\",\"min_value\":\"0.0\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":63,\"ch_name\":\"Microphone\",\"dev_nickname\":\"AR_Basestation\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-05T06:31:51-05:00\"}",
			"{\"max_value\":\"255.0\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-06T12:27:38-05:00\",\"min_value\":\"0.0\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":65,\"ch_name\":\"Pressure\",\"dev_nickname\":\"AR_Basestation\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-05T06:31:51-05:00\"}",
			"{\"max_value\":\"100.0\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-06T12:39:56-05:00\",\"min_value\":\"19.1\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":66,\"ch_name\":\"Temperature\",\"dev_nickname\":\"AR_Basestation\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-05T06:31:51-05:00\"}",
			"{\"max_value\":\"50.0\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-13T08:49:12-05:00\",\"min_value\":\"0.0\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":69,\"ch_name\":\"Dew_Point\",\"dev_nickname\":\"PIT\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-13T08:49:12-05:00\"}",
			"{\"max_value\":\"30\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-13T08:49:12-05:00\",\"min_value\":\"28\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":68,\"ch_name\":\"Pressure\",\"dev_nickname\":\"PIT\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-13T08:49:12-05:00\"}",
			"{\"max_value\":\"100.0\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-13T08:49:12-05:00\",\"min_value\":\"0.0\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":67,\"ch_name\":\"Temperature\",\"dev_nickname\":\"PIT\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-13T08:49:12-05:00\"}",
			"{\"max_value\":\"50.0\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-13T08:49:12-05:00\",\"min_value\":\"0.0\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":69,\"ch_name\":\"Dew_Point\",\"dev_nickname\":\"CSG\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-13T08:49:12-05:00\"}",
			"{\"max_value\":\"30\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-13T08:49:12-05:00\",\"min_value\":\"28\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":68,\"ch_name\":\"Pressure\",\"dev_nickname\":\"CSG\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-13T08:49:12-05:00\"}",
			"{\"max_value\":\"100.0\",\"graph_type\":\"Line\",\"updated_at\":\"2011-01-13T08:49:12-05:00\",\"min_value\":\"0.0\",\"data_type\":\"Double\",\"max_graph_value\":null,\"id\":67,\"ch_name\":\"Temperature\",\"dev_nickname\":\"CSG\",\"user_id\":1,\"min_graph_value\":null,\"created_at\":\"2011-01-13T08:49:12-05:00\"}"
	};
	
	String[] randyChannels = anneChannels;
	
	String fetchChannelsJSON() {
		String[] channels={};
		switch (G.user_id) {
			case 1:  channels = anneChannels; break;
			case 2:  channels = randyChannels; break;
		}
		return "[" + StringUtil.join(channels, ",") +"]";
	}
	
	static class Graphable extends JavaScriptObject {
		protected Graphable() {}
		final native double min_value()    /*-{ return this.min_value-0;  }-*/;
		final native double max_value()    /*-{ return this.max_value-0;  }-*/;
		final native String dev_nickname() /*-{ return this.dev_nickname; }-*/;
		final native String ch_name()      /*-{ return this.ch_name;      }-*/;
		final native int    user_id()      /*-{ return this.user_id;      }-*/;
		final String tileUrl() {
			return "/tiles/" + user_id() + "/" + dev_nickname() + "." + ch_name() + "/";
		}
	}
	
	static native JsArray<Graphable> jsArrayGraphableFromJSON(String json) /*-{ return eval(json); }-*/;
	


	void receiveChannels(JsArray<Graphable> channels) {
		GWT.log("received " + channels.length() + " channels");
	
		for (int i = 0; i < channels.length(); i++) {
			int axisMargin = 10;
			Graphable channel = channels.get(i);
			int minLevel = -20;
			
			// Reduce vertical scale by 10x.  Offset vertically
			double min = channel.min_value();
			double max = channel.max_value();
			double span = max-min;
			double center = .5 * (min+max);
			center += span * (i - 4);
			span *= 10;
			min = center - .5 * span;
			max = center + .5 * span;
			
			GraphAxis yAxis = new GraphAxis(channel.ch_name(),
					min, max,
					Basis.xRightYUp,
					axisMargin * 3,
					false);
			
			graphWidget.addDataPlot(new DataPlot(graphWidget, timeAxis, yAxis, channel.tileUrl(), minLevel, DATA_PLOT_COLORS[i]));
		}
	}
	
	
	void selectGraph() {
		if (!loginWidget.isLoggedIn()) return;
		unselect();
		
		int axisMargin = 10;
					
		graphWidget = new GraphWidget(Window.getClientWidth(), 
					         Window.getClientHeight() - 100, 
					         axisMargin);

		timeAxis = new TimeGraphAxis(
				1293861600-86400*365,
				1293861600,
				Basis.xDownYRight,
				axisMargin * 7,
				true);

		
		mainPanel = graphWidget;
		this.add(mainPanel);
		
		graphWidget.paint();		
		
		receiveChannels(jsArrayGraphableFromJSON(fetchChannelsJSON()));
	}

	void selectPhotos() {
		if (!loginWidget.isLoggedIn()) return;
		unselect();
		mainPanel = new FlowPhotosWidget(1293861600-86400*365, 1293861600+86400*365);
		add(mainPanel);
	}
	void changeLoginStatus(boolean loggedIn) {
		if (!loggedIn) unselect();
	}
}