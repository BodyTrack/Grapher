package org.bodytrack.client;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class PointRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy implements DataPointRenderingStrategy {
   private static final int DEFAULT_RADIUS = 3;
   private final double radius;

   public PointRenderingStrategy(final StyleDescription.StyleType styleType,
                                 final Double theHighlightLineWidth) {
      super(styleType, theHighlightLineWidth);
      radius = styleType.getDoubleValue("radius", DEFAULT_RADIUS);
   }

   @Override
   public final void paintEdgePoint(final BoundedDrawingBox drawing,
                                    final GrapherTile tile,
                                    final GraphAxis xAxis,
                                    final GraphAxis yAxis,
                                    final PlottablePoint highlightedPoint,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
	   double oldWidth = drawing.getCanvas().getLineWidth();
	   if (willFill()) {
         drawing.fillClippedPath();
      }
      drawing.strokeClippedPath();
      drawing.getCanvas().setLineWidth(rawDataPoint.equals(highlightedPoint) ? highlightLineWidth : lineWidth);
      drawing.getCanvas().setStrokeStyle(strokeColor);
      drawing.getCanvas().setFillStyle(fillColor);
  	 drawing.beginClippedPath();
  	
  	 
  	 paintPoint(drawing, xAxis, yAxis, x, y, rawDataPoint);
  	 
  	 
  	if (willFill()) {
        drawing.fillClippedPath();
     }
     drawing.strokeClippedPath();
     drawing.getCanvas().setLineWidth(oldWidth);
     drawing.getCanvas().setStrokeStyle(strokeColor);
     drawing.getCanvas().setFillStyle(fillColor);
   drawing.beginClippedPath();
   drawing.getCanvas().setLineWidth(oldWidth);
     
   }

   @Override
   public final void paintDataPoint(final BoundedDrawingBox drawing,
                                    final GrapherTile tile,
                                    final GraphAxis xAxis,
                                    final GraphAxis yAxis,
                                    final PlottablePoint highlightedPoint,
                                    final double prevX,
                                    final double prevY,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
	   paintEdgePoint(drawing, tile,xAxis,yAxis,highlightedPoint,x,y,rawDataPoint);
   }

   /** Returns the radius of the dot. */
   protected final double getRadius() {
      return radius;
   }
}