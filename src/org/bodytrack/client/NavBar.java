package org.bodytrack.client;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;


public class NavBar extends HorizontalPanel {
	NavBar() {}
	
	ArrayList<ClickableLink> links = new ArrayList<ClickableLink>();
	ClickableLink selected;
	
	void add(String label, ClickHandler clickHandler) {
		add(new ClickableLink(label, clickHandler));
	}	
}
