package org.bodytrack.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Grapher2 implements EntryPoint {
	private VerticalPanel mainLayout = new VerticalPanel(); 
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		Label label = new Label();
		label.setText("hi there");
		
		mainLayout.add(label);

		RootPanel.get("graph").add(mainLayout);
	}
}
