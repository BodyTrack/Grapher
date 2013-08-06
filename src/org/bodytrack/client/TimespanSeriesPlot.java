package org.bodytrack.client;

import java.util.ArrayList;
import java.util.List;

import org.bodytrack.client.StyleDescription.StyleType;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class TimespanSeriesPlot extends BaseSeriesPlot {
	
	private SeriesPlotRenderer renderer;

	protected TimespanSeriesPlot(JavaScriptObject datasource,
			JavaScriptObject nativeXAxis, 
			JavaScriptObject nativeYAxis,
			final JavaScriptObject styleJson,
			boolean localDisplay) {
		super(datasource, nativeXAxis, nativeYAxis, localDisplay);
		renderer = new TimespanRenderer(styleJson.<StyleDescription>cast());
		
	}
	
	public List<PlottablePoint> getDataPoints(GrapherTile tile) {
		ArrayList<PlottablePoint> points = new ArrayList<PlottablePoint>();
		if (tile.getTimespanDescriptions() != null){
			for (TimespanDescription desc : tile.getTimespanDescriptions()){
				points.add(new TimespanPoint(desc.getStart(),desc.getEnd(),0));
			}
		}
		return points;
	}

	@Override
	public boolean highlightIfNear(Vector2 pos) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doCursorClick() {
		// TODO Auto-generated method stub

	}

	@Override
	protected SeriesPlotRenderer getRenderer() {
		return renderer;
	}
	
	private static class TimespanPoint extends PlottablePoint{
		
		private double start, end;

		public TimespanPoint(double start, double end, double value) {
			super((start / 2) + (end / 2), value);
			this.start = start;
			this.end = end;
		}
		
		public double getStart(){
			return start;
		}
		
		public double getEnd(){
			return end;
		}
		
	}
	
	private class TimespanRenderingStrategy implements SeriesPlotRenderingStrategy {

		@Override
		public void beforeRender(GrapherCanvas canvas,
				BoundedDrawingBox drawing, boolean isAnyPointHighlighted) {
		}

		@Override
		public void afterRender(GrapherCanvas canvas, BoundedDrawingBox drawing) {
		}

		@Override
		public void paintEdgePoint(BoundedDrawingBox drawing, 
				GrapherTile tile,
				GraphAxis xAxis, 
				GraphAxis yAxis,
				PlottablePoint highlightedPoint, 
				double x, 
				double y,
				PlottablePoint rawDataPoint) {
			paintPoint(drawing, (TimespanPoint) rawDataPoint);
			
		}

		@Override
		public void paintDataPoint(BoundedDrawingBox drawing, GrapherTile tile,
				GraphAxis xAxis, GraphAxis yAxis,
				PlottablePoint highlightedPoint, double prevX, double prevY,
				double x, double y, PlottablePoint rawDataPoint) {
			paintPoint(drawing, (TimespanPoint) rawDataPoint);
			
		}
		
		public void paintPoint(BoundedDrawingBox drawing, TimespanPoint point){
			drawing.getCanvas().setFillStyle(ColorUtils.GREEN);
			double startX = getXAxis().project2D(point.getStart()).getX();
			double endX = getXAxis().project2D(point.getEnd()).getX();
			drawing.getCanvas().fillRectangle(startX, 0, endX - startX, drawing.getHeight());
			
		}
		
	}
	
	private final class TimespanRenderer extends BaseSeriesPlotRenderer {

		private TimespanRenderer(final StyleDescription styleDescription) {
			super(styleDescription);
		}

		@Override
		protected List<SeriesPlotRenderingStrategy> buildSeriesPlotRenderingStrategies(
				JsArray<StyleType> styleTypes, Double highlightLineWidth) {
			// TODO: honor the style...
			final List<SeriesPlotRenderingStrategy> renderingStrategies =
				new ArrayList<SeriesPlotRenderingStrategy>();
			renderingStrategies.add(new TimespanRenderingStrategy());
			return renderingStrategies;
		}

		@Override
		protected List<DataPointRenderingStrategy> buildPointRenderingStrategies(
				final JsArray<StyleDescription.StyleType> styleTypes,
				final Double highlightLineWidth) {
			// TODO: honor the style...
			return null;
		}

		@Override
		protected List<PlottablePoint> getDataPoints(final GrapherTile tile) {
			return TimespanSeriesPlot.this.getDataPoints(tile);
		}
	}

}
