package org.bodytrack.client;

public class LollipopRenderingStrategy extends CircleRenderingStrategy {
   public LollipopRenderingStrategy(final StyleDescription.StyleType styleType,
                                    final Double highlightLineWidth) {
      super(styleType, highlightLineWidth);
   }

   @Override
   public void paintPoint(final BoundedDrawingBox drawing,
                           final GraphAxis xAxis,
                           final GraphAxis yAxis,
                           final double x,
                           final double y,
                           final PlottablePoint rawDataPoint) {
	   
	   double minDrawY, maxDrawY;
	   
	   double zeroY = yAxis.project2D(0).getY();
	   
	   if (zeroY > y){
		   minDrawY = zeroY;
		   maxDrawY = y + getRadius();
	   }
	   else{
		   maxDrawY = zeroY;
		   minDrawY = y - getRadius();
	   }
	  
	  
      // The Y-value in pixels corresponding to the lowest point to draw on the lollipop stick

      
      // The Y-value in pixels corresponding to the highest point to draw on the lollipop stick
      
      

      // Don't draw the stick if the circle part of the lollipop would occlude the stick
      if (minDrawY > maxDrawY) {
         drawing.drawLineSegment(x, minDrawY, x, maxDrawY);
      }
      
     

      super.paintPoint(drawing, xAxis, yAxis, x, y, rawDataPoint);
   }
}
