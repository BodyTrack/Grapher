package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * <code>BaseDataSeriesPlotRenderingStrategy</code> provides base functionality for {@link DataSeriesPlot}
 * {@link SeriesPlotRenderingStrategy style rendering strategies}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseDataSeriesPlotRenderingStrategy implements SeriesPlotRenderingStrategy {

   public static SeriesPlotRenderingStrategy create(final StyleDescription.StyleType styleType,
                                                    final boolean willShowComments) {

      if (styleType != null) {
         final Type type = Type.findByName(styleType.getType());
         switch (type) {
            case POINT:
               return new DotRenderingStrategy(styleType, willShowComments);
            case LOLLIPOP:
               return new LollipopRenderingStrategy(styleType, willShowComments);
            case ZEO:
               return new ZeoRenderingStrategy(styleType, willShowComments);
            case LINE:
            default:
               return new LineRenderingStrategy(styleType, willShowComments);
         }
      }

      return null;
   }

   private StyleDescription.StyleType styleType;
   private final boolean willShowComments;
   private final double lineWidth;
   private final Color strokeColor;
   private final boolean willFill;
   private final Color fillColor;

   protected BaseDataSeriesPlotRenderingStrategy(final StyleDescription.StyleType styleType, final boolean willShowComments) {
      this.styleType = styleType;
      this.willShowComments = willShowComments;
      lineWidth = getStyleType().getLineWidth(NORMAL_STROKE_WIDTH);
      strokeColor = getStyleType().getColor(DEFAULT_STROKE_COLOR);
      willFill = getStyleType().willFill();
      fillColor = getStyleType().getFillColor(DEFAULT_FILL_COLOR);
   }

   protected final StyleDescription.StyleType getStyleType() {
      return styleType;
   }

   protected final boolean willShowComments() {
      return willShowComments;
   }

   protected final double getLineWidth() {
      return lineWidth;
   }

   protected final Color getStrokeColor() {
      return strokeColor;
   }

   protected final boolean willFill() {
      return willFill;
   }

   protected final Color getFillColor() {
      return fillColor;
   }

   /**
    * <p>Sets the line width, stroke color, and fill color.</p>
    *
    * <p>If the {@link StyleDescription.StyleType StyleType} has a "lineWidth" field, then the line width is set to
    * either the that width or the {@link SeriesPlotRenderingStrategy#HIGHLIGHT_STROKE_WIDTH highlighted stroke width},
    * depending on the value of <code>isAnyPointHighlighted</code>.  If there is no <code>lineWidth</code> field,
    * the line width is set to {@link SeriesPlotRenderingStrategy#NORMAL_STROKE_WIDTH}.</p>
    *
    * <p>If the {@link StyleDescription.StyleType StyleType} has a "color" field, then the stroke color is set to
    * either the that color or the {@link SeriesPlotRenderingStrategy#DEFAULT_STROKE_COLOR} if no such field exists.</p>
    *
    * <p>If the {@link StyleDescription.StyleType StyleType} has a "fillColor" field, then the fill color is set to
    * either the that color or the {@link SeriesPlotRenderingStrategy#DEFAULT_FILL_COLOR} if no such field exists.</p>
    */
   @Override
   public void beforeRender(final Canvas canvas, final boolean isAnyPointHighlighted) {
      canvas.setLineWidth(isAnyPointHighlighted ? HIGHLIGHT_STROKE_WIDTH : lineWidth);
      canvas.setStrokeStyle(strokeColor);
      canvas.setFillStyle(fillColor);
   }

   /**
    * Sets the stroke and fill style on the {@link Canvas} back to the {@link Canvas#DEFAULT_COLOR default color} and
    * sets the line width to {@link SeriesPlotRenderingStrategy#NORMAL_STROKE_WIDTH normal stroke width}.
    */
   @Override
   public void afterRender(final Canvas canvas) {
      // Clean up after ourselves
      canvas.setLineWidth(NORMAL_STROKE_WIDTH);
      canvas.setStrokeStyle(DEFAULT_STROKE_COLOR);
      canvas.setFillStyle(DEFAULT_FILL_COLOR);
   }

   /** Enumeration defining the various types of data series plot style types. */
   private static enum Type {

      POINT("point"),
      LOLLIPOP("lollipop"),
      LINE("line"),
      ZEO("zeo");

      private static final Map<String, Type> NAME_TO_TYPE_MAP;

      public static final Type DEFAULT_DATA_SERIES_PLOT_TYPE = LINE;

      static {
         final Map<String, Type> nameToTypeMap = new HashMap<String, Type>(Type.values().length);
         for (final Type dataSeriesPlotType : Type.values()) {
            nameToTypeMap.put(dataSeriesPlotType.getName(), dataSeriesPlotType);
         }
         NAME_TO_TYPE_MAP = Collections.unmodifiableMap(nameToTypeMap);
      }

      /**
       * Returns the <code>Type</code> associated with the given <code>name</code> (case insensitive), or
       * {@link #DEFAULT_DATA_SERIES_PLOT_TYPE} if no such type exists. Guaranteed to never return <code>null</code>.
       */
      public static Type findByName(final String name) {
         if (name != null) {
            final String lowercaseName = name.toLowerCase();
            if (NAME_TO_TYPE_MAP.containsKey(lowercaseName)) {
               return NAME_TO_TYPE_MAP.get(lowercaseName);
            }
         }
         return DEFAULT_DATA_SERIES_PLOT_TYPE;
      }

      private final String name;

      private Type(final String name) {
         this.name = name.toLowerCase();  // force all names to be lowercase so that we can do a case-insensitive search in findByName()
      }

      public String getName() {
         return name;
      }

      @Override
      public String toString() {
         return getName();
      }
   }
}
