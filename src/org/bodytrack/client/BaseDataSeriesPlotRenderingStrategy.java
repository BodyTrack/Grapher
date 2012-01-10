package org.bodytrack.client;

import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.Surface;

/**
 * <p>
 * <code>BaseDataSeriesPlotRenderingStrategy</code> provides base functionality for {@link DataSeriesPlot}
 * {@link SeriesPlotRenderingStrategy style rendering strategies}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseDataSeriesPlotRenderingStrategy implements SeriesPlotRenderingStrategy {
   private StyleDescription.StyleType styleType;
   private final double lineWidth;
   private final double highlightLineWidth;
   private final Color strokeColor;
   private final boolean willFill;
   private final Color fillColor;

   protected BaseDataSeriesPlotRenderingStrategy(final StyleDescription.StyleType styleType, final Double theHighlightLineWidth) {
      this.styleType = styleType;
      lineWidth = getStyleType().getLineWidth(DEFAULT_STROKE_WIDTH);
      strokeColor = getStyleType().getColor(DEFAULT_STROKE_COLOR);
      willFill = getStyleType().willFill();
      fillColor = getStyleType().getFillColor(DEFAULT_FILL_COLOR);
      highlightLineWidth = (theHighlightLineWidth == null) ? lineWidth : theHighlightLineWidth;
   }

   protected final StyleDescription.StyleType getStyleType() {
      return styleType;
   }

   protected final double getLineWidth() {
      return lineWidth;
   }

   protected final double getHighlightLineWidth() {
      return highlightLineWidth;
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
    * either the that width or the highlighted line width (if defined), depending on the value of 
    * <code>isAnyPointHighlighted</code>.  If there is no <code>lineWidth</code> field, the line width is set to 
    * {@link SeriesPlotRenderingStrategy#DEFAULT_STROKE_WIDTH}.</p>
    *
    * <p>If the {@link StyleDescription.StyleType StyleType} has a "color" field, then the stroke color is set to
    * either the that color or the {@link SeriesPlotRenderingStrategy#DEFAULT_STROKE_COLOR} if no such field exists.</p>
    *
    * <p>If the {@link StyleDescription.StyleType StyleType} has a "fillColor" field, then the fill color is set to
    * either the that color or the {@link SeriesPlotRenderingStrategy#DEFAULT_FILL_COLOR} if no such field exists.</p>
    */
   @Override
   public void beforeRender(final Canvas canvas, final boolean isAnyPointHighlighted) {
      final Surface surface = canvas.getSurface();
      final double actualLineWidth = isAnyPointHighlighted ? highlightLineWidth : lineWidth;
      surface.setLineWidth(actualLineWidth);
      surface.setStrokeStyle(strokeColor);
      surface.setFillStyle(fillColor);
   }

   /**
    * Sets the stroke style back to {@link SeriesPlotRenderingStrategy#DEFAULT_STROKE_COLOR}, sets the fill style back
    * to {@link SeriesPlotRenderingStrategy#DEFAULT_FILL_COLOR}, and sets the line width to
    * {@link SeriesPlotRenderingStrategy#DEFAULT_STROKE_WIDTH normal stroke width}.
    */
   @Override
   public void afterRender(final Canvas canvas) {
      // Clean up after ourselves
      final Surface surface = canvas.getSurface();
      surface.setLineWidth(DEFAULT_STROKE_WIDTH);
      surface.setStrokeStyle(DEFAULT_STROKE_COLOR);
      surface.setFillStyle(DEFAULT_FILL_COLOR);
   }
}
