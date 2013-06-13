package org.bodytrack.client;

import com.google.gwt.i18n.client.NumberFormat;

public class ValueRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy implements DataPointRenderingStrategy {
   private static final String DEFAULT_FONT = "7pt Helvetica,Arial,Verdana,sans-serif";
   private static final double DEFAULT_VERTICAL_OFFSET = 3;
   private static final int DEFAULT_MARGIN_WIDTH = 5;
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
      marginWidth = styleType.getDoubleValue("marginWidth", DEFAULT_MARGIN_WIDTH);
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
                                    final PlottablePoint highlightedPoint,
                                    final double x,
                                    final double y,
                                    final PlottablePoint rawDataPoint) {
      paintPoint(drawing, xAxis, yAxis, x, y, rawDataPoint, highlightedPoint);
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
      drawValue(drawing, prevX, x, y, rawDataPoint, true);
   }

   @Override
   public void paintPoint(final BoundedDrawingBox drawing,
                          final GraphAxis xAxis,
                          final GraphAxis yAxis,
                          final double x,
                          final double y,
                          final PlottablePoint rawDataPoint,
                          final PlottablePoint highlightedPoint) {
      drawValue(drawing, 0, x, y, rawDataPoint, false);
   }

   private void drawValue(final BoundedDrawingBox drawing,
                          final double prevX,
                          final double x,
                          final double y,
                          final PlottablePoint rawDataPoint,
                          final boolean shouldConsiderPrevXValue) {

      // get the current font so we can revert to it later
      final String originalFont = drawing.getCanvas().getFont();

      // set the font and then measure the text so we can compute the desired x position
      drawing.getCanvas().setFont(font);
      final String valueAsString = numberFormat.format(rawDataPoint.getValue());
      final double widthInPixels = drawing.getCanvas().measureText(valueAsString);
      final double desiredX = x - (widthInPixels / 2);

      // if we should be considering the previous x value, then check for overlap
      if (!shouldConsiderPrevXValue || desiredX >= (prevX + marginWidth)) {
         final double desiredY = y - verticalOffset;
         drawing.getCanvas().fillText(valueAsString, desiredX, desiredY);
      }

      // clean up after ourselves
      drawing.getCanvas().setFont(originalFont);
   }
}
