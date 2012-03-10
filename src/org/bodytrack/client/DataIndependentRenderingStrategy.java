package org.bodytrack.client;

public interface DataIndependentRenderingStrategy extends RenderingStrategy {
	public void render(BoundedDrawingBox drawing, GraphAxis xAxis, GraphAxis yAxis);
}
