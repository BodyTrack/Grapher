package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ZeoRenderingStrategy extends BaseDataSeriesPlotRenderingStrategy {

   /** Enumeration defining the various Zeo states. */
   private static enum ZeoState {
      NO_DATA(0, "No Data", new Color(0, 0, 0)),
      DEEP(1, "Deep", new Color(0x00, 0x66, 0x00)),
      LIGHT(2, "Light", new Color(0x99, 0x99, 0x99)),
      REM(3, "REM", new Color(0x00, 0xCC, 0x00)),
      WAKE(4, "Wake", new Color(0xFF, 0x66, 0x33));

      private static final Map<Integer, ZeoState> VALUE_TO_STATE_MAP;

      static {
         final Map<Integer, ZeoState> valueToStateMap = new HashMap<Integer, ZeoState>(ZeoState.values().length);
         for (final ZeoState zeoState : ZeoState.values()) {
            valueToStateMap.put(zeoState.getValue(), zeoState);
         }
         VALUE_TO_STATE_MAP = Collections.unmodifiableMap(valueToStateMap);
      }

      /**
       * Returns the <code>ZeoState</code> associated with the given <code>value</code>, or <code>null</code> if no such
       * state exists.
       */
      public static ZeoState findByValue(final int value) {
         return VALUE_TO_STATE_MAP.get(value);
      }

      private final int value;
      private final String name;
      private final Color color;

      ZeoState(final int value, final String name, final Color color) {
         this.value = value;
         this.name = name;
         this.color = color;
      }

      public int getValue() {
         return value;
      }

      public String getName() {
         return name;
      }

      public Color getColor() {
         return color;
      }

      @Override
      public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append("ZeoState");
         sb.append("{value=").append(value);
         sb.append(", name='").append(name).append('\'');
         sb.append('}');
         return sb.toString();
      }
   }

   /**
    * The alpha value used when drawing rectangles for Zeo plots.
    */
   //private static final double NORMAL_ALPHA = 0.4;
   private static final double NORMAL_ALPHA = 1.0;

   /**
    * The alpha used when drawing rectangles for highlighted Zeo plots.
    */
   //private static final double HIGHLIGHTED_ALPHA = 0.5;
   private static final double HIGHLIGHTED_ALPHA = 1.0;

   public ZeoRenderingStrategy(final StyleDescription.StyleType styleType,
                               final boolean willShowComments) {
      super(styleType, willShowComments);
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
      paintEdgePoint(drawing, tile, xAxis, yAxis, isAnyPointHighlighted, x, y, rawDataPoint);
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
      // get the ZeoState
      final int val = (int)Math.round(rawDataPoint.getValue());
      final ZeoState zeoState = ZeoState.findByValue(val);

      // use the sample width to compute the left and right x values for the bar (we want the data point to be in the
      // center of the bar)
      final double sampleHalfWidth = tile.getPlottableTile().getSampleWidth() / 2;
      final double leftX = xAxis.project2D(rawDataPoint.getDate() - sampleHalfWidth).getX();
      final double rightX = xAxis.project2D(rawDataPoint.getDate() + sampleHalfWidth).getX();

      // draw the rectangle
      drawRectangle(drawing.getCanvas(), yAxis, isAnyPointHighlighted, zeoState, leftX, rightX, y);
   }

   /**
    * Draws a rectangle with the specified corners, stretching down to 0.
    */
   private void drawRectangle(final Canvas canvas,
                              final GraphAxis yAxis,
                              final boolean isAnyPointHighlighted,
                              final ZeoState zeoState,
                              final double leftX,
                              final double rightX,
                              final double y) {
      if (zeoState == null) {
         return;
      }

      // The Y-value in units on the Y-axis corresponding to the lowest
      // point to draw on the rectangle
      final double minDrawUnits = Math.max(0.0, yAxis.getMin());

      // The Y-value in pixels corresponding to the lowest point to
      // draw on the rectangle
      final double minDrawY = yAxis.project2D(minDrawUnits).getY();

      // Define variables for the corners, making variable names
      // explicit so the code is clear
      // GWT will optimize away these variables, so there will be
      // no performance issues from these
      final double bottomY = minDrawY;
      final double topY = y;

      // Draw the Zeo plot with the specified color
      canvas.setGlobalAlpha(isAnyPointHighlighted ? HIGHLIGHTED_ALPHA : NORMAL_ALPHA);
      canvas.setFillStyle(zeoState.getColor());

      final boolean isNoDataState = ZeoState.NO_DATA.equals(zeoState);

      if (isNoDataState) {
         // Draw a line
         final double oldLineWidth = canvas.getLineWidth();
         canvas.setLineWidth(isAnyPointHighlighted
                             ? SeriesPlotRenderingStrategy.HIGHLIGHT_STROKE_WIDTH
                             : SeriesPlotRenderingStrategy.NORMAL_STROKE_WIDTH);

         canvas.beginPath()
            .moveTo(leftX, bottomY)
            .drawLineTo(rightX, bottomY)
            .closePath();

         canvas.stroke();
         canvas.setLineWidth(oldLineWidth);
      } else {
         // Fill rectangle, without outline. Round to nearest pixels and
         // offset by half a pixel, so that we're always completely
         // filling pixels. Otherwise antialiasing will cause us to
         // paint partial pixels, which will make the graph fade on the
         // edges of the rectangles.
         canvas.fillRectangle(Math.round(leftX) + .5,
                              Math.round(topY) + .5,
                              Math.round(rightX) - Math.round(leftX),
                              Math.round(bottomY) - Math.round(topY));
      }

      // Draw lines around rectangles, but only if the width in pixels is large
      // enough and it's not the NO_DATA state
      final int widthInPixels = (int)Math.round(rightX - leftX);
      if (!isNoDataState && widthInPixels > 6) {
         canvas.setGlobalAlpha(Canvas.DEFAULT_ALPHA);
         canvas.setFillStyle(Canvas.DEFAULT_COLOR);

         final double oldLineWidth = canvas.getLineWidth();
         canvas.setLineWidth(isAnyPointHighlighted
                             ? SeriesPlotRenderingStrategy.HIGHLIGHT_STROKE_WIDTH
                             : SeriesPlotRenderingStrategy.NORMAL_STROKE_WIDTH);

         // Stroke the outside of the rectangle
         // Round to nearest pixels so we draw the line in such a way that
         // it completely fills pixels.  Otherwise a 1-pixel line turns into
         // a 2-pixel grey blurry line.
         canvas.strokeRectangle(Math.round(leftX),
                                Math.round(topY),
                                Math.round(rightX) - Math.round(leftX),
                                Math.round(bottomY) - Math.round(topY));
         canvas.setLineWidth(oldLineWidth);
      }
   }
}
