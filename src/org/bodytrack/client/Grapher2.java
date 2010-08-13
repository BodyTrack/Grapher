package org.bodytrack.client;


import java.util.Date;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Grapher2 implements EntryPoint {
	private VerticalPanel mainLayout = new VerticalPanel();
	//Surface surface = new Surface(400, 400);
	int axisMargin = 10;
	GraphWidget gw = new GraphWidget(400, 400, axisMargin);
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		mainLayout.add(gw);
				
		RootPanel.get("graph").add(mainLayout);
		
		setupGraphWidget();
	}
	
	private void setupGraphWidget() {
//		gw.addXAxis(new TimeGraphAxis(0, 24*3600*365*10, // min, max values
//		Basis.xDownYRight, 
//		  30   // width, in pixels
//		  ));
		
		gw.addXAxis(new TimeGraphAxis((new Date()).getTime()/1000., 
				                      86400 + (new Date()).getTime()/1000., // min, max values
				                      Basis.xDownYRight, 
		                              70   // width, in pixels
		  ));

		gw.addYAxis(new GraphAxis(-1, 1, // min, max value
				Basis.xRightYUp,
				  30   // width, in pixels
				  ));

		gw.addYAxis(new GraphAxis(0, 10, // min, max value
				Basis.xRightYUp,
				  30   // width, in pixels
				  ));
		
		
		gw.paint();
	}
}
