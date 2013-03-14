package org.bodytrack.client;

import com.google.gwt.canvas.dom.client.CssColor;

/**
 * <p>
 * <code>BaseDataSeriesPlotRenderingStrategy</code> provides base functionality
 * for {@link DataSeriesPlot}
 * {@link SeriesPlotRenderingStrategy style rendering strategies}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseDataSeriesPlotRenderingStrategy
      implements SeriesPlotRenderingStrategy {
   private StyleDescription.StyleType styleType;
   private final double lineWidth;
   private final double highlightLineWidth;
   private final CssColor strokeColor;
   private final boolean willFill;
   private final CssColor fillColor;

   protected BaseDataSeriesPlotRenderingStrategy(final StyleDescription.StyleType styleType,
                                                 final Double theHighlightLineWidth) {
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

   protected final CssColor getStrokeColor() {
      return strokeColor;
   }

   protected final boolean willFill() {
      return willFill;
   }

   protected final CssColor getFillColor() {
      return fillColor;
   }

   /**
    * <p>
    * Sets the line width, stroke color, and fill color, and then calls {@link BoundedDrawingBox#beginClippedPath()}.
    * </p>
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
   public void beforeRender(final GrapherCanvas canvas,
                            final BoundedDrawingBox drawing,
                            final boolean isAnyPointHighlighted) {
      canvas.setLineWidth(isAnyPointHighlighted ? highlightLineWidth : lineWidth);
      canvas.setStrokeStyle(strokeColor);
      canvas.setFillStyle(fillColor);

      drawing.beginClippedPath();

   }

   /**
    * First calls {@link BoundedDrawingBox#fillClippedPath()} if the style specifies that the strategy should fill, and
    * then calls {@link BoundedDrawingBox#strokeClippedPath()}.  Finally, this method then sets the stroke style back to
    * {@link SeriesPlotRenderingStrategy#DEFAULT_STROKE_COLOR}, sets the fill style back to
    * {@link SeriesPlotRenderingStrategy#DEFAULT_FILL_COLOR}, and sets the line width to
    * {@link SeriesPlotRenderingStrategy#DEFAULT_STROKE_WIDTH normal stroke width}.
    */
   @Override
   public void afterRender(final GrapherCanvas canvas,
                           final BoundedDrawingBox drawing) {

      if (willFill()) {
         drawing.fillClippedPath();
      }

      drawing.strokeClippedPath();

      // Clean up after ourselves
      canvas.setLineWidth(DEFAULT_STROKE_WIDTH);
      canvas.setStrokeStyle(DEFAULT_STROKE_COLOR);
      canvas.setFillStyle(DEFAULT_FILL_COLOR);
   }
}
