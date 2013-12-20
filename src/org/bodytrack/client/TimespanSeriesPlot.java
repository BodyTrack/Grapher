package org.bodytrack.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bodytrack.client.DataPointListener.TriggerAction;
import org.bodytrack.client.StyleDescription.StyleType;
import org.bodytrack.client.StyleDescription.TimespanStyle;
import org.bodytrack.client.StyleDescription.TimespanStyles;

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
	
	public List<PlottablePoint> getDataPoints(GrapherTile tile,boolean sort) {
		ArrayList<PlottablePoint> points = new ArrayList<PlottablePoint>();
		if (tile.getTimespanDescriptions() != null){
			for (TimespanDescription desc : tile.getTimespanDescriptions()){
				points.add(new TimespanPoint(desc));
			}
		}
		if (sort){
			Collections.sort(points, new Comparator<PlottablePoint>(){
	
				@Override
				public int compare(PlottablePoint o1, PlottablePoint o2) {
					double value = o1.getDate() - o2.getDate();
					if (value < 0)
						return -1;
					else if (value > 0)
						return 1;
					return 0;
				}
				
			});
		}
		return points;
	}
	
	public List<PlottablePoint> getDataPoints(GrapherTile tile) {
		return getDataPoints(tile,true);
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
		
		private TimespanDescription desc;

		public TimespanPoint(TimespanDescription description) {
			super((description.getStart() / 2) + (description.getEnd() / 2), 0);
			desc = description;
		}
		
		public double getStart(){
			return desc.getStart();
		}
		
		public double getEnd(){
			return desc.getEnd();
		}
		
		public String getTimespanValue(){
			return desc.getValue();
		}
		
		public TimespanStyle getStyle(){
			return desc.getStyle();
		}
		
		public TimespanDescription getDescription(){
			return desc;
		}
		
	}
	
	private TimespanStyles style;
	
	private class TimespanRenderingStrategy implements SeriesPlotRenderingStrategy {
		
		public TimespanRenderingStrategy(){
		}

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
			TimespanStyle styling = style.getStyle(point.getTimespanValue(), point.getStyle());
			double borderWidth = styling.getBorderWidth();
			if (borderWidth < 0) borderWidth = 0;
			drawing.getCanvas().setFillStyle(styling.getFillColor());
			
			drawing.getCanvas().setLineWidth(borderWidth);
			double startX = getXAxis().project2D(point.getStart()).getX();
			double endX = getXAxis().project2D(point.getEnd()).getX();
			
			///*
			double bottom = getYAxis().project2D(styling.getTop()).getY();
			double top = getYAxis().project2D(styling.getBottom()).getY();
			//*/
			/*
			double height = drawing.getHeight();
			double top = styling.getTop() * height;
			double bottom = styling.getBottom() * height;
			//*/
			
			if (borderWidth > 0){
				drawing.getCanvas().setFillStyle(styling.getBorderColor());
				drawing.getCanvas().fillRectangle(startX, top, endX - startX, bottom - top);				
			}
			if (endX - startX - borderWidth * 2 > 0){
				drawing.getCanvas().setFillStyle(styling.getFillColor());
				drawing.getCanvas().fillRectangle(startX + borderWidth, top + borderWidth, endX - startX - borderWidth * 2, bottom - top - borderWidth * 2);
				
			}
			
		}
		
	}
	
	private final static class ClickInfo extends JavaScriptObject{
		
		private static native ClickInfo construct(TimespanDescription desc, double x, double y, String color)/*-{
			var newObject = {};
			newObject.timespanInfo = desc;
			newObject.position = {
				x: x,
				y: y
			};
			newObject.color = color;
			return newObject;
		}-*/;
		 
		protected ClickInfo(){
			
		}
		
		public static ClickInfo construct(TimespanDescription desc, Vector2 pos,String color){
			return construct(desc,pos.getX(),pos.getY(), color);			
		}
		
	}
	
	public void onClick(final Vector2 pos){
		double targetTime = getXAxis().unproject(pos);
		outerloop: for (GrapherTile tile : getBestResolutionTiles(getXAxis().getMin(),getXAxis().getMax())){
			for (PlottablePoint point : getDataPoints(tile,false)){
				TimespanPoint timespan = (TimespanPoint) point;
				TimespanStyle styling = style.getStyle(timespan.getTimespanValue(), timespan.getStyle());
				
				double bottom = getYAxis().project2D(styling.getTop()).getY();
				double top = getYAxis().project2D(styling.getBottom()).getY();
				if (timespan.getStart() <= targetTime && timespan.getEnd() >= targetTime && pos.getY() >= top && pos.getY() <= bottom){
					getXAxis().setCursorPosition(getXAxis().unproject(pos)); 
					publishDataPoint(timespan, TriggerAction.CLICK,ClickInfo.construct(timespan.getDescription(),pos,
													style.getStyle(timespan.getTimespanValue(), timespan.getStyle()).getFillColor().toString()));
					break outerloop;
				}
				
			}
		}
	}
	
	
	
	private final class TimespanRenderer extends BaseSeriesPlotRenderer {

		private TimespanRenderer(final StyleDescription styleDescription) {
			super(styleDescription);
			style = styleDescription.getTimespanStyles();
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
