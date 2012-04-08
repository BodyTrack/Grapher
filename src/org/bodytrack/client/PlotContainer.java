package org.bodytrack.client;

public interface PlotContainer {
	public void paint();
	public void paint(int newPaintEventId);
	public void addPlot(Plot plot);
	public void removePlot(Plot plot);
}
