package org.bodytrack.client;

import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * <code>DataSeriesPlotRenderer</code> is the default {@link SeriesPlotRenderer} for {@link DataSeriesPlot}s.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class DataSeriesPlotRenderer extends BaseSeriesPlotRenderer {

   public DataSeriesPlotRenderer(final StyleDescription styleDescription) {
      super(styleDescription);
   }

   @Override
   protected List<SeriesPlotRenderingStrategy> buildRenderingStrategies(final StyleDescription styleDescription) {
      final List<SeriesPlotRenderingStrategy> renderingStrategies = new ArrayList<SeriesPlotRenderingStrategy>();

      if (styleDescription != null) {
         // get the style types
         final JsArray<StyleDescription.StyleType> styleTypes = styleDescription.getStyleTypes();

         if (styleTypes != null) {

            // loop over all the style types
            for (int i = 0; i < styleTypes.length(); i++) {
               final StyleDescription.StyleType styleType = styleTypes.get(i);

               // don't bother creating a rendering strategy if the style type shouldn't be shown
               if (styleType != null && styleType.willShow()) {
                  final SeriesPlotRenderingStrategy renderingStrategy = BaseDataSeriesPlotRenderingStrategy.create(styleType,
                                                                                                                   styleDescription.willShowComments());
                  if (renderingStrategy != null) {
                     renderingStrategies.add(renderingStrategy);
                  }
               }
            }
         }
      }

      return renderingStrategies;
   }
}
