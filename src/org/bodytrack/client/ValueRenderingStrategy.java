package org.bodytrack.client;

import com.google.gwt.i18n.client.NumberFormat;
import gwt.g2d.client.graphics.canvas.Context;

public class ValueRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy implements DataPointRenderingStrategy {
   private static final String DEFAULT_FONT = "7pt Helvetica,Arial,Verdana,sans-serif";
   private static final double DEFAULT_VERTICAL_OFFSET = -3;
   private static final int DEFAULT_RADIUS = 5;
   private static final NumberFormat DEFAULT_VALUE_FORMAT = NumberFormat.getFormat(PlottablePoint.DEFAULT_VALUE_FORMAT_STRING);
   private final double marginWidth;
   private final String font;
   private final double verticalOffset;
   private NumberFormat numberFormat = DEFAULT_VALUE_FORMAT;

   public ValueRenderingStrategy(final StyleDescription.StyleType styleType,
                                 final Double highlightLineWidth) {
      super(styleType, highlightLineWidth);
      final String tempFont = styleType.getValue("font");
      font = (tempFont == null) ? DEFAULT_FONT : tempFont;
      verticalOffset = styleType.getDoubleValue("verticalOffset", DEFAULT_VERTICAL_OFFSET);
      marginWidth = styleType.getDoubleValue("marginWidth", DEFAULT_RADIUS);
      final String numberFormatStr = styleType.getValue("numberFormat");
      if (numberFormatStr != null) {
         try {
            numberFormat = NumberFormat.getFormat(numberFormatStr);
         }
         catch (Exception e) {
            Log.debug("ValueRenderingStrategy.ValueRenderingStrategy(): Invalid numberFormat [" + numberFormatStr + "].  Using default instead.");
         }
      }
   }

   @Override
   public final void paintEdgePoint(final BoundedDrawingBox drawing,
                                    final GrapherTile tile,
                                    final GraphAxis xAxis,
                                    final GraphAxis yAxis,
                                    final boolean isAnyPointHighlighted,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
      paintPoint(drawing, xAxis, yAxis, x, y, rawDataPoint);
   }

   @Override
   public final void paintDataPoint(final BoundedDrawingBox drawing,
                                    final GrapherTile tile,
                                    final GraphAxis xAxis,
                                    final GraphAxis yAxis,
                                    final boolean isAnyPointHighlighted,
                                    final double prevX,
                                    final double prevY,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
      drawValue(drawing, prevX, x, y, rawDataPoint, true);
   }

   @Override
   public void paintPoint(final BoundedDrawingBox drawing,
                          final GraphAxis xAxis,
                          final GraphAxis yAxis,
                          final double x,
                          final double y,
                          final PlottablePoint rawDataPoint) {
      drawValue(drawing, 0, x, y, rawDataPoint, false);
   }

   private void drawValue(final BoundedDrawingBox drawing,
                          final double prevX,
                          final double x,
                          final double y,
                          final PlottablePoint rawDataPoint,
                          final boolean shouldConsiderPrevXValue) {
      final Context ctx = drawing.getCanvas().getSurface().getContext();

      // get the current font so we can revert to it later
      final String originalFont = ctx.getFont();

      // set the font and then measure the text so we can compute the desired x position
      ctx.setFont(font);
      final String valueAsString = numberFormat.format(rawDataPoint.getValue());
      final double widthInPixels = ctx.measureText(valueAsString);
      final double desiredX = x - (widthInPixels / 2);

      // if we should be considering the previous x value, then check for overlap
      if (!shouldConsiderPrevXValue || desiredX >= (prevX + marginWidth)) {
         final double desiredY = y - verticalOffset;
         ctx.fillText(valueAsString, desiredX, desiredY);
      }

      // clean up after ourselves
      ctx.setFont(originalFont);
   }
}
