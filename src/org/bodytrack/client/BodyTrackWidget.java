package org.bodytrack.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class BodyTrackWidget extends VerticalPanel {
	FlowPanel topBar = new FlowPanel();
	LoginWidget loginWidget = new LoginWidget();
	NavBar navBar = new NavBar();
	FlowPhotosWidget flowPhotosWidget;
	
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

	void selectGraph() {
		GWT.log("selectGraph");
	}
	void selectPhotos() {
		GWT.log("selectPhotos");
	}
	void changeLoginStatus(boolean loggedIn) {
		
	}
}
