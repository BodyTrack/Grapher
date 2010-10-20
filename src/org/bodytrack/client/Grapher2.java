package org.bodytrack.client;


import java.util.Date;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * This is currently a "Hello World" with a sine wave drawn on a
 * set of axes.
 */
public class Grapher2 implements EntryPoint {
	private VerticalPanel mainLayout;
	private GraphWidget gw;
	private DataPlot plot;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		mainLayout = new VerticalPanel();

		setupGraphWidget();

		mainLayout.add(gw);
		RootPanel.get("graph").add(mainLayout);
	}

	private void setupGraphWidget() {
		// This is not the most general code, but it is good for a demo

		gw = new GraphWidget(400, 400, 10);

		GraphAxis time = new TimeGraphAxis((new Date()).getTime()/1000.0,
				86400 + (new Date()).getTime()/1000.0, // min, max values
				Basis.xDownYRight,
				70);					// width, in pixels

		GraphAxis value = new GraphAxis(-1, 1,	// min, max value
				Basis.xRightYUp,
				30);							// width, in pixels

		plot = new DataPlot(gw, time, value, "/tiles/1/foo.bar/");

		gw.addDataPlot(plot);

		gw.paint();
	}
}
